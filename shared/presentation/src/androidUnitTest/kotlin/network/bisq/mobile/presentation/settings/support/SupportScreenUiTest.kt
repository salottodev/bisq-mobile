package network.bisq.mobile.presentation.settings.support

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import network.bisq.mobile.domain.service.community.CommunitySegment
import network.bisq.mobile.domain.utils.DeviceInfoProvider
import network.bisq.mobile.domain.utils.VersionProvider
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.context.ExternalUrlOpener
import network.bisq.mobile.presentation.common.ui.components.context.LocalExternalUrlOpener
import network.bisq.mobile.presentation.common.ui.components.molecules.ITopBarPresenter
import network.bisq.mobile.presentation.common.ui.components.molecules.PreviewTopBarPresenter
import network.bisq.mobile.presentation.common.ui.navigation.NavRoute
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.test.fixtures.testCommunityHubService
import network.bisq.mobile.test.presentation.compose.PresentationKoinComposeTestBase
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * #1746 asks for the Support chat to be reachable from the Help screen as well as the hub, and the
 * Help screen is the entry that has to withhold it: it is reachable on every build, including the
 * ones that serve no public chat.
 *
 * The issue also asks for back navigation to be correct from both entry paths, and the hub path is
 * covered (`CommunityHubScreenSegmentUiTest`) because the hub has a segment selection to preserve.
 * This path has nothing: `SupportPresenter` is a Koin `factory` behind `RememberPresenterLifecycle`,
 * so returning from the channel builds a fresh presenter that seeds availability from the hub
 * service, and there is no state a return could arrive stale with. A remount test here was written
 * and dropped — neither reverting the synchronous seeding nor rebinding the presenter as a `single`
 * reddened it, so it asserted nothing the mount tests do not already assert. Correct by
 * construction; re-test the path the day this presenter starts carrying state across a visit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SupportScreenUiTest : PresentationKoinComposeTestBase() {
    private lateinit var mainPresenter: MainPresenter
    private var enabledSegments: Set<CommunitySegment> = emptySet()

    private val openChannelLabel = "mobile.community.support.openChannel".i18n()

    override fun additionalModules(): List<Module> =
        listOf(
            module {
                single<ITopBarPresenter> { PreviewTopBarPresenter() }
                factory {
                    SupportPresenter(
                        mainPresenter,
                        mockk<VersionProvider>(relaxed = true),
                        mockk<DeviceInfoProvider>(relaxed = true),
                        testCommunityHubService(
                            enabled = enabledSegments,
                            // What is under test is the rollout flag, not the capability gate, and
                            // the fixture's default requirements come from production — so the day
                            // DISCUSSIONS declares one, this would withhold the entry for a reason
                            // it is not about.
                            requiredFeatures = emptyMap(),
                            dispatcher = UnconfinedTestDispatcher(testDispatcher.scheduler),
                        ),
                    )
                }
            },
        )

    override fun onKoinReady() {
        mainPresenter = mockk(relaxed = true)
    }

    /** The screen is a wall of external links, and every one of them reads the opener. */
    private fun setSupportScreen() =
        setTestContent {
            CompositionLocalProvider(LocalExternalUrlOpener provides ExternalUrlOpener { true }) {
                SupportScreen()
            }
        }

    @Test
    fun `the support channel entry is offered when discussions is live`() {
        enabledSegments = setOf(CommunitySegment.DISCUSSIONS)

        setSupportScreen()

        composeTestRule.onNodeWithText(openChannelLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.support.communityChannels".i18n()).assertIsDisplayed()
    }

    @Test
    fun `the support channel entry is withheld when discussions is not live`() {
        enabledSegments = emptySet()

        setSupportScreen()

        composeTestRule.onNodeWithText(openChannelLabel).assertDoesNotExist()
        composeTestRule.onNodeWithText("mobile.support.communityChannels".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.support.matrix".i18n()).assertIsDisplayed()
    }

    /** The same destination the hub row pushes, so both entry points land on one channel. */
    @Test
    fun `tapping the support channel entry pushes the channel`() {
        enabledSegments = setOf(CommunitySegment.DISCUSSIONS)
        setSupportScreen()

        composeTestRule.onNodeWithText(openChannelLabel).performClick()
        composeTestRule.waitForIdle()

        verify { navigationManager.navigate(NavRoute.SupportChannel, any(), any()) }
    }
}
