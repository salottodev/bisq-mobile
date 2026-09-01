package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.common.CommonPublicChatChannel as Bisq2CommonPublicChatChannel

/**
 * [channelTitle] is the raw title (`bisq`, `support`, or a legacy `bitcoin` on a store upgraded from
 * before v2.1.1), not bisq2's `getDisplayString()`. The composable resolves
 * `"<domain>.<channelTitle>.title"` from mobile's own bundle, so the node's bisq2 `Res` locale never
 * decides what the UI shows.
 */
fun Bisq2CommonPublicChatChannel.toDomain(): CommonPublicChatChannel =
    CommonPublicChatChannel(
        // Read through the accessors, never the raw fields: both migrate a legacy channel onto the
        // one channel its domain still serves.
        id = id,
        chatChannelDomain = Mappings.ChatChannelDomainMapping.fromBisq2Model(chatChannelDomain),
        channelTitle = channelTitle,
    )
