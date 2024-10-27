package network.bisq.mobile

import androidx.compose.ui.window.ComposeUIViewController
import network.bisq.mobile.presentation.ui.App
import network.bisq.mobile.presentation.ui.AppPresenter

fun MainViewController(presenter: AppPresenter) = ComposeUIViewController { App(presenter) }