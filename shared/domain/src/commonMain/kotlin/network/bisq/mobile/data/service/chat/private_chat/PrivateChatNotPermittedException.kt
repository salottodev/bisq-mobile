package network.bisq.mobile.data.service.chat.private_chat

/**
 * The paired trusted node runs a version that has private chat, but this pairing was not granted the
 * permission for it.
 *
 * Its own type rather than a status code, so presenters in `:shared:presentation` can tell this apart
 * from a dropped connection without depending on the client app's HTTP types. Only the Bisq Connect
 * flavour can produce it — the node flavour has no permission layer.
 */
class PrivateChatNotPermittedException : Exception("The paired trusted node did not grant permission for private chats")
