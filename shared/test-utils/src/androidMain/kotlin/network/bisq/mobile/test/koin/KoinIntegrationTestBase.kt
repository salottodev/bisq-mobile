package network.bisq.mobile.test.koin

import network.bisq.mobile.test.coroutines.CoroutineTestBase
import org.junit.After
import org.junit.Before
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest

/**
 * Parameterized Koin test base for Android unit tests.
 *
 * Subclasses an app-specific leaf (e.g. `ClientKoinIntegrationTestBase`) that implements
 * [baseModules] — do not extend this class directly from test cases.
 */
abstract class KoinIntegrationTestBase :
    CoroutineTestBase(),
    KoinTest {
    /**
     * The graph this test owns. Kept because Compose needs the [KoinApplication] itself, not just
     * the global instance: `org.koin.compose.KoinIsolatedContext` takes one. Reassign it in any
     * base that restarts Koin mid-test, or a composition will hold on to the stopped graph.
     */
    protected lateinit var koinApplication: KoinApplication

    protected abstract fun baseModules(): List<Module>

    /**
     * Override to provide Koin modules loaded after [baseModules].
     */
    protected open fun additionalModules(): List<Module> = emptyList()

    /**
     * Called after [setUpCoroutines] and before Koin starts. Use to create mocks that must
     * exist before modules are loaded (e.g. navigation or UI managers passed into test modules).
     */
    protected open fun beforeStartKoin() {}

    protected open fun onSetup() {}

    protected open fun onTearDown() {}

    @Before
    fun baseSetup() {
        setUpCoroutines()
        beforeStartKoin()
        koinApplication =
            startKoin {
                // Loaded first so a test that binds its own AnalyticsService mock still wins.
                modules(analyticsTestModule)
                modules(baseModules())
                modules(additionalModules())
            }
        onSetup()
    }

    @After
    fun baseTearDown() {
        onTearDown()
        stopKoin()
        tearDownCoroutines()
    }
}
