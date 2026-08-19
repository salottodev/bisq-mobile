package network.bisq.mobile.client.common.domain.service.trades

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.replicated.trade.bisq_easy.BisqEasyTradeModel

/**
 * Replaces the former `TradeItemPresentationModel.from(dto)` factory, which could not stay in
 * `:shared:domain` once the DTO became client-owned. The node builds the same model directly from
 * its Bisq 2 types in `TradeItemPresentationModelFactory`.
 *
 * `directionalTitle` and `mediatorUserProfile` are deliberately not carried over: the model derives
 * both from [TradeItemPresentationModel.bisqEasyTradeModel], so the DTO's copies were already dead.
 */
fun TradeItemPresentationDto.toDomain(): TradeItemPresentationModel =
    TradeItemPresentationModel(
        channelModel = channel.toDomain(),
        bisqEasyTradeModel = BisqEasyTradeModel(trade),
        makerUserProfile = makerUserProfile,
        takerUserProfile = takerUserProfile,
        formattedDate = formattedDate,
        formattedTime = formattedTime,
        market = market,
        price = price,
        formattedPrice = formattedPrice,
        baseAmount = baseAmount,
        formattedBaseAmount = formattedBaseAmount,
        quoteAmount = quoteAmount,
        formattedQuoteAmount = formattedQuoteAmount,
        bitcoinSettlementMethod = bitcoinSettlementMethod,
        bitcoinSettlementMethodDisplayString = bitcoinSettlementMethodDisplayString,
        fiatPaymentMethod = fiatPaymentMethod,
        fiatPaymentMethodDisplayString = fiatPaymentMethodDisplayString,
        isFiatPaymentMethodCustom = isFiatPaymentMethodCustom,
        formattedMyRole = formattedMyRole,
        peersReputationScore = peersReputationScore,
    )

fun BisqEasyOpenTradeChannelDto.toDomain(): BisqEasyOpenTradeChannel =
    BisqEasyOpenTradeChannel(
        id = id,
        tradeId = tradeId,
        bisqEasyOffer = bisqEasyOffer,
        myUserIdentity = myUserIdentity,
        traders = traders,
        mediator = mediator,
    )
