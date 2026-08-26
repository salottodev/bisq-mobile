package network.bisq.mobile.node.common.domain.mapping.chat

import bisq.user.profile.UserProfile
import network.bisq.mobile.data.replicated.chat.two_party.TwoPartyPrivateChatMessage
import network.bisq.mobile.node.common.domain.mapping.Mappings
import kotlin.jvm.optionals.getOrNull
import bisq.chat.reactions.TwoPartyPrivateChatMessageReaction as Bisq2TwoPartyPrivateChatMessageReaction
import bisq.chat.two_party.TwoPartyPrivateChatMessage as Bisq2TwoPartyPrivateChatMessage

/**
 * @param visibleReactions the message's reactions the caller wants on the model; the caller owns the
 *   ban and removal rules, this only maps what it is handed. Same split as bisq2's
 *   `TwoPartyPrivateChatMessageDtoMapping`.
 */
fun Bisq2TwoPartyPrivateChatMessage.toDomain(
    citationAuthorUserProfile: UserProfile?,
    myUserProfile: UserProfile,
    visibleReactions: Collection<Bisq2TwoPartyPrivateChatMessageReaction>,
): TwoPartyPrivateChatMessage =
    TwoPartyPrivateChatMessage(
        id = id,
        chatMessageType = Mappings.ChatMessageTypeMapping.fromBisq2Model(chatMessageType),
        text = text.getOrNull(),
        citation = citation.getOrNull()?.let { Mappings.CitationMapping.fromBisq2Model(it) },
        citationAuthorUserProfile = citationAuthorUserProfile?.let { Mappings.UserProfileMapping.fromBisq2Model(it) },
        date = date,
        senderUserProfile = Mappings.UserProfileMapping.fromBisq2Model(senderUserProfile),
        myUserProfile = Mappings.UserProfileMapping.fromBisq2Model(myUserProfile),
        chatReactions = visibleReactions.map { it.toDomain() },
    )
