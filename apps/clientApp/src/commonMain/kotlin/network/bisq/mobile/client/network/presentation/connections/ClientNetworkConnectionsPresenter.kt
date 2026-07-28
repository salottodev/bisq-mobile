package network.bisq.mobile.client.network.presentation.connections

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import network.bisq.mobile.client.common.domain.service.network.ClientConnectivityService
import network.bisq.mobile.client.common.domain.service.network.ClientNetworkServiceFacade
import network.bisq.mobile.client.common.domain.service.network.ConnectionDto
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.common.ui.components.network.NetworkConnectionUiItem
import network.bisq.mobile.presentation.main.MainPresenter

class ClientNetworkConnectionsPresenter(
    private val networkServiceFacade: ClientNetworkServiceFacade,
    private val connectivityService: ClientConnectivityService,
    mainPresenter: MainPresenter,
) : BasePresenter(mainPresenter) {
    private val _uiState = MutableStateFlow(ClientNetworkConnectionsUiState())
    val uiState: StateFlow<ClientNetworkConnectionsUiState> = _uiState.asStateFlow()

    override fun onViewAttached() {
        super.onViewAttached()
        presenterScope.launch {
            combine(
                connectivityService.status,
                networkServiceFacade.networkInfo,
            ) { status, info ->
                // networkInfo is not cleared when the link drops, so gate the list on reachability:
                // otherwise the sub-page would show a full peer list while the Overview shows OFFLINE.
                if (status.isConnected()) info?.connections.orEmpty() else emptyList()
            }.collect { connections ->
                // Newest peers first; stable tie-break so the LazyColumn keys don't reshuffle across rebuilds.
                val peers =
                    connections
                        .sortedWith(
                            compareByDescending<ConnectionDto> { it.establishedAtMillis }.thenBy { it.connectionId },
                        ).map { it.toUiItem() }
                _uiState.value =
                    ClientNetworkConnectionsUiState(
                        peerCount = peers.size,
                        peers = peers,
                    )
            }
        }
    }

    private fun ConnectionDto.toUiItem(): NetworkConnectionUiItem =
        NetworkConnectionUiItem(
            connectionId = connectionId,
            address = address,
            isOutbound = outbound,
            isSeed = seed,
            establishedAtMillis = establishedAtMillis,
        )
}
