package network.bisq.mobile.presentation.common.notification.model.android

/**
 * What the SystemUI is allowed to reveal about a notification in untrusted situations — namely a
 * secure lock screen and screen sharing.
 *
 * Android models this as two independent knobs, `Notification.visibility` and
 * `Notification.publicVersion`, which leaves two meaningless states representable: PRIVATE without a
 * redacted form (Android substitutes its own generic placeholder, so the copy we would have chosen is
 * never shown), and a publicVersion attached to a PUBLIC notification (dead weight that never
 * renders). Collapsing both into one choice makes those unrepresentable. There is deliberately no
 * "hide entirely" case: `VISIBILITY_SECRET` has no caller, and a variant nobody constructs works
 * against the point of a closed set.
 *
 * Scope of the protection, so nobody reads more into it than it gives: this only governs SystemUI
 * rendering, and only on a device with a secure lock whose owner has chosen to hide sensitive content
 * — on the common "show all content" setting nothing here changes what is displayed. An app holding
 * notification-listener access reads the real title and text regardless. It is a defence against
 * shoulder-surfing, not against on-device exfiltration.
 */
sealed interface AndroidLockScreenPolicy {
    /** The real content is shown as-is. */
    data object ShowContent : AndroidLockScreenPolicy

    /** [title] and [body] are shown in place of the real content. */
    data class Redact(
        val title: String,
        val body: String,
    ) : AndroidLockScreenPolicy
}
