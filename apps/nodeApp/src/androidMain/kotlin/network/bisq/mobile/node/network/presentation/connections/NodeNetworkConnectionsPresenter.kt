package network.bisq.mobile.node.network.presentation.connections

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import network.bisq.mobile.node.common.domain.service.network.NodeNetworkServiceFacade
import network.bisq.mobile.node.common.domain.service.network.NodePeerInfo
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.main.MainPresenter

class NodeNetworkConnectionsPresenter(
    private val networkServiceFacade: NodeNetworkServiceFacade,
    mainPresenter: MainPresenter,
) : BasePresenter(mainPresenter) {
    private val _uiState = MutableStateFlow(NodeNetworkConnectionsUiState())
    val uiState: StateFlow<NodeNetworkConnectionsUiState> = _uiState.asStateFlow()

    override fun onViewAttached() {
        super.onViewAttached()
        presenterScope.launch {
            combine(
                networkServiceFacade.connectedPeers,
                networkServiceFacade.myNodeInfo,
            ) { peers, nodeInfo ->
                // Newest peers first; stable tie-break so the LazyColumn keys don't reshuffle across rebuilds.
                val sorted =
                    peers.sortedWith(
                        compareByDescending<NodePeerInfo> { it.establishedAtMillis }.thenBy { it.connectionId },
                    )
                NodeNetworkConnectionsUiState(
                    peerCount = sorted.size,
                    peers = sorted,
                    keyId = nodeInfo.keyId,
                    nodeTag = nodeInfo.nodeTag,
                )
            }.collect { _uiState.value = it }
        }
    }
}
