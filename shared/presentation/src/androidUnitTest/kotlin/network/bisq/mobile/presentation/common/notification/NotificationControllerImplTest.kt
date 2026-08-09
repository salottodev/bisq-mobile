package network.bisq.mobile.presentation.common.notification

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import network.bisq.mobile.data.service.AppForegroundController
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.notification.model.NotificationBuilder
import network.bisq.mobile.presentation.common.notification.model.android.AndroidLockScreenPolicy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * What a posted notification reveals on a secure lock screen.
 *
 * These assert the built `Notification`, not the config that asked for it, because the failure this
 * guards against is the wiring silently going missing: a `Redact` policy that never reaches
 * `setPublicVersion` leaves Android substituting its own placeholder, which looks fine in every
 * config-level test and wrong on the device.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationControllerImplTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val notificationManager get() = context.getSystemService(NotificationManager::class.java)

    private lateinit var controller: NotificationControllerImpl

    @Before
    fun setUp() {
        I18nSupport.initialize("en")
        Shadows.shadowOf(context).grantPermissions(NotificationControllerImpl.POST_NOTIFS_PERM)
        // Compat: a no-op below API 26, where channels do not exist.
        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat
                .Builder(NotificationChannels.USER_MESSAGES, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("User messages")
                .build(),
        )

        val appForegroundController: AppForegroundController = mockk(relaxed = true)
        every { appForegroundController.context } returns context
        every { appForegroundController.isForeground } returns MutableStateFlow(false)

        controller = NotificationControllerImpl(appForegroundController, Application::class.java)
    }

    @Test
    fun `a redacted notification is posted with the summary as its public form`() {
        controller.notify {
            id = "dm-1"
            title = "New message"
            body = "You received a new message from Alice"
            android {
                channelId = NotificationChannels.USER_MESSAGES
                lockScreen = NotificationRedactions.chatMessage()
                pressAction = null
            }
        }

        val posted = postedNotification()
        assertEquals(Notification.VISIBILITY_PRIVATE, posted.visibility)
        assertEquals("You received a new message from Alice", posted.text())

        val public = assertNotNull(posted.publicVersion, "without one, Android shows its own placeholder")
        assertEquals("Bisq", public.title())
        assertEquals("New message", public.text(), "the stand-in must not name the peer")
        assertEquals(Notification.VISIBILITY_PUBLIC, public.visibility, "the stand-in is what gets shown")
    }

    @Test
    fun `a notification shown in full is posted without a public form`() {
        controller.notify {
            id = "trade-1"
            title = "Trade [abc12345]"
            body = "Trade state changed"
            android {
                channelId = NotificationChannels.USER_MESSAGES
                pressAction = null
            }
        }

        val posted = postedNotification()
        // The DSL default, asserted here so a change to it cannot silently start hiding copy.
        assertEquals(
            AndroidLockScreenPolicy.ShowContent,
            NotificationBuilder()
                .apply { android { } }
                .build()
                .android
                ?.lockScreen,
        )
        assertEquals(Notification.VISIBILITY_PUBLIC, posted.visibility)
        assertNull(posted.publicVersion)
    }

    private fun postedNotification(): Notification = Shadows.shadowOf(notificationManager).allNotifications.single()

    private fun Notification.title() = extras.getString(Notification.EXTRA_TITLE)

    private fun Notification.text() = extras.getString(Notification.EXTRA_TEXT)
}
