package network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades

import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.domain.utils.createUuid

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

/**
 * A fast way to create a mock trade chat message, for tests and Compose previews. Mirrors
 * [createMockUserProfile], and lives in `commonMain` for the same reason it does: the chat previews
 * are production composables and cannot see a test source set.
 *
 * Fixture only. The two production mappings that build this type —
 * `BisqEasyOpenTradeMessageMapping` (node) and `BisqEasyOpenTradeMessageDtoMapping` (client) — must
 * keep calling the constructor directly; every field they set comes off the wire, so there is nothing
 * for a default to supply.
 *
 * [date] is a constant rather than the current time so previews render deterministically and
 * `dateString` assertions stay stable. The [senderUserProfile] and [myUserProfile] defaults are
 * *different* profiles, so an unqualified mock is a message from the peer; pass the same instance to
 * both for one of mine.
 */
fun createMockBisqEasyOpenTradeMessage(
    id: String = createUuid(),
    chatMessageType: ChatMessageTypeEnum = ChatMessageTypeEnum.TEXT,
    text: String? = "Hello",
    citation: Citation? = null,
    citationAuthorUserProfile: UserProfileVO? = null,
    date: Long = 1234567890000L,
    senderUserProfile: UserProfileVO = createMockUserProfile("Alice"),
    myUserProfile: UserProfileVO = createMockUserProfile("Bob"),
    chatReactions: List<BisqEasyOpenTradeMessageReaction> = emptyList(),
    tradeId: String = "trade-1",
    mediator: UserProfileVO? = null,
    bisqEasyOffer: BisqEasyOfferVO? = null,
) = BisqEasyOpenTradeMessage(
    id = id,
    chatMessageType = chatMessageType,
    text = text,
    citation = citation,
    citationAuthorUserProfile = citationAuthorUserProfile,
    date = date,
    senderUserProfile = senderUserProfile,
    myUserProfile = myUserProfile,
    chatReactions = chatReactions,
    tradeId = tradeId,
    mediator = mediator,
    bisqEasyOffer = bisqEasyOffer,
)
