package network.bisq.mobile.data.replicated.chat.reactions

import kotlinx.serialization.Serializable
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.network.identity.NetworkIdVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id

@Serializable
data class BisqEasyOpenTradeMessageReaction(
    override val id: String,
    override val senderUserProfile: UserProfileVO,
    override val receiverUserProfileId: String,
    override val receiverNetworkId: NetworkIdVO,
    override val chatChannelId: String,
    override val chatChannelDomain: ChatChannelDomainEnum,
    override val chatMessageId: String,
    override val reactionId: Int,
    override val date: Long,
    override val isRemoved: Boolean,
) : PrivateChatMessageReaction {
    // Body property, so the serialized shape is unchanged. Mirrors Bisq 2, which passes
    // senderUserProfile.getId() up as the base class's userProfileId.
    override val userProfileId: String get() = senderUserProfile.id
}
