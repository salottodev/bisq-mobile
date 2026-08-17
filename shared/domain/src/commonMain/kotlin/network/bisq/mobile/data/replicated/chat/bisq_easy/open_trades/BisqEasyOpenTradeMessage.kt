package network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades

import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * A trade chat message. Mirrors Bisq 2's `BisqEasyOpenTradeMessage`, which extends
 * `PrivateChatMessage` and adds only the three trade-specific fields below — everything else lives
 * in [PrivateChatMessage].
 *
 * Takes plain values rather than a DTO, per the rule stated on [PrivateChatMessage]: the client's
 * `BisqEasyOpenTradeMessageDto` lives in `apps/clientApp` and maps into this type.
 */
class BisqEasyOpenTradeMessage(
    id: String,
    chatMessageType: ChatMessageTypeEnum,
    text: String?,
    citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    date: Long,
    senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<BisqEasyOpenTradeMessageReaction>,
    val tradeId: String,
    val mediator: UserProfileVO?,
    val bisqEasyOffer: BisqEasyOfferVO?,
) : PrivateChatMessage<BisqEasyOpenTradeMessageReaction>(
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
