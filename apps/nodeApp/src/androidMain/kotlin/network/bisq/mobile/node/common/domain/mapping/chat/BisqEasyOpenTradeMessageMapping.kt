package network.bisq.mobile.node.common.domain.mapping.chat

import bisq.user.profile.UserProfile
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.node.common.domain.mapping.Mappings
import kotlin.jvm.optionals.getOrNull
import bisq.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage as Bisq2BisqEasyOpenTradeMessage

fun Bisq2BisqEasyOpenTradeMessage.toDomain(
    citationAuthorUserProfile: UserProfile?,
    myUserProfile: UserProfile,
): BisqEasyOpenTradeMessage =
    BisqEasyOpenTradeMessage(
        id = id,
        chatMessageType = Mappings.ChatMessageTypeMapping.fromBisq2Model(chatMessageType),
        text = text.getOrNull(),
        citation = citation.getOrNull()?.let { Mappings.CitationMapping.fromBisq2Model(it) },
        citationAuthorUserProfile = citationAuthorUserProfile?.let { Mappings.UserProfileMapping.fromBisq2Model(it) },
        date = date,
        senderUserProfile = Mappings.UserProfileMapping.fromBisq2Model(senderUserProfile),
        myUserProfile = Mappings.UserProfileMapping.fromBisq2Model(myUserProfile),
        chatReactions =
            chatMessageReactions
                .filter { !it.isRemoved }
                .map { it.toDomain() },
        tradeId = tradeId,
        mediator = mediator.getOrNull()?.let { Mappings.UserProfileMapping.fromBisq2Model(it) },
        bisqEasyOffer = bisqEasyOffer.getOrNull()?.let { Mappings.BisqEasyOfferMapping.fromBisq2Model(it) },
    )
