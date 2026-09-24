package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_b

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.presentation.trade.trade_detail.states.common.TxConfirmationState
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import kotlin.test.assertTrue

class SellerStateMainChain3bUiTest : BisqComposeUiTestBase() {
    @Test
    fun `transaction id view blocks screenshots`() {
        val presenter =
            mockk<SellerStateMainChain3bPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
                every { txConfirmationState } returns MutableStateFlow(TxConfirmationState.IDLE)
                every { balanceFromTx } returns MutableStateFlow("")
                every { buttonText } returns MutableStateFlow("")
                every { errorMessage } returns MutableStateFlow(null)
                every { blockExplorer } returns MutableStateFlow("")
                every { skip } returns MutableStateFlow(false)
                every { amountNotMatchingDialogText } returns MutableStateFlow(null)
                every { isCompleteTradeEnabled } returns MutableStateFlow(true)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            SellerStateMainChain3b(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
