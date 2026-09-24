package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_2.state_a

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import kotlin.test.assertTrue

class BuyerState2aUiTest : BisqComposeUiTestBase() {
    @Test
    fun `seller account data view blocks screenshots even before the trade loads`() {
        val presenter =
            mockk<BuyerState2aPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
                every { isConfirmFiatSentEnabled } returns MutableStateFlow(true)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BuyerState2a(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }

    @Test
    fun `seller account data on screen blocks screenshots`() {
        val trade =
            mockk<TradeItemPresentationModel>(relaxed = true) {
                every { quoteAmountWithCode } returns "100 USD"
                every { bisqEasyTradeModel.paymentAccountData } returns MutableStateFlow("IBAN: DE89370400440532013000")
                every { bisqEasyTradeModel.shortId } returns "abc123"
            }
        val presenter =
            mockk<BuyerState2aPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(trade)
                every { isConfirmFiatSentEnabled } returns MutableStateFlow(true)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BuyerState2a(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
