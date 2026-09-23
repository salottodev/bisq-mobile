package network.bisq.mobile.presentation.tabs.my_trades.closed

import android.view.Window
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import network.bisq.mobile.domain.model.trade.ClosedTradeListItem
import network.bisq.mobile.presentation.common.ui.components.molecules.ITopBarPresenter
import network.bisq.mobile.presentation.common.ui.components.molecules.PreviewTopBarPresenter
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.PresentationKoinComposeTestBase
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.test.assertTrue

/**
 * The details dialog of a closed trade keeps the payment account data and the BTC address the live
 * trade screens hide, so the list screen carries the protection the dialog inherits.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClosedTradeListScreenUiTest : PresentationKoinComposeTestBase() {
    private lateinit var presenter: ClosedTradeListPresenter

    override fun additionalModules(): List<Module> =
        listOf(
            module {
                single { presenter }
                single<ITopBarPresenter> { PreviewTopBarPresenter() }
            },
        )

    override fun onKoinReady() {
        presenter =
            mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(ClosedTradeListUiState())
                every { totalCount } returns MutableStateFlow(0)
                every { pagingData } returns flowOf(PagingData.empty<ClosedTradeListItem>())
            }
    }

    @Test
    fun `closed trade history blocks screenshots`() {
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            ClosedTradeListScreen()
        }

        assertTrue(window.isSecure)
    }
}
