package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.reactions.CommonPublicChatMessageReaction as Bisq2CommonPublicChatMessageReaction

/**
 * Inbound only, per the replicated data conventions. Unlike the private reactions there is no
 * `isRemoved` flag: a public reaction is taken back by removing it from the P2P store, so what is
 * here is what is live.
 */
fun Bisq2CommonPublicChatMessageReaction.toDomain(): CommonPublicChatMessageReaction =
    CommonPublicChatMessageReaction(
        id = id,
        userProfileId = userProfileId,
        chatChannelId = chatChannelId,
        chatChannelDomain = Mappings.ChatChannelDomainMapping.fromBisq2Model(chatChannelDomain),
        chatMessageId = chatMessageId,
        reactionId = reactionId,
        date = date,
    )
