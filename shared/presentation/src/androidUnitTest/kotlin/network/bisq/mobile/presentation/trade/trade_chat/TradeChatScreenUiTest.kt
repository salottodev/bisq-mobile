package network.bisq.mobile.presentation.trade.trade_chat

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.presentation.common.ui.components.molecules.ITopBarPresenter
import network.bisq.mobile.presentation.common.ui.components.molecules.PreviewTopBarPresenter
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.PresentationKoinComposeTestBase
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.test.assertTrue

class TradeChatScreenUiTest : PresentationKoinComposeTestBase() {
    private val presenter =
        mockk<TradeChatPresenter>(relaxed = true) {
            every { uiState } returns MutableStateFlow(TradeChatUiState())
            every { isSendChatMessageEnabled } returns MutableStateFlow(true)
            every { isConfirmIgnoreUserEnabled } returns MutableStateFlow(true)
            every { isConfirmUndoIgnoreUserEnabled } returns MutableStateFlow(true)
        }

    override fun additionalModules(): List<Module> =
        listOf(
            module {
                single<ITopBarPresenter> { PreviewTopBarPresenter() }
                single { presenter }
            },
        )

    @Test
    fun `trade chat blocks screenshots`() {
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            TradeChatScreen(tradeId = "trade-1")
        }

        assertTrue(window.isSecure)
    }
}
