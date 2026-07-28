package network.bisq.mobile.node.network.presentation.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.i18n.i18nPlural
import network.bisq.mobile.node.common.domain.service.network.NodePeerInfo
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.layout.BisqScaffold
import network.bisq.mobile.presentation.common.ui.components.molecules.TopBar
import network.bisq.mobile.presentation.common.ui.components.network.ConnectionCard
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionsEmptyState
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import network.bisq.mobile.presentation.common.ui.utils.RememberPresenterLifecycleBackStackAware

@Composable
fun NodeNetworkConnectionsScreen() {
    val presenter = RememberPresenterLifecycleBackStackAware<NodeNetworkConnectionsPresenter>()
    val uiState by presenter.uiState.collectAsState()

    NodeNetworkConnectionsContent(
        uiState = uiState,
        topBar = { TopBar("mobile.networkInfo.connections.title".i18n(), showUserAvatar = false) },
    )
}

@Composable
internal fun NodeNetworkConnectionsContent(
    uiState: NodeNetworkConnectionsUiState,
    topBar: @Composable () -> Unit,
) {
    BisqScaffold(topBar = topBar) { paddingValues ->
        if (uiState.peers.isEmpty()) {
            NetworkConnectionsEmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(
                            horizontal = BisqUIConstants.ScreenPadding,
                            vertical = BisqUIConstants.ScreenPadding,
                        ),
                verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingHalf),
            ) {
                item {
                    BisqText.SmallRegular(
                        text = "mobile.networkInfo.connections.peers".i18nPlural(uiState.peerCount),
                        color = BisqTheme.colors.mid_grey20,
                    )
                }
                items(uiState.peers, key = { it.connectionId }) { peer ->
                    ConnectionCard(peer = peer.toUiItem())
                }
            }
        }
    }
}

private fun NodePeerInfo.toUiItem(): NetworkConnectionUiItem =
    NetworkConnectionUiItem(
        connectionId = connectionId,
        address = address,
        isOutbound = isOutbound,
        isSeed = isSeed,
        establishedAtMillis = establishedAtMillis,
    )

@ExcludeFromCoverage
@Preview
@Composable
private fun NodeNetworkConnectionsContentPopulatedPreview() {
    BisqTheme.Preview {
        NodeNetworkConnectionsContent(
            uiState =
                NodeNetworkConnectionsUiState(
                    peerCount = 3,
                    peers =
                        listOf(
                            NodePeerInfo(
                                connectionId = "1",
                                address = "abcd1234efgh5678ijkl.onion:1234",
                                isOutbound = true,
                                establishedAtMillis = 0L,
                                isSeed = true,
                            ),
                            NodePeerInfo(
                                connectionId = "2",
                                address = "mnop9012qrst3456uvwx.onion:1234",
                                isOutbound = false,
                                establishedAtMillis = 0L,
                                isSeed = false,
                            ),
                        ),
                ),
            topBar = {},
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun NodeNetworkConnectionsContentEmptyPreview() {
    BisqTheme.Preview {
        NodeNetworkConnectionsContent(
            uiState = NodeNetworkConnectionsUiState(),
            topBar = {},
        )
    }
}
