package network.bisq.mobile.data.replicated.chat.two_party

import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * A message in a two-party private chat (DM), replicating Bisq 2's `TwoPartyPrivateChatMessage`.
 *
 * Adds nothing to [PrivateChatMessage] — exactly as upstream, where `TwoPartyPrivateChatMessage`
 * extends `PrivateChatMessage` without introducing a single field of its own. The trade sibling
 * [network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage] is
 * where the extra trade fields live.
 */
class TwoPartyPrivateChatMessage(
    id: String,
    chatMessageType: ChatMessageTypeEnum,
    text: String?,
    citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    date: Long,
    senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<TwoPartyPrivateChatMessageReaction>,
) : PrivateChatMessage<TwoPartyPrivateChatMessageReaction>(
        id = id,
        chatMessageType = chatMessageType,
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        date = date,
        senderUserProfile = senderUserProfile,
        myUserProfile = myUserProfile,
        chatReactions = chatReactions,
    )
