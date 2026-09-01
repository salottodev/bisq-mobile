package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.common.createMockCommonPublicChatMessage
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Edit and Delete are public-chat-only: bisq2 has no edit or delete endpoint for a trade or private
 * message, so rendering them there would be an affordance that cannot work. The callbacks are
 * nullable rather than gated on a boolean precisely so that the trade and private call sites, which
 * pass nothing, cannot get them.
 */
class ChatMessageContextMenuUiTest : BisqComposeUiTestBase() {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `without the callbacks neither edit nor delete renders on an own message`() {
        setTestContent { Menu(message = ownMessage()) }

        composeTestRule.onNodeWithText("action.edit".i18n()).assertDoesNotExist()
        composeTestRule.onNodeWithText("action.delete".i18n()).assertDoesNotExist()
    }

    @Test
    fun `with the callbacks both render on an own message`() {
        setTestContent { Menu(message = ownMessage(), onEditMessage = {}, onDeleteMessage = {}) }

        composeTestRule.onNodeWithText("action.edit".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("action.delete".i18n()).assertIsDisplayed()
    }

    @Test
    fun `neither renders on a peer's message even with the callbacks`() {
        setTestContent { Menu(message = peerMessage(), onEditMessage = {}, onDeleteMessage = {}) }

        composeTestRule.onNodeWithText("action.edit".i18n()).assertDoesNotExist()
        composeTestRule.onNodeWithText("action.delete".i18n()).assertDoesNotExist()
    }

    /** Reply is already gated on a peer's message, so an own message never shows both. */
    @Test
    fun `an own message offers edit instead of reply`() {
        setTestContent { Menu(message = ownMessage(), onEditMessage = {}, onDeleteMessage = {}) }

        composeTestRule.onNodeWithText("chat.message.reply".i18n()).assertDoesNotExist()
        composeTestRule.onNodeWithText("action.edit".i18n()).assertIsDisplayed()
    }

    @Test
    fun `tapping edit fires the callback and closes the menu`() {
        var edited = false
        val menuStates = mutableListOf<Boolean>()
        setTestContent {
            Menu(
                message = ownMessage(),
                onSetShowMenu = { menuStates += it },
                onEditMessage = { edited = true },
                onDeleteMessage = {},
            )
        }

        composeTestRule.onNodeWithText("action.edit".i18n()).performClick()

        assertTrue(edited)
        assertEquals(listOf(false), menuStates)
    }

    @Test
    fun `tapping delete fires the callback and closes the menu`() {
        var deleted = false
        val menuStates = mutableListOf<Boolean>()
        setTestContent {
            Menu(
                message = ownMessage(),
                onSetShowMenu = { menuStates += it },
                onEditMessage = {},
                onDeleteMessage = { deleted = true },
            )
        }

        composeTestRule.onNodeWithText("action.delete".i18n()).performClick()

        assertTrue(deleted)
        assertEquals(listOf(false), menuStates)
    }

    /** The peer-side half of the menu, which the Edit and Delete items are now interleaved with. */
    @Test
    fun `tapping reply fires the callback and closes the menu`() {
        var replied = false
        val menuStates = mutableListOf<Boolean>()
        setTestContent { Menu(message = peerMessage(), onSetShowMenu = { menuStates += it }, onReply = { replied = true }) }

        composeTestRule.onNodeWithText("chat.message.reply".i18n()).performClick()

        assertTrue(replied)
        assertEquals(listOf(false), menuStates)
    }

    @Test
    fun `tapping report user fires the callback and closes the menu`() {
        var reported = false
        val menuStates = mutableListOf<Boolean>()
        setTestContent { Menu(message = peerMessage(), onSetShowMenu = { menuStates += it }, onReportUser = { reported = true }) }

        composeTestRule.onNodeWithText("chat.message.contextMenu.reportUser".i18n()).performClick()

        assertTrue(reported)
        assertEquals(listOf(false), menuStates)
    }

    @androidx.compose.runtime.Composable
    private fun Menu(
        message: ChatMessage<*>,
        onSetShowMenu: (Boolean) -> Unit = {},
        onReply: () -> Unit = {},
        onReportUser: () -> Unit = {},
        onEditMessage: (() -> Unit)? = null,
        onDeleteMessage: (() -> Unit)? = null,
    ) {
        ChatMessageContextMenu(
            message = message,
            isIgnored = false,
            onSetShowMenu = onSetShowMenu,
            onAddReaction = {},
            showMenu = true,
            onReply = onReply,
            onReportUser = onReportUser,
            onEditMessage = onEditMessage,
            onDeleteMessage = onDeleteMessage,
        )
    }

    private fun ownMessage() = createMockCommonPublicChatMessage(id = "msg-1", text = "Payment sent", senderUserProfile = me, myUserProfile = me)

    private fun peerMessage() = createMockCommonPublicChatMessage(id = "msg-2", text = "Payment received", senderUserProfile = peer, myUserProfile = me)
}
