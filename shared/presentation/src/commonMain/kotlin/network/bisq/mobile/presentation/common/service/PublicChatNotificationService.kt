package network.bisq.mobile.presentation.common.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import network.bisq.mobile.data.model.CommunityNotificationLevel
import network.bisq.mobile.data.model.notificationLevelFor
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.common.CommonPublicChatChannel
import network.bisq.mobile.data.replicated.chat.mentionsOrCites
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.service.ForegroundDetector
import network.bisq.mobile.data.service.chat.public_chat.PublicChatServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.repository.SettingsRepository
import network.bisq.mobile.domain.service.community.CommunitySegment
import network.bisq.mobile.domain.utils.Logging
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.notification.NotificationChannels
import network.bisq.mobile.presentation.common.notification.NotificationController
import network.bisq.mobile.presentation.common.notification.NotificationIds
import network.bisq.mobile.presentation.common.notification.NotificationRedactions
import network.bisq.mobile.presentation.common.notification.model.NotificationPressAction
import network.bisq.mobile.presentation.common.notification.model.android.AndroidNotificationCategory
import network.bisq.mobile.presentation.common.ui.navigation.NavRoute

/**
 * Community notifications for the PUBLIC channels (Discussions and Support), each governed by its own
 * [CommunityNotificationLevel] preference. Structure mirrors [PrivateChatNotificationService]
 * deliberately — unread-count deltas over a seen-baseline, so the channels' replayed history (10-day
 * P2P TTL) never storms on a cold start: a burst is at most one notification per channel.
 *
 * This service only READS the channel flows. The hub badge pipeline (channel unread →
 * CommunityUnreadCountAggregator → CommunityHubService) has no writer here and cannot be disturbed.
 *
 * MENTIONS_AND_REPLIES classifies the burst's newest messages with the shared desktop-semantics
 * predicate [mentionsOrCites]; OFF skips that channel, and observers are never armed while both
 * channels are OFF.
 */
