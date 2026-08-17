package network.bisq.mobile.data.replicated.presentation.open_trades

import io.mockk.every
import io.mockk.mockk
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel
import network.bisq.mobile.data.replicated.trade.bisq_easy.BisqEasyTradeModel
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.i18n.i18n
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeItemPresentationModelDirectionTest {
    @BeforeTest
    fun initI18n() {
        I18nSupport.initialize("en")
    }

    @Test
    fun directionalTitle_usesIsSeller_whenSeller() {
        val model = createModel(isSeller = true)

        assertEquals(
            "bisqEasy.openTrades.table.direction.seller".i18n().uppercase(),
            model.directionalTitle,
        )
    }

    @Test
    fun directionalTitle_usesIsSeller_whenBuyer() {
        val model = createModel(isSeller = false)

        assertEquals(
            "bisqEasy.openTrades.table.direction.buyer".i18n().uppercase(),
            model.directionalTitle,
        )
    }

    /** Only [BisqEasyTradeModel.isSeller] matters here; the rest is filler the model needs. */
    private fun createModel(isSeller: Boolean): TradeItemPresentationModel {
        val tradeModel = mockk<BisqEasyTradeModel>(relaxed = true)
        every { tradeModel.isSeller } returns isSeller

        return TradeItemPresentationModel(
            channelModel = mockk<BisqEasyOpenTradeChannel>(relaxed = true),
            bisqEasyTradeModel = tradeModel,
            makerUserProfile = createMockUserProfile("maker"),
            takerUserProfile = createMockUserProfile("taker"),
            formattedDate = "",
            formattedTime = "",
            market = "",
            price = 0L,
            formattedPrice = "",
            baseAmount = 0L,
            formattedBaseAmount = "",
            quoteAmount = 0L,
            formattedQuoteAmount = "",
            bitcoinSettlementMethod = "",
            bitcoinSettlementMethodDisplayString = "",
            fiatPaymentMethod = "",
            fiatPaymentMethodDisplayString = "",
            isFiatPaymentMethodCustom = false,
            formattedMyRole = "",
            peersReputationScore = mockk<ReputationScoreVO>(relaxed = true),
        )
    }
}
