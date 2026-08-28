package network.bisq.mobile.data.replicated.chat.priv

import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.network.confidential.ack.MessageDeliveryInfoVO
import network.bisq.mobile.data.replicated.network.confidential.ack.MessageDeliveryStatusEnum
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.service.message_delivery.MessageDeliveryServiceFacade
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Only what the private branch adds to [network.bisq.mobile.data.replicated.chat.ChatMessage] — the
 * shared body is covered by `ChatMessageTest`. [PrivateChatMessage] is abstract, so this goes through
 * [BisqEasyOpenTradeMessage].
 */
class PrivateChatMessageTest {
    private val me = createMockUserProfile("Bob")
    private val peer = createMockUserProfile("Alice")

    @Test
    fun `delivery status observer feeds the status flow and is keyed by message id`() {
        val message = createMessage()
        val facade = RecordingMessageDeliveryServiceFacade()

        message.addMessageDeliveryStatusObserver(facade)
        assertEquals(emptyMap(), message.messageDeliveryStatus.value)

        val info =
            MessageDeliveryInfoVO(
                messageDeliveryStatus = MessageDeliveryStatusEnum.SENT,
                ackRequestingMessageId = "ack-1",
                canManuallyResendMessage = false,
            )
        facade.emit(MESSAGE_ID, peer.id to info)

        assertEquals(mapOf(peer.id to info), message.messageDeliveryStatus.value)

        message.removeMessageDeliveryStatusObserver(facade)
        assertEquals(listOf(MESSAGE_ID), facade.removed)
    }

    private fun createMessage() =
        createMockBisqEasyOpenTradeMessage(
            id = MESSAGE_ID,
            senderUserProfile = peer,
            myUserProfile = me,
        )

    /** No mockk in `commonTest`, and the observer callback has to actually fire for line 78 to run. */
    private class RecordingMessageDeliveryServiceFacade : MessageDeliveryServiceFacade() {
        private val observers = mutableMapOf<String, (Pair<String, MessageDeliveryInfoVO>) -> Unit>()
        val removed = mutableListOf<String>()

        override fun onResendMessage(messageId: String) = Unit

        override fun addMessageDeliveryStatusObserver(
            tradeMessageId: String,
            onNewStatus: (entry: Pair<String, MessageDeliveryInfoVO>) -> Unit,
        ) {
            observers[tradeMessageId] = onNewStatus
        }

        override fun removeMessageDeliveryStatusObserver(tradeMessageId: String) {
            removed += tradeMessageId
            observers.remove(tradeMessageId)
        }

        fun emit(
            tradeMessageId: String,
            entry: Pair<String, MessageDeliveryInfoVO>,
        ) {
            observers.getValue(tradeMessageId)(entry)
        }
    }

    private companion object {
        const val MESSAGE_ID = "msg-1"
    }
}
