package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_1.state_a

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.presentation.common.ui.components.molecules.inputfield.BitcoinLnAddressFieldType
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertTrue

class BuyerState1aUiTest : BisqComposeUiTestBase() {
    @Test
    fun `bitcoin address entry blocks screenshots`() {
        val presenter =
            mockk<BuyerState1aPresenter>(relaxed = true) {
                every { headline } returns MutableStateFlow("")
                every { description } returns MutableStateFlow("")
                every { bitcoinPaymentData } returns MutableStateFlow("")
                every { bitcoinLnAddressFieldType } returns MutableStateFlow(BitcoinLnAddressFieldType.Bitcoin)
                every { triggerBitcoinLnAddressValidation } returns MutableStateFlow(0)
                every { isSendBitcoinPaymentDataEnabled } returns MutableStateFlow(true)
                every { wasPrefilled } returns MutableStateFlow(false)
                every { hasConfirmedPrefill } returns MutableStateFlow(false)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BuyerState1a(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
