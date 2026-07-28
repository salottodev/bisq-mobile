package network.bisq.mobile.client.network.presentation.connections

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import network.bisq.mobile.client.common.domain.service.network.ClientConnectivityService
import network.bisq.mobile.client.common.domain.service.network.ClientNetworkServiceFacade
import network.bisq.mobile.client.common.domain.service.network.ConnectionDto
import network.bisq.mobile.client.common.domain.service.network.NetworkInfoDto
import network.bisq.mobile.client.common.test_utils.ClientKoinIntegrationTestBase
import network.bisq.mobile.data.service.network.ConnectivityService.ConnectivityStatus
import network.bisq.mobile.presentation.main.MainPresenter
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ClientNetworkConnectionsPresenterTest : ClientKoinIntegrationTestBase() {
    private val networkServiceFacade: ClientNetworkServiceFacade = mockk(relaxed = true)
    private val connectivityService: ClientConnectivityService = mockk(relaxed = true)
    private val mainPresenter: MainPresenter = mockk(relaxed = true)

    private val status = MutableStateFlow(ConnectivityStatus.CONNECTED_AND_DATA_RECEIVED)
    private val networkInfo = MutableStateFlow<NetworkInfoDto?>(null)

    private lateinit var presenter: ClientNetworkConnectionsPresenter

    override fun onSetup() {
        every { connectivityService.status } returns status
        every { networkServiceFacade.networkInfo } returns networkInfo
    }

    private fun createPresenter(): ClientNetworkConnectionsPresenter =
        ClientNetworkConnectionsPresenter(
            networkServiceFacade = networkServiceFacade,
            connectivityService = connectivityService,
            mainPresenter = mainPresenter,
        )

    @Test
    fun `when peers are present then uiState exposes count and peers`() =
        runTest {
            // Given
            networkInfo.value =
                networkInfoWith(
                    samplePeer(connectionId = "a", establishedAtMillis = 100L),
                    samplePeer(connectionId = "b", establishedAtMillis = 200L),
                )

            // When
            presenter = createPresenter()
            presenter.onViewAttached()
            advanceUntilIdle()

            // Then
            val state = presenter.uiState.value
            assertEquals(2, state.peerCount)
            assertEquals(2, state.peers.size)
        }

    @Test
    fun `when there are no peers then uiState is empty`() =
        runTest {
            // Given the node pushed a snapshot with no connections
            networkInfo.value = networkInfoWith()

            // When
            presenter = createPresenter()
            presenter.onViewAttached()
            advanceUntilIdle()

            // Then
            val state = presenter.uiState.value
            assertEquals(0, state.peerCount)
            assertEquals(emptyList(), state.peers)
        }

    @Test
    fun `when peers arrive unordered then newest established peers come first`() =
        runTest {
            // Given peers in mixed establishment order
            networkInfo.value =
                networkInfoWith(
                    samplePeer(connectionId = "early", establishedAtMillis = 100L),
                    samplePeer(connectionId = "late", establishedAtMillis = 300L),
                    samplePeer(connectionId = "mid", establishedAtMillis = 200L),
                )

            // When
            presenter = createPresenter()
            presenter.onViewAttached()
            advanceUntilIdle()

            // Then newest (highest establishedAtMillis) first
            assertEquals(
                listOf("late", "mid", "early"),
                presenter.uiState.value.peers
                    .map { it.connectionId },
            )
        }

    @Test
    fun `when the peer list changes then uiState updates reactively`() =
        runTest {
            // Given an attached presenter with no peers
            networkInfo.value = networkInfoWith()
            presenter = createPresenter()
            presenter.onViewAttached()
            advanceUntilIdle()
            assertEquals(0, presenter.uiState.value.peerCount)

            // When a peer connects
            networkInfo.value = networkInfoWith(samplePeer(connectionId = "a"))
            advanceUntilIdle()

            // Then
            assertEquals(1, presenter.uiState.value.peerCount)
        }

    @Test
    fun `when the link drops then the peer list is cleared`() =
        runTest {
            // Given a reachable link with peers
            networkInfo.value = networkInfoWith(samplePeer(connectionId = "a"))
            presenter = createPresenter()
            presenter.onViewAttached()
            advanceUntilIdle()
            assertEquals(1, presenter.uiState.value.peerCount)

            // When connectivity drops (the last snapshot is not cleared by the facade)
            status.value = ConnectivityStatus.DISCONNECTED
            advanceUntilIdle()

            // Then the stale peers are not shown
            val state = presenter.uiState.value
            assertEquals(0, state.peerCount)
            assertEquals(emptyList(), state.peers)
        }

    private fun networkInfoWith(vararg peers: ConnectionDto): NetworkInfoDto =
        NetworkInfoDto(
            allDataReceived = true,
            torRunning = true,
            connections = peers.toList(),
        )

    private fun samplePeer(
        connectionId: String = "id",
        address: String = "abcd.onion:1234",
        outbound: Boolean = true,
        seed: Boolean = false,
        establishedAtMillis: Long = 0L,
    ): ConnectionDto =
        ConnectionDto(
            connectionId = connectionId,
            address = address,
            outbound = outbound,
            seed = seed,
            establishedAtMillis = establishedAtMillis,
        )
}
