package network.bisq.mobile.node.settings.backup.presentation

import android.app.Application
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
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

/**
 * Plain [Application] instead of the node `TestApplication`: this base owns `startKoin`, and the
 * real node Application would boot the bisq2 stack.
 */
@Config(application = Application::class)
class BackupScreenUiTest : PresentationKoinComposeTestBase() {
    private val presenter =
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

    override fun additionalModules(): List<Module> =
        listOf(
            module {
                single<ITopBarPresenter> { PreviewTopBarPresenter() }
                single { presenter }
            },
        )

    @Test
    fun `backup screen blocks screenshots`() {
        lateinit var window: Window

        setTestContent {
            CaptureHostWindow { window = it }
            BackupScreen()
        }

        assertTrue(window.isSecure)
    }
}
