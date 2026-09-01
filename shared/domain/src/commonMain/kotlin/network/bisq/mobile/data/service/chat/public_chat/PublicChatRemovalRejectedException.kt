package network.bisq.mobile.data.service.chat.public_chat

/**
 * The removal was published to the network but the local store refused it, so the message or
 * reaction is gone everywhere except here. bisq2's store answers a rejected removal with a
 * successful, empty result and no event, which is why this is detected by re-reading the set
 * rather than by the future — the same 500 `PublicChatRestApi` answers.
 */
class PublicChatRemovalRejectedException(
    what: String,
) : Exception("The $what was published as removed but could not be removed locally")
