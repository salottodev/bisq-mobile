package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_3.state_a

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import kotlin.test.assertFalse

/**
 * This phase is deliberately left unprotected: it only says the buyer is waiting for the seller's
 * Bitcoin settlement, and its `bitcoinPaymentData` is the i18n label ("Bitcoin address"), never the
 * address itself. Issue #1868 lists it, so this test records the decision.
 */
class BuyerState3aUiTest : BisqComposeUiTestBase() {
    @Test
    fun `waiting for settlement carries no secrets and allows screenshots`() {
        val presenter =
            mockk<BuyerState3aPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BuyerState3a(presenter = presenter)
        }

        assertFalse(window.isSecure)
    }
}
