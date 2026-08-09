package network.bisq.mobile.data.replicated.chat.reactions

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum

/**
 * Common surface of a reaction on any chat message, mirroring Bisq 2's
 * `bisq.chat.reactions.ChatMessageReaction` field for field.
 *
 * Not every member is read polymorphically today — shared code groups and renders by [reactionId],
 * and [network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage.isMyChatReaction] compares
 * [userProfileId] — but the hierarchy is kept faithful so a mobile type can be read next to its
 * Bisq 2 counterpart without translating it.
 */
interface ChatMessageReaction {
    val id: String

    /**
     * The reaction's author. Bisq 2 stores this on the base class; the private reactions populate it
     * by passing `senderUserProfile.getId()` up to the superclass constructor. Here the wire format
     * carries the whole profile instead, so they derive it from
     * [PrivateChatMessageReaction.senderUserProfile].
     */
    val userProfileId: String

    val chatChannelId: String

    val chatChannelDomain: ChatChannelDomainEnum

    val chatMessageId: String

    /** Ordinal of the [ReactionEnum] icon. */
    val reactionId: Int

    val date: Long
}
