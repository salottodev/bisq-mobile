package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_2.state_a

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.presentation.common.ui.security.CaptureHostWindow
import network.bisq.mobile.presentation.common.ui.security.isSecure
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
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
}
