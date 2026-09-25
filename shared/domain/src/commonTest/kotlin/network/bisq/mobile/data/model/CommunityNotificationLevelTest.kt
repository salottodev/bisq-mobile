package network.bisq.mobile.data.model

import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CommunityNotificationLevelTest {
    @Test
    fun `default settings give ALL on both channels`() {
        val settings = Settings()

        assertEquals(CommunityNotificationLevel.ALL, settings.notificationLevelFor(ChatChannelDomainEnum.DISCUSSION))
        assertEquals(CommunityNotificationLevel.ALL, settings.notificationLevelFor(ChatChannelDomainEnum.SUPPORT))
    }

    @Test
    fun `a channel's own level wins over the legacy level`() {
        val settings =
            Settings(
                communityNotificationLevel = CommunityNotificationLevel.OFF,
                discussionsNotificationLevel = CommunityNotificationLevel.MENTIONS_AND_REPLIES,
                supportNotificationLevel = CommunityNotificationLevel.ALL,
            )

        assertEquals(CommunityNotificationLevel.MENTIONS_AND_REPLIES, settings.notificationLevelFor(ChatChannelDomainEnum.DISCUSSION))
        assertEquals(CommunityNotificationLevel.ALL, settings.notificationLevelFor(ChatChannelDomainEnum.SUPPORT))
    }

    @Test
    fun `a channel without its own level falls back to the legacy level`() {
        val settings =
            Settings(
                communityNotificationLevel = CommunityNotificationLevel.OFF,
                discussionsNotificationLevel = CommunityNotificationLevel.ALL,
            )

        assertEquals(CommunityNotificationLevel.ALL, settings.notificationLevelFor(ChatChannelDomainEnum.DISCUSSION))
        assertEquals(CommunityNotificationLevel.OFF, settings.notificationLevelFor(ChatChannelDomainEnum.SUPPORT))
    }

    @Test
    fun `domains other than Discussions and Support give OFF`() {
        val settings = Settings(communityNotificationLevel = CommunityNotificationLevel.ALL)

        ChatChannelDomainEnum.entries
            .filter { it != ChatChannelDomainEnum.DISCUSSION && it != ChatChannelDomainEnum.SUPPORT }
            .forEach { domain ->
                assertEquals(CommunityNotificationLevel.OFF, settings.notificationLevelFor(domain), "domain $domain")
            }
    }

    @Test
    fun `withNotificationLevel sets only the given channel`() {
        val settings = Settings().withNotificationLevel(ChatChannelDomainEnum.SUPPORT, CommunityNotificationLevel.OFF)

        assertEquals(CommunityNotificationLevel.OFF, settings.supportNotificationLevel)
        assertNull(settings.discussionsNotificationLevel)
        assertEquals(CommunityNotificationLevel.ALL, settings.communityNotificationLevel)
    }

    @Test
    fun `withNotificationLevel rejects domains without a community level`() {
        assertFailsWith<IllegalArgumentException> {
            Settings().withNotificationLevel(ChatChannelDomainEnum.BISQ_EASY_OFFERBOOK, CommunityNotificationLevel.OFF)
        }
    }
}
