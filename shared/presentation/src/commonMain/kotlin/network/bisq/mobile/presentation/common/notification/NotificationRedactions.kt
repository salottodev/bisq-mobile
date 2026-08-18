package network.bisq.mobile.presentation.common.notification

import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.notification.model.android.AndroidLockScreenPolicy

/**
 * keep all lock-screen stand-ins here for clarity, so the locally raised and the relayed notification
 * cannot drift into showing different redacted copy for the same thing.
 */
object NotificationRedactions {
    private const val APP_NAME = "Bisq"

    /**
     * For any notification whose copy names another user. Says a message arrived and nothing else —
     * the same summary the relayed path falls back to when the node sends no peer name.
     */
    fun chatMessage() =
        AndroidLockScreenPolicy.Redact(
            title = APP_NAME,
            body = "mobile.pushNotifications.category.chatMessage".i18n(),
        )

    /**
     * For a trade update. Every one of them names the peer — "Your offer was taken by {0}", "Your
     * payment details were shared with {0}" — so a trade update is no less revealing than a chat
     * message and gets the same treatment. Reuses the copy the relayed path titles a
     * `trade_update` push with, so the two cannot drift.
     */
    fun tradeUpdate() =
        AndroidLockScreenPolicy.Redact(
            title = APP_NAME,
            body = "mobile.pushNotifications.category.tradeUpdate".i18n(),
        )

    /**
     * The fallback every notification starts from, so a new producer is private until its author says
     * otherwise. Says only that something arrived, which is also what the relayed path posts for a
     * push it cannot categorise — the two therefore agree without anyone having to remember to make
     * them agree. A producer whose copy is genuinely safe on a lock screen opts out with
     * [AndroidLockScreenPolicy.ShowContent], and that opt-out is one visible line in review.
     */
    fun general() =
        AndroidLockScreenPolicy.Redact(
            title = APP_NAME,
            body = "mobile.pushNotifications.category.general".i18n(),
        )
}
