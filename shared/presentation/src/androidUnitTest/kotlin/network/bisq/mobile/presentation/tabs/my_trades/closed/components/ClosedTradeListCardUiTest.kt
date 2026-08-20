package network.bisq.mobile.presentation.tabs.my_trades.closed.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.utils.createEmptyImage
import network.bisq.mobile.domain.model.trade.ClosedTradeListItem
import network.bisq.mobile.domain.model.trade.TradeOutcome
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.rememberStarPainters
import network.bisq.mobile.test.presentation.compose.PresentationKoinComposeTestBase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The whole card is a tap target — it opens the trade details — so the peer profile link must stay
 * confined to the avatar. The first two tests pin both halves of that split.
 */
class ClosedTradeListCardUiTest : PresentationKoinComposeTestBase() {
    private val peerName = "Satoshi"

    private var detailsOpened = false
    private var peerProfileOpened = false

    private fun tradeWith(outcome: TradeOutcome): ClosedTradeListItem =
        sampleClosedTrade(
            tradeId = "t-abc123def456ghi789",
            peerName = peerName,
            reputation = ReputationScoreVO(totalScore = 1200L, fiveSystemScore = 4.5, ranking = 12),
            fiatPaymentMethod = "SEPA",
            bitcoinSettlementMethod = "MAIN_CHAIN",
            isMaker = false,
            isBuyer = true,
            outcome = outcome,
            takeOfferDate = 1_743_000_000_000L,
            quoteAmount = 34210L,
        )

    private fun renderCard(outcome: TradeOutcome = TradeOutcome.COMPLETED) {
        setTestContent {
            ClosedTradeListCard(
                item = tradeWith(outcome),
                userProfileIconProvider = { createEmptyImage() },
                starPainters = rememberStarPainters(),
                onClick = { detailsOpened = true },
                onPeerProfileClick = { peerProfileOpened = true },
            )
        }
    }

    @Test
    fun `when the peer avatar is tapped then the peer profile is opened`() {
        renderCard()

        composeTestRule.onNodeWithContentDescription("mobile.createProfile.iconGenerated".i18n()).performClick()
        composeTestRule.waitForIdle()

        assertTrue(peerProfileOpened)
        assertFalse(detailsOpened)
    }

    @Test
    fun `when the card is tapped outside the avatar then the trade details are opened`() {
        renderCard()

        // The peer's name sits directly beside the avatar — the likeliest mis-tap.
        composeTestRule.onNodeWithText(peerName).performClick()
        composeTestRule.waitForIdle()

        assertTrue(detailsOpened)
        assertFalse(peerProfileOpened)
    }

    /**
     * The outcome drives both the badge label and the card's accent colour, and nothing pinned that
     * mapping until now. All four render in one composition — [setTestContent] wraps `setContent`,
     * which a test may only call once.
     */
    @Test
    fun `each outcome renders its own badge label`() {
        val outcomes =
            listOf(
                TradeOutcome.COMPLETED to "mobile.tradeHistory.outcome.completed",
                TradeOutcome.CANCELLED to "mobile.tradeHistory.outcome.cancelled",
                TradeOutcome.REJECTED to "mobile.tradeHistory.outcome.rejected",
                TradeOutcome.FAILED to "mobile.tradeHistory.outcome.failed",
            )

        setTestContent {
            Column {
                outcomes.forEach { (outcome, _) ->
                    ClosedTradeListCard(
                        item = tradeWith(outcome),
                        userProfileIconProvider = { createEmptyImage() },
                        starPainters = rememberStarPainters(),
                        onPeerProfileClick = {},
                    )
                }
            }
        }

        outcomes.forEach { (_, labelKey) ->
            composeTestRule.onNodeWithText(labelKey.i18n()).assertExists()
        }
    }
}
