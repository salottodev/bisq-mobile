package network.bisq.mobile.presentation.common.notification.model.ios

import network.bisq.mobile.presentation.common.notification.model.NotificationButton

data class IosNotificationCategory(
    val id: String,
    /**
     * Only for defining the button and the type of the action
     *
     * action details (like which route to navigate to) will not be used here and
     * needs to be handled on iosClient using userInfo
     */
    val actions: List<NotificationButton> = emptyList(),
    /**
     * What iOS shows in place of the body while previews are hidden (Show Previews = When Unlocked,
     * the default on Face ID devices). Without it iOS shows a bare "Notification".
     */
    val hiddenPreviewsBodyPlaceholder: String? = null,
)
