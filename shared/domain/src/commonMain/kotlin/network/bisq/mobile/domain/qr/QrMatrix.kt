package network.bisq.mobile.domain.qr

/** QR module matrix without quiet zone; `true` = dark. Row-major, index = y * size + x. */
class QrMatrix(
    val size: Int,
    val modules: BooleanArray,
) {
    init {
        require(modules.size == size * size) { "modules must hold size*size entries" }
    }

    operator fun get(
        x: Int,
        y: Int,
    ): Boolean = modules[y * size + x]
}