@OptIn(FlowPreview::class)
class PublicChatNotificationService(
    private val notificationController: NotificationController,
    private val publicChatServiceFacade: PublicChatServiceFacade,
    private val userProfileServiceFacade: UserProfileServiceFacade,
    private val settingsRepository: SettingsRepository,
    private val appForegroundController: ForegroundDetector,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Logging {
    private companion object {
        const val FOREGROUND_DEBOUNCE_MS = 1000L
        val PUBLIC_DOMAINS = listOf(ChatChannelDomainEnum.DISCUSSION, ChatChannelDomainEnum.SUPPORT)
    }

    // Same isolation rationale as PrivateChatNotificationService: never routed through
    // ForegroundServiceController, whose process-wide unregisterObservers() the trade service calls.
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private var lifecycleObserverJob: Job? = null
    private var levelObserverJob: Job? = null

    // Read and written only under [jobMutex]; see the private sibling for why @Volatile is not enough.
    private var observerJob: Job? = null

    private val unreadCountByChannelId = mutableMapOf<String, Long>()
    private val stateMutex = Mutex()
    private val jobMutex = Mutex()

    @kotlin.concurrent.Volatile
    private var isLocalDeliverySuppressed = false

    // Empty until the first settings emission, which reads as OFF for every channel.
    @kotlin.concurrent.Volatile
    private var levelByDomain: Map<ChatChannelDomainEnum, CommunityNotificationLevel> = emptyMap()

    @kotlin.concurrent.Volatile
    private var isForegroundNow = true

    fun startService() {
        setupLifecycleObserver()
        setupLevelObserver()
    }

    fun setLocalDeliverySuppressed(suppressed: Boolean) {
        if (isLocalDeliverySuppressed == suppressed) return
        isLocalDeliverySuppressed = suppressed
        if (suppressed) {
            log.i { "Suppressing local community notifications — unregistering observers" }
            scope.launch { unregisterObservers() }
        }
    }

    suspend fun stopNotificationService() {
        log.d { "Stopping PublicChatNotificationService." }
        lifecycleObserverJob?.cancelAndJoin()
        lifecycleObserverJob = null
        levelObserverJob?.cancelAndJoin()
        levelObserverJob = null
        unregisterObservers()
        stateMutex.withLock { unreadCountByChannelId.clear() }
    }

    private fun setupLifecycleObserver() {
        if (lifecycleObserverJob?.isActive == true) return

        lifecycleObserverJob =
            appForegroundController.isForeground
                .onEach { isForeground ->
                    isForegroundNow = isForeground
                    // Seen-snapshot ahead of the debounce, like the sibling: a message landing inside
                    // the window must still notify.
                    if (!isForeground && deliveryArmed()) markCurrentCountsAsSeen()
                }.debounce(FOREGROUND_DEBOUNCE_MS)
                .distinctUntilChanged()
                .onEach { isForeground ->
                    if (isForeground) {
                        unregisterObservers()
                        markCurrentCountsAsSeen()
                    } else if (deliveryArmed()) {
                        registerObservers()
                    }
                }.launchIn(scope)
    }

    /**
     * The preferences are live: both channels OFF disarms observers that are already running, and
     * turning either channel on while backgrounded arms them — no app restart needed for a setting
     * to take effect. Only arming re-baselines: live collectors already keep every channel's
     * baseline, and a fresh snapshot would record an increase they have not processed yet as seen.
     */
    private fun setupLevelObserver() {
        if (levelObserverJob?.isActive == true) return

        levelObserverJob =
            settingsRepository.data
                .map { settings -> PUBLIC_DOMAINS.associateWith { settings.notificationLevelFor(it) } }
                .distinctUntilChanged()
                .onEach { levels ->
                    levelByDomain = levels
                    if (!deliveryArmed()) {
                        unregisterObservers()
                    } else if (!isForegroundNow) {
                        registerObservers(rebaseline = true)
                    }
                }.launchIn(scope)
    }

    private fun deliveryArmed(): Boolean = !isLocalDeliverySuppressed && levelByDomain.values.any { it != CommunityNotificationLevel.OFF }

    private fun levelFor(channel: CommonPublicChatChannel): CommunityNotificationLevel = levelByDomain[channel.chatChannelDomain] ?: CommunityNotificationLevel.OFF

    private suspend fun registerObservers(rebaseline: Boolean = false) {
        jobMutex.withLock {
            if (!deliveryArmed()) return@withLock
            if (observerJob?.isActive == true) return@withLock
            if (rebaseline) markCurrentCountsAsSeen()
            observerJob =
                scope.launch {
                    publicChatServiceFacade.channels.collectLatest { channels ->
                        coroutineScope {
                            channels.forEach { channel ->
                                launch {
                                    channel.unreadCount.collect { unreadCount ->
                                        onUnreadCountChanged(channel, unreadCount)
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private suspend fun markCurrentCountsAsSeen() {
        val current = publicChatServiceFacade.channels.value.associate { it.id to it.unreadCount.value }
        stateMutex.withLock {
            unreadCountByChannelId.clear()
            unreadCountByChannelId.putAll(current)
        }
    }

    private suspend fun unregisterObservers() {
        jobMutex.withLock {
            observerJob?.cancel()
            observerJob = null
        }
    }

    private suspend fun onUnreadCountChanged(
        channel: CommonPublicChatChannel,
        unreadCount: Long,
    ) {
        val previous =
            stateMutex.withLock {
                val previous = unreadCountByChannelId[channel.id] ?: 0L
                unreadCountByChannelId[channel.id] = unreadCount
                previous
            }
        if (unreadCount <= previous) return
        val level = levelFor(channel)
        if (level == CommunityNotificationLevel.OFF) return
        if (!burstQualifies(channel, level, (unreadCount - previous).toInt())) return

        val isSupport = channel.chatChannelDomain == ChatChannelDomainEnum.SUPPORT
        val channelName =
            if (isSupport) "mobile.communityNotifications.channel.support".i18n() else "mobile.communityNotifications.channel.discussions".i18n()
        val route =
            if (isSupport) {
                NavRoute.SupportChannel
            } else {
                NavRoute.CommunityHub(initialSegment = CommunitySegment.DISCUSSIONS.name)
            }
        notificationController.notify {
            this.id = NotificationIds.getNewPublicChatMessageId(channel.id)
            this.title = "mobile.communityNotifications.newMessage.title".i18n(channelName)
            this.body = "mobile.communityNotifications.newMessage.message".i18n(channelName)
            android {
                channelId = NotificationChannels.USER_MESSAGES
                category = AndroidNotificationCategory.CATEGORY_MESSAGE
                lockScreen = NotificationRedactions.chatMessage()
                pressAction = NotificationPressAction.Route(route)
                group = NotificationIds.getNewPublicChatMessageId(channel.id)
            }
            ios {
                categoryId = NotificationRedactions.CHAT_MESSAGE_CATEGORY
                pressAction = NotificationPressAction.Route(route)
            }
        }
    }

    /**
     * ALL passes every burst. MENTIONS_AND_REPLIES inspects the burst's newest [delta] messages with
     * the shared desktop-semantics predicate — a mention of any of my profiles or a citation of one
     * of my messages (checked against ALL identity ids, like desktop checks all identities).
     */
    private suspend fun burstQualifies(
        channel: CommonPublicChatChannel,
        level: CommunityNotificationLevel,
        delta: Int,
    ): Boolean {
        if (level == CommunityNotificationLevel.ALL) return true

        // All owned profiles, like desktop checks all identities — a mention of a non-selected
        // profile's userName must still qualify. Empty only before the first profile load, where
        // the selected profile is the best (and only) approximation available.
        val myProfiles =
            userProfileServiceFacade.userProfiles.value
                .ifEmpty { listOfNotNull(userProfileServiceFacade.selectedUserProfile.value) }
        val myIdentityIds =
            try {
                userProfileServiceFacade.getUserIdentityIds().toSet()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptySet()
            }
        return channel.chatMessages.value
            .sortedByDescending { it.date }
            .take(delta.coerceAtLeast(1))
            .any { message ->
                !message.isMyMessage &&
                    (message.mentionsOrCites(myProfiles) || message.citation?.authorUserProfileId in myIdentityIds)
            }
    }
}
