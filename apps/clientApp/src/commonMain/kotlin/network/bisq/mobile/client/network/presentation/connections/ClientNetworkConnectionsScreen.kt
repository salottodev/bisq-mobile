package network.bisq.mobile.client.network.presentation.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.i18n.i18nPlural
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.InfoIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.components.layout.BisqScaffold
import network.bisq.mobile.presentation.common.ui.components.molecules.TopBar
import network.bisq.mobile.presentation.common.ui.components.network.ConnectionCard
import network.bisq.mobile.presentation.common.ui.components.network.ConnectionsIdentityHeader
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionsEmptyState
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import network.bisq.mobile.presentation.common.ui.utils.RememberPresenterLifecycleBackStackAware

@Composable
fun ClientNetworkConnectionsScreen() {
    val presenter = RememberPresenterLifecycleBackStackAware<ClientNetworkConnectionsPresenter>()
    val uiState by presenter.uiState.collectAsState()

    ClientNetworkConnectionsContent(
        uiState = uiState,
        topBar = { TopBar("mobile.networkInfo.connections.title".i18n(), showUserAvatar = false) },
    )
}

@Composable
internal fun ClientNetworkConnectionsContent(
    uiState: ClientNetworkConnectionsUiState,
    topBar: @Composable () -> Unit,
) {
    BisqScaffold(topBar = topBar) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = BisqUIConstants.ScreenPadding,
                        vertical = BisqUIConstants.ScreenPadding,
                    ),
        ) {
            // Connect users are one hop removed: these peers belong to the trusted node, not the device.
            // Kept above the list (and the empty state) so the trust framing is always visible.
            ViaYourNodeBanner()
            BisqGap.VHalf()

            // These connections belong to the trusted node — echo its identity. Self-hides while the link is down.
            ConnectionsIdentityHeader(keyId = uiState.keyId, nodeTag = uiState.nodeTag)
            if (uiState.keyId != null) {
                BisqGap.VHalf()
            }

            if (uiState.peers.isEmpty()) {
                NetworkConnectionsEmptyState(
                    modifier = Modifier.weight(1f),
                    hintKey = "mobile.networkInfo.connect.connections.emptyHint",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingHalf),
                ) {
                    item {
                        BisqText.SmallRegular(
                            text = "mobile.networkInfo.connections.peers".i18nPlural(uiState.peerCount),
                            color = BisqTheme.colors.mid_grey20,
                        )
                    }
                    items(uiState.peers, key = { it.connectionId }) { peer ->
                        ConnectionCard(peer = peer)
                    }
                }
            }
        }
    }
}

@Composable
private fun ViaYourNodeBanner() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                .background(BisqTheme.colors.dark_grey40)
                .padding(
                    horizontal = BisqUIConstants.ScreenPadding,
                    vertical = BisqUIConstants.ScreenPadding,
                ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start,
    ) {
        InfoIcon()
        BisqGap.H1()
        BisqText.XSmallLight(
            text = "mobile.networkInfo.connect.connections.banner".i18n(),
            color = BisqTheme.colors.mid_grey20,
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ClientNetworkConnectionsContentPopulatedPreview() {
    BisqTheme.Preview {
        ClientNetworkConnectionsContent(
            uiState =
                ClientNetworkConnectionsUiState(
                    peerCount = 2,
                    peers =
                        listOf(
                            NetworkConnectionUiItem(
                                connectionId = "1",
                                address = "abcd1234efgh5678ijkl.onion:1234",
                                isOutbound = true,
                                isSeed = true,
                                establishedAtMillis = 0L,
                            ),
                            NetworkConnectionUiItem(
                                connectionId = "2",
                                address = "mnop9012qrst3456uvwx.onion:1234",
                                isOutbound = false,
                                isSeed = false,
                                establishedAtMillis = 0L,
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
private fun ClientNetworkConnectionsContentEmptyPreview() {
    BisqTheme.Preview {
        ClientNetworkConnectionsContent(
            uiState = ClientNetworkConnectionsUiState(),
            topBar = {},
        )
    }
}
