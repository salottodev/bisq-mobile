package network.bisq.mobile.data.replicated.chat

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.domain.utils.DateUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [ChatMessage] is abstract, so everything here goes through [BisqEasyOpenTradeMessage] — the
 * subclasses add fields and inherit the whole body under test.
 */
class ChatMessageTest {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `nullable text and citation fall back to empty strings`() {
        val message = createMessage(text = null, citation = null)

        assertEquals("", message.textString)
        assertEquals("", message.citationString)
        assertEquals("", message.decodedText)
        assertNull(message.citationAuthorUserName)
    }

    @Test
    fun `text and citation are exposed when present`() {
        val message =
            createMessage(
                text = "Payment sent",
                citation = Citation(authorUserProfileId = peer.id, text = "When do we settle?", chatMessageId = "msg-0"),
                citationAuthorUserProfile = peer,
            )

        assertEquals("Payment sent", message.textString)
        assertEquals("When do we settle?", message.citationString)
        assertEquals("Alice", message.citationAuthorUserName)
    }

    /**
     * `decodedText` runs the text through [network.bisq.mobile.i18n.I18nSupport.decode], which
     * returns the input untouched when it is not a known key — which is what a plain chat message is.
     */
    @Test
    fun `decodedText passes a non-key through unchanged`() {
        assertEquals("Payment sent", createMessage(text = "Payment sent").decodedText)
    }

    @Test
    fun `dateString formats the epoch millis`() {
        val message = createMessage()

        assertEquals(DateUtils.toDateTime(MESSAGE_DATE), message.dateString)
    }

    @Test
    fun `isMyMessage compares the sender against my profile`() {
        assertTrue(createMessage(sender = me).isMyMessage)
        assertFalse(createMessage(sender = peer).isMyMessage)
    }

    @Test
    fun `senderUserName and senderUserProfileId come from the sender`() {
        val message = createMessage(sender = peer)

        assertEquals("Alice", message.senderUserName)
        assertEquals(peer.id, message.senderUserProfileId)
    }

    @Test
    fun `isMyChatReaction is true only for a reaction I sent`() {
        val message = createMessage()

        assertTrue(message.isMyChatReaction(createReaction("r-1", sender = me)))
        assertFalse(message.isMyChatReaction(createReaction("r-2", sender = peer)))
    }

    @Test
    fun `setReactions replaces the reaction flow`() {
        val message = createMessage(reactions = listOf(createReaction("r-1", sender = peer)))

        message.setReactions(listOf(createReaction("r-2", sender = me), createReaction("r-3", sender = peer)))

        assertEquals(listOf("r-2", "r-3"), message.chatReactions.value.map { it.id })
    }

    private fun createMessage(
        text: String? = "hello",
        citation: Citation? = null,
        citationAuthorUserProfile: UserProfileVO? = null,
        sender: UserProfileVO = peer,
        reactions: List<BisqEasyOpenTradeMessageReaction> = emptyList(),
    ) = createMockBisqEasyOpenTradeMessage(
        id = MESSAGE_ID,
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        date = MESSAGE_DATE,
        senderUserProfile = sender,
        myUserProfile = me,
        chatReactions = reactions,
    )

    private fun createReaction(
        id: String,
        sender: UserProfileVO,
    ) = BisqEasyOpenTradeMessageReaction(
        id = id,
        senderUserProfile = sender,
        receiverUserProfileId = me.id,
        receiverNetworkId = me.networkId,
        chatChannelId = "channel-1",
        chatChannelDomain = ChatChannelDomainEnum.BISQ_EASY_OPEN_TRADES,
        chatMessageId = MESSAGE_ID,
        reactionId = 0,
        date = MESSAGE_DATE,
        isRemoved = false,
    )

    private companion object {
        const val MESSAGE_ID = "msg-1"
        const val MESSAGE_DATE = 1234567890000L
    }
}
