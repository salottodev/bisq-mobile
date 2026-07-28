package network.bisq.mobile.client.network.presentation.connections

import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem

data class ClientNetworkConnectionsUiState(
    val peerCount: Int = 0,
    val peers: List<NetworkConnectionUiItem> = emptyList(),
)
