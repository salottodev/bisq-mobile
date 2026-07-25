package network.bisq.mobile.domain.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ByteUnitUtilTest {
    @Test
    fun `values below one KiB are returned as raw bytes`() {
        assertEquals("0 B", ByteUnitUtil.formatBytesPrecise(0))
        assertEquals("1 B", ByteUnitUtil.formatBytesPrecise(1))
        assertEquals("1023 B", ByteUnitUtil.formatBytesPrecise(1023))
    }

    @Test
    fun `the 1024 boundary scales to KB`() {
        assertEquals("1.0 KB", ByteUnitUtil.formatBytesPrecise(1024))
    }

    @Test
    fun `one decimal matches what the connections card renders`() {
        // The values behind "12.1 KB · 340 msgs" / "18.5 KB · 512 msgs" on the Connections screen.
        assertEquals("12.1 KB", ByteUnitUtil.formatBytesPrecise(12_400, decimals = 1))
        // 18900 / 1024 = 18.457… — pins the rounding direction, not just the truncation.
        assertEquals("18.5 KB", ByteUnitUtil.formatBytesPrecise(18_900, decimals = 1))
    }

    @Test
    fun `larger values step up through the unit table`() {
        assertEquals("5.0 MB", ByteUnitUtil.formatBytesPrecise(5L * 1024 * 1024, decimals = 1))
        assertEquals("3.0 GB", ByteUnitUtil.formatBytesPrecise(3L * 1024 * 1024 * 1024, decimals = 1))
    }

    @Test
    fun `very large values clamp at the last unit instead of running off the table`() {
        val formatted = ByteUnitUtil.formatBytesPrecise(Long.MAX_VALUE, decimals = 1)

        assertTrue(formatted.endsWith(" PB"), "expected the largest unit, got: $formatted")
    }
}
