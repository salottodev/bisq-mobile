package network.bisq.mobile.test.fixtures

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import network.bisq.mobile.domain.service.capabilities.BackendCapabilities
import network.bisq.mobile.domain.service.capabilities.BackendCapabilitiesService
import network.bisq.mobile.domain.service.capabilities.Feature
import network.bisq.mobile.domain.service.community.CommunityHubService
import network.bisq.mobile.domain.service.community.CommunitySegment

/**
 * A real [CommunityHubService] on a test dispatcher — the service is cheap and its whole point is
 * the `enabled ∩ capabilities` arithmetic, so tests exercise it rather than mock it.
 *
 * Every argument is defaulted to the inert value, which is what makes this worth sharing: a caller
 * names only the axis it is about, and a new constructor parameter lands in one place instead of
 * every test that happens to need a hub service.
 *
 * @param capabilities pass a `MutableStateFlow` to move the backend manifest mid-test; the default
 *   is the fail-closed one, so any segment that does declare a requirement starts withheld.
 * @param requiredFeatures defaults to production's own [CommunityHubService.REQUIRED_FEATURES], so
 *   a test that wants no capability gate has to say `requiredFeatures = emptyMap()` and mean it. The
 *   other way round, a test asserting a segment is live would keep passing on the day that segment
 *   starts requiring a backend feature production would gate it on.
 * @param dispatcher pass `UnconfinedTestDispatcher(testScheduler)` from a `runTest` block so
 *   `advanceUntilIdle()` drives the service's own `stateIn` scope. The default keeps that scope off
 *   `Dispatchers.Default` for tests that have no scheduler to share.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun testCommunityHubService(
    enabled: Set<CommunitySegment> = emptySet(),
    requiredFeatures: Map<CommunitySegment, Feature> = CommunityHubService.REQUIRED_FEATURES,
    capabilities: StateFlow<BackendCapabilities> = MutableStateFlow(BackendCapabilities.UNAVAILABLE),
    dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
): CommunityHubService =
    CommunityHubService(
        backendCapabilitiesService =
            object : BackendCapabilitiesService {
                override val capabilities: StateFlow<BackendCapabilities> = capabilities
            },
        enabledSegments = enabled,
        requiredFeatures = requiredFeatures,
        dispatcher = dispatcher,
    )
