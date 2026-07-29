package network.bisq.mobile.client.common.domain.service.network

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkInfoDtoTest {
    // Mirrors the injected client Json (ClientDomainModule): unknown keys tolerated for forward-compat.
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ConnectionDto from an older node without a metrics field decodes with null metrics`() {
        // Given JSON from a trusted node running a bisq2 that predates per-peer metrics
        val jsonString =
            """
            {
                "connectionId": "conn-1",
                "address": "abcd.onion:1234",
                "outbound": true,
                "seed": false,
                "establishedAtMillis": 100
            }
            """.trimIndent()

        // When
        val dto = json.decodeFromString<ConnectionDto>(jsonString)

        // Then the card stays non-expandable rather than failing to parse
        assertNull(dto.metrics)
    }

    @Test
    fun `ConnectionDto with a nested metrics object decodes each field`() {
        // Given JSON matching the bisq2 API Jackson output (nested metrics object)
        val jsonString =
            """
            {
                "connectionId": "conn-1",
                "address": "abcd.onion:1234",
                "outbound": true,
                "seed": true,
                "establishedAtMillis": 100,
                "metrics": {
                    "rttMillis": 184,
                    "sentBytes": 12400,
                    "sentMessageCount": 340,
                    "receivedBytes": 18900,
                    "receivedMessageCount": 512
                }
            }
            """.trimIndent()

        // When
        val metrics = json.decodeFromString<ConnectionDto>(jsonString).metrics

        // Then
        assertEquals(184L, metrics?.rttMillis)
        assertEquals(12_400L, metrics?.sentBytes)
        assertEquals(340L, metrics?.sentMessageCount)
        assertEquals(18_900L, metrics?.receivedBytes)
        assertEquals(512L, metrics?.receivedMessageCount)
    }

    @Test
    fun `ConnectionMetricsDto with an unmeasured rtt decodes rttMillis as null`() {
        // Given the API sends null rttMillis until a round-trip is measured
        val jsonString =
            """
            {
                "connectionId": "conn-1",
                "address": "abcd.onion:1234",
                "outbound": false,
                "seed": false,
                "establishedAtMillis": 0,
                "metrics": {
                    "rttMillis": null,
                    "sentBytes": 0,
                    "sentMessageCount": 0,
                    "receivedBytes": 128,
                    "receivedMessageCount": 1
                }
            }
            """.trimIndent()

        // When
        val metrics = json.decodeFromString<ConnectionDto>(jsonString).metrics

        // Then
        assertNull(metrics?.rttMillis)
        assertEquals(128L, metrics?.receivedBytes)
        assertEquals(1L, metrics?.receivedMessageCount)
    }

    @Test
    fun `NetworkInfoDto round-trips connections carrying metrics`() {
        // Given
        val original =
            NetworkInfoDto(
                allDataReceived = true,
                torRunning = true,
                keyId = "trusted-key",
                connections =
                    listOf(
                        ConnectionDto(
                            connectionId = "conn-1",
                            address = "abcd.onion:1234",
                            outbound = true,
                            seed = false,
                            establishedAtMillis = 100,
                            metrics =
                                ConnectionMetricsDto(
                                    rttMillis = 62L,
                                    sentBytes = 2_048L,
                                    sentMessageCount = 12L,
                                    receivedBytes = 4_096L,
                                    receivedMessageCount = 20L,
                                ),
                        ),
                    ),
            )

        // When
        val decoded = json.decodeFromString<NetworkInfoDto>(json.encodeToString(original))

        // Then
        assertEquals(original, decoded)
    }
}
