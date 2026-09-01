package network.bisq.mobile.client.common.domain.service.chat.public_chat

import kotlinx.coroutines.flow.first
import network.bisq.mobile.client.common.test_utils.ClientKoinIntegrationTestBase
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The client public chat facade is a DORMANT STUB until bisq2's `feature/public-chat-api` merges:
 * nothing user-reachable calls it, since [ClientPublicChatServiceFacade.isSupported] is false and the
 * Discussions segment is gated off besides. These pin that it stays dormant rather than half-working —
 * a stub that reported support would leave the screen waiting on a channel that never arrives.
 */
class ClientPublicChatServiceFacadeTest : ClientKoinIntegrationTestBase() {
    private val facade = ClientPublicChatServiceFacade()

    @Test
    fun `reports the feature unsupported and serves no channels`() =
        runTest {
            assertFalse(facade.isSupported.first())
            assertTrue(facade.channels.value.isEmpty())
        }

    @Test
    fun `every mutation fails until the trusted-node API ships`() =
        runTest {
            assertTrue(facade.sendChatMessage("channel", "text", null).isFailure)
            assertTrue(facade.editChatMessage("channel", "message", "text").isFailure)
            assertTrue(facade.deleteChatMessage("channel", "message").isFailure)
            assertTrue(facade.addChatMessageReaction("channel", "message", ReactionEnum.THUMBS_UP).isFailure)
            assertTrue(facade.removeChatMessageReaction("channel", "message", reaction()).isFailure)
        }

    private fun reaction() =
        CommonPublicChatMessageReaction(
            id = "r1",
            userProfileId = "p1",
            chatChannelId = "discussion.bisq",
            chatChannelDomain = ChatChannelDomainEnum.DISCUSSION,
            chatMessageId = "message",
            reactionId = ReactionEnum.THUMBS_UP.ordinal,
            date = 1,
        )
}
