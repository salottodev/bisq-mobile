package network.bisq.mobile.data.replicated.chat.pub

import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.reactions.ChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * A message in a public channel, mirroring Bisq 2's `bisq.chat.pub.PublicChatMessage`: the
 * discussion and support channels ([network.bisq.mobile.data.replicated.chat.common.CommonPublicChatMessage])
 * and, upstream, the offerbook. Adds nothing to [ChatMessage] that the UI reads — upstream's additions are
 * the P2P-store concerns (`DistributedData`, `isDataInvalid`) — and, unlike the private branch,
 * carries no delivery status: a broadcast message has no single receiver to acknowledge it.
 */
abstract class PublicChatMessage<R : ChatMessageReaction>(
    id: String,
    chatMessageType: ChatMessageTypeEnum,
    text: String?,
    citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    date: Long,
    senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<R>,
    wasEdited: Boolean,
) : ChatMessage<R>(
        id = id,
        chatMessageType = chatMessageType,
        text = text,
        citation = citation,
        citationAuthorUserProfile = citationAuthorUserProfile,
        date = date,
        senderUserProfile = senderUserProfile,
        myUserProfile = myUserProfile,
        chatReactions = chatReactions,
        wasEdited = wasEdited,
    )
