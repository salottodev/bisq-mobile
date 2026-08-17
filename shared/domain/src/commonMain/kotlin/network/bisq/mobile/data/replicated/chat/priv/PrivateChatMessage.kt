package network.bisq.mobile.data.replicated.chat.priv

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.reactions.ChatMessageReaction
import network.bisq.mobile.data.replicated.network.confidential.ack.MessageDeliveryInfoVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.service.message_delivery.MessageDeliveryServiceFacade
import network.bisq.mobile.domain.utils.DateUtils
import network.bisq.mobile.i18n.I18nSupport

/**
 * Everything a private chat message has in common, mirroring Bisq 2's
 * `bisq.chat.priv.PrivateChatMessage<R extends ChatMessageReaction>`.
 *
 * Bisq 2 models trade chat and peer-to-peer DMs as siblings under this base:
 * `BisqEasyOpenTradeMessage` adds only `tradeId` / `mediator` / `bisqEasyOffer`, and
 * `TwoPartyPrivateChatMessage` adds nothing at all. This class holds the shared body so neither
 * subclass has to repeat it.
 *
 * Takes plain values rather than a DTO: DTOs are a client-side transport concern and live in
 * `apps/clientApp`, so a type in `:shared:domain` must not depend on one. The node maps Bisq 2
 * objects straight into these arguments; a client implementation destructures its own DTO into the
 * same ones.
 *
 * Generic in [R] for the same reason Bisq 2 is: [isMyChatReaction] takes a reaction, so a
 * non-generic parameter type would force every caller that removes a reaction to widen and then
 * down-cast before handing it to a service facade.
 */
abstract class PrivateChatMessage<R : ChatMessageReaction>(
    val id: String,
    val chatMessageType: ChatMessageTypeEnum,
    val text: String?,
    val citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    val date: Long,
    val senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<R>,
) {
    private val myUserProfileId = myUserProfile.id

    private val _chatReactions: MutableStateFlow<List<R>> = MutableStateFlow(chatReactions)
    val chatReactions: StateFlow<List<R>> = _chatReactions.asStateFlow()

    private val _messageDeliveryStatus = MutableStateFlow<Map<String, MessageDeliveryInfoVO>>(emptyMap())
    val messageDeliveryStatus = _messageDeliveryStatus.asStateFlow()

    val textString: String get() = text ?: ""

    // Used for protocol log message
    val decodedText: String get() = text?.let { I18nSupport.decode(it) } ?: ""

    val dateString: String get() = DateUtils.toDateTime(date)
    val citationString: String get() = citation?.text ?: ""
    val citationAuthorUserName: String? = citationAuthorUserProfile?.userName
    val senderUserProfileId get() = senderUserProfile.id
    val senderUserName get() = senderUserProfile.userName
    val isMyMessage: Boolean get() = senderUserProfileId == myUserProfileId

    fun isMyChatReaction(reaction: R): Boolean = myUserProfileId == reaction.userProfileId

    fun setReactions(chatMessageReactions: List<R>) {
        _chatReactions.value = chatMessageReactions
    }

    fun removeMessageDeliveryStatusObserver(messageDeliveryServiceFacade: MessageDeliveryServiceFacade) {
        messageDeliveryServiceFacade.removeMessageDeliveryStatusObserver(id)
    }

    fun addMessageDeliveryStatusObserver(messageDeliveryServiceFacade: MessageDeliveryServiceFacade) {
        messageDeliveryServiceFacade.addMessageDeliveryStatusObserver(id) { entry ->
            _messageDeliveryStatus.update { it + entry }
        }
    }
}
