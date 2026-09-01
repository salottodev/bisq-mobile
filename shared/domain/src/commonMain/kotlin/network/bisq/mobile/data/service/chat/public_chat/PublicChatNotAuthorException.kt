package network.bisq.mobile.data.service.chat.public_chat

/**
 * Only the author can edit or delete a message, or remove their own reaction. bisq2's
 * `PublicChatRestApi` answers 403 for this; the node reaches it when `findUserIdentity` cannot
 * resolve the author among my identities.
 */
class PublicChatNotAuthorException : Exception("Only the author can change this message")
