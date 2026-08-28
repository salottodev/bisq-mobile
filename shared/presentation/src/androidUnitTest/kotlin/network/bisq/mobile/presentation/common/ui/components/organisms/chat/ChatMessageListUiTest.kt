package network.bisq.mobile.presentation.common.ui.components.organisms.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.common.createMockCommonPublicChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.ChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.utils.createEmptyImage
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.molecules.chat.trade.TradePeerLeftMessageBox
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test

/**
 * Renders the list through its required parameters only, which is how `TradeChatScreen` and the
 * private chat screen both call it — everything else is a defaulted no-op lambda.
 *
 * The inner `LazyColumn` fills its parent, so the list needs a bounded height or it collapses and
 * nothing renders. `reverseLayout = true`, so index 0 is the newest message at the bottom.
 */
class ChatMessageListUiTest : BisqComposeUiTestBase() {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `renders the conversation with defaulted callbacks`() {
        setTestContent {
            MessageList(
                listOf(
                    message("msg-2", sender = me, text = "Just sent it over."),
                    message("msg-1", sender = peer, text = "Payment received, thanks!"),
                ),
            )
        }

        composeTestRule.onNodeWithText("Just sent it over.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment received, thanks!").assertIsDisplayed()
    }

    @Test
    fun `renders an empty conversation without crashing`() {
        setTestContent { MessageList(emptyList<BisqEasyOpenTradeMessage>()) }

        composeTestRule.waitForIdle()
    }

    /**
     * A `LEAVE` message goes through the `leaveMessageContent` slot instead of the text bubble. The
     * slot has no default: a caller inside a trade passes the trade wording, one outside it its own.
     */
    @Test
    fun `a leave message renders the default trade wording, not a text bubble`() {
        setTestContent {
            MessageList(
                listOf(
                    message("msg-2", sender = peer, text = null, type = ChatMessageTypeEnum.LEAVE),
                    message("msg-1", sender = peer, text = "Payment received, thanks!"),
                ),
                leaveMessageContent = { message, modifier -> TradePeerLeftMessageBox(message, modifier) },
            )
        }

        composeTestRule.onNodeWithText("bisqEasy.openTrades.chat.peerLeft.subHeadline".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment received, thanks!").assertIsDisplayed()
    }

    /**
     * A public channel message has no delivery status and no peer, and the list must not need
     * either: it is generic over the shared [ChatMessage] base, not the private branch.
     */
    @Test
    fun `renders a public channel conversation`() {
        setTestContent {
            MessageList(
                listOf(
                    createMockCommonPublicChatMessage(id = "msg-2", text = "Welcome to Bisq", senderUserProfile = me, myUserProfile = me),
                    createMockCommonPublicChatMessage(id = "msg-1", text = "Hi all", senderUserProfile = peer, myUserProfile = me),
                ),
            )
        }

        composeTestRule.onNodeWithText("Welcome to Bisq").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hi all").assertIsDisplayed()
    }

    @Composable
    private fun <M : ChatMessage<R>, R : ChatMessageReaction> MessageList(
        messages: List<M>,
        leaveMessageContent: @Composable (M, Modifier) -> Unit = { _, _ -> },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatMessageList(
                messages = messages,
                ignoredUserIds = emptySet(),
                showChatRulesWarnBox = false,
                readCount = messages.size,
                userProfileIconProvider = { { createEmptyImage() } },
                onResendMessage = {},
                userNameProvider = { it },
                onPeerProfileClick = {},
                modifier = Modifier.fillMaxSize(),
                leaveMessageContent = leaveMessageContent,
            )
        }
    }

    private fun message(
        id: String,
        sender: UserProfileVO,
        text: String?,
        type: ChatMessageTypeEnum = ChatMessageTypeEnum.TEXT,
    ) = createMockBisqEasyOpenTradeMessage(
        id = id,
        chatMessageType = type,
        text = text,
        senderUserProfile = sender,
        myUserProfile = me,
    )
}
