package network.bisq.mobile.presentation.community

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import network.bisq.mobile.domain.service.community.CommunitySegment
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI tests for [CommunityHubScreenContent]: the gated tab row (hidden for a single live
 * segment, rendered for several), the pinned Support quick access, and the placeholder body.
 */
class CommunityHubScreenUiTest : BisqComposeUiTestBase() {
    private val actions = mutableListOf<CommunityHubUiAction>()

    private fun setContent(uiState: CommunityHubUiState) {
        actions.clear()
        setTestContent {
            CommunityHubScreenContent(uiState = uiState, onAction = actions::add)
        }
    }

    private fun tabLabel(key: String) = key.i18n()

    @Test
    fun `single live segment renders no tab row but shows support row and placeholder`() {
        setContent(
            CommunityHubUiState(
                liveSegments = listOf(CommunitySegment.DISCUSSIONS),
                selectedSegment = CommunitySegment.DISCUSSIONS,
            ),
        )

        composeTestRule.onNodeWithText(tabLabel("mobile.community.tab.messages")).assertDoesNotExist()
        composeTestRule.onNodeWithText(tabLabel("mobile.community.support.needHelp")).assertIsDisplayed()
        composeTestRule
            .onNodeWithText("mobile.community.comingSoon".i18n(), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `multiple live segments render the tab row and tapping one selects it`() {
        setContent(
            CommunityHubUiState(
                liveSegments = CommunitySegment.entries.toList(),
                selectedSegment = CommunitySegment.DISCUSSIONS,
            ),
        )

        composeTestRule.onNodeWithText(tabLabel("mobile.community.tab.contacts")).assertIsDisplayed()
        composeTestRule.onNodeWithText(tabLabel("mobile.community.tab.messages")).performClick()

        assertEquals(listOf<CommunityHubUiAction>(CommunityHubUiAction.OnSegmentSelect(CommunitySegment.MESSAGES)), actions)
    }

    @Test
    fun `support row tap emits the open support action`() {
        setContent(
            CommunityHubUiState(
                liveSegments = listOf(CommunitySegment.DISCUSSIONS),
                selectedSegment = CommunitySegment.DISCUSSIONS,
            ),
        )

        composeTestRule.onNodeWithText(tabLabel("mobile.community.support.needHelp")).performClick()

        assertEquals(listOf<CommunityHubUiAction>(CommunityHubUiAction.OnOpenSupportChannel), actions)
    }

    /**
     * The row navigates as of #1746, and `NavigationManagerImpl.navigate` applies no `launchSingleTop`,
     * so an undebounced double tap leaves two Support screens on the stack and two backs to get out.
     *
     * The one real-time dependency in this class: `rememberDebouncedClick` reads `Clock.System.now()`,
     * not the test clock, so the two taps have to land within the 300 ms window in wall time. There is
     * no virtual clock to advance instead — if this ever flakes red on a stalled CI box, that is why.
     */
    @Test
    fun `a double tap on the support row opens the channel once`() {
        setContent(
            CommunityHubUiState(
                liveSegments = listOf(CommunitySegment.DISCUSSIONS),
                selectedSegment = CommunitySegment.DISCUSSIONS,
            ),
        )

        val supportRow = composeTestRule.onNodeWithText(tabLabel("mobile.community.support.needHelp"))
        supportRow.performClick()
        supportRow.performClick()

        assertEquals(listOf<CommunityHubUiAction>(CommunityHubUiAction.OnOpenSupportChannel), actions)
    }

    @Test
    fun `no live segments renders the bare coming soon state`() {
        setContent(CommunityHubUiState())

        composeTestRule.onNodeWithText("mobile.community.comingSoon".i18n()).assertIsDisplayed()
    }

    /**
     * The row now navigates, so offering it with no segment live would push a public chat thread in a
     * build that serves none. Hard to reach in practice — `TabContainerPresenter` hides the hub icon
     * while `liveSegments` is empty — but the state is representable, so the gate stays.
     */
    @Test
    fun `support row is hidden when no segment is live`() {
        setContent(CommunityHubUiState())

        composeTestRule.onNodeWithText(tabLabel("mobile.community.support.needHelp")).assertDoesNotExist()
    }

    /** The pinned Support row is Discussions-context only — directory/inbox segments drop it. */
    @Test
    fun `support row is hidden on the contacts segment`() {
        setContent(
            CommunityHubUiState(
                liveSegments = CommunitySegment.entries.toList(),
                selectedSegment = CommunitySegment.CONTACTS,
            ),
        )

        composeTestRule.onNodeWithText(tabLabel("mobile.community.support.needHelp")).assertDoesNotExist()
        composeTestRule.onNodeWithText(tabLabel("mobile.community.tab.contacts")).assertIsDisplayed()
    }
}
