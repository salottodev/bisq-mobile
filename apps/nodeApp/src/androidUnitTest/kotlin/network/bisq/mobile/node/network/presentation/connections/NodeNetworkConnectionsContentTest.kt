package network.bisq.mobile.node.network.presentation.connections

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.i18n.i18nPlural
import network.bisq.mobile.node.common.domain.service.network.NodePeerInfo
import network.bisq.mobile.node.common.test_utils.TestApplication
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.utils.LocalIsTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(application = TestApplication::class)
@RunWith(AndroidJUnit4::class)
class NodeNetworkConnectionsContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        I18nSupport.setLanguage()
    }

    private fun setTestContent(uiState: NodeNetworkConnectionsUiState) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalIsTest provides true) {
                BisqTheme {
                    NodeNetworkConnectionsContent(uiState = uiState, topBar = {})
                }
            }
        }
    }

    @Test
    fun `when there are no peers then the empty state is displayed`() {
        setTestContent(NodeNetworkConnectionsUiState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.empty".i18n())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.emptyHint".i18n())
            .assertIsDisplayed()
    }

    @Test
    fun `when peers are present then the peer count line is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.peers".i18nPlural(2))
            .assertIsDisplayed()
    }

    @Test
    fun `when there is a single peer then the peer count line uses singular grammar`() {
        setTestContent(singlePeerState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.peers".i18nPlural(1))
            .assertIsDisplayed()
    }

    @Test
    fun `when peers are present then their addresses are displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("outbound.onion:1234")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a peer is outbound then the outbound label is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.outbound".i18n())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a peer is inbound then the inbound label is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.inbound".i18n())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a peer is a seed then the seed badge is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.seed".i18n())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a peer has a measured rtt then it is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("184 ms")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a peer rtt is not yet measured then a dash is displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("–")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a card is tapped then the sent and received section is revealed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        // Collapsed by default: the Sent/Received labels are not present yet.
        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.sent".i18n())
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag("connection_card_1").performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.sent".i18n())
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.received".i18n())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when a card is expanded then the sent and received traffic is formatted`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        // Only card 1 is expanded, so these strings are unambiguous.
        composeTestRule.onNodeWithTag("connection_card_1").performClick()
        composeTestRule.waitForIdle()

        // 12_400 bytes / 340 messages and 18_900 bytes / 512 messages, plural key.
        composeTestRule
            .onNodeWithText("12.1 KB · 340 msgs")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("18.5 KB · 512 msgs")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when an expanded peer has a single message then the count uses singular grammar`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        // Peer 2 received exactly one message — the only case that hits the ioData.1 key.
        composeTestRule.onNodeWithTag("connection_card_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("128 B · 1 msg")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `when identity is present then the key id and node tag are displayed`() {
        setTestContent(sampleState())
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.identity.keyId".i18n())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("mobile.networkInfo.connections.identity.nodeTag".i18n())
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("default")
            .assertIsDisplayed()
    }

    private fun sampleState(): NodeNetworkConnectionsUiState =
        NodeNetworkConnectionsUiState(
            peerCount = 2,
            keyId = "02a1c98f4b7e29d3f18a",
            nodeTag = "default",
            peers =
                listOf(
                    NodePeerInfo(
                        connectionId = "1",
                        address = "outbound.onion:1234",
                        isOutbound = true,
                        establishedAtMillis = 0L,
                        isSeed = true,
                        rttMillis = 184L,
                        sentBytes = 12_400L,
                        sentMessageCount = 340L,
                        receivedBytes = 18_900L,
                        receivedMessageCount = 512L,
                    ),
                    NodePeerInfo(
                        connectionId = "2",
                        address = "inbound.onion:1234",
                        isOutbound = false,
                        establishedAtMillis = 0L,
                        isSeed = false,
                        rttMillis = null,
                        sentBytes = 0L,
                        sentMessageCount = 0L,
                        receivedBytes = 128L,
                        receivedMessageCount = 1L,
                    ),
                ),
        )

    private fun singlePeerState(): NodeNetworkConnectionsUiState =
        NodeNetworkConnectionsUiState(
            peerCount = 1,
            peers =
                listOf(
                    NodePeerInfo(
                        connectionId = "1",
                        address = "outbound.onion:1234",
                        isOutbound = true,
                        establishedAtMillis = 0L,
                        isSeed = false,
                        rttMillis = 62L,
                        sentBytes = 2_048L,
                        sentMessageCount = 12L,
                        receivedBytes = 4_096L,
                        receivedMessageCount = 20L,
                    ),
                ),
        )
}
