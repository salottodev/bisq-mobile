package network.bisq.mobile.client.common.domain.service.chat.public_chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.service.chat.public_chat.PublicChatServiceFacade

/**
 * Client-mode public chat: DORMANT STUB, for the same reason [
 * network.bisq.mobile.client.common.domain.service.contacts.ClientContactsServiceFacade] is one.
 *
 * `/public-chat-channels`, `Permission.PUBLIC_CHAT_CHANNELS` and `ApiFeature.PUBLIC_CHAT` live on
 * bisq2's unmerged `feature/public-chat-api`, so no released trusted node serves them. The client's
 * half of #1744 replaces this with a gateway plus the three `PUBLIC_CHAT_*` subscriptions once that
 * branch lands and `bisq-api` can move past 2.1.12.
 *
 * Registered rather than left out so the DI graph and its integration tests stay symmetric with the
 * node's. [isSupported] is false, so the screen renders its unavailable state instead of calling any
 * of this; the failure results are a defensive backstop, not UX.
 */
class ClientPublicChatServiceFacade : PublicChatServiceFacade {
    override val isSupported: Flow<Boolean> = flowOf(false)

    private val _channels = MutableStateFlow<List<CommonPublicChatChannel>>(emptyList())
    override val channels: StateFlow<List<CommonPublicChatChannel>> = _channels.asStateFlow()

    override suspend fun sendChatMessage(
        channelId: String,
        text: String,
        citation: Citation?,
    ): Result<Unit> = notAvailable()

    override suspend fun editChatMessage(
        channelId: String,
        messageId: String,
        text: String,
    ): Result<Unit> = notAvailable()

    override suspend fun deleteChatMessage(
        channelId: String,
        messageId: String,
    ): Result<Unit> = notAvailable()

    override suspend fun addChatMessageReaction(
        channelId: String,
        messageId: String,
        reactionEnum: ReactionEnum,
    ): Result<Unit> = notAvailable()

    override suspend fun removeChatMessageReaction(
        channelId: String,
        messageId: String,
        reaction: CommonPublicChatMessageReaction,
    ): Result<Unit> = notAvailable()

    override suspend fun consumeNotifications(channelId: String) = Unit

    private fun <T> notAvailable(): Result<T> = Result.failure(UnsupportedOperationException("Public chat API not available on trusted nodes yet"))
}
