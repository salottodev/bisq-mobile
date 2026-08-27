package network.bisq.mobile.client.common.domain.service.chat.private_chat

import kotlinx.serialization.Serializable

/**
 * Body of a 409 from the node's send endpoints. The prose `message` the node sends alongside is
 * deliberately not declared, so that nothing but [rejection] can fail the decode.
 */
@Serializable
data class SendRefusedResponse(
    val rejection: SendRejectionDto,
)
