package network.bisq.mobile.client.common.domain.service.chat.trade

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * The transport type is a client concern; [BisqEasyOpenTradeMessage] is the shared domain model both
 * apps present. The node produces the same model straight from its embedded Bisq 2 types.
 */
fun BisqEasyOpenTradeMessageDto.toDomain(
    myUserProfile: UserProfileVO,
    chatReactions: List<BisqEasyOpenTradeMessageReaction>,
): BisqEasyOpenTradeMessage =
    BisqEasyOpenTradeMessage(
        id = messageId,
        chatMessageType = chatMessageType,
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        date = date,
        senderUserProfile = senderUserProfile,
        myUserProfile = myUserProfile,
        chatReactions = chatReactions,
        tradeId = tradeId,
        mediator = mediator,
        bisqEasyOffer = bisqEasyOffer,
    )
