package network.bisq.mobile.data.replicated.chat.common

import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.pub.PublicChatMessage
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.domain.utils.createUuid

/**
 * A message in the discussion or support channel, replicating Bisq 2's
 * `bisq.chat.common.CommonPublicChatMessage`. Adds nothing to [PublicChatMessage], exactly as
 * upstream; the offerbook sibling is where the offer lives.
 */
class CommonPublicChatMessage(
    id: String,
    chatMessageType: ChatMessageTypeEnum,
    text: String?,
    citation: Citation?,
    citationAuthorUserProfile: UserProfileVO?,
    date: Long,
    senderUserProfile: UserProfileVO,
    myUserProfile: UserProfileVO,
    chatReactions: List<CommonPublicChatMessageReaction>,
    wasEdited: Boolean,
) : PublicChatMessage<CommonPublicChatMessageReaction>(
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

/**
 * A fast way to create a mock public channel message. Mirrors
 * [network.bisq.mobile.data.replicated.chat.two_party.createMockTwoPartyPrivateChatMessage] and
 * lives in `commonMain` because its consumers span modules: `ChatMessageListUiTest` is in
 * `:shared:presentation` and cannot see this module's `commonTest`.
 *
 * The [senderUserProfile] and [myUserProfile] defaults are *different* profiles, so an unqualified
 * mock is someone else's message; pass the same instance to both for one of mine.
 */
fun createMockCommonPublicChatMessage(
    id: String = createUuid(),
    chatMessageType: ChatMessageTypeEnum = ChatMessageTypeEnum.TEXT,
    text: String? = "Hello",
    citation: Citation? = null,
    citationAuthorUserProfile: UserProfileVO? = null,
    date: Long = 1234567890000L,
    senderUserProfile: UserProfileVO = createMockUserProfile("Alice"),
    myUserProfile: UserProfileVO = createMockUserProfile("Bob"),
    chatReactions: List<CommonPublicChatMessageReaction> = emptyList(),
    wasEdited: Boolean = false,
) = CommonPublicChatMessage(
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
