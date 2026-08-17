package network.bisq.mobile.data.replicated.chat.bisq_easy.offerbook

import kotlinx.serialization.json.Json
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Wire-shape guard. The citation carried by an offerbook message is the shared [Citation] type — a
 * chat type that deliberately drops the `VO` postfix to mirror Bisq 2 — so this pins that a message
 * with and without one both round trip.
 */
class BisqEasyOfferbookMessageDtoTest {
    @Test
    fun `round trips with a citation`() {
        val dto = createDto(Citation(authorUserProfileId = "author-2", text = "the quoted line", chatMessageId = "msg-0"))

        val decoded = Json.decodeFromString<BisqEasyOfferbookMessageDto>(Json.encodeToString(dto))

        assertEquals(dto, decoded)
        assertEquals("the quoted line", decoded.citation?.text)
    }

    @Test
    fun `round trips without a citation`() {
        val dto = createDto(citation = null)

        val decoded = Json.decodeFromString<BisqEasyOfferbookMessageDto>(Json.encodeToString(dto))

        assertEquals(dto, decoded)
        assertNull(decoded.citation)
    }

    private fun createDto(citation: Citation?) =
        BisqEasyOfferbookMessageDto(
            id = "msg-1",
            channelId = "channel-1",
            authorUserProfileId = "author-1",
            bisqEasyOffer = null,
            text = "Looking for USD",
            citation = citation,
            date = 1234567890000L,
            wasEdited = false,
            chatMessageType = ChatMessageTypeEnum.TEXT,
        )
}
