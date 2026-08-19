package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.utils.createEmptyImage
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test

/**
 * Reactions always sit below the bubble. They used to move beside it for short messages with few
 * distinct reactions, which made a conversation change shape from one message to the next — so the
 * bubble and the reaction row now render from a single [androidx.compose.foundation.layout.Column]
 * regardless of the message. These tests pin that both halves render for own and peer messages, with
 * and without a citation.
 */
class ChatTextMessageBoxUiTest : BisqComposeUiTestBase() {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `peer message renders sender and text`() {
        setTestContent { MessageBox(createMessage(sender = peer)) }

        composeTestRule.onNodeWithText("Payment sent").assertIsDisplayed()
    }

    @Test
    fun `own message renders the same content on the other side`() {
        setTestContent { MessageBox(createMessage(sender = me)) }

        composeTestRule.onNodeWithText("Payment sent").assertIsDisplayed()
    }

    /**
     * A short message with few distinct reactions is exactly the case that used to move the row
     * beside the bubble. Two reactions of the same kind, so [ReactionDisplay] draws its count badge —
     * asserting the message text alone would still pass with the reaction row gone.
     */
    @Test
    fun `short message with reactions keeps both the bubble and the reaction row`() {
        setTestContent {
            MessageBox(
                createMessage(
                    text = "ok",
                    reactions =
                        listOf(
                            reaction("r-1", peer, ReactionEnum.THUMBS_UP),
                            reaction("r-2", me, ReactionEnum.THUMBS_UP),
                        ),
                ),
            )
        }

        composeTestRule.onNodeWithText("ok").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun `a message with a citation shows the quoted text above its own`() {
        setTestContent {
            MessageBox(
                createMessage(
                    citation = Citation(authorUserProfileId = me.id, text = "When do we settle?", chatMessageId = "msg-0"),
                    citationAuthorUserProfile = me,
                ),
            )
        }

        composeTestRule.onNodeWithText("When do we settle?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment sent").assertIsDisplayed()
    }

    /**
     * Long-pressing the bubble opens the context menu, which is the only way into
     * [ChatReactionInput] — the reaction picker is not on the message row itself.
     */
    @Test
    fun `long press on a peer message opens the context menu`() {
        setTestContent { MessageBox(createMessage(sender = peer)) }

        composeTestRule.onNodeWithText("Payment sent").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("chat.message.reply".i18n()).assertIsDisplayed()
        composeTestRule.onNodeWithText("action.copyToClipboard".i18n()).assertIsDisplayed()
    }

    @Composable
    private fun MessageBox(message: BisqEasyOpenTradeMessage) {
        ChatTextMessageBox(
            message = message,
            isIgnored = false,
            onAddReaction = {},
            onRemoveReaction = {},
            userProfileIconProvider = { createEmptyImage() },
            onResendMessage = {},
            userNameProvider = { it },
            onPeerProfileClick = {},
        )
    }

    private fun createMessage(
        text: String? = "Payment sent",
        sender: UserProfileVO = peer,
        citation: Citation? = null,
        citationAuthorUserProfile: UserProfileVO? = null,
        reactions: List<BisqEasyOpenTradeMessageReaction> = emptyList(),
    ) = createMockBisqEasyOpenTradeMessage(
        id = "msg-1",
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        senderUserProfile = sender,
        myUserProfile = me,
        chatReactions = reactions,
    )

    private fun reaction(
        id: String,
        sender: UserProfileVO,
        kind: ReactionEnum,
    ) = BisqEasyOpenTradeMessageReaction(
        id = id,
        senderUserProfile = sender,
        receiverUserProfileId = me.id,
        receiverNetworkId = me.networkId,
        chatChannelId = "channel-1",
        chatChannelDomain = ChatChannelDomainEnum.BISQ_EASY_OPEN_TRADES,
        chatMessageId = "msg-1",
        reactionId = kind.ordinal,
        date = 1234567890000L,
        isRemoved = false,
    )
}
