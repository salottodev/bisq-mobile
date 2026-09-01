package network.bisq.mobile.domain.service.community

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.data.service.chat.public_chat.PublicChatServiceFacade
import kotlin.concurrent.Volatile

/**
 * Feeds the Community hub's entry-point badge from the public chat channels — the producer
 * [CommunityHubService.unreadCount] has been waiting for since #1743 shipped the slot.
 *
 * Two rules here are load-bearing, not defensive:
 *  - **Support is excluded.** The facade serves both domains, and the hub's aggregate is a strict
 *    Discussions + Messages sum by design (#1746).
 *  - **Gated on the segment being live.** The hub's icon appears whenever *any* segment is live and
 *    Contacts already ships on the node, so without this a release build would badge the icon for a
 *    segment the user cannot open.
 *
 * Domain rather than presentation because both collaborators are domain and it touches no UI; and it
 * has to outlive the segment, since the badge shows on every main tab while the hub presenter is a
 * factory bound to a mounted tab. [CommunityHubService.setUnreadCount] stays the single write seam,
 * so Messages later adds its addend here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CommunityUnreadCountAggregator(
    private val publicChatServiceFacade: PublicChatServiceFacade,
    private val communityHubService: CommunityHubService,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    @Volatile
    private var job: Job? = null

    /**
     * Idempotent, because activation is not a once-per-process call: the lifecycle restart paths
     * (a Tor bootstrap retry, for one) deactivate then activate the same singleton, and a second
     * collector on the same flows would outlive every one of them.
     */
    fun start() {
        if (job?.isActive == true) {
            return
        }
        job =
            scope.launch {
                combine(
                    communityHubService.liveSegments,
                    publicChatServiceFacade.channels.flatMapLatest { discussionUnreadCount(it) },
                ) { liveSegments, unreadCount ->
                    if (CommunitySegment.DISCUSSIONS in liveSegments) unreadCount else 0L
                }.collect { unreadCount ->
                    // The channel counts are Longs and the badge is an Int; an unchecked toInt() wraps negative.
                    communityHubService.setUnreadCount(unreadCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                }
            }
    }

    /**
     * Clears the badge as it stops: with no producer left, a stale count would sit there.
     *
     * Joins rather than only cancelling, because the collector runs on its own dispatcher while this
     * is called from the lifecycle's: an emission already inside [CommunityHubService.setUnreadCount]
     * would otherwise land after the clear and freeze the badge on exactly the stale count.
     */
    suspend fun stop() {
        job?.cancelAndJoin()
        job = null
        communityHubService.setUnreadCount(0)
    }

    private fun discussionUnreadCount(channels: List<CommonPublicChatChannel>): Flow<Long> {
        val discussions = channels.filter { it.chatChannelDomain == ChatChannelDomainEnum.DISCUSSION }
        if (discussions.isEmpty()) {
            return flowOf(0L)
        }
        return combine(discussions.map { it.unreadCount }) { counts -> counts.sum() }
    }
}
