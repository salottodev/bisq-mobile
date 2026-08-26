package network.bisq.mobile.data.service.chat.private_chat

/**
 * The node refused the send outright: nothing was stored, nothing reaches the peer and nothing
 * will appear on the message stream. Not a transport problem, so retrying cannot help — the only
 * actionable part is [rejection], which says which side of the conversation is banned.
 *
 * Its own type rather than a status code, for the same reason as [PrivateChatNotPermittedException]:
 * presenters must tell it apart from a dropped connection without depending on HTTP types, and the
 * node flavour produces it from Bisq 2's `SendOutcome` with no HTTP involved at all.
 */
class PrivateChatSendRefusedException(
    val rejection: PrivateChatSendRejection,
) : Exception("The node refused the private chat send: $rejection")
