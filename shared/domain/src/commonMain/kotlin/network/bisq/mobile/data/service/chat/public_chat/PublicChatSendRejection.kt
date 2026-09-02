package network.bisq.mobile.data.service.chat.public_chat

/**
 * Why the node refused a public chat mutation before doing anything. Mirrors the two answers
 * bisq2's `PublicChatRestApi` gives for a profile it will not publish for — 409 `MY_PROFILE_BANNED`
 * and 429 — with [UNKNOWN] for a node that reports a refusal without naming a reason this build
 * knows.
 */
enum class PublicChatSendRejection {
    MY_PROFILE_BANNED,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN,
}
