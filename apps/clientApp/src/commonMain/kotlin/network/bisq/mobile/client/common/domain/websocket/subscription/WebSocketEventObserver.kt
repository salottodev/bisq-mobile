package network.bisq.mobile.client.common.domain.websocket.subscription

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import network.bisq.mobile.client.common.domain.websocket.messages.WebSocketEvent
import network.bisq.mobile.domain.utils.Logging

class WebSocketEventObserver : Logging {
    // Not a StateFlow: back-to-back events must not conflate. Replay gives a late collector only the latest event,
    // a delta if one followed the snapshot. The unlimited buffer keeps setEvent from suspending the reader.
    private val _webSocketEvent = MutableSharedFlow<WebSocketEvent>(replay = 1, extraBufferCapacity = Channel.UNLIMITED)
    val webSocketEvent: SharedFlow<WebSocketEvent> = _webSocketEvent.asSharedFlow()
    private val sequenceNumber = MutableStateFlow(-1)
    private val sequenceMutex = Mutex()

    val hasReceivedData: Flow<Boolean> = sequenceNumber.map { it != -1 }

    suspend fun resetSequence() {
        sequenceMutex.withLock {
            sequenceNumber.value = -1
        }
    }

    suspend fun setEvent(value: WebSocketEvent) {
        sequenceMutex.withLock {
            if (sequenceNumber.value >= value.sequenceNumber) {
                log.w {
                    "Sequence number is larger or equal than the one we " +
                        "received from the backend. We ignore that event."
                }
                return
            }
            sequenceNumber.value = value.sequenceNumber
            // Under the lock so events reach collectors in sequence order.
            _webSocketEvent.emit(value)
        }
    }
}
