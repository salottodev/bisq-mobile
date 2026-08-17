package network.bisq.mobile.data.replicated.chat.reactions

import network.bisq.mobile.data.replicated.network.identity.NetworkIdVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * A reaction on a private chat message, mirroring Bisq 2's
 * `bisq.chat.reactions.PrivateChatMessageReaction` field for field.
 *
 * These four are the ones Bisq 2 adds at this level rather than on [ChatMessageReaction], because a
 * public chat reaction has no sender/receiver envelope and no removed state. As with the base
 * interface, shared code does not read them polymorphically today — the service facades work with
 * the concrete types — so they exist to keep the hierarchy faithful.
 */
interface PrivateChatMessageReaction : ChatMessageReaction {
    val senderUserProfile: UserProfileVO

    val receiverUserProfileId: String

    val receiverNetworkId: NetworkIdVO

    val isRemoved: Boolean
}
