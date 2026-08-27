package network.bisq.mobile.data.service.chat.private_chat

/**
 * Why the node refused a send before storing anything. Mirrors Bisq 2's `SendRejection`;
 * [UNKNOWN] covers a node that reports the refusal without naming its reason.
 */
enum class PrivateChatSendRejection {
    MY_PROFILE_BANNED,
    PEER_BANNED,
    UNKNOWN,
}
