package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import network.bisq.mobile.domain.qr.QrMatrix
import network.bisq.mobile.domain.qr.encodeQrCode
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage

/** Quiet zone drawn on every side, in modules; the spec minimum for scanners. */
private const val QR_QUIET_ZONE_MODULES = 4

/** Draws [matrix] black on white regardless of theme, with its own quiet zone, centered in the largest square that fits. */
@Composable
fun QrCodeCanvas(
    matrix: QrMatrix,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .background(Color.White)
                .testTag("seller_qr_canvas"),
    ) {
        val bounds = qrSquareBounds(widthPx = size.width.toInt(), heightPx = size.height.toInt())
        val edges = qrCellEdges(canvasPx = bounds.width, cellCount = matrix.size + 2 * QR_QUIET_ZONE_MODULES)
        for (y in 0 until matrix.size) {
            for (x in 0 until matrix.size) {
                if (!matrix[x, y]) continue
                val left = edges[x + QR_QUIET_ZONE_MODULES]
                val top = edges[y + QR_QUIET_ZONE_MODULES]
                val right = edges[x + QR_QUIET_ZONE_MODULES + 1]
                val bottom = edges[y + QR_QUIET_ZONE_MODULES + 1]
                drawRect(
                    color = Color.Black,
                    topLeft = Offset((bounds.left + left).toFloat(), (bounds.top + top).toFloat()),
                    size = Size((right - left).toFloat(), (bottom - top).toFloat()),
                )
            }
        }
    }
}

/** Largest square centered in [widthPx] x [heightPx], so the QR is never stretched or cut off. */
internal fun qrSquareBounds(
    widthPx: Int,
    heightPx: Int,
): IntRect {
    val side = minOf(widthPx, heightPx)
    val left = (widthPx - side) / 2
    val top = (heightPx - side) / 2
    return IntRect(left = left, top = top, right = left + side, bottom = top + side)
}

/**
 * Whole-pixel boundaries of [cellCount] cells over [canvasPx]: fractional cell sizes would leave antialiased
 * hairlines between adjacent dark modules, which some scanners read as light pixels.
 */
internal fun qrCellEdges(
    canvasPx: Int,
    cellCount: Int,
): IntArray = IntArray(cellCount + 1) { i -> i * canvasPx / cellCount }

@ExcludeFromCoverage
@Preview
@Composable
private fun QrCodeCanvasPreview() {
    BisqTheme.Preview {
        QrCodeCanvas(
            matrix = encodeQrCode("bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025")!!,
            modifier = Modifier.size(240.dp),
        )
    }
}
