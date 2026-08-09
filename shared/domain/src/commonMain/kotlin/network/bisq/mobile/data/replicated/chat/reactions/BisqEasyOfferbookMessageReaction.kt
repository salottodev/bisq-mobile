package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.Serializable
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum

/**
 * A reaction on a public offerbook message. Implements [ChatMessageReaction] directly, with no
 * private level — mirroring Bisq 2, where `BisqEasyOfferbookMessageReaction extends
 * ChatMessageReaction`. A public reaction has no removed state, hence no [PrivateChatMessageReaction].
 */
@Serializable
data class BisqEasyOfferbookMessageReaction(
    override val id: String,
    override val userProfileId: String,
    override val chatChannelId: String,
    override val chatChannelDomain: ChatChannelDomainEnum,
    override val chatMessageId: String,
    override val reactionId: Int,
    override val date: Long,
) : ChatMessageReaction
