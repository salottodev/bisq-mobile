package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel
import network.bisq.mobile.node.common.domain.mapping.Mappings
import kotlin.jvm.optionals.getOrNull
import bisq.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel as Bisq2BisqEasyOpenTradeChannel

fun Bisq2BisqEasyOpenTradeChannel.toDomain(): BisqEasyOpenTradeChannel =
    BisqEasyOpenTradeChannel(
        id = id,
        tradeId = tradeId,
        bisqEasyOffer = Mappings.BisqEasyOfferMapping.fromBisq2Model(bisqEasyOffer),
        myUserIdentity = Mappings.UserIdentityMapping.fromBisq2Model(myUserIdentity),
        traders = traders.map { Mappings.UserProfileMapping.fromBisq2Model(it) }.toSet(),
        mediator = mediator.getOrNull()?.let { Mappings.UserProfileMapping.fromBisq2Model(it) },
    )
