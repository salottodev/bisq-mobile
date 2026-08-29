package network.bisq.mobile.data.replicated.chat.pub

import network.bisq.mobile.data.replicated.chat.ChatChannel
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum

/**
 * A public channel, mirroring Bisq 2's `bisq.chat.pub.PublicChatChannel<M>`. Adds nothing to
 * [ChatChannel] that mobile reads — upstream's additions are the P2P-store bookkeeping — and exists
 * for the same reason [PublicChatMessage] does: to keep the hierarchy readable next to Bisq 2's.
 */
abstract class PublicChatChannel<M : PublicChatMessage<*>>(
    id: String,
    chatChannelDomain: ChatChannelDomainEnum,
) : ChatChannel<M>(id, chatChannelDomain)
