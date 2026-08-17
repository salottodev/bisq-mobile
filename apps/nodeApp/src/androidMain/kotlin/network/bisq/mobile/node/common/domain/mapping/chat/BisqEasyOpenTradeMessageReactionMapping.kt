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

/**
 * The Bisq 2 constructor is Java, so the arguments can only be positional. Four of them are adjacent
 * `String`s, and swapping any two still compiles — the order below is
 * `id, senderUserProfile, receiverUserProfileId, receiverNetworkId, chatChannelId,
 * chatChannelDomain, chatMessageId, reactionId, date, isRemoved`.
 */
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
