package network.bisq.mobile.client.common.domain.websocket.subscription

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import network.bisq.mobile.client.common.domain.websocket.messages.WebSocketEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Delivery contract of [WebSocketEventObserver.webSocketEvent]: no event is conflated away, a late collector gets the
 * latest event, and a stalled collector never blocks [WebSocketEventObserver.setEvent].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketEventObserverTest {
    @Test
    fun `consecutive events are all delivered in order`() =
        runTest {
            val observer = WebSocketEventObserver()
            val received = mutableListOf<Int>()
            val job = launch { observer.webSocketEvent.collect { received += it.sequenceNumber } }
            runCurrent()

            // E.g. paymentAccountData and tradeState of one trade, pushed back to back by the node.
            observer.setEvent(event(sequenceNumber = 1))
            observer.setEvent(event(sequenceNumber = 2))
            runCurrent()

            assertEquals(listOf(1, 2), received)
            job.cancel()
        }

    @Test
    fun `a collector that starts after the snapshot still receives it`() =
        runTest {
            val observer = WebSocketEventObserver()
            observer.setEvent(event(sequenceNumber = 0))

            val received = mutableListOf<Int>()
            val job = launch { observer.webSocketEvent.collect { received += it.sequenceNumber } }
            runCurrent()

            assertEquals(listOf(0), received)
            job.cancel()
        }

    @Test
    fun `setEvent does not wait for a stalled collector`() =
        runTest {
            val observer = WebSocketEventObserver()
            val job = launch { observer.webSocketEvent.collect { awaitCancellation() } }
            runCurrent()

            // setEvent runs on the WebSocket reader: suspending it here would stall every topic.
            withTimeout(1.seconds) {
                repeat(1_000) { observer.setEvent(event(sequenceNumber = it + 1)) }
            }
            job.cancel()
        }

    private fun event(sequenceNumber: Int) =
        WebSocketEvent(
            topic = Topic.TRADE_PROPERTIES,
            subscriberId = "test-subscriber",
            deferredPayload = "[]",
            modificationType = ModificationType.ADDED,
            sequenceNumber = sequenceNumber,
        )
}
