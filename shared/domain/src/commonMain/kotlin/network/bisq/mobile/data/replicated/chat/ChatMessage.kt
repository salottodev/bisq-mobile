package network.bisq.mobile.data.replicated.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import network.bisq.mobile.data.replicated.chat.reactions.ChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.domain.utils.DateUtils
import network.bisq.mobile.i18n.I18nSupport

/**
 * Everything a chat message has in common, mirroring Bisq 2's `bisq.chat.ChatMessage`: the root
 * of both the private branch ([network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage]:
 * trade chat, DMs) and the public one ([network.bisq.mobile.data.replicated.chat.pub.PublicChatMessage]:
 * discussion, support, offerbook). The shared chat composables are typed to this class, so a
 * message of either branch renders through the same list.
 *
 * Bisq 2 keeps only the author's id here and resolves the profile in the UI; mobile resolves it
 * once at construction ([senderUserProfile]), so a composable never has to look a profile up.
 * `channelId` and `chatChannelDomain` are left out for the same reason as before: the channel that
 * holds the message already carries both.
 *
 * Takes plain values rather than a DTO: the private chat DTOs are a client-side transport concern
 * and live in `apps/clientApp`, so a type in `:shared:domain` must not depend on one (the offerbook
 * is the exception — `BisqEasyOfferbookMessageDto` predates this hierarchy and feeds the offerbook,
 * not a chat). The node maps Bisq 2 objects straight into these arguments; a client implementation
 * destructures its own DTO into the same ones.
 *
 * Generic in [R] for the same reason Bisq 2 is: [isMyChatReaction] takes a reaction, so a
 * non-generic parameter type would force every caller that removes a reaction to widen and then
 * down-cast before handing it to a service facade.
 *
 * [wasEdited] sits on the base because upstream has it there, although only a public message can be
 * edited.
 *
 * `isMyMessage` and [myUserProfile] answer two different questions and must stay apart. Bisq 2
 * authorizes edit and delete against ANY of my identities
 * (`ChatMessage.isMyMessage` is `isUserIdentityPresent(authorUserProfileId)`), while reaction
 * ownership is decided against the SELECTED profile, as desktop's `ReactionItem` does. A caller that
 * knows the identity set states the answer; the private branch omits it and gets the sender/channel
 * comparison, which is the same thing for a DM.
 */
abstract class ChatMessage<R : ChatMessageReaction>(
    val id: String,
    val chatMessageType: ChatMessageTypeEnum,
    val text: String?,
    val citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    val date: Long,
    val senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<R>,
    val wasEdited: Boolean = false,
    isMyMessage: Boolean? = null,
) {
    private val myUserProfileId = myUserProfile.id

    private val _chatReactions: MutableStateFlow<List<R>> = MutableStateFlow(chatReactions)
    val chatReactions: StateFlow<List<R>> = _chatReactions.asStateFlow()

    val textString: String get() = text ?: ""

    // Used for protocol log message. Eager like `citationAuthorUserName` below, and for the same
    // reason: both are pure functions of constructor vals, and a `get()` would re-run the i18n decode
    // and the platform date format every time a row enters composition in the chat list.
    val decodedText: String = text?.let { I18nSupport.decode(it) } ?: ""

    val dateString: String = DateUtils.toDateTime(date)
    val citationString: String get() = citation?.text ?: ""
    val citationAuthorUserName: String? = citationAuthorUserProfile?.userName
    val senderUserProfileId get() = senderUserProfile.id
    val senderUserName get() = senderUserProfile.userName
    val isMyMessage: Boolean = isMyMessage ?: (senderUserProfile.id == myUserProfile.id)

    fun isMyChatReaction(reaction: R): Boolean = myUserProfileId == reaction.userProfileId

    fun setReactions(chatMessageReactions: List<R>) {
        _chatReactions.value = chatMessageReactions
    }
}
