package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertTrue

class SellerState3aUiTest : BisqComposeUiTestBase() {
    @Test
    fun `buyer bitcoin address view blocks screenshots even before the trade loads`() {
        val presenter =
            mockk<SellerState3aPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            SellerState3a(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
