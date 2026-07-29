package network.bisq.mobile.client.common.domain.service.network

import kotlinx.serialization.Serializable

@Serializable
data class NetworkInfoDto(
    val allDataReceived: Boolean,
    val torRunning: Boolean,
    val myAddress: String? = null,
    val keyId: String? = null,
    val connections: List<ConnectionDto> = emptyList(),
)

@Serializable
data class ConnectionDto(
    val connectionId: String,
    val address: String,
    val outbound: Boolean,
    val seed: Boolean,
    val establishedAtMillis: Long,
    // Null when the trusted node runs an older bisq2 that doesn't send per-peer metrics; the card then
    // stays non-expandable, matching pre-metrics behavior.
    val metrics: ConnectionMetricsDto? = null,
)

@Serializable
data class ConnectionMetricsDto(
    // Null until a round-trip has been measured (handshake / request-response); rendered as "–".
    val rttMillis: Long? = null,
    val sentBytes: Long = 0,
    val sentMessageCount: Long = 0,
    val receivedBytes: Long = 0,
    val receivedMessageCount: Long = 0,
)
