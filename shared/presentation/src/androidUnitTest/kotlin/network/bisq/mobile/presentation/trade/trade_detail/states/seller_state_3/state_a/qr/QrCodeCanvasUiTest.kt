package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import network.bisq.mobile.domain.qr.encodeQrCode
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QrCodeCanvasUiTest : BisqComposeUiTestBase() {
    @Test
    fun `renders a canvas for a real matrix`() {
        val matrix = encodeQrCode("bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025")!!

        setTestContent {
            QrCodeCanvas(matrix, Modifier.size(200.dp))
        }

        composeTestRule.onNodeWithTag("seller_qr_canvas").assertIsDisplayed()
    }

    @Test
    fun `module edges are whole pixels that tile the canvas without gaps`() {
        // 25 modules + 2 * 4 quiet zone = 33 cells over 500 px: 500 / 33 is not an integer.
        val edges = qrCellEdges(canvasPx = 500, cellCount = 33)

        assertEquals(34, edges.size)
        assertEquals(0, edges.first())
        assertEquals(500, edges.last())
        edges.toList().zipWithNext().forEach { (a, b) -> assertTrue(b - a in 15..16, "cell width ${b - a}") }
    }

    @Test
    fun `qr is drawn in the largest centered square of a wide box`() {
        assertEquals(IntRect(left = 50, top = 0, right = 250, bottom = 200), qrSquareBounds(widthPx = 300, heightPx = 200))
    }

    @Test
    fun `qr is drawn in the largest centered square of a tall box`() {
        assertEquals(IntRect(left = 0, top = 50, right = 200, bottom = 250), qrSquareBounds(widthPx = 200, heightPx = 300))
    }
}
