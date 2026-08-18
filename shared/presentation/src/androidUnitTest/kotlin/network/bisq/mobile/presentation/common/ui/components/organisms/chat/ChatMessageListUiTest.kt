package network.bisq.mobile.presentation.common.ui.components.organisms.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
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
        setTestContent { MessageList(emptyList()) }

        composeTestRule.waitForIdle()
    }

    /**
     * A `LEAVE` message goes through the `leaveMessageContent` slot instead of the text bubble. The
     * default is the trade wording, which is what a caller inside a trade wants; a caller outside one
     * has to override it.
     */
    @Test
    fun `a leave message renders the default trade wording, not a text bubble`() {
        setTestContent {
            MessageList(
                listOf(
                    message("msg-2", sender = peer, text = null, type = ChatMessageTypeEnum.LEAVE),
                    message("msg-1", sender = peer, text = "Payment received, thanks!"),
                ),
            )
        }

        composeTestRule.onNodeWithText("bisqEasy.openTrades.chat.peerLeft.subHeadline".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment received, thanks!").assertIsDisplayed()
    }

    @Composable
    private fun MessageList(messages: List<BisqEasyOpenTradeMessage>) {
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
                leaveMessageContent = { message, modifier -> TradePeerLeftMessageBox(message, modifier) },
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
