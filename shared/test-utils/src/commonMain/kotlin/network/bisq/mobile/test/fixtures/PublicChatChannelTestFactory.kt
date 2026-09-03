package network.bisq.mobile.test.fixtures

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.data.replicated.chat.common.createMockCommonPublicChatMessage
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile

/**
 * The one message the Discussions channel carries in a test that seeds both channels, and the one
 * the Support channel carries. They exist as a pair: a `PublicChatServiceFacade` serves every domain
 * from one `channels` flow, so a thread mounted on the wrong domain does not fail — it renders a
 * plausible conversation. Asserting one text is displayed *and the other is not* is what separates
 * the two, and it only reads that way if both texts are obviously from different conversations.
 */
const val DISCUSSION_MESSAGE_TEXT = "what do you all think of the new fee model"
const val SUPPORT_MESSAGE_TEXT = "how do I reopen a dispute"

/**
 * A [CommonPublicChatChannel] on [domain] holding a single message with [text], sent by [author].
 *
 * [myUserProfile] defaults to someone other than [author], so the message renders as a peer's —
 * which is what a public thread mostly holds, and what the peer affordances (report, ignore, sender
 * name, avatar) need. Pass `myUserProfile = author` for one of the reader's own.
 *
 * Seed a list of these into a mocked `PublicChatServiceFacade.channels` to give a public chat thread
 * something to resolve. The order of that list is usually load-bearing — a thread that picks the
 * first channel instead of the one matching its domain still renders — so build the list at the call
 * site, with a comment saying which order it is proving, rather than defaulting it here.
 */
fun testPublicChatChannel(
    domain: ChatChannelDomainEnum,
    text: String,
    author: UserProfileVO,
    myUserProfile: UserProfileVO = createMockUserProfile("someone-else"),
    id: String = "${domain.name.lowercase()}.channel",
): CommonPublicChatChannel =
    CommonPublicChatChannel(
        id = id,
        chatChannelDomain = domain,
        channelTitle = domain.name.lowercase(),
    ).apply {
        setAllChatMessages(
            setOf(
                createMockCommonPublicChatMessage(
                    id = "msg-$id",
                    text = text,
                    senderUserProfile = author,
                    myUserProfile = myUserProfile,
                ),
            ),
        )
    }
