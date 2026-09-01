package network.bisq.mobile.data.replicated.chat.common

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.chat.pub.PublicChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertTrue

/**
 * The shared body is covered by `ChatMessageTest`; this pins what the public branch adds and the
 * one thing it must not have — a public message is not a private one, so the UI cannot ask it for
 * a delivery status.
 */
class CommonPublicChatMessageTest {
    private val selected = createMockUserProfile("Bob")
    private val myOtherIdentity = createMockUserProfile("Bob-second")

    @Test
    fun `is a public chat message and not a private one`() {
        val message: ChatMessage<*> = createMockCommonPublicChatMessage()

        assertIs<PublicChatMessage<*>>(message)
        assertIsNot<PrivateChatMessage<*>>(message)
    }

    @Test
    fun `wasEdited defaults to false and is carried when set`() {
        assertFalse(createMockCommonPublicChatMessage().wasEdited)
        assertTrue(createMockCommonPublicChatMessage(wasEdited = true).wasEdited)
    }

    /**
     * The two answers a single derivation from `myUserProfile` cannot give at once: bisq2 authorizes
     * edit and delete against ANY of my identities (`isUserIdentityPresent(authorUserProfileId)`)
     * while reaction ownership is decided against the SELECTED profile, as desktop's `ReactionItem`
     * does. Collapsed, a multi-identity user loses Edit/Delete on their own message.
     */
    @Test
    fun `a message from another of my identities is mine while reaction ownership stays with the selected profile`() {
        val message =
            createMockCommonPublicChatMessage(
                id = "message-1",
                senderUserProfile = myOtherIdentity,
                myUserProfile = selected,
                isMyMessage = true,
            )

        assertTrue(message.isMyMessage)
        assertTrue(message.isMyChatReaction(reactionBy(selected.id)))
        assertFalse(message.isMyChatReaction(reactionBy(myOtherIdentity.id)))
    }

    @Test
    fun `isMyMessage falls back to the sender when it is not stated`() {
        assertFalse(
            createMockCommonPublicChatMessage(senderUserProfile = myOtherIdentity, myUserProfile = selected).isMyMessage,
        )
        assertTrue(
            createMockCommonPublicChatMessage(senderUserProfile = selected, myUserProfile = selected).isMyMessage,
        )
    }

    private fun reactionBy(userProfileId: String) =
        CommonPublicChatMessageReaction(
            id = "reaction-$userProfileId",
            userProfileId = userProfileId,
            chatChannelId = "discussion.bisq",
            chatChannelDomain = ChatChannelDomainEnum.DISCUSSION,
            chatMessageId = "message-1",
            reactionId = 0,
            date = 1234567890000L,
        )
}
