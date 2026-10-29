package network.bisq.mobile.domain.qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.Decoder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Round-trips the Android [encodeQrCode] actual through zxing's own readers. */
class QrCodeEncoderAndroidTest {
    private val bip21 = "bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025"
    private val invoice =
        "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4jsxqzpuaztrnwngzn3kdzw5hydlzf03qdgm2hdq27cqv3agm2awhz5se903vruatfhq77w3ls4evs3ch9zw97j25emudupq63nyw24cg27h2rspfj9srp" +
            "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4jsxqzpuaztrnwngzn3kdzw5hydlzf03qdgm2hdq27cqv3agm2awhz5se903vruatfhq77w3ls4evs3ch9zw97j25emudupq63nyw24cg27h2rspfj9srp"

    @Test
    fun `short payload is a version 1 symbol`() {
        assertEquals(21, encodeQrCode("Hello")!!.size)
    }

    @Test
    fun `matrix has no quiet zone`() {
        val m = encodeQrCode("Hello")!!
        val last = m.size - 1
        for (i in 0 until 7) {
            assertTrue(m[i, 0] && m[0, i] && m[last - i, 0] && m[last, i] && m[i, last] && m[0, last - i], "finder edge at $i")
        }
    }

    @Test
    fun `round trips through zxing module decoder`() {
        val m = encodeQrCode(bip21)!!
        assertEquals(bip21, Decoder().decode(m.toBitMatrix()).text)
    }

    @Test
    fun `round trips through scanner-style reader with renderer quiet zone`() {
        assertEquals(bip21, scan(encodeQrCode(bip21)!!))
        assertTrue(invoice.length > 300)
        assertEquals(invoice, scan(encodeQrCode(invoice)!!))
    }

    @Test
    fun `non latin1 payload survives`() {
        assertEquals("ñ€", Decoder().decode(encodeQrCode("ñ€")!!.toBitMatrix()).text)
    }

    @Test
    fun `empty and oversize input return null`() {
        assertNull(encodeQrCode(""))
        assertNull(encodeQrCode("a".repeat(3000)))
    }

    @Test
    fun `upper case alphanumeric text fits where lower case does not`() {
        val lower = "lnbc1" + "qpzry9x8gf2tvdw0s3jn54khce6mua7l".repeat(110)

        assertNull(encodeQrCode(lower))
        assertNotNull(encodeQrCode(lower.uppercase()))
    }

    @Test
    fun `gray pixel trimming strips margin`() {
        val source = encodeQrCode("Hello")!!
        val margin = 3
        val w = source.size + 2 * margin
        val pixels = ByteArray(w * w) { 0xFF.toByte() }
        for (y in 0 until source.size) {
            for (x in 0 until source.size) {
                if (source[x, y]) pixels[(y + margin) * w + (x + margin)] = 0
            }
        }
        val trimmed = qrMatrixFromGrayPixels(pixels, w, w)
        assertNotNull(trimmed)
        assertEquals(source.size, trimmed.size)
        assertContentEquals(source.modules, trimmed.modules)
    }

    private fun scan(m: QrMatrix): String =
        QRCodeReader()
            .decode(BinaryBitmap(HybridBinarizer(m.toLuminance())), mapOf(DecodeHintType.PURE_BARCODE to true))
            .text

    private fun QrMatrix.toBitMatrix(): BitMatrix {
        val bits = BitMatrix(size, size)
        for (y in 0 until size) for (x in 0 until size) if (this[x, y]) bits.set(x, y)
        return bits
    }

    /** Pixels the way the renderer paints them: [scale] px per module, [quietModules] white modules around. */
    private fun QrMatrix.toLuminance(
        scale: Int = 4,
        quietModules: Int = 4,
    ): RGBLuminanceSource {
        val side = (size + 2 * quietModules) * scale
        val pixels = IntArray(side * side) { 0xFFFFFFFF.toInt() }
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (!this[x, y]) continue
                for (dy in 0 until scale) {
                    for (dx in 0 until scale) {
                        pixels[((y + quietModules) * scale + dy) * side + (x + quietModules) * scale + dx] = 0xFF000000.toInt()
                    }
                }
            }
        }
        return RGBLuminanceSource(side, side, pixels)
    }
}
