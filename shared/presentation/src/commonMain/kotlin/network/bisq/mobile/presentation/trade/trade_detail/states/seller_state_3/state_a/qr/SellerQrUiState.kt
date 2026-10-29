package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

/**
 * @property payload exactly what the QR encodes: BIP21 with amount on-chain, the raw invoice for Lightning.
 * @property baseAmountDisplay localized amount for the hint line only; never used to build the payload.
 */
data class SellerQrUiState(
    val shortTradeId: String = "",
    val isLightning: Boolean = false,
    val payload: String = "",
    val baseAmountDisplay: String = "",
)
