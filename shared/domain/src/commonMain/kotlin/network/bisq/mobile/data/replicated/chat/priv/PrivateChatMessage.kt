package network.bisq.mobile.data.replicated.chat.priv

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.reactions.ChatMessageReaction
import network.bisq.mobile.data.replicated.network.confidential.ack.MessageDeliveryInfoVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.service.message_delivery.MessageDeliveryServiceFacade

/**
 * What a private chat message adds to [ChatMessage], mirroring Bisq 2's
 * `bisq.chat.priv.PrivateChatMessage<R extends ChatMessageReaction>`: the delivery status of a
 * message sent point to point. A public message is broadcast through the P2P store and has none.
 *
 * Bisq 2 models trade chat and peer-to-peer DMs as siblings under this base:
 * `BisqEasyOpenTradeMessage` adds only `tradeId` / `mediator` / `bisqEasyOffer`, and
 * `TwoPartyPrivateChatMessage` adds nothing at all.
 */
abstract class PrivateChatMessage<R : ChatMessageReaction>(
    id: String,
    chatMessageType: ChatMessageTypeEnum,
    text: String?,
    citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    date: Long,
    senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<R>,
) : ChatMessage<R>(
        id = id,
        chatMessageType = chatMessageType,
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        date = date,
        senderUserProfile = senderUserProfile,
        myUserProfile = myUserProfile,
        chatReactions = chatReactions,
    ) {
    private val _messageDeliveryStatus = MutableStateFlow<Map<String, MessageDeliveryInfoVO>>(emptyMap())
    val messageDeliveryStatus = _messageDeliveryStatus.asStateFlow()

    fun removeMessageDeliveryStatusObserver(messageDeliveryServiceFacade: MessageDeliveryServiceFacade) {
        messageDeliveryServiceFacade.removeMessageDeliveryStatusObserver(id)
    }

    fun addMessageDeliveryStatusObserver(messageDeliveryServiceFacade: MessageDeliveryServiceFacade) {
        messageDeliveryServiceFacade.addMessageDeliveryStatusObserver(id) { entry ->
            _messageDeliveryStatus.update { it + entry }
        }
    }
}

/**
 * The delivery status of a message sent point to point, or null for a broadcast public one. The
 * one place the shared chat composables tell the two branches apart, as desktop does in
 * `ChatMessageListItem`.
 */
val ChatMessage<*>.messageDeliveryStatusOrNull: StateFlow<Map<String, MessageDeliveryInfoVO>>?
    get() = (this as? PrivateChatMessage<*>)?.messageDeliveryStatus
