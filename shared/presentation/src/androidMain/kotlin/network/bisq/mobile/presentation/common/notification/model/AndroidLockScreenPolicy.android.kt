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
 * Only `NotificationControllerImpl` builds one today. `BisqFirebaseMessagingService` does not consult
 * a policy at all — it posts "Bisq" plus a bare category string and never names the peer, so there is
 * nothing there to redact, and it leaves `visibility` at the platform default. The two paths
 * therefore agree on what they reveal while disagreeing on how: the relayed one leans on Android's
 * generic placeholder, this one supplies its own copy. Routing FCM through here would make the
 * lock screen read the same either way; it is a consistency fix, not a leak.
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
