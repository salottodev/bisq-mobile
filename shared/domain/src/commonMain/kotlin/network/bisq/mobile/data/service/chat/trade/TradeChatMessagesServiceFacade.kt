package network.bisq.mobile.data.service.chat.trade

import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.service.LifeCycleAware

interface TradeChatMessagesServiceFacade : LifeCycleAware {
    suspend fun sendChatMessage(
        text: String,
        citation: Citation?,
    ): Result<Unit>

    suspend fun addChatMessageReaction(
        messageId: String,
        reactionEnum: ReactionEnum,
    ): Result<Unit>

    suspend fun removeChatMessageReaction(
        messageId: String,
        reaction: BisqEasyOpenTradeMessageReaction,
    ): Result<Boolean>
}
