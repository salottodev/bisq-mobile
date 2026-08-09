package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.reactions.BisqEasyOpenTradeMessageReaction as Bisq2BisqEasyOpenTradeMessageReaction

fun Bisq2BisqEasyOpenTradeMessageReaction.toDomain(): BisqEasyOpenTradeMessageReaction =
    BisqEasyOpenTradeMessageReaction(
        id,
        Mappings.UserProfileMapping.fromBisq2Model(senderUserProfile),
        receiverUserProfileId,
        Mappings.NetworkIdMapping.fromBisq2Model(receiverNetworkId),
        chatChannelId,
        Mappings.ChatChannelDomainMapping.fromBisq2Model(chatChannelDomain),
        chatMessageId,
        reactionId,
        date,
        isRemoved,
    )

fun BisqEasyOpenTradeMessageReaction.toBisq2(): Bisq2BisqEasyOpenTradeMessageReaction =
    Bisq2BisqEasyOpenTradeMessageReaction(
        id,
        Mappings.UserProfileMapping.toBisq2Model(senderUserProfile),
        receiverUserProfileId,
        Mappings.NetworkIdMapping.toBisq2Model(receiverNetworkId),
        chatChannelId,
        Mappings.ChatChannelDomainMapping.toBisq2Model(chatChannelDomain),
        chatMessageId,
        reactionId,
        date,
        isRemoved,
    )
