package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.json.Json
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Like [BisqEasyOfferbookMessageReactionTest]: a public reaction implements [ChatMessageReaction]
 * directly, with no sender/receiver envelope and no removed state.
 */
class CommonPublicChatMessageReactionTest {
    @Test
    fun `carries the ChatMessageReaction surface`() {
        val reaction: ChatMessageReaction = createReaction()

        assertEquals("reaction-1", reaction.id)
        assertEquals("author-1", reaction.userProfileId)
        assertEquals("discussion.bisq", reaction.chatChannelId)
        assertEquals(ChatChannelDomainEnum.DISCUSSION, reaction.chatChannelDomain)
        assertEquals("msg-1", reaction.chatMessageId)
        assertEquals(ReactionEnum.THUMBS_UP.ordinal, reaction.reactionId)
        assertEquals(1234567890000L, reaction.date)
    }

    @Test
    fun `survives a json round trip`() {
        val reaction = createReaction()

        assertEquals(reaction, Json.decodeFromString<CommonPublicChatMessageReaction>(Json.encodeToString(reaction)))
    }

    private fun createReaction() =
        CommonPublicChatMessageReaction(
            id = "reaction-1",
            userProfileId = "author-1",
            chatChannelId = "discussion.bisq",
            chatChannelDomain = ChatChannelDomainEnum.DISCUSSION,
            chatMessageId = "msg-1",
            reactionId = ReactionEnum.THUMBS_UP.ordinal,
            date = 1234567890000L,
        )
}
