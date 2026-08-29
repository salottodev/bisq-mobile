package network.bisq.mobile.data.replicated.chat.common

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.pub.PublicChatChannel

/**
 * The discussion or support channel, mirroring Bisq 2's `bisq.chat.common.CommonPublicChatChannel`.
 * [id] is `<domain>.<title>` (`discussion.bisq`, `support.support`), the one channel per domain
 * that upstream still serves. The admin and moderator ids are left out: nothing in the UI reads them.
 */
class CommonPublicChatChannel(
    id: String,
    chatChannelDomain: ChatChannelDomainEnum,
    val channelTitle: String,
) : PublicChatChannel<CommonPublicChatMessage>(id, chatChannelDomain)
