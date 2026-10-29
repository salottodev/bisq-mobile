package network.bisq.mobile.domain.utils

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogRedactionTest {
    private class RemoteFailure(
        val status: Int,
        body: String,
        cause: Throwable? = null,
    ) : Exception(body, cause),
        LogRedactable {
        override fun redactedDetails(): String = "http=$status"
    }

    private class NothingToAdd(
        message: String,
    ) : Exception(message),
        LogRedactable {
        override fun redactedDetails(): String? = null
    }

    private class CapturingWriter(
        private val loggable: Boolean = true,
    ) : LogWriter() {
        val lines = mutableListOf<Triple<Severity, String, Throwable?>>()

        override fun isLoggable(
            tag: String,
            severity: Severity,
        ): Boolean = loggable

        override fun log(
            severity: Severity,
            message: String,
            tag: String,
            throwable: Throwable?,
        ) {
            lines += Triple(severity, "$tag/$message", throwable)
        }
    }

    @Test
    fun `summary is the class name for a plain exception`() {
        assertEquals("IllegalStateException", IllegalStateException("peer 3f9a2c1d rejected").redactedSummary())
    }

    @Test
    fun `summary never contains the message of any throwable in the chain`() {
        val secret = "onion=abcdef.onion sessionId=42"
        val error = RuntimeException("outer $secret", RemoteFailure(400, "body $secret", IllegalArgumentException("root $secret")))

        val summary = error.redactedSummary()

        assertFalse(summary.contains("secret"), summary)
        assertFalse(summary.contains("onion"), summary)
        assertFalse(summary.contains("42"), summary)
    }

    @Test
    fun `summary walks the cause chain from outer to root`() {
        val error = RuntimeException("outer", RemoteFailure(503, "body", IllegalArgumentException("root")))

        assertEquals("RuntimeException <- RemoteFailure(http=503) <- IllegalArgumentException", error.redactedSummary())
    }

    @Test
    fun `summary appends redactable details in parentheses and skips null details`() {
        assertEquals("RemoteFailure(http=404)", RemoteFailure(404, "not found").redactedSummary())
        assertEquals("NothingToAdd", NothingToAdd("quiet").redactedSummary())
    }

    // initCause is JVM-only; a mutable cause link builds the cycle on every target.
    private class Linked : Exception("linked") {
        var next: Throwable? = null
        override val cause: Throwable? get() = next
    }

    @Test
    fun `summary stops on a cause cycle`() {
        val a = Linked()
        val b = Linked()
        a.next = b
        b.next = a

        assertEquals("Linked <- Linked", a.redactedSummary())
    }

    @Test
    fun `summary caps the chain depth`() {
        var error: Throwable = IllegalStateException("0")
        repeat(10) { error = RuntimeException("$it", error) }

        assertEquals(5, error.redactedSummary().split(" <- ").size)
    }

    @Test
    fun `writer forwards a message without throwable unchanged`() {
        val delegate = CapturingWriter()

        RedactingLogWriter(delegate).log(Severity.Warn, "plain", "Tag", null)

        val (severity, line, throwable) = delegate.lines.single()
        assertEquals(Severity.Warn, severity)
        assertEquals("Tag/plain", line)
        assertNull(throwable)
    }

    @Test
    fun `writer replaces the throwable with its summary and passes none on`() {
        val delegate = CapturingWriter()
        val error = RuntimeException("response body with peer data", RemoteFailure(400, "body"))

        RedactingLogWriter(delegate).log(Severity.Error, "Failed to take offer", "Tag", error)

        val (severity, line, throwable) = delegate.lines.single()
        assertEquals(Severity.Error, severity)
        assertEquals("Tag/Failed to take offer | RuntimeException <- RemoteFailure(http=400)", line)
        assertNull(throwable)
        assertFalse(line.contains("peer data"), line)
    }

    @Test
    fun `writer delegates isLoggable`() {
        assertTrue(RedactingLogWriter(CapturingWriter(loggable = true)).isLoggable("Tag", Severity.Debug))
        assertFalse(RedactingLogWriter(CapturingWriter(loggable = false)).isLoggable("Tag", Severity.Error))
    }

    @Test
    fun `a web url is logged as its scheme and host only`() {
        assertEquals("https://mempool.space", urlForLog("https://mempool.space/tx/4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b?x=1#top"))
        assertEquals("http://example.org:8080", urlForLog("http://user:secret@example.org:8080/path"))
    }

    @Test
    fun `a payment uri is logged as its scheme only`() {
        assertEquals("bitcoin:<redacted>", urlForLog("bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025"))
        assertEquals("lightning:<redacted>", urlForLog("lightning:lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyq"))
        assertEquals("bitcoin:<redacted>", urlForLog("BITCOIN:BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4"))
        assertEquals("market:<redacted>", urlForLog("market://details?id=network.bisq.mobile"))
    }

    @Test
    fun `text without a scheme is logged as invalid`() {
        assertEquals("invalid-url", urlForLog(""))
        assertEquals("invalid-url", urlForLog("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"))
        assertEquals("invalid-url", urlForLog("not a url"))
    }
}
