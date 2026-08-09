package network.bisq.mobile.data.replicated.chat

import kotlinx.serialization.Serializable

@Serializable
data class Citation(
    val authorUserProfileId: String,
    val text: String,
    var chatMessageId: String?,
)
