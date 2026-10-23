package network.bisq.mobile.client.common.domain.websocket

import network.bisq.mobile.client.common.domain.websocket.exception.IncompatibleHttpApiVersionException

sealed class ConnectionState {
    data class Disconnected(
        val error: Throwable? = null,
    ) : ConnectionState()

    object Connecting : ConnectionState()

    object Connected : ConnectionState()
}

val ConnectionState.incompatibleApiVersionError: IncompatibleHttpApiVersionException?
    get() = (this as? ConnectionState.Disconnected)?.error as? IncompatibleHttpApiVersionException
