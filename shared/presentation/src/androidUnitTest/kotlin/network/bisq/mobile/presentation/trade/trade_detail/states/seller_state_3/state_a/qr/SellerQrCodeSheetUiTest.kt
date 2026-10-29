package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

import android.view.Window
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.molecules.dialog.clipboardPrimaryText
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import network.bisq.mobile.test.presentation.compose.CaptureHostWindow
import network.bisq.mobile.test.presentation.compose.isSecure
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SellerQrCodeSheetUiTest : BisqComposeUiTestBase() {
    private val bip21 = "bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025"
    private val invoice = "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4js"
    private val onChain = SellerQrUiState(shortTradeId = "abc123", isLightning = false, payload = bip21, baseAmountDisplay = "0.00250000")
    private val lightning = SellerQrUiState(shortTradeId = "abc123", isLightning = true, payload = invoice, baseAmountDisplay = "0.00250000")

    @Test
    fun `on-chain content shows the amount hint and the bip21 payload`() {
        setTestContent { SellerQrCodeContent(uiState = onChain, onAction = {}) }

        composeTestRule
            .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.amountIncluded".i18n("0.00250000"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("seller_qr_payload_text").assertTextEquals(bip21)
        composeTestRule.onNodeWithTag("seller_qr_canvas").assertIsDisplayed()
    }

    @Test
    fun `copy copies exactly the encoded payload`() {
        setTestContent { SellerQrCodeContent(uiState = onChain, onAction = {}) }

        composeTestRule.onNodeWithContentDescription("Copy icon").performClick()

        assertEquals(bip21, clipboardPrimaryText())
    }

    @Test
    fun `lightning content shows the invoice hint and no amount`() {
        setTestContent { SellerQrCodeContent(uiState = lightning, onAction = {}) }

        composeTestRule
            .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.invoiceIncludesAmount".i18n())
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("seller_qr_payload_text").assertTextEquals(invoice)
    }

    @Test
    fun `lightning content without an invoice amount tells the seller the amount to send`() {
        val noAmount = "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w"
        setTestContent { SellerQrCodeContent(uiState = lightning.copy(payload = noAmount), onAction = {}) }

        composeTestRule
            .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.invoiceWithoutAmount".i18n("0.00250000"))
            .assertIsDisplayed()
    }

    @Test
    fun `qr is capped at 260dp on wide layouts`() {
        setTestContent {
            Box(Modifier.width(400.dp)) { SellerQrCodeContent(uiState = onChain, onAction = {}) }
        }

        composeTestRule.onNodeWithTag("seller_qr_canvas").assertWidthIsEqualTo(260.dp)
    }

    @Test
    fun `open in wallet dispatches the action`() {
        val actions = mutableListOf<SellerQrUiAction>()
        setTestContent { SellerQrCodeContent(uiState = onChain, onAction = { actions += it }) }

        composeTestRule
            .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.openInWallet".i18n())
            .performClick()

        assertEquals(listOf<SellerQrUiAction>(SellerQrUiAction.OnOpenInWallet), actions)
    }

    @Test
    fun `tapping the copy label copies the payload`() {
        setTestContent { SellerQrCodeContent(uiState = onChain, onAction = {}) }

        composeTestRule.onNodeWithText("action.copyToClipboard".i18n()).performClick()
        composeTestRule.waitForIdle()

        assertEquals(bip21, clipboardPrimaryText())
    }

    @Test
    fun `open in wallet sits above the payload so it is visible without scrolling`() {
        setTestContent { SellerQrCodeContent(uiState = lightning, onAction = {}) }

        val button =
            composeTestRule
                .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.openInWallet".i18n())
                .getUnclippedBoundsInRoot()
        val payload = composeTestRule.onNodeWithTag("seller_qr_payload_text").getUnclippedBoundsInRoot()

        assertTrue(button.bottom <= payload.top, "button bottom ${button.bottom} is below payload top ${payload.top}")
    }

    @Test
    fun `empty payload renders no qr but keeps the text`() {
        setTestContent { SellerQrCodeContent(uiState = onChain.copy(payload = ""), onAction = {}) }

        composeTestRule.onNodeWithTag("seller_qr_canvas").assertDoesNotExist()
        composeTestRule.onNodeWithTag("seller_qr_payload_text").assertExists()
    }

    @Test
    fun `payment data too long for a qr explains it and keeps copy and wallet`() {
        val tooLong = "lnbc1" + "qpzry9x8gf2tvdw0s3jn54khce6mua7l".repeat(140)
        val actions = mutableListOf<SellerQrUiAction>()
        setTestContent { SellerQrCodeContent(uiState = lightning.copy(payload = tooLong), onAction = { actions += it }) }

        composeTestRule.onNodeWithTag("seller_qr_canvas").assertDoesNotExist()
        composeTestRule.onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.unavailable".i18n()).assertExists()
        composeTestRule
            .onNodeWithText("bisqEasy.tradeState.info.seller.phase3a.qrCodeDisplay.window.title".i18n("abc123"))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.paymentDataTitle".i18n("abc123")).assertExists()

        composeTestRule.onNodeWithText("action.copyToClipboard".i18n()).performClick()
        composeTestRule.waitForIdle()
        assertEquals(tooLong, clipboardPrimaryText())
        composeTestRule
            .onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.openInWallet".i18n())
            .performClick()
        assertEquals(listOf<SellerQrUiAction>(SellerQrUiAction.OnOpenInWallet), actions)
    }

    @Test
    fun `a lightning invoice longer than the lower case qr capacity still gets a qr`() {
        val longInvoice = "lnbc1" + "qpzry9x8gf2tvdw0s3jn54khce6mua7l".repeat(110)
        setTestContent { SellerQrCodeContent(uiState = lightning.copy(payload = longInvoice), onAction = {}) }

        composeTestRule.onNodeWithTag("seller_qr_canvas").assertExists()
        composeTestRule.onNodeWithText("mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.unavailable".i18n()).assertDoesNotExist()
        composeTestRule.onNodeWithTag("seller_qr_payload_text").assertTextEquals(longInvoice)
    }

    @Test
    fun `sheet blocks screenshots`() {
        lateinit var window: Window
        setTestContent {
            CaptureHostWindow { window = it }
            SellerQrCodeSheet(uiState = onChain, onAction = {})
        }

        assertTrue(window.isSecure)
    }
}
