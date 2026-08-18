package network.bisq.mobile.client.common.domain.service.chat.trade

import kotlinx.serialization.Serializable
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.reactions.BisqEasyOpenTradeMessageReaction
import network.bisq.mobile.data.replicated.network.identity.NetworkIdVO
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

@Serializable
data class BisqEasyOpenTradeMessageDto(
    val tradeId: String,
    val messageId: String,
    val channelId: String,
    val senderUserProfile: UserProfileVO,
    val receiverUserProfileId: String,
    val receiverNetworkId: NetworkIdVO,
    val text: String?,
    val citation: Citation?,
    val date: Long,
    val mediator: UserProfileVO?,
    val chatMessageType: ChatMessageTypeEnum,
    val bisqEasyOffer: BisqEasyOfferVO?,
    /**
     * Never read. `toDomain()` ignores it deliberately: reactions arrive on the CHAT_REACTIONS topic,
     * which is the only source that also reports removals, so seeding a message from this field would
     * resurrect reactions the peer already took back. Kept because it is on the wire — do not start
     * consuming it without moving removal handling along with it.
     */
    val chatMessageReactions: Set<BisqEasyOpenTradeMessageReaction>,
    val citationAuthorUserProfile: UserProfileVO?,
)
