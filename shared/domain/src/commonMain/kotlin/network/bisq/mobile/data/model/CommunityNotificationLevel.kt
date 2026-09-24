package network.bisq.mobile.data.model

import kotlinx.serialization.Serializable
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum

/**
 * How much activity of a public channel (Discussions or Support) the app delivers as notifications,
 * set per channel (#1877) and resolved with [notificationLevelFor]. Mirrors the semantics of bisq2
 * desktop's ChatChannelNotificationType global default (ALL / MENTION / OFF) — MENTIONS_AND_REPLIES
 * is desktop's MENTION, which treats a citation of one of my messages like a mention. App-local
 * (DataStore), never synced to the node: desktop keeps its own. Defaults to ALL, like desktop.
 * Besides notifications the Discussions level also filters its contribution to the Community badge
 * (`CommunityUnreadCountAggregator`), again matching desktop, whose nav badges count notifications.
 */
@Serializable
enum class CommunityNotificationLevel {
    ALL,
    MENTIONS_AND_REPLIES,
    OFF,
}

/**
 * The level of a public channel: its own level, or the legacy [Settings.communityNotificationLevel]
 * when it has none. Only Discussions and Support have a community level; the other domains notify
 * through their own paths (trades, private chats), so they give [CommunityNotificationLevel.OFF].
 */
fun Settings.notificationLevelFor(domain: ChatChannelDomainEnum): CommunityNotificationLevel =
    when (domain) {
        ChatChannelDomainEnum.DISCUSSION -> discussionsNotificationLevel ?: communityNotificationLevel
        ChatChannelDomainEnum.SUPPORT -> supportNotificationLevel ?: communityNotificationLevel
        else -> CommunityNotificationLevel.OFF
    }

/** Sets the own level of a public channel; the legacy level is never written. */
fun Settings.withNotificationLevel(
    domain: ChatChannelDomainEnum,
    level: CommunityNotificationLevel,
): Settings =
    when (domain) {
        ChatChannelDomainEnum.DISCUSSION -> copy(discussionsNotificationLevel = level)
        ChatChannelDomainEnum.SUPPORT -> copy(supportNotificationLevel = level)
        else -> throw IllegalArgumentException("$domain has no community notification level")
    }
