package network.bisq.mobile.client.common.domain.service.chat.private_chat

import kotlinx.serialization.Serializable

/** Transport shape of bisq2's `SendRejectionDto`: why the node refused a private chat send. */
@Serializable
enum class SendRejectionDto {
    MY_PROFILE_BANNED,
    PEER_BANNED,
}
