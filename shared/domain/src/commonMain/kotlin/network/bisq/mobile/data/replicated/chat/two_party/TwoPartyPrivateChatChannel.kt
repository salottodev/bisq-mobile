package network.bisq.mobile.data.replicated.chat.two_party

import network.bisq.mobile.data.replicated.chat.ChatChannel
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO

/**
 * A two-party private chat channel (a DM with one peer), replicating Bisq 2's
 * `TwoPartyPrivateChatChannel`.
 *
 * [peer] and [myUserProfile] are plain profiles rather than a user identity: the UI only ever needs
 * the profile, and it keeps key material out of the shared layer.
 *
 * Both are fixed for the life of the model, and nothing the UI shows about them can move: a Bisq 2
 * nickname is immutable (`UserIdentityService.editUserProfile` only takes terms and statement) and
 * the avatar is derived from the profile's proof of work. The producers still re-resolve the peer
 * through `UserProfileService.getManagedUserProfile` before building a channel, as desktop does —
 * that swaps the copy persisted inside the channel for the one in the network store, refreshing the
 * editable fields (`publishDate`, terms, statement) and falling back to the embedded copy when the
 * profile has been pruned.
 */
class TwoPartyPrivateChatChannel(
    id: String,
    chatChannelDomain: ChatChannelDomainEnum,
    val peer: UserProfileVO,
    val myUserProfile: UserProfileVO,
) : ChatChannel<TwoPartyPrivateChatMessage>(id, chatChannelDomain)
