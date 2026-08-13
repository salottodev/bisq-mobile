package network.bisq.mobile.data.service.reputation

import kotlinx.coroutines.flow.StateFlow
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.LifeCycleAware

interface ReputationServiceFacade : LifeCycleAware {
    /**
     * The locally known scores. Starts empty on both flavours and fills asynchronously — over the
     * `REPUTATION` websocket topic on the client, from Bisq2's `ReputationService` on the node — so
     * it is a flow rather than a snapshot: read once, it cannot tell "nothing has arrived yet" apart
     * from "this peer has no reputation", and a screen holding that verdict would never revise it.
     *
     * Scores rather than full [ReputationScoreVO]s because that is what both flavours observe
     * underneath. The client's websocket payload is richer only because the trusted node's API
     * converts every entry through `ReputationService.getReputationScore`, which sorts all scores per
     * entry — work our node has no reason to repeat on the phone. Use [getReputation] for a single
     * peer's full score; it is cheap on both flavours.
     */
    val scoreByUserProfileId: StateFlow<Map<String, Long>>

    suspend fun getReputation(userProfileId: String): Result<ReputationScoreVO>

    /**
     * Get the profile age (creation timestamp) for a user profile
     * @param userProfileId The user profile ID
     * @return The profile creation timestamp in milliseconds, or null if not available
     */
    suspend fun getProfileAge(userProfileId: String): Result<Long?>
}
