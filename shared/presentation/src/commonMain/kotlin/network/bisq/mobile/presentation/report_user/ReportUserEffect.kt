package network.bisq.mobile.presentation.report_user

sealed class ReportUserEffect {
    object ReportSuccess : ReportUserEffect()

    /**
     * @param reportMessage what the user had typed. The error itself is already on screen —
     *   [ReportUserPresenter] shows the snackbar — so all the host has to do is keep the draft for a
     *   second attempt.
     */
    data class ReportError(
        val reportMessage: String,
    ) : ReportUserEffect()
}
