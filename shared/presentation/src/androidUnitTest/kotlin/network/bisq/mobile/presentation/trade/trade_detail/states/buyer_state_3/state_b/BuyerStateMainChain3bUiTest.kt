package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_3.state_b

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import kotlin.test.assertTrue

class BuyerStateMainChain3bUiTest : BisqComposeUiTestBase() {
    @Test
    fun `transaction id view blocks screenshots even before the trade loads`() {
        val presenter =
            mockk<BuyerStateMainChain3bPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BuyerStateMainChain3b(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
