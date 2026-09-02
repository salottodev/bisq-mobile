package network.bisq.mobile.node.common.domain.mapping.chat

import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.node.common.domain.mapping.Mappings
import bisq.chat.common.CommonPublicChatChannel as Bisq2CommonPublicChatChannel

/**
 * [channelTitle] is the raw title mobile builds `"<domain>.<channelTitle>.title"` from and resolves
 * against its own bundle, so the node's bisq2 `Res` locale never decides what the UI shows — which
 * is what rules out `getDisplayString()`.
 *
 * It comes from the tail of the id, not from `getChannelTitle()`. bisq2 migrates the id and the
 * domain but leaves that field raw, so a store written before the v2.1.1 consolidation would pair a
 * migrated `DISCUSSION` with a title like `conferences`, whose strings are filed under `events.` —
 * a key no bundle has. A migrated id only ever ends in `bisq` or `support`, the two titles still
 * served. The client twin reads the id off the DTO for the same reason.
 */
fun Bisq2CommonPublicChatChannel.toDomain(): CommonPublicChatChannel =
    CommonPublicChatChannel(
        // Read through the accessors, never the raw fields: both migrate a legacy channel onto the
        // one channel its domain still serves.
        id = id,
        chatChannelDomain = Mappings.ChatChannelDomainMapping.fromBisq2Model(chatChannelDomain),
        channelTitle = id.substringAfterLast('.'),
    )
