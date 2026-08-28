package network.bisq.mobile.data.replicated.chat.common

import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.chat.pub.PublicChatMessage
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
}
