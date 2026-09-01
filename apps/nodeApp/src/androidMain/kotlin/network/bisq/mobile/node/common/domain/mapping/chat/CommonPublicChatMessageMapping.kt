package network.bisq.mobile.node.common.domain.mapping.chat

import bisq.user.profile.UserProfile
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatMessage
import network.bisq.mobile.node.common.domain.mapping.Mappings
import kotlin.jvm.optionals.getOrNull
import bisq.chat.common.CommonPublicChatMessage as Bisq2CommonPublicChatMessage
import bisq.chat.reactions.CommonPublicChatMessageReaction as Bisq2CommonPublicChatMessageReaction

/**
 * @param author resolved by the caller: a public message carries only `authorUserProfileId`, and the
 *   profile store is pruned independently, so who resolves it also owns what to do when it is gone.
 * @param myUserProfile the node's selected identity, which decides reaction ownership only.
 * @param isMyMessage stated by the caller rather than derived, because bisq2 authorizes edit and
 *   delete against ANY of my identities while reactions belong to the selected one. See
 *   [network.bisq.mobile.data.replicated.chat.ChatMessage].
 * @param visibleReactions the reactions the caller wants on the model; the caller owns the ban rules,
 *   this only maps what it is handed. Same split as bisq2's `PublicChatDtoFactory`.
 */
fun Bisq2CommonPublicChatMessage.toDomain(
    author: UserProfile,
    citationAuthorUserProfile: UserProfile?,
    myUserProfile: UserProfile,
    isMyMessage: Boolean,
    visibleReactions: Collection<Bisq2CommonPublicChatMessageReaction>,
): CommonPublicChatMessage =
    CommonPublicChatMessage(
        id = id,
        chatMessageType = Mappings.ChatMessageTypeMapping.fromBisq2Model(chatMessageType),
        text = text.getOrNull(),
        citation = citation.getOrNull()?.let { Mappings.CitationMapping.fromBisq2Model(it) },
        citationAuthorUserProfile = citationAuthorUserProfile?.let { Mappings.UserProfileMapping.fromBisq2Model(it) },
        date = date,
        senderUserProfile = Mappings.UserProfileMapping.fromBisq2Model(author),
        myUserProfile = Mappings.UserProfileMapping.fromBisq2Model(myUserProfile),
        chatReactions = visibleReactions.map { it.toDomain() },
        wasEdited = isWasEdited,
        isMyMessage = isMyMessage,
    )
