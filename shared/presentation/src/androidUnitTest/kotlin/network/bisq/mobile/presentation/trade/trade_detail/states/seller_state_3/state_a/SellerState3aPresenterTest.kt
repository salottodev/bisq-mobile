package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.test.presentation.coroutines.PresentationKoinTestBase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SellerState3aPresenterTest : PresentationKoinTestBase() {
    private val mainPresenter: MainPresenter = mockk(relaxed = true)
    private val tradesServiceFacade: TradesServiceFacade = mockk(relaxed = true)

    @Test
    fun `rapid double-tap on confirmSend triggers sellerConfirmBtcSent only once`() =
        runTest {
            val presenter = SellerState3aPresenter(mainPresenter, tradesServiceFacade)
            presenter.onPaymentProofInput("tx-id-123", isValid = true)
            coEvery { tradesServiceFacade.sellerConfirmBtcSent(any()) } coAnswers {
                delay(Long.MAX_VALUE)
                Result.success(Unit)
            }

            presenter.confirmSend()
            presenter.confirmSend()
            advanceUntilIdle()

            coVerify(exactly = 1) { tradesServiceFacade.sellerConfirmBtcSent("tx-id-123") }
            assertFalse(presenter.isConfirmSendEnabled.value)
        }

    @Test
    fun `confirm send failure re-enables confirm guard`() =
        runTest {
            val presenter = SellerState3aPresenter(mainPresenter, tradesServiceFacade)
            presenter.onPaymentProofInput("tx-id-123", isValid = true)
            coEvery { tradesServiceFacade.sellerConfirmBtcSent(any()) } returns
                Result.failure(RuntimeException("confirm failed"))

            presenter.confirmSend()
            advanceUntilIdle()

            assertTrue(presenter.isConfirmSendEnabled.value)
        }

    @Test
    fun `show and close toggle the qr sheet`() =
        runTest {
            val presenter = SellerState3aPresenter(mainPresenter, tradesServiceFacade)
            assertFalse(presenter.isQrSheetVisible.value)

            presenter.onShowQr()
            assertTrue(presenter.isQrSheetVisible.value)

            presenter.onCloseQr()
            assertFalse(presenter.isQrSheetVisible.value)
        }

    @Test
    fun `open in wallet closes the sheet and hands the schemed uri to the url launcher`() =
        runTest {
            val presenter = SellerState3aPresenter(mainPresenter, tradesServiceFacade)
            val uri = "lightning:lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypq"
            presenter.onShowQr()

            presenter.onOpenInWallet(uri)
            advanceUntilIdle()

            // The error snackbar lives in the activity window, which the sheet would cover.
            assertFalse(presenter.isQrSheetVisible.value)
            coVerify(exactly = 1) { mainPresenter.navigateToUrlWithLauncher(uri) }
        }
}
