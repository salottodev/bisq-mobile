package network.bisq.mobile.data.service.chat.public_chat

/**
 * The node refused the mutation before touching the store: nothing was published and nothing will
 * appear on the message stream. Not a transport problem, so retrying cannot help.
 *
 * Its own type rather than a status code, so presenters in `:shared:presentation` can tell it apart
 * from a dropped connection without depending on the client app's HTTP types — the node flavour
 * raises it from its own pre-checks, with no HTTP involved at all.
 */
class PublicChatSendRefusedException(
    val rejection: PublicChatSendRejection,
) : Exception("The node refused the public chat request: $rejection")
