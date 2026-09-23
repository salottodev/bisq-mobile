package network.bisq.mobile.presentation.trade.trade_detail

import android.view.Window
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.utils.DataEntry
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_1.SellerState1Presenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_2.state_a.SellerState2aPresenter
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.PresentationKoinComposeTestBase
import network.bisq.mobile.test.presentation.compose.isSecure
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A trade in raw INIT used to render a fully blank step 1 — the "dead box" a user with a stuck
 * trade stares at. This pins the waiting fallback for that branch.
 */
class TradeFlowPaneUiTest : PresentationKoinComposeTestBase() {
    @Test
    fun `raw INIT phase renders the waiting fallback instead of a blank step`() {
        val presenter = mockk<TradeFlowPresenter>(relaxed = true)
        every { presenter.tradePhaseState } returns MutableStateFlow(TradeFlowPresenter.TradePhaseState.INIT)
        every { presenter.steps } returns
            listOf(
                TradeFlowPresenter.TradeFlowStep.ACCOUNT_DETAILS,
                TradeFlowPresenter.TradeFlowStep.FIAT_PAYMENT,
                TradeFlowPresenter.TradeFlowStep.BITCOIN_TRANSFER,
                TradeFlowPresenter.TradeFlowStep.TRADE_COMPLETED,
            )
        every { presenter.presenterForPhase(any()) } returns null

        setTestContent {
            TradeFlowPane(presenter)
        }

        composeTestRule.onNodeWithText("mobile.bisqEasy.openTrades.waitingForTradeData".i18n()).assertExists()
    }

    /**
     * The phase steps are inline in the stepper, so the screenshot protection a phase applies has to
     * follow the phase the trade is in — which the per-phase tests cannot show on their own.
     */
    @Test
    fun `screenshot protection follows the phase the trade is in`() {
        val phase = MutableStateFlow(TradeFlowPresenter.TradePhaseState.SELLER_STATE1)
        val sellerState1Presenter =
            mockk<SellerState1Presenter>(relaxed = true) {
                every { paymentAccountDataEntry } returns MutableStateFlow(DataEntry())
                every { paymentAccountName } returns MutableStateFlow("")
                every { accounts } returns MutableStateFlow(emptyList())
                every { isSendPaymentDataEnabled } returns MutableStateFlow(true)
            }
        val sellerState2aPresenter =
            mockk<SellerState2aPresenter>(relaxed = true) {
                every { selectedTrade } returns MutableStateFlow(null)
            }
        val presenter = mockk<TradeFlowPresenter>(relaxed = true)
        every { presenter.tradePhaseState } returns phase
        every { presenter.steps } returns
            listOf(
                TradeFlowPresenter.TradeFlowStep.ACCOUNT_DETAILS,
                TradeFlowPresenter.TradeFlowStep.FIAT_PAYMENT,
                TradeFlowPresenter.TradeFlowStep.BITCOIN_TRANSFER,
                TradeFlowPresenter.TradeFlowStep.TRADE_COMPLETED,
            )
        every { presenter.presenterForPhase(TradeFlowPresenter.TradePhaseState.SELLER_STATE1) } returns sellerState1Presenter
        every { presenter.presenterForPhase(TradeFlowPresenter.TradePhaseState.SELLER_STATE2A) } returns sellerState2aPresenter

        lateinit var window: Window
        setTestContent {
            CaptureHostWindow { window = it }
            TradeFlowPane(presenter)
        }
        assertTrue(window.isSecure, "the seller's account data entry must be protected")

        phase.value = TradeFlowPresenter.TradePhaseState.SELLER_STATE2A
        composeTestRule.waitForIdle()
        assertFalse(window.isSecure, "waiting for the fiat payment shows nothing to protect")

        phase.value = TradeFlowPresenter.TradePhaseState.SELLER_STATE1
        composeTestRule.waitForIdle()
        assertTrue(window.isSecure, "returning to the account data entry protects it again")
    }
}
