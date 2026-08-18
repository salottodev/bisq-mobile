package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.reactions.BisqEasyOpenTradeMessageReaction as Bisq2BisqEasyOpenTradeMessageReaction

fun Bisq2BisqEasyOpenTradeMessageReaction.toDomain(): BisqEasyOpenTradeMessageReaction =
    BisqEasyOpenTradeMessageReaction(
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
