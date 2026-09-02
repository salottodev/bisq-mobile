package network.bisq.mobile.presentation.community.discussions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import network.bisq.mobile.presentation.common.ui.utils.RememberPresenterLifecycle
import network.bisq.mobile.presentation.community.public_chat.PublicChatPresenter
import network.bisq.mobile.presentation.community.public_chat.PublicChatThreadContent
import network.bisq.mobile.presentation.community.public_chat.PublicChatUiAction
import network.bisq.mobile.presentation.report_user.ReportUserDialog
import org.koin.compose.koinInject

/**
 * The Discussions segment's body inside the Community hub shell — #1744's glue between the facade and
 * the hub. Owns its presenter, like `ContactsTabContent`, so the hub screen stays presenter-agnostic
 * about segment content and its previews keep rendering without Koin.
 *
 * No top bar: the hub already has one, and `PublicChatThreadContent` is a layout rather than a
 * scaffold. #1746's pushed Support screen supplies the chrome from its own side.
 */
@ExcludeFromCoverage
@Composable
fun DiscussionsTabContent() {
    val presenter: PublicChatPresenter = koinInject()
    RememberPresenterLifecycle(presenter)

    LaunchedEffect(presenter) {
        presenter.initialize(ChatChannelDomainEnum.DISCUSSION)
    }

    val uiState by presenter.uiState.collectAsState()
    val isSendChatMessageEnabled by presenter.isSendChatMessageEnabled.collectAsState()
    val isIgnoreActionEnabled by presenter.isIgnoreActionEnabled.collectAsState()

    PublicChatThreadContent(
        uiState = uiState,
        onAction = presenter::onAction,
        userProfileIconProvider = presenter.userProfileIconProvider,
        userNameProvider = { profileId -> presenter.getUserName(profileId) },
        isSendChatMessageEnabled = isSendChatMessageEnabled,
        isIgnoreActionEnabled = isIgnoreActionEnabled,
        // A slot rather than rendered inside the content, since it injects its own presenter — the
        // same reason `PrivateChatScreen` keeps it out.
        reportDialog = {
            val target = uiState.reportTargetUserProfile
            if (target != null) {
                ReportUserDialog(
                    accusedUserProfile = target,
                    reportMessage = uiState.reportDraft,
                    onReportFailure = { reportMessage ->
                        presenter.onAction(PublicChatUiAction.OnReportFailure(reportMessage))
                    },
                    onReportSuccess = { presenter.onAction(PublicChatUiAction.OnDismissReportDialog) },
                )
            }
        },
    )
}
