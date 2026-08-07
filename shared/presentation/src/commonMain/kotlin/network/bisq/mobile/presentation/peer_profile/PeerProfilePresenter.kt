package network.bisq.mobile.presentation.peer_profile

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.reputation.ReputationServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.data.utils.PlatformImage
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.common.ui.components.organisms.SnackbarType
import network.bisq.mobile.presentation.main.MainPresenter

class PeerProfilePresenter(
    private val userProfileServiceFacade: UserProfileServiceFacade,
    private val reputationServiceFacade: ReputationServiceFacade,
    mainPresenter: MainPresenter,
) : BasePresenter(mainPresenter) {
    private companion object {
        /**
         * `ClientReputationServiceFacade.getReputation` returns a failure — not a zero score — for
         * any peer with no reputation yet, and only in release builds. Surfacing that as an error
         * would break the screen for exactly the peers a user most wants to inspect, so it is
         * mapped to this instead. See [loadReputation] for when a failure means zero and when it
         * means "not known yet".
         */
        val ZERO_REPUTATION = ReputationScoreVO(totalScore = 0L, fiveSystemScore = 0.0, ranking = 0)
    }

    private val _uiState = MutableStateFlow(PeerProfileUiState())
    val uiState: StateFlow<PeerProfileUiState> = _uiState.asStateFlow()

    private val _isIgnoreActionEnabled = MutableStateFlow(true)
    val isIgnoreActionEnabled: StateFlow<Boolean> = _isIgnoreActionEnabled.asStateFlow()

    val userProfileIconProvider: suspend (UserProfileVO) -> PlatformImage
        get() = userProfileServiceFacade::getUserProfileIcon

    private var initializedProfileId: String? = null
    private var ignoredStateJob: Job? = null
    private var loadProfileJob: Job? = null

    /**
     * Idempotent: the screen's `LaunchedEffect` re-fires whenever this destination is revealed from
     * the back stack, and reloading then would flash the loading state over already-correct data.
     * Keyed on the id rather than a boolean so navigating to a different peer still reloads — which
     * is why the state is replaced wholesale and the previous ignored-state collector is cancelled
     * rather than left running alongside a second one.
     */
    fun initialize(profileId: String) {
        if (initializedProfileId == profileId) return
        initializedProfileId = profileId
        _uiState.value = PeerProfileUiState(profileId = profileId)
        loadProfile(profileId)
        observeIgnoredState(profileId)
    }

    fun onAction(action: PeerProfileUiAction) {
        when (action) {
            PeerProfileUiAction.OnRetryLoadClick -> onRetryLoad()

            PeerProfileUiAction.OnIgnoreClick ->
                _uiState.update { it.copy(showIgnoreConfirmDialog = true) }

            PeerProfileUiAction.OnConfirmIgnore -> onConfirmIgnore()

            PeerProfileUiAction.OnDismissIgnoreDialog ->
                _uiState.update { it.copy(showIgnoreConfirmDialog = false) }

            PeerProfileUiAction.OnUndoIgnoreClick -> onUndoIgnore()

            PeerProfileUiAction.OnReportClick ->
                _uiState.update { it.copy(showReportDialog = true) }

            PeerProfileUiAction.OnDismissReportDialog ->
                _uiState.update { it.copy(showReportDialog = false, reportDraft = null) }

            is PeerProfileUiAction.OnReportFailure -> onReportFailure(action.message, action.reportMessage)
        }
    }

    private fun onRetryLoad() {
        val profileId = _uiState.value.profileId
        if (profileId.isEmpty()) return
        _uiState.update { it.copy(isLoading = true, isLoadFailed = false) }
        loadProfile(profileId)
    }

    /**
     * Only one load may be in flight. Retrying supersedes the previous attempt rather than racing
     * it: `findUserProfile` is a node round-trip on the client flavour, so a slow first attempt can
     * otherwise land *after* a fast retry and replace a rendered profile with its own failure.
     * Cancelling is not enough on its own — a coroutine already past its last suspension point runs
     * to completion — so every write also goes through [updateIfCurrent].
     */
    private fun loadProfile(profileId: String) {
        loadProfileJob?.cancel()
        loadProfileJob =
            presenterScope.launch {
                try {
                    if (isOwnProfile(profileId)) {
                        updateIfCurrent(profileId) { it.copy(isOwnProfile = true, isLoading = false) }
                        return@launch
                    }

                    val userProfile = userProfileServiceFacade.findUserProfile(profileId)
                    if (userProfile == null) {
                        updateIfCurrent(profileId) { it.copy(isNotFound = true, isLoading = false) }
                        return@launch
                    }

                    val reputation = loadReputation(profileId)

                    updateIfCurrent(profileId) {
                        it.copy(
                            userProfile = userProfile,
                            displayName = userProfile.userName,
                            starRating = reputation?.fiveSystemScore ?: 0.0,
                            reputationScore = reputation?.totalScore ?: 0L,
                            isReputationUnknown = reputation == null,
                            isLoading = false,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Not `isNotFound`: the lookup crossing the network means this is just as likely a
                    // connection problem, and telling the user their peer does not exist would be wrong.
                    log.e(e) { "Failed to load peer profile $profileId" }
                    updateIfCurrent(profileId) { it.copy(isLoadFailed = true, isLoading = false) }
                }
            }
    }

    /**
     * Drops a write whose load has been superseded. Keyed on the requested id because [initialize]
     * replaces the state — and with it `profileId` — synchronously before launching, so the state
     * already identifies the load that owns it by the time any result arrives.
     */
    private fun updateIfCurrent(
        profileId: String,
        transform: (PeerProfileUiState) -> PeerProfileUiState,
    ) {
        _uiState.update { if (it.profileId == profileId) transform(it) else it }
    }

    /**
     * "Own" means any of my identities, not just the selected one — multiple profiles are supported
     * and this screen must never render for any of them.
     *
     * Checks the already-loaded owned-profiles flow first (no network round-trip, and its ids are
     * `UserProfileVO.id`, exactly what callers navigate with). Falls back to the identity-ids call
     * only when that flow hasn't been warmed yet, e.g. right after startup.
     */
    private suspend fun isOwnProfile(profileId: String): Boolean {
        val ownProfiles = userProfileServiceFacade.userProfiles.value
        if (ownProfiles.isNotEmpty()) {
            return ownProfiles.any { it.id == profileId }
        }
        return try {
            userProfileServiceFacade.getUserIdentityIds().contains(profileId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.w(e) { "Failed to read identity ids while checking own profile" }
            false
        }
    }

    /**
     * Returns null when the score is not known yet, as opposed to known-and-zero.
     *
     * The two failure shapes are not interchangeable. The client facade returns `Result.failure` for
     * an unknown peer in release builds, while in debug it calls the API and can throw instead.
     *
     * A *completed* failure is ambiguous but recoverable: `ClientReputationServiceFacade.getReputation`
     * reads a local snapshot filled asynchronously by `subscribeUserReputation()`, so "absent" means
     * either the peer has no reputation or nothing has loaded yet.
     * [ReputationServiceFacade.scoreByUserProfileId] separates the two — it starts empty and is only
     * ever replaced wholesale by a payload, so a non-empty map proves a snapshot arrived and this
     * peer is genuinely unscored.
     *
     * A *thrown* lookup carries no such information: the call never reached a verdict, so the
     * snapshot says nothing about this peer and the result stays unknown. Feeding it through the
     * fallback would render a transport error as a confident zero, which is exactly what this
     * function exists to prevent — the offerbook card the user tapped through may be showing 4.5
     * stars for the same peer.
     *
     * Follow-up: a late-arriving snapshot does not refresh this screen, because the facade exposes
     * the map as a plain getter rather than a flow and the load runs once.
     */
    private suspend fun loadReputation(profileId: String): ReputationScoreVO? {
        val result =
            try {
                reputationServiceFacade.getReputation(profileId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.w(e) { "Failed to load reputation for $profileId" }
                return null
            }
        result.getOrNull()?.let { return it }
        return if (reputationServiceFacade.scoreByUserProfileId.isNotEmpty()) ZERO_REPUTATION else null
    }

    /**
     * Binds to the facade's ignored-ids flow rather than tracking the state locally, so an
     * ignore/unignore performed elsewhere (chat context menu, ignored-users list) is reflected here
     * live. It is a StateFlow, so the current value arrives immediately and no seed call is needed.
     */
    private fun observeIgnoredState(profileId: String) {
        ignoredStateJob?.cancel()
        ignoredStateJob =
            presenterScope.launch {
                userProfileServiceFacade.ignoredProfileIds.collect { ignoredIds ->
                    _uiState.update { it.copy(isIgnored = profileId in ignoredIds) }
                }
            }
    }

    private fun onConfirmIgnore() {
        val profileId = _uiState.value.profileId
        if (profileId.isEmpty()) return
        guardedSuspendAction(_isIgnoreActionEnabled, "onConfirmIgnore") {
            _uiState.update { it.copy(showIgnoreConfirmDialog = false) }
            try {
                // isIgnored is deliberately not set here — the ignoredProfileIds collector owns it,
                // so the two never disagree if the call fails.
                userProfileServiceFacade.ignoreUserProfile(profileId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.e(e) { "Failed to ignore $profileId" }
                handleError(e)
            }
        }
    }

    /**
     * No confirmation dialog: un-ignoring is fully reversible, so the design surfaces it as a plain
     * visible button. (The ignored-users list does confirm — there a mis-tap is harder to notice.)
     */
    private fun onUndoIgnore() {
        val profileId = _uiState.value.profileId
        if (profileId.isEmpty()) return
        guardedSuspendAction(_isIgnoreActionEnabled, "onUndoIgnore") {
            try {
                userProfileServiceFacade.undoIgnoreUserProfile(profileId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.e(e) { "Failed to undo ignore for $profileId" }
                handleError(e)
            }
        }
    }

    /**
     * Closes the dialog but holds on to [reportMessage]: reporting can fail on a dropped connection,
     * and losing the text the user just wrote would make them compose it a second time.
     */
    private fun onReportFailure(
        message: String,
        reportMessage: String,
    ) {
        _uiState.update { it.copy(showReportDialog = false, reportDraft = reportMessage) }
        showSnackbar(message, type = SnackbarType.ERROR)
    }
}
