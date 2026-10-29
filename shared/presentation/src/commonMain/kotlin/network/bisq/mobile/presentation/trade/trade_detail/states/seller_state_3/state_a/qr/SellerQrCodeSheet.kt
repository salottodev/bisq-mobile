package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import network.bisq.mobile.domain.qr.encodeQrCode
import network.bisq.mobile.domain.qr.lightningInvoiceHasAmount
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqButton
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqButtonType
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.button.CopyIconButton
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.components.molecules.bottom_sheet.BisqBottomSheet
import network.bisq.mobile.presentation.common.ui.security.SecureScreenEffect
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage

private val QR_MAX_SIZE = 260.dp

/** Secure sheet with the buyer's payout data as a QR. Swipe, scrim and back all dispatch [SellerQrUiAction.OnClose]. */
@Composable
fun SellerQrCodeSheet(
    uiState: SellerQrUiState,
    onAction: (SellerQrUiAction) -> Unit,
) {
    SecureScreenEffect()
    BisqBottomSheet(onDismissRequest = { onAction(SellerQrUiAction.OnClose) }) {
        SellerQrCodeContent(uiState = uiState, onAction = onAction)
    }
}

/** Scrolls as a whole: the sheet caps its height and a Lightning invoice is several lines long. */
@Composable
fun SellerQrCodeContent(
    uiState: SellerQrUiState,
    onAction: (SellerQrUiAction) -> Unit,
) {
    val qrMatrix = remember(uiState.payload) { encodeQrCode(uiState.payload) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BisqUIConstants.ScreenPadding2X)
                .padding(bottom = BisqUIConstants.ScreenPadding2X),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BisqText.H5Light(
            text = "bisqEasy.tradeState.info.seller.phase3a.qrCodeDisplay.window.title".i18n(uiState.shortTradeId),
        )

        if (qrMatrix != null) {
            BisqGap.V1()
            Box(
                modifier =
                    Modifier
                        .widthIn(max = QR_MAX_SIZE)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                        .background(Color.White),
            ) {
                QrCodeCanvas(matrix = qrMatrix, modifier = Modifier.fillMaxSize())
            }
        }

        BisqGap.V1()
        BisqText.SmallLight(
            text = amountHint(uiState),
            color = BisqTheme.colors.mid_grey20,
        )

        // The button goes before the (possibly long) payload so it is reachable without scrolling.
        BisqGap.V2()
        BisqButton(
            text = "mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.openInWallet".i18n(),
            type = BisqButtonType.Outline,
            fullWidth = true,
            onClick = { onAction(SellerQrUiAction.OnOpenInWallet) },
        )

        BisqGap.V2()
        BisqText.StyledText(
            text = uiState.payload,
            style = BisqTheme.typography.smallLight.copy(fontFamily = FontFamily.Monospace),
            color = BisqTheme.colors.light_grey10,
            modifier = Modifier.testTag("seller_qr_payload_text"),
        )

        BisqGap.VHalf()
        CopyIconButton(value = uiState.payload, showToast = false, label = "action.copyToClipboard".i18n())
    }
}

/** An amount-less invoice makes the wallet ask for the amount, so the seller must be told which one. */
private fun amountHint(uiState: SellerQrUiState): String =
    when {
        !uiState.isLightning -> "mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.amountIncluded".i18n(uiState.baseAmountDisplay)
        lightningInvoiceHasAmount(uiState.payload) -> "mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.invoiceIncludesAmount".i18n()
        else -> "mobile.bisqEasy.tradeState.info.seller.phase3a.qrCode.invoiceWithoutAmount".i18n(uiState.baseAmountDisplay)
    }

@ExcludeFromCoverage
@Preview(heightDp = 800)
@Composable
private fun SellerQrCodeContent_OnChainPreview() {
    BisqTheme.Preview {
        SellerQrCodeContent(
            uiState =
                SellerQrUiState(
                    shortTradeId = "a1b2c3",
                    payload = "bitcoin:bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4?amount=0.0025",
                    baseAmountDisplay = "0.00250000",
                ),
            onAction = {},
        )
    }
}

@ExcludeFromCoverage
@Preview(heightDp = 900)
@Composable
private fun SellerQrCodeContent_LightningPreview() {
    BisqTheme.Preview {
        SellerQrCodeContent(
            uiState =
                SellerQrUiState(
                    shortTradeId = "a1b2c3",
                    isLightning = true,
                    payload =
                        "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4jsxqzpu" +
                            "aztrnwngzn3kdzw5hydlzf03qdgm2hdq27cqv3agm2awhz5se903vruatfhq77w3ls4evs3ch9zw97j25emudupq63nyw24cg27h2rspfj9srp",
                    baseAmountDisplay = "0.00250000",
                ),
            onAction = {},
        )
    }
}
