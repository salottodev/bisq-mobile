package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.qr

sealed interface SellerQrUiAction {
    data object OnClose : SellerQrUiAction

    data object OnOpenInWallet : SellerQrUiAction
}
