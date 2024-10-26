package network.bisq.mobile

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(presenter: AppPresenter) = ComposeUIViewController { App(presenter) }