package network.bisq.mobile.data.service.network

import io.matthewnelson.kmp.tor.runtime.Action
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpTorServiceStopTest {
    @After
    fun tearDown() {
        unmockkObject(Action.Companion)
    }

    @Test
    fun `stopTor stops the daemon when the caller is already cancelled`() =
        runTest {
            var daemonStopped = false
            val runtime = mockk<TorRuntime>()
            mockkObject(Action.Companion)
            coEvery { with(Action.Companion) { runtime.executeAsync(Action.StopDaemon) } } coAnswers {
                // kmp-tor checks that the caller is active before it enqueues the stop
                currentCoroutineContext().ensureActive()
                daemonStopped = true
                runtime
            }
            val service = createStartingService(runtime)

            // Like startTor's catch when the coroutine starting Tor is cancelled
            val job =
                launch {
                    try {
                        awaitCancellation()
                    } finally {
                        service.stopTor()
                    }
                }
            runCurrent()
            job.cancelAndJoin()

            assertTrue(daemonStopped)
            assertEquals(KmpTorService.TorState.Stopped(), service.state.value)
        }

    private fun createStartingService(runtime: TorRuntime): KmpTorService {
        val root = Files.createTempDirectory("kmpTorStop").toFile().apply { deleteOnExit() }
        val service = KmpTorService(root.absolutePath.toPath())
        setField(service, "torRuntime", runtime)
        @Suppress("UNCHECKED_CAST")
        (getField(service, "_state") as MutableStateFlow<KmpTorService.TorState>).value = KmpTorService.TorState.Starting
        return service
    }

    private fun setField(
        service: KmpTorService,
        name: String,
        value: Any?,
    ) {
        KmpTorService::class.java
            .getDeclaredField(name)
            .apply { isAccessible = true }
            .set(service, value)
    }

    private fun getField(
        service: KmpTorService,
        name: String,
    ): Any? =
        KmpTorService::class.java
            .getDeclaredField(name)
            .apply { isAccessible = true }
            .get(service)
}
