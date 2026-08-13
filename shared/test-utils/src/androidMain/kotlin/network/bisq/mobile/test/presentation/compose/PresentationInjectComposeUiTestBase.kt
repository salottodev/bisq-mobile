package network.bisq.mobile.test.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.junit.After
import org.koin.compose.KoinIsolatedContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Leaf base for `:shared:presentation` Compose UI tests that drive a whole screen resolved through
 * `RememberPresenterLifecycleBackStackAware`. The presentation-side counterpart of
 * `ClientInjectComposeUiTestBase`; prefer [PresentationKoinComposeTestBase] whenever the test can
 * render a stateless `Content` instead.
 *
 * Such a screen needs two things a plain `setContent` does not give it, both provided by
 * [setInjectTestContent]:
 *
 * - a [LocalViewModelStoreOwner], because the lifecycle helper stores the presenter in a
 *   `viewModel { }` and without an owner the composition cannot create one;
 * - an explicitly provided Koin context, because `org.koin.compose.getKoin()` — which that helper
 *   uses — caches its default context wrapper process-wide and only re-resolves when reading it
 *   *throws*. The first test in a class would otherwise pin the Koin instance and every later test
 *   would resolve against the one stopped in teardown (`Scope '_root_' is closed`). `koinInject` is
 *   immune; it goes through `currentKoinScope()`, which does check `scope.closed`.
 *
 * Koin itself still comes from [PresentationKoinComposeTestBase] — `presentationTestModule` plus
 * `additionalModules()`; do **not** call `startKoin` here.
 */
abstract class PresentationInjectComposeUiTestBase : PresentationKoinComposeTestBase() {
    protected lateinit var viewModelStore: ViewModelStore
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    override fun onSetup() {
        viewModelStore = ViewModelStore()
        viewModelStoreOwner =
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = this@PresentationInjectComposeUiTestBase.viewModelStore
            }
        super.onSetup()
    }

    /**
     * Runs before `KoinIntegrationTestBase.baseTearDown` (JUnit calls the subclass `@After` first),
     * so the presenter is released while its container is still alive: clearing the store fires
     * `PresenterHolder.onCleared()` → `onViewUnattaching()`.
     */
    @After
    fun tearDownInjectCompose() {
        runCatching { composeTestRule.waitForIdle() }.onFailure { if (it is CancellationException) throw it }
        runCatching { viewModelStore.clear() }.onFailure { if (it is CancellationException) throw it }
    }

    /** [setTestContent] plus this test's own Koin graph and [ViewModelStore]. */
    protected fun setInjectTestContent(content: @Composable () -> Unit) {
        setTestContent {
            KoinIsolatedContext(koinApplication) {
                CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                    content()
                }
            }
        }
    }
}
