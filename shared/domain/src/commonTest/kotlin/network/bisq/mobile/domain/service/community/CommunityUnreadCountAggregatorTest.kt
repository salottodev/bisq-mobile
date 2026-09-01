package network.bisq.mobile.domain.service.community

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.Citation
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.data.replicated.chat.reactions.CommonPublicChatMessageReaction
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.service.chat.public_chat.PublicChatServiceFacade
import network.bisq.mobile.domain.service.capabilities.BackendCapabilities
import network.bisq.mobile.domain.service.capabilities.BackendCapabilitiesService
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The #1743 ↔ #1744 handshake: the hub's badge slot has had no producer until now.
 *
 * Two rules are load-bearing rather than defensive. The facade really does serve Support, and #1746
 * requires the aggregate to exclude it; and the hub's entry icon appears whenever *any* segment is
 * live, so without the liveness gate a build shipping only Contacts would badge the icon for a
 * segment the user cannot open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CommunityUnreadCountAggregatorTest {
    @Test
    fun `the discussions unread count reaches the hub`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val hub = startAggregator(listOf(discussion))

            discussion.setUnreadCount(7)

            assertEquals(7, hub.unreadCount.value)
        }

    @Test
    fun `the support unread count does not`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val support = channel(ChatChannelDomainEnum.SUPPORT)
            val hub = startAggregator(listOf(discussion, support))

            support.setUnreadCount(4)

            assertEquals(0, hub.unreadCount.value)
        }

    @Test
    fun `nothing is published while discussions is not live`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val hub = startAggregator(listOf(discussion), liveSegments = setOf(CommunitySegment.CONTACTS))

            discussion.setUnreadCount(7)

            assertEquals(0, hub.unreadCount.value)
        }

    @Test
    fun `a later count change is republished`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val hub = startAggregator(listOf(discussion))

            discussion.setUnreadCount(7)
            discussion.setUnreadCount(2)

            assertEquals(2, hub.unreadCount.value)
        }

    /** `ChatChannel.unreadCount` is a Long and `setUnreadCount` takes an Int; `toInt()` alone wraps negative. */
    @Test
    fun `a count above the Int range clamps instead of wrapping`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val hub = startAggregator(listOf(discussion))

            discussion.setUnreadCount(Int.MAX_VALUE.toLong() + 1)

            assertEquals(Int.MAX_VALUE, hub.unreadCount.value)
        }

    /**
     * `activateServiceFacades()` is not a once-per-process call — a Tor bootstrap retry deactivates
     * and activates the same singleton — so a second `start()` must not add a second collector.
     */
    @Test
    fun `starting twice collects once`() =
        runTest {
            val facade = FakePublicChatServiceFacade(listOf(channel(ChatChannelDomainEnum.DISCUSSION)))
            val aggregator = aggregator(hubService(), facade)

            aggregator.start()
            aggregator.start()

            assertEquals(1, facade.channels.subscriptionCount.value)
        }

    @Test
    fun `stopping releases the collector and clears the badge`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val facade = FakePublicChatServiceFacade(listOf(discussion))
            val hub = hubService()
            val aggregator = aggregator(hub, facade)
            aggregator.start()
            discussion.setUnreadCount(7)

            aggregator.stop()

            assertEquals(0, facade.channels.subscriptionCount.value)
            assertEquals(0, hub.unreadCount.value)
        }

    @Test
    fun `starting again after a stop republishes`() =
        runTest {
            val discussion = channel(ChatChannelDomainEnum.DISCUSSION)
            val facade = FakePublicChatServiceFacade(listOf(discussion))
            val hub = hubService()
            val aggregator = aggregator(hub, facade)
            aggregator.start()
            aggregator.stop()

            aggregator.start()
            discussion.setUnreadCount(3)

            assertEquals(3, hub.unreadCount.value)
        }

    private fun channel(domain: ChatChannelDomainEnum) =
        CommonPublicChatChannel(
            id = "${domain.name.lowercase()}.channel",
            chatChannelDomain = domain,
            channelTitle = "title",
        )

    private fun TestScope.startAggregator(
        channels: List<CommonPublicChatChannel>,
        liveSegments: Set<CommunitySegment> = setOf(CommunitySegment.DISCUSSIONS),
    ): CommunityHubService {
        val hub = hubService(liveSegments)
        aggregator(hub, FakePublicChatServiceFacade(channels)).start()
        return hub
    }

    private fun TestScope.hubService(liveSegments: Set<CommunitySegment> = setOf(CommunitySegment.DISCUSSIONS)) =
        CommunityHubService(
            backendCapabilitiesService = NoCapabilities,
            shippedSegments = liveSegments,
            requiredFeatures = emptyMap(),
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

    private fun TestScope.aggregator(
        hub: CommunityHubService,
        facade: PublicChatServiceFacade,
    ) = CommunityUnreadCountAggregator(
        publicChatServiceFacade = facade,
        communityHubService = hub,
        dispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    private object NoCapabilities : BackendCapabilitiesService {
        override val capabilities: StateFlow<BackendCapabilities> = MutableStateFlow(BackendCapabilities.UNAVAILABLE)
    }

    /** Only [channels] is read; the mutations exist because the interface has them. */
    private class FakePublicChatServiceFacade(
        channels: List<CommonPublicChatChannel>,
    ) : PublicChatServiceFacade {
        override val isSupported: Flow<Boolean> = flowOf(true)

        /** Mutable so a test can read `subscriptionCount`, which is how a stacked collector shows up. */
        override val channels: MutableStateFlow<List<CommonPublicChatChannel>> = MutableStateFlow(channels)

        override suspend fun sendChatMessage(
            channelId: String,
            text: String,
            citation: Citation?,
        ) = Result.success(Unit)

        override suspend fun editChatMessage(
            channelId: String,
            messageId: String,
            text: String,
        ) = Result.success(Unit)

        override suspend fun deleteChatMessage(
            channelId: String,
            messageId: String,
        ) = Result.success(Unit)

        override suspend fun addChatMessageReaction(
            channelId: String,
            messageId: String,
            reactionEnum: ReactionEnum,
        ) = Result.success(Unit)

        override suspend fun removeChatMessageReaction(
            channelId: String,
            messageId: String,
            reaction: CommonPublicChatMessageReaction,
        ) = Result.success(Unit)

        override suspend fun consumeNotifications(channelId: String) = Unit
    }
}
