package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertEquals

/**
 * The row groups by the resolved [ReactionEnum] rather than the raw id, so the two things worth
 * pinning are the grouping (one pill per distinct reaction, count badge above one) and that a tap
 * removes my reaction but adds someone else's.
 */
class ReactionDisplayUiTest : BisqComposeUiTestBase() {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `groups equal reactions into one pill with a count`() {
        setTestContent {
            ReactionDisplay(
                message =
                    createMessage(
                        reaction("r-1", peer, ReactionEnum.THUMBS_UP),
                        reaction("r-2", me, ReactionEnum.THUMBS_UP),
                        reaction("r-3", peer, ReactionEnum.HEART),
                    ),
                onAddReaction = {},
                onRemoveReaction = {},
            )
        }

        // Two distinct reactions → two pills; only the doubled one shows a count.
        assertEquals(2, pillCount())
        composeTestRule.onNodeWithText("2").assertExists()
    }

    /**
     * Bisq 2 can send a reaction id this build has no icon for. It is dropped instead of indexing
     * [ReactionEnum.entries] out of range.
     */
    @Test
    fun `drops a reaction whose id this build does not know`() {
        setTestContent {
            ReactionDisplay(
                message =
                    createMessage(
                        reaction("r-1", peer, ReactionEnum.THUMBS_UP),
                        reaction("r-unknown", peer, reactionId = ReactionEnum.entries.size + 5),
                    ),
                onAddReaction = {},
                onRemoveReaction = {},
            )
        }

        assertEquals(1, pillCount())
    }

    @Test
    fun `tapping my own reaction removes it`() {
        val mine = reaction("r-mine", me, ReactionEnum.THUMBS_UP)
        var removed: BisqEasyOpenTradeMessageReaction? = null
        var added: ReactionEnum? = null

        setTestContent {
            ReactionDisplay(
                message = createMessage(mine),
                onAddReaction = { added = it },
                onRemoveReaction = { removed = it },
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()
        composeTestRule.waitForIdle()

        assertEquals(mine, removed)
        assertEquals(null, added)
    }

    @Test
    fun `tapping a peer-only reaction adds mine`() {
        var removed: BisqEasyOpenTradeMessageReaction? = null
        var added: ReactionEnum? = null

        setTestContent {
            ReactionDisplay(
                message = createMessage(reaction("r-peer", peer, ReactionEnum.HEART)),
                onAddReaction = { added = it },
                onRemoveReaction = { removed = it },
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()
        composeTestRule.waitForIdle()

        assertEquals(ReactionEnum.HEART, added)
        assertEquals(null, removed)
    }

    /** Each pill is the only clickable node in the row, so counting them counts the groups. */
    private fun pillCount() = composeTestRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size

    private fun createMessage(vararg reactions: BisqEasyOpenTradeMessageReaction) =
        createMockBisqEasyOpenTradeMessage(
            id = "msg-1",
            text = "Payment sent",
            senderUserProfile = peer,
            myUserProfile = me,
            chatReactions = reactions.toList(),
        )

    private fun reaction(
        id: String,
        sender: UserProfileVO,
        kind: ReactionEnum,
    ) = reaction(id, sender, kind.ordinal)

    private fun reaction(
        id: String,
        sender: UserProfileVO,
        reactionId: Int,
    ) = BisqEasyOpenTradeMessageReaction(
        id = id,
        senderUserProfile = sender,
        receiverUserProfileId = me.id,
        receiverNetworkId = me.networkId,
        chatChannelId = "channel-1",
        chatChannelDomain = ChatChannelDomainEnum.BISQ_EASY_OPEN_TRADES,
        chatMessageId = "msg-1",
        reactionId = reactionId,
        date = 1234567890000L,
        isRemoved = false,
    )
}
