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
}
