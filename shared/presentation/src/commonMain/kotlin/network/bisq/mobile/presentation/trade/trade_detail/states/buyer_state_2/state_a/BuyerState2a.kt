@file:Suppress("ktlint:compose:vm-forwarding-check")

package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_2.state_a

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqButton
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqTextFieldV0
import network.bisq.mobile.presentation.common.ui.components.atoms.button.CopyIconButton
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.security.SecureScreenEffect
import network.bisq.mobile.presentation.common.ui.utils.RememberPresenterLifecycle

@Composable
fun BuyerState2a(
    presenter: BuyerState2aPresenter,
) {
    RememberPresenterLifecycle(presenter)
    SecureScreenEffect()

    val uiState by presenter.uiState.collectAsState()
    BuyerState2aContent(uiState = uiState, onAction = presenter::onAction)
}

@Composable
private fun BuyerState2aContent(
    uiState: BuyerState2aUiState,
    onAction: (BuyerState2aUiAction) -> Unit,
) {
    if (!uiState.isTradeLoaded) return

    val quoteAmount = uiState.quoteAmountWithCode
    val paymentAccountData = uiState.paymentAccountData ?: "data.na".i18n()

    Column(horizontalAlignment = Alignment.Start) {
        BisqGap.V1()
        // Send {0} to the seller''s payment account
        BisqText.H5Light("bisqEasy.tradeState.info.buyer.phase2a.headline".i18n(quoteAmount))

        BisqGap.VHalf()
        BisqTextFieldV0(
            // Amount to transfer
            label = "bisqEasy.tradeState.info.buyer.phase2a.quoteAmount".i18n(),
            value = quoteAmount,
            enabled = false,
            trailingIcon = { CopyIconButton(value = quoteAmount) },
        )

        BisqGap.VHalf()
        BisqTextFieldV0(
            // Payment account of seller
            label = "bisqEasy.tradeState.info.buyer.phase2a.sellersAccount".i18n(),
            // In Bisq Easy we show the Reason for payment with the trade ID as extra field, but on mobile we don't want to
            // use up too much space for that and show it as helper text instead.
            // Use the trade ID {0} for the 'Reason for payment' field
            bottomMessage = "mobile.tradeState.info.buyer.phase2a.reasonForPaymentInfo".i18n(uiState.tradeShortId),
            value = paymentAccountData,
            enabled = false,
            trailingIcon = { CopyIconButton(value = paymentAccountData) },
            maxLines = Int.MAX_VALUE,
            minLines = 2,
        )

        BisqGap.V1()
        BisqButton(
            // Confirm payment of {0}
            text = "bisqEasy.tradeState.info.buyer.phase2a.confirmFiatSent".i18n(quoteAmount),
            onClick = { onAction(BuyerState2aUiAction.OnConfirmFiatSent) },
            disabled = !uiState.isConfirmFiatSentEnabled,
        )
    }
}
