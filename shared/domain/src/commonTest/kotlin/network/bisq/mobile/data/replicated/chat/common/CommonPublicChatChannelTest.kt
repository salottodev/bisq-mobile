package network.bisq.mobile.data.replicated.chat.common

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Same identity rule as `TwoPartyPrivateChatChannelTest`: a message keeps its id while its
 * reactions move, so re-adding it must replace the old copy rather than keep both.
 */
class CommonPublicChatChannelTest {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `a message re-added with the same id replaces the old copy`() {
        val channel = createChannel()
        val original = createMessage("message-1", text = "first")
        val updated = createMessage("message-1", text = "edited")

        channel.addChatMessage(original)
        channel.addChatMessage(updated)

        assertSame(updated, channel.chatMessages.value.single())
    }

    @Test
    fun `messages with different ids all stay`() {
        val channel = createChannel()

        channel.addChatMessage(createMessage("message-1"))
        channel.addChatMessage(createMessage("message-2"))

        assertEquals(setOf("message-1", "message-2"), channel.ids())
    }

    @Test
    fun `setAllChatMessages keeps one copy per id`() {
        val channel = createChannel()

        channel.setAllChatMessages(
            setOf(
                createMessage("message-1", text = "first"),
                createMessage("message-1", text = "updated"),
                createMessage("message-2"),
            ),
        )

        val stored = channel.chatMessages.value
        assertEquals(2, stored.size)
        assertEquals(setOf("message-1", "message-2"), channel.ids())
        assertEquals("updated", stored.single { it.id == "message-1" }.text)
    }

    @Test
    fun `unread count starts at zero and follows the setter`() {
        val channel = createChannel()

        assertEquals(0L, channel.unreadCount.value)
        channel.setUnreadCount(3)
        assertEquals(3L, channel.unreadCount.value)
    }

    private fun CommonPublicChatChannel.ids() = chatMessages.value.map { it.id }.toSet()

    private fun createChannel() =
        CommonPublicChatChannel(
            id = "discussion.bisq",
            chatChannelDomain = ChatChannelDomainEnum.DISCUSSION,
            channelTitle = "bisq",
        )

    private fun createMessage(
        id: String,
        text: String = "hello",
    ) = createMockCommonPublicChatMessage(
        id = id,
        text = text,
        senderUserProfile = peer,
        myUserProfile = me,
    )
}
