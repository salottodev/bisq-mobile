package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.json.Json
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BisqEasyOpenTradeMessageReactionTest {
    private val sender = createMockUserProfile("Alice")
    private val receiver = createMockUserProfile("Bob")

    /**
     * Bisq 2 passes `senderUserProfile.getId()` up to the base class as `userProfileId`. Here the
     * wire format carries the whole profile, so the base field is derived from it instead.
     */
    @Test
    fun `userProfileId is derived from the sender profile`() {
        val reaction: ChatMessageReaction = createReaction()

        assertEquals(sender.id, reaction.userProfileId)
    }

    /**
     * It is a body property precisely so the serialized shape stays what the backend sends — a
     * constructor field would add a `userProfileId` key that Bisq 2 does not emit at this level.
     */
    @Test
    fun `userProfileId is not serialized`() {
        assertFalse(Json.encodeToString(createReaction()).contains("userProfileId"))
    }

    @Test
    fun `survives a json round trip`() {
        val reaction = createReaction()

        assertEquals(reaction, Json.decodeFromString<BisqEasyOpenTradeMessageReaction>(Json.encodeToString(reaction)))
    }

    private fun createReaction() =
        BisqEasyOpenTradeMessageReaction(
            id = "reaction-1",
            senderUserProfile = sender,
            receiverUserProfileId = receiver.id,
            receiverNetworkId = receiver.networkId,
            chatChannelId = "channel-1",
            chatChannelDomain = ChatChannelDomainEnum.BISQ_EASY_OPEN_TRADES,
            chatMessageId = "msg-1",
            reactionId = ReactionEnum.HEART.ordinal,
            date = 1234567890000L,
            isRemoved = false,
        )
}
