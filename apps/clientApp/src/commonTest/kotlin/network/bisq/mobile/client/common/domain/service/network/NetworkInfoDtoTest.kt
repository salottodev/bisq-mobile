package network.bisq.mobile.client.common.domain.service.network

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkInfoDtoTest {
    // ignoreUnknownKeys mirrors the production client Json: unknown or renamed wire fields decode to
    // defaults instead of throwing, so the exact-value assertions below are what catch silent
    // "0 B · 0 msgs" drift if bisq2's payload or these property names ever diverge.
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
}
