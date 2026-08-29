package network.bisq.mobile.data.replicated.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * What every chat channel has in common, mirroring Bisq 2's `bisq.chat.ChatChannel<M>`: an id, a
 * domain and the messages. The private channels extend it directly; the public ones go through
 * [network.bisq.mobile.data.replicated.chat.pub.PublicChatChannel].
 *
 * [unreadCount] is a mobile addition: Bisq 2 keeps that number in `ChatNotificationService`, keyed
 * by channel id, and mobile hangs it off the channel because that is where the presenters read it.
 * It is sourced from the persisted service rather than counted locally, so it survives an app
 * restart. Trade chat tracks its read state in `TradeReadStateRepository` instead and leaves it at 0.
 */
abstract class ChatChannel<M : ChatMessage<*>>(
    val id: String,
    val chatChannelDomain: ChatChannelDomainEnum,
) {
    private val _chatMessages: MutableStateFlow<Set<M>> = MutableStateFlow(emptySet())
    val chatMessages: StateFlow<Set<M>> = _chatMessages.asStateFlow()

    private val _unreadCount: MutableStateFlow<Long> = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    fun addChatMessage(message: M) {
        // Last write wins: equals/hashCode is not enough, because a message keeps its id while its
        // reactions and delivery status change.
        _chatMessages.update { current ->
            current
                .filterNot { it.id == message.id }
                .toSet() + message
        }
    }

    fun setAllChatMessages(messages: Set<M>) {
        _chatMessages.value = messages.associateBy { it.id }.values.toSet()
    }

    fun setUnreadCount(value: Long) {
        _unreadCount.value = value
    }
}
