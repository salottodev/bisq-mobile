package network.bisq.mobile.presentation.common.ui.navigation.graph

import android.app.Application
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.test.core.app.ApplicationProvider
import network.bisq.mobile.presentation.common.ui.navigation.NavRoute
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

/**
 * Guards the registration of [NavRoute.SupportChannel] in [addCommonAppRoutes]. Both entry points
 * to the in-app Support channel — the hub's "Need help?" row and More → Help — push that one route,
 * so a missing registration breaks both at once, and breaks them silently: `NavigationManagerImpl`
 * wraps the push in `runCatching { … }.onFailure { log.e(…) }`, which turns an unregistered route
 * into a log line and a dead tap, with nothing on screen to say so.
 *
 * No leaf base and no Koin, because building the graph only registers destinations — it never
 * invokes the `composable` content lambdas, so no screen is constructed and nothing needs injecting.
 * It sits in `androidUnitTest` rather than beside its `commonMain` subject because `NavHostController`
 * takes a `Context` on the Android target, so there is no portable version to write.
 *
 * Deliberately narrow, and [addCommonAppRoutes] keeps its `@ExcludeFromCoverage`: this pins the one
 * route a feature hangs off, it is not a bid to cover the graph.
 */
@RunWith(RobolectricTestRunner::class)
class CommonNavGraphTest {
    @Test
    fun `the support channel route is registered`() {
        val context: Application = ApplicationProvider.getApplicationContext()
        val navController = NavHostController(context)
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        val graph =
            navController.createGraph(startDestination = NavRoute.Support) {
                addCommonAppRoutes { true }
            }

        assertNotNull(graph.findNode<NavRoute.SupportChannel>())
    }
}
