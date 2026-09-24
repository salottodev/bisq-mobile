package network.bisq.mobile.node.settings.backup.presentation

import android.view.Window
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.node.common.test_utils.TestApplication
import network.bisq.mobile.presentation.common.ui.components.molecules.ITopBarPresenter
import network.bisq.mobile.presentation.common.ui.components.molecules.PreviewTopBarPresenter
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

/**
 * The screen resolves its presenter through `koinInject`, so the mock is loaded into the Koin
 * context `TestApplication` owns rather than through a base that would start a second one.
 */
@Config(application = TestApplication::class)
class BackupScreenUiTest : BisqComposeUiTestBase() {
    @Test
    fun `backup screen blocks screenshots`() {
        val presenter =
            mockk<BackupPresenter>(relaxed = true) {
                every { uiState } returns
                    MutableStateFlow(
                        BackupUiState(
                            showBackupDialog = false,
                            showWorkingDialog = false,
                            showRestorePasswordDialogForUri = null,
                            errorMessage = null,
                        ),
                    )
            }
        loadKoinModules(
            module {
                single<ITopBarPresenter> { PreviewTopBarPresenter() }
                // TopBar reads both, and the node test module's relaxed mock cannot type them.
                single<NavigationManager> {
                    mockk(relaxed = true) {
                        every { currentTab } returns MutableStateFlow(null)
                        every { showBackButton() } returns false
                    }
                }
                single { presenter }
            },
        )
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BackupScreen()
        }

        assertTrue(window.isSecure)
    }
}
