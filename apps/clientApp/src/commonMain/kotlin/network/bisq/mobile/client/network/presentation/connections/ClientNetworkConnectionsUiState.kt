package network.bisq.mobile.client.network.presentation.connections

import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem

data class ClientNetworkConnectionsUiState(
    val peerCount: Int = 0,
    val peers: List<NetworkConnectionUiItem> = emptyList(),
    // The trusted node's key id (from NetworkInfoDto). nodeTag is not yet delivered over the websocket,
    // so it stays null and Connect's identity header shows only the key id for now.
    val keyId: String? = null,
    val nodeTag: String? = null,
)
