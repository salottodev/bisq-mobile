package network.bisq.mobile.presentation.common.notification.model

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import network.bisq.mobile.data.utils.ResourceUtils
import network.bisq.mobile.presentation.common.notification.model.android.AndroidLockScreenPolicy

// VISIBILITY_SECRET has no policy mapping to it on purpose: nothing produces one today, and a
// variant nobody constructs works against the point of modelling this as a closed set. Add it here
// alongside the producer when something needs a notification hidden outright.
fun AndroidLockScreenPolicy.toNotificationCompat(): Int =
    when (this) {
        is AndroidLockScreenPolicy.ShowContent -> NotificationCompat.VISIBILITY_PUBLIC
        is AndroidLockScreenPolicy.Redact -> NotificationCompat.VISIBILITY_PRIVATE
    }

/**
 * The stand-in the SystemUI draws on a secure lock screen in place of the real notification.
 *
 * Deliberately bare: no content intent, no group, no sound — it is only ever rendered, never
 * interacted with. It must itself be PUBLIC, or there would be nothing left to fall back to.
 *
 * Shared by both delivery paths (`NotificationControllerImpl` and `BisqFirebaseMessagingService`) so
 * the same notification cannot redact differently depending on whether the app happened to be alive.
 */
fun AndroidLockScreenPolicy.Redact.toPublicNotification(
    context: Context,
    channelId: String,
): Notification =
    NotificationCompat
        .Builder(context, channelId)
        .setSmallIcon(ResourceUtils.getNotifResId(context))
        .setContentTitle(title)
        .setContentText(body)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()
