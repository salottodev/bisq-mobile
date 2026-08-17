package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.json.Json
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A public offerbook reaction implements [ChatMessageReaction] directly — no sender/receiver
 * envelope and no removed state, which is the level Bisq 2 puts those on. The round trip guards the
 * wire shape: every field is declared in the constructor, so all of them are serialized.
 */
class BisqEasyOfferbookMessageReactionTest {
    @Test
    fun `carries the ChatMessageReaction surface`() {
        val reaction: ChatMessageReaction = createReaction()

        assertEquals("reaction-1", reaction.id)
        assertEquals("author-1", reaction.userProfileId)
        assertEquals("channel-1", reaction.chatChannelId)
        assertEquals(ChatChannelDomainEnum.BISQ_EASY_OFFERBOOK, reaction.chatChannelDomain)
        assertEquals("msg-1", reaction.chatMessageId)
        assertEquals(ReactionEnum.THUMBS_UP.ordinal, reaction.reactionId)
        assertEquals(1234567890000L, reaction.date)
    }

    @Test
    fun `survives a json round trip`() {
        val reaction = createReaction()

        assertEquals(reaction, Json.decodeFromString<BisqEasyOfferbookMessageReaction>(Json.encodeToString(reaction)))
    }

    /** `userProfileId` is a constructor field here, unlike on the private reactions. */
    @Test
    fun `serializes userProfileId`() {
        assertTrue(Json.encodeToString(createReaction()).contains("\"userProfileId\":\"author-1\""))
    }

    private fun createReaction() =
        BisqEasyOfferbookMessageReaction(
            id = "reaction-1",
            userProfileId = "author-1",
            chatChannelId = "channel-1",
            chatChannelDomain = ChatChannelDomainEnum.BISQ_EASY_OFFERBOOK,
            chatMessageId = "msg-1",
            reactionId = ReactionEnum.THUMBS_UP.ordinal,
            date = 1234567890000L,
        )
}
