package network.bisq.mobile.domain.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

actual fun encodeQrCode(data: String): QrMatrix? {
    val hints =
        buildMap<EncodeHintType, Any> {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
            put(EncodeHintType.MARGIN, 0)
            // zxing defaults to ISO-8859-1; only switch when the payload needs more
            if (data.any { it.code > 0xFF }) put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }
    val bits =
        try {
            // Width/height 0 + MARGIN 0 yields the bare module matrix
            QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (e: WriterException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
    val size = bits.width
    if (size == 0 || bits.height != size) return null
    return QrMatrix(size, BooleanArray(size * size) { i -> bits.get(i % size, i / size) })
}
