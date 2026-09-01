package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.common.createMockCommonPublicChatMessage
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertTrue

/**
 * The two things public chat is the first caller to reach in this row.
 *
 * The `(edited)` marker: bisq2 models an edit as a removal plus a new message that keeps the original
 * date, so without the marker an edit silently rewrites history — the message just reads differently
 * in the same place. `wasEdited` is always false on the private branch, which is why nothing in
 * `:shared:presentation` read it until public chat shipped.
 *
 * And the null delivery flow, which only a broadcast message has. Everything the row draws between
 * the date and the username hangs off that flow, so the layout has to hold up with none of it.
 */
class UsernameMessageDeliveryAndDateUiTest : BisqComposeUiTestBase() {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `an edited message is marked as edited`() {
        setTestContent { Row(message(wasEdited = true)) }

        composeTestRule.onNodeWithText("chat.message.wasEdited".i18n()).assertIsDisplayed()
    }

    @Test
    fun `an unedited message is not`() {
        setTestContent { Row(message(wasEdited = false)) }

        composeTestRule.onNodeWithText("chat.message.wasEdited".i18n()).assertDoesNotExist()
    }

    @Test
    fun `the sender name still renders`() {
        setTestContent { Row(message(wasEdited = true)) }

        composeTestRule.onNodeWithText(peer.userName).assertIsDisplayed()
    }

    /**
     * On my own message the row runs date-then-username, and the gap between them used to be drawn by
     * the delivery box's spacer. A public message has no delivery box, so both vanished together and
     * the date sat flush against the name. Unmerged because the row merges its descendants, and the
     * merged node would answer both queries with one set of bounds.
     */
    @Test
    fun `my own message keeps the date clear of the username`() {
        val mine = message(wasEdited = false, sender = me)
        setTestContent { Row(mine) }

        val date = composeTestRule.onNodeWithText(mine.dateString, useUnmergedTree = true).getUnclippedBoundsInRoot()
        val username = composeTestRule.onNodeWithText(me.userName, useUnmergedTree = true).getUnclippedBoundsInRoot()

        assertTrue(username.left > date.right, "date ends at ${date.right}, username starts at ${username.left}")
    }

    @Composable
    private fun Row(message: ChatMessage<*>) {
        UsernameMessageDeliveryAndDate(
            message = message,
            onResendMessage = {},
            userNameProvider = { it },
            messageDeliveryInfoByPeersProfileId = null,
            onPeerProfileClick = {},
        )
    }

    private fun message(
        wasEdited: Boolean,
        sender: UserProfileVO = peer,
    ) = createMockCommonPublicChatMessage(
        id = "msg-1",
        text = "Payment sent",
        senderUserProfile = sender,
        myUserProfile = me,
        wasEdited = wasEdited,
    )
}
