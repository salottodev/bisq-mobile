package network.bisq.mobile.domain.qr

/**
 * Encodes [data] at error-correction level L without quiet zone (the renderer adds >= 4 modules).
 * Returns null for empty input, data over QR capacity or a platform failure. Synchronous, CPU-bound (ms).
 */
expect fun encodeQrCode(data: String): QrMatrix?

private const val MIN_QR_SIZE = 21
private const val QR_VERSION_STEP = 4

/**
 * Trims an 8-bit gray buffer rendered at 1 px/module to the bounding box of its dark pixels.
 * Null when nothing is dark, the box is not square or its side is not a valid QR size (21 + 4k).
 */
internal fun qrMatrixFromGrayPixels(
    pixels: ByteArray,
    width: Int,
    height: Int,
    darkThreshold: Int = 128,
): QrMatrix? {
    if (pixels.size < width * height) return null

    fun isDark(
        x: Int,
        y: Int,
    ) = (pixels[y * width + x].toInt() and 0xFF) < darkThreshold

    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (!isDark(x, y)) continue
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
    }
    if (maxX < 0) return null
    val size = maxX - minX + 1
    if (size != maxY - minY + 1) return null
    if (size < MIN_QR_SIZE || (size - MIN_QR_SIZE) % QR_VERSION_STEP != 0) return null

    val modules = BooleanArray(size * size) { i -> isDark(minX + i % size, minY + i / size) }
    return QrMatrix(size, modules)
}
