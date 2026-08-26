package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.two_party.TwoPartyPrivateChatMessageReaction
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.reactions.TwoPartyPrivateChatMessageReaction as Bisq2TwoPartyPrivateChatMessageReaction

/**
 * Inbound only, per the replicated data conventions: nothing needs to turn a mobile reaction back
 * into a Bisq 2 one — removing a reaction re-derives it from the reaction id instead.
 */
fun Bisq2TwoPartyPrivateChatMessageReaction.toDomain(): TwoPartyPrivateChatMessageReaction =
    TwoPartyPrivateChatMessageReaction(
        id = id,
        senderUserProfile = Mappings.UserProfileMapping.fromBisq2Model(senderUserProfile),
        receiverUserProfileId = receiverUserProfileId,
        receiverNetworkId = Mappings.NetworkIdMapping.fromBisq2Model(receiverNetworkId),
        chatChannelId = chatChannelId,
        chatChannelDomain = Mappings.ChatChannelDomainMapping.fromBisq2Model(chatChannelDomain),
        chatMessageId = chatMessageId,
        reactionId = reactionId,
        date = date,
        isRemoved = isRemoved,
    )
