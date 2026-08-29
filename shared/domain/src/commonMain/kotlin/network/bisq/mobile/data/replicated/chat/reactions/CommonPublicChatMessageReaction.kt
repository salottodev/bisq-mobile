package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.Serializable
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum

/**
 * A reaction on a discussion or support message. Like [BisqEasyOfferbookMessageReaction], it
 * implements [ChatMessageReaction] directly — Bisq 2's `CommonPublicChatMessageReaction extends
 * ChatMessageReaction` — with no sender/receiver envelope and no removed state.
 */
@Serializable
data class CommonPublicChatMessageReaction(
    override val id: String,
    override val userProfileId: String,
    override val chatChannelId: String,
    override val chatChannelDomain: ChatChannelDomainEnum,
    override val chatMessageId: String,
    override val reactionId: Int,
    override val date: Long,
) : ChatMessageReaction
