package network.bisq.mobile.presentation.community.public_chat

import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum

sealed interface PublicChatUiAction {
    /** Also the save while [PublicChatUiState.editingMessageId] is set: the composer has one button. */
    data class OnSendMessage(
        val text: String,
    ) : PublicChatUiAction

    data class OnAddReaction(
        val message: CommonPublicChatMessage,
        val reaction: ReactionEnum,
    ) : PublicChatUiAction

    data class OnRemoveReaction(
        val message: CommonPublicChatMessage,
        val reaction: CommonPublicChatMessageReaction,
    ) : PublicChatUiAction

    /** Null clears the quote. */
    data class OnReply(
        val message: CommonPublicChatMessage?,
    ) : PublicChatUiAction

    data class OnEditMessage(
        val message: CommonPublicChatMessage,
    ) : PublicChatUiAction

    data object OnCancelEdit : PublicChatUiAction

    data class OnDeleteMessageClick(
        val message: CommonPublicChatMessage,
    ) : PublicChatUiAction

    data object OnConfirmDelete : PublicChatUiAction

    data object OnDismissDeleteDialog : PublicChatUiAction

    data class OnSearchQueryChange(
        val query: String,
    ) : PublicChatUiAction

    /** Per message, not per channel: a public channel has no fixed peer. */
    data class OnPeerProfileClick(
        val profileId: String,
    ) : PublicChatUiAction

    data class OnIgnoreUserClick(
        val profileId: String,
    ) : PublicChatUiAction

    data object OnConfirmIgnore : PublicChatUiAction

    data object OnDismissIgnoreDialog : PublicChatUiAction

    data class OnUndoIgnoreUserClick(
        val profileId: String,
    ) : PublicChatUiAction

    data object OnConfirmUndoIgnore : PublicChatUiAction

    data object OnDismissUndoIgnoreDialog : PublicChatUiAction

    data class OnReportUserClick(
        val message: CommonPublicChatMessage,
    ) : PublicChatUiAction

    data object OnDismissReportDialog : PublicChatUiAction

    /**
     * @param reportMessage what the user had typed, kept so the dialog can be reopened with it.
     *   `ReportUserPresenter` has already surfaced the error itself.
     */
    data class OnReportFailure(
        val reportMessage: String,
    ) : PublicChatUiAction

    data object OnOpenChatRules : PublicChatUiAction

    data object OnDontShowAgainChatRulesWarningBox : PublicChatUiAction

    /** Raised by the message list as the user scrolls; marks the channel read. */
    data class OnUpdateReadCount(
        val count: Int,
    ) : PublicChatUiAction
}
