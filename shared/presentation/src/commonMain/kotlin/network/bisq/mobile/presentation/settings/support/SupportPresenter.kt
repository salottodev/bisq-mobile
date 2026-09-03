package network.bisq.mobile.presentation.settings.support

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import network.bisq.mobile.domain.service.community.CommunityHubService
import network.bisq.mobile.domain.service.community.CommunitySegment
import network.bisq.mobile.domain.utils.DeviceInfoProvider
import network.bisq.mobile.domain.utils.StringUtils.urlEncode
import network.bisq.mobile.domain.utils.VersionProvider
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.common.ui.navigation.NavRoute
import network.bisq.mobile.presentation.common.ui.utils.BisqLinks
import network.bisq.mobile.presentation.main.MainPresenter

class SupportPresenter(
    mainPresenter: MainPresenter,
    private val versionProvider: VersionProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val communityHubService: CommunityHubService,
) : BasePresenter(mainPresenter) {
    // Seeded synchronously from the service's current value (stateIn Eagerly), the way
    // CommunityHubPresenter seeds its own state: this presenter is a Koin factory behind
    // RememberPresenterLifecycle, so the Help screen builds a fresh one every time it enters
    // composition, and a flag that only fills in onViewAttached pops the entry in a frame late on
    // every return from the Support channel.
    private val _isSupportChannelAvailable =
        MutableStateFlow(CommunitySegment.DISCUSSIONS in communityHubService.liveSegments.value)

    /**
     * Whether to offer the in-app Support channel alongside the external ones. Keyed on DISCUSSIONS
     * being live because that segment carries the public chat rollout — the closest thing to a
     * rollout switch the Help screen can read, and the precondition the Community hub's Support row
     * is itself gated behind (that row additionally requires the segment to be *selected*).
     *
     * Not that the ungated entry would be unsafe to tap: a build with no public chat reports
     * `isSupported == false`, and the thread renders that as a terminal "not available" hint. The
     * gate is about the Help screen, which ships on every build — a permanently dead entry there is
     * worse than no entry.
     *
     * This is a proxy, not the source of truth, and it holds only while [CommunityHubService.REQUIRED_FEATURES]
     * carries no DISCUSSIONS entry to make it exact. Register that entry as part of shipping public
     * chat on Connect: without it, flipping `feature.communityHubSegments.client` on offers the
     * channel from every Connect build, including ones whose trusted node cannot serve it — the dead
     * entry this gate exists to prevent. The exact predicate is `PublicChatServiceFacade.isSupported`,
     * which the thread already collects; fold it in here if the two ever have to come apart.
     */
    val isSupportChannelAvailable: StateFlow<Boolean> = _isSupportChannelAvailable.asStateFlow()

    protected val _reportUrl: MutableStateFlow<String> = MutableStateFlow("")
    val reportUrl: StateFlow<String> = _reportUrl.asStateFlow()

    override fun onViewAttached() {
        super.onViewAttached()

        communityHubService.liveSegments
            .onEach { _isSupportChannelAvailable.value = CommunitySegment.DISCUSSIONS in it }
            .launchIn(presenterScope)

        val versionInfo = versionProvider.getVersionInfo(isDemo(), isIOS())
        val deviceInfo = deviceInfoProvider.getDeviceInfo()

        val body = "mobile.support.troubleShooting.github.body".i18n(versionInfo, deviceInfo)
        _reportUrl.value = BisqLinks.BISQ_MOBILE_GH_ISSUES + "/new?body=" + body.urlEncode()
    }

    fun onOpenSupportChannel() {
        navigateTo(NavRoute.SupportChannel)
    }

    fun onRestartApp() {
        restartApp()
    }

    fun onTerminateApp() {
        terminateApp()
    }
}
