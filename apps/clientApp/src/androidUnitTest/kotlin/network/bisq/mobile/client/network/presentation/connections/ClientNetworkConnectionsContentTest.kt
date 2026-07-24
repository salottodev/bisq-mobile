package network.bisq.mobile.client.network.presentation.connections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import network.bisq.mobile.client.common.test_utils.TestApplication
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.i18n.i18nPlural
import network.bisq.mobile.presentation.common.test_utils.compose.BisqComposeUiTestBase
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = TestApplication::class)
class ClientNetworkConnectionsContentTest : BisqComposeUiTestBase() {
    @Test
    fun `when there are no peers then the empty state and banner are displayed`() {
        setTestContent {
            ClientNetworkConnectionsContent(uiState = ClientNetworkConnectionsUiState(), topBar = {})
        }
        composeTestRule.waitForIdle()

        // Trust framing stays visible even with no peers.
        composeTestRule.onNodeWithText("mobile.networkInfo.connect.connections.banner".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connections.empty".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connect.connections.emptyHint".i18n()).assertIsDisplayed()
    }

    @Test
    fun `when peers are present then the via-your-node banner and peer count are displayed`() {
        setTestContent {
            ClientNetworkConnectionsContent(uiState = sampleState(), topBar = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("mobile.networkInfo.connect.connections.banner".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connections.peers".i18nPlural(2)).assertIsDisplayed()
    }

    @Test
    fun `when peers are present then their addresses and direction labels are displayed`() {
        setTestContent {
            ClientNetworkConnectionsContent(uiState = sampleState(), topBar = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("outbound.onion:1234").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connections.outbound".i18n()).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connections.inbound".i18n()).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("mobile.networkInfo.connections.seed".i18n()).performScrollTo().assertIsDisplayed()
    }

    private fun sampleState(): ClientNetworkConnectionsUiState =
        ClientNetworkConnectionsUiState(
            peerCount = 2,
            peers =
                listOf(
                    NetworkConnectionUiItem(
                        connectionId = "1",
                        address = "outbound.onion:1234",
                        isOutbound = true,
                        isSeed = true,
                        establishedAtMillis = 0L,
                    ),
                    NetworkConnectionUiItem(
                        connectionId = "2",
                        address = "inbound.onion:1234",
                        isOutbound = false,
                        isSeed = false,
                        establishedAtMillis = 0L,
                    ),
                ),
        )
}
