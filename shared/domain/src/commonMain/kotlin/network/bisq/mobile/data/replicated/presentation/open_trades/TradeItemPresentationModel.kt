package network.bisq.mobile.data.replicated.presentation.open_trades

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.offer.price.spec.FixPriceSpecVO
import network.bisq.mobile.data.replicated.trade.bisq_easy.BisqEasyTradeModel
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.domain.formatters.PriceSpecFormatter
import network.bisq.mobile.i18n.i18n

/**
 * This model is used in the UI and will get the mutual fields updated from domain services.
 */
data class TradeItemPresentationModel(
    private val channelModel: BisqEasyOpenTradeChannel?,
    val bisqEasyTradeModel: BisqEasyTradeModel,
    val makerUserProfile: UserProfileVO,
    val takerUserProfile: UserProfileVO,
    val formattedDate: String,
    val formattedTime: String,
    val market: String,
    val price: Long,
    val formattedPrice: String,
    val baseAmount: Long,
    val formattedBaseAmount: String,
    val quoteAmount: Long,
    val formattedQuoteAmount: String,
    val bitcoinSettlementMethod: String,
    val bitcoinSettlementMethodDisplayString: String,
    val fiatPaymentMethod: String,
    val fiatPaymentMethodDisplayString: String,
    val isFiatPaymentMethodCustom: Boolean,
    val formattedMyRole: String,
    val peersReputationScore: ReputationScoreVO,
) {
    // Non-null accessor: throws for closed trades if called in open-trade context
    val bisqEasyOpenTradeChannelModel: BisqEasyOpenTradeChannel
        get() = channelModel ?: error("Trade $tradeId has no channel (closed trade)")

    val directionalTitle: String
        get() =
            if (bisqEasyTradeModel.isSeller) {
                "bisqEasy.openTrades.table.direction.seller".i18n().uppercase()
            } else {
                "bisqEasy.openTrades.table.direction.buyer".i18n().uppercase()
            }

    val formattedPriceSpec: String
        get() {
            val spec = bisqEasyOffer.priceSpec
            return if (spec is FixPriceSpecVO) "" else "(${PriceSpecFormatter.getFormattedPriceSpec(spec, true)})"
        }
    val paymentMethodCsvDisplayString: String
        get() = "$bitcoinSettlementMethodDisplayString / $fiatPaymentMethodDisplayString"

    // Convenience properties
    val myUserProfile: UserProfileVO
        get() = if (bisqEasyTradeModel.isMaker) makerUserProfile else takerUserProfile
    val myUserName: String get() = myUserProfile.userName

    val peersUserProfile: UserProfileVO get() = if (bisqEasyTradeModel.isMaker) takerUserProfile else makerUserProfile
    val peersUserName: String get() = peersUserProfile.userName
    val mediator: UserProfileVO? get() = bisqEasyTradeModel.contract.mediator
    val mediatorUserName: String? get() = mediator?.userName

    val bisqEasyOffer: BisqEasyOfferVO
        get() = channelModel?.bisqEasyOffer ?: bisqEasyTradeModel.contract.offer
    val offerId: String get() = bisqEasyOffer.id
    val tradeId: String get() = bisqEasyTradeModel.id
    val shortTradeId: String get() = bisqEasyTradeModel.shortId
    val baseCurrencyCode: String get() = bisqEasyOffer.market.baseCurrencyCode
    val quoteCurrencyCode: String get() = bisqEasyOffer.market.quoteCurrencyCode
    val quoteAmountWithCode: String get() = "$formattedQuoteAmount $quoteCurrencyCode"
    val baseAmountWithCode: String get() = "$formattedBaseAmount $baseCurrencyCode"

    override fun toString(): String =
        """
        TradeItemPresentationModel(
            tradeId=$tradeId,
            shortTradeId=$shortTradeId,
            offerId=$offerId,
            baseCurrencyCode=$baseCurrencyCode,
            quoteCurrencyCode=$quoteCurrencyCode,
            quoteAmountWithCode=$quoteAmountWithCode,
            baseAmountWithCode=$baseAmountWithCode,
            makerUserName=${makerUserProfile.userName},
            takerUserName=${takerUserProfile.userName},
            myUserName=$myUserName,
            peersUserName=$peersUserName,
            formattedDate=$formattedDate,
            formattedTime=$formattedTime,
            market=$market,
            price=$price,
            formattedPrice=$formattedPrice,
            bitcoinSettlementMethod=$bitcoinSettlementMethodDisplayString,
            fiatPaymentMethod=$fiatPaymentMethodDisplayString,
            mediatorUserName=$mediatorUserName
        )
        """.trimIndent()
}
