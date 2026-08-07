package network.bisq.mobile.presentation.peer_profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.reputation.ReputationServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.test.presentation.coroutines.PresentationKoinTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behaviour tests for [PeerProfilePresenter] (issue #545).
 *
 * The lookup distinguishes three failure shapes that look alike from the outside — an own profile, a
 * peer the network does not know, and a lookup that could not complete — and only the last offers a
 * retry. Most of what follows pins those apart.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PeerProfilePresenterTest : PresentationKoinTestBase() {
    private lateinit var userProfileServiceFacade: UserProfileServiceFacade
    private lateinit var reputationServiceFacade: ReputationServiceFacade
    private lateinit var ignoredProfileIds: MutableStateFlow<Set<String>>
    private lateinit var ownProfiles: MutableStateFlow<List<UserProfileVO>>
    private lateinit var presenter: PeerProfilePresenter

    private val peer = createMockUserProfile(PEER_ID)

    private companion object {
        /** [createMockUserProfile] sets `networkId.pubKey.id` to the name, so the id is the name. */
        const val PEER_ID = "peer-1"
        const val OWN_ID = "my-profile"

        val REPUTATION = ReputationScoreVO(totalScore = 12_400L, fiveSystemScore = 4.5, ranking = 7)
    }

    override fun onKoinReady() {
        ignoredProfileIds = MutableStateFlow(emptySet())
        ownProfiles = MutableStateFlow(emptyList())

        userProfileServiceFacade =
            mockk(relaxed = true) {
                every { ignoredProfileIds } returns this@PeerProfilePresenterTest.ignoredProfileIds
                every { userProfiles } returns ownProfiles
            }
        reputationServiceFacade = mockk(relaxed = true)

        coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } returns peer
        coEvery { reputationServiceFacade.getReputation(PEER_ID) } returns Result.success(REPUTATION)

        presenter =
            PeerProfilePresenter(
                userProfileServiceFacade = userProfileServiceFacade,
                reputationServiceFacade = reputationServiceFacade,
                mainPresenter = mockk<MainPresenter>(relaxed = true),
            )
    }

    // ---------------------------------------------------------------------------------------
    // Loading
    // ---------------------------------------------------------------------------------------

    @Test
    fun `when a known peer is initialized then profile and reputation are exposed`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertEquals(PEER_ID, state.profileId)
            assertEquals(peer, state.userProfile)
            assertEquals(peer.userName, state.displayName)
            assertEquals(4.5, state.starRating)
            assertEquals(12_400L, state.reputationScore)
            assertFalse(state.isLoading)
            assertFalse(state.isNotFound)
            assertFalse(state.isLoadFailed)
        }

    @Test
    fun `when the peer is unknown then reports not found rather than a load failure`() =
        runTest {
            coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } returns null

            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertTrue(state.isNotFound)
            assertFalse(state.isLoadFailed)
            assertFalse(state.isLoading)
        }

    @Test
    fun `when the lookup throws then reports a load failure rather than not found`() =
        runTest {
            // On the client flavour findUserProfile is a round-trip to the trusted node, so a
            // dropped connection must not be surfaced as "this peer does not exist".
            coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } throws RuntimeException("connection lost")

            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertTrue(state.isLoadFailed)
            assertFalse(state.isNotFound)
            assertFalse(state.isLoading)
        }

    @Test
    fun `when retry is clicked after a load failure then the profile loads`() =
        runTest {
            coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } throws RuntimeException("connection lost")
            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            assertTrue(presenter.uiState.value.isLoadFailed)

            coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } returns peer
            presenter.onAction(PeerProfileUiAction.OnRetryLoadClick)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertFalse(state.isLoadFailed)
            assertEquals(peer, state.userProfile)
            assertFalse(state.isLoading)
        }

    @Test
    fun `when reputation is absent from a loaded snapshot then the score is a genuine zero`() =
        runTest {
            // A peer with no reputation yet is exactly the peer a user most wants to inspect, so the
            // screen must still render. A non-empty score map proves the snapshot arrived and this
            // peer simply is not in it.
            coEvery { reputationServiceFacade.getReputation(PEER_ID) } returns Result.failure(RuntimeException("no score"))
            every { reputationServiceFacade.scoreByUserProfileId } returns mapOf("someone-else" to 500L)

            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertEquals(peer, state.userProfile)
            assertEquals(0L, state.reputationScore)
            assertEquals(0.0, state.starRating)
            assertFalse(state.isReputationUnknown)
            assertFalse(state.isLoadFailed)
        }

    @Test
    fun `when the reputation snapshot has not arrived then the score is unknown rather than zero`() =
        runTest {
            // The same failure as above, but with nothing loaded at all. Reporting "0 pts" here would
            // contradict the offerbook card the user tapped through, which may show 4.5 stars for
            // this very peer.
            coEvery { reputationServiceFacade.getReputation(PEER_ID) } returns Result.failure(RuntimeException("no score"))
            every { reputationServiceFacade.scoreByUserProfileId } returns emptyMap()

            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertEquals(peer, state.userProfile)
            assertTrue(state.isReputationUnknown)
            assertFalse(state.isLoadFailed)
        }

    @Test
    fun `when the reputation lookup throws then the score is unknown even with a loaded snapshot`() =
        runTest {
            // A thrown lookup never reached a verdict, so the snapshot says nothing about this peer.
            // Running it through the zero fallback would render a transport error as a confident
            // "0 pts" — the misreading the unknown state exists to prevent.
            coEvery { reputationServiceFacade.getReputation(PEER_ID) } throws RuntimeException("transport")
            every { reputationServiceFacade.scoreByUserProfileId } returns mapOf("someone-else" to 500L)

            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertEquals(peer, state.userProfile)
            assertTrue(state.isReputationUnknown)
            assertEquals(0L, state.reputationScore)
            assertFalse(state.isLoadFailed)
        }

    @Test
    fun `when a slow load is superseded by a retry then its failure is discarded`() =
        runTest {
            // findUserProfile is a node round-trip on the client flavour, so a stalled first attempt
            // can finish *after* a fast retry has already rendered the profile. The blocker releases
            // it at exactly that point: without cancelling the superseded load, its failure lands
            // last and replaces the profile with the load-failed screen.
            val firstCallBlocker = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { userProfileServiceFacade.findUserProfile(PEER_ID) } coAnswers {
                calls++
                if (calls == 1) {
                    firstCallBlocker.await()
                    throw RuntimeException("slow failure")
                }
                peer
            }

            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            assertTrue(presenter.uiState.value.isLoading)

            presenter.onAction(PeerProfileUiAction.OnRetryLoadClick)
            advanceUntilIdle()
            assertEquals(peer, presenter.uiState.value.userProfile)

            firstCallBlocker.complete(Unit)
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertEquals(peer, state.userProfile)
            assertFalse(state.isLoadFailed)
            assertFalse(state.isLoading)
        }

    @Test
    fun `when initialized twice with the same id then the profile is loaded once`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            coVerify(exactly = 1) { userProfileServiceFacade.findUserProfile(PEER_ID) }
        }

    // ---------------------------------------------------------------------------------------
    // Own-profile guard
    // ---------------------------------------------------------------------------------------

    @Test
    fun `when the id is one of my profiles then guards without looking the peer up`() =
        runTest {
            ownProfiles.value = listOf(createMockUserProfile(OWN_ID))

            presenter.initialize(OWN_ID)
            advanceUntilIdle()

            assertTrue(presenter.uiState.value.isOwnProfile)
            assertFalse(presenter.uiState.value.isLoading)
            coVerify(exactly = 0) { userProfileServiceFacade.findUserProfile(any()) }
        }

    @Test
    fun `when the owned-profiles flow is not warmed yet then falls back to the identity ids`() =
        runTest {
            ownProfiles.value = emptyList()
            coEvery { userProfileServiceFacade.getUserIdentityIds() } returns listOf(OWN_ID)

            presenter.initialize(OWN_ID)
            advanceUntilIdle()

            assertTrue(presenter.uiState.value.isOwnProfile)
        }

    // ---------------------------------------------------------------------------------------
    // Ignore / undo ignore
    // ---------------------------------------------------------------------------------------

    @Test
    fun `when the peer is ignored elsewhere then the ignored state follows live`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            assertFalse(presenter.uiState.value.isIgnored)

            ignoredProfileIds.value = setOf(PEER_ID)
            advanceUntilIdle()
            assertTrue(presenter.uiState.value.isIgnored)

            ignoredProfileIds.value = emptySet()
            advanceUntilIdle()
            assertFalse(presenter.uiState.value.isIgnored)
        }

    @Test
    fun `when ignore is confirmed twice in a row then the peer is ignored once`() =
        runTest {
            coEvery { userProfileServiceFacade.ignoreUserProfile(PEER_ID) } coAnswers { delay(Long.MAX_VALUE) }
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnConfirmIgnore)
            presenter.onAction(PeerProfileUiAction.OnConfirmIgnore)
            advanceUntilIdle()

            coVerify(exactly = 1) { userProfileServiceFacade.ignoreUserProfile(PEER_ID) }
            assertFalse(presenter.uiState.value.showIgnoreConfirmDialog)
            assertFalse(presenter.isIgnoreActionEnabled.value)
        }

    @Test
    fun `when ignoring fails then the action becomes available again`() =
        runTest {
            coEvery { userProfileServiceFacade.ignoreUserProfile(PEER_ID) } throws RuntimeException("fail")
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnConfirmIgnore)
            advanceUntilIdle()

            assertTrue(presenter.isIgnoreActionEnabled.value)
        }

    @Test
    fun `when the ignore call is cancelled then no error is surfaced to the user`() =
        runTest {
            // Backing out while the call is in flight cancels presenterScope. Letting that reach
            // handleError would pop a global "unexpected error" snackbar on whatever screen the user
            // landed on, plus log a failure that never happened.
            coEvery { userProfileServiceFacade.ignoreUserProfile(PEER_ID) } throws CancellationException("navigated away")
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnConfirmIgnore)
            advanceUntilIdle()

            verify(exactly = 0) { globalUiManager.showSnackbar(any(), any(), any(), any()) }
        }

    @Test
    fun `when undo ignore is clicked then the ignore is lifted`() =
        runTest {
            ignoredProfileIds.value = setOf(PEER_ID)
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnUndoIgnoreClick)
            advanceUntilIdle()

            coVerify(exactly = 1) { userProfileServiceFacade.undoIgnoreUserProfile(PEER_ID) }
        }

    // ---------------------------------------------------------------------------------------
    // Dialogs and the report draft
    // ---------------------------------------------------------------------------------------

    @Test
    fun `when dialog actions are dispatched then their flags toggle`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnIgnoreClick)
            assertTrue(presenter.uiState.value.showIgnoreConfirmDialog)

            presenter.onAction(PeerProfileUiAction.OnDismissIgnoreDialog)
            assertFalse(presenter.uiState.value.showIgnoreConfirmDialog)

            presenter.onAction(PeerProfileUiAction.OnReportClick)
            assertTrue(presenter.uiState.value.showReportDialog)
        }

    @Test
    fun `when a report fails then the typed message is kept for a second attempt`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            presenter.onAction(PeerProfileUiAction.OnReportClick)

            presenter.onAction(
                PeerProfileUiAction.OnReportFailure(
                    message = "Could not reach the moderator",
                    reportMessage = "This user violated chat rules",
                ),
            )
            advanceUntilIdle()

            val state = presenter.uiState.value
            assertFalse(state.showReportDialog)
            assertEquals("This user violated chat rules", state.reportDraft)
        }

    @Test
    fun `when the report dialog is dismissed then the kept message is discarded`() =
        runTest {
            presenter.initialize(PEER_ID)
            advanceUntilIdle()
            presenter.onAction(PeerProfileUiAction.OnReportFailure("error", "a half-written report"))
            advanceUntilIdle()

            presenter.onAction(PeerProfileUiAction.OnDismissReportDialog)

            assertNull(presenter.uiState.value.reportDraft)
            assertFalse(presenter.uiState.value.showReportDialog)
        }
}
