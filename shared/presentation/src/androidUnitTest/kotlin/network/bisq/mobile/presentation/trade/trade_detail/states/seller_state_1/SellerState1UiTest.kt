package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_1

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.presentation.common.ui.security.CaptureHostWindow
import network.bisq.mobile.presentation.common.ui.security.isSecure
import network.bisq.mobile.presentation.common.ui.utils.DataEntry
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertTrue

class SellerState1UiTest : BisqComposeUiTestBase() {
    @Test
    fun `seller account data entry blocks screenshots`() {
        val presenter =
            mockk<SellerState1Presenter>(relaxed = true) {
                every { paymentAccountDataEntry } returns MutableStateFlow(DataEntry())
                every { paymentAccountName } returns MutableStateFlow("")
                every { accounts } returns MutableStateFlow(emptyList())
                every { isSendPaymentDataEnabled } returns MutableStateFlow(true)
            }
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            SellerState1(presenter = presenter)
        }

        assertTrue(window.isSecure)
    }
}
