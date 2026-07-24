package network.bisq.mobile.presentation.common.ui.components.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bisqapps.shared.presentation.generated.resources.Res
import bisqapps.shared.presentation.generated.resources.no_connections
import network.bisq.mobile.domain.utils.DateUtils
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import org.jetbrains.compose.resources.painterResource

/**
 * UI model for one peer connection row, shared by the Node and Connect apps.
 *
 * Both apps map their own domain snapshot to this (`NodePeerInfo` on the node, `ConnectionDto`
 * on the client) so [ConnectionCard] stays app-agnostic.
 */
data class NetworkConnectionUiItem(
    val connectionId: String,
    val address: String,
    val isOutbound: Boolean,
    val isSeed: Boolean,
    val establishedAtMillis: Long,
)

/**
 * A single peer connection row: direction dot + address + established timestamp, with an
 * optional seed badge and inbound/outbound label.
 */
@Composable
fun ConnectionCard(peer: NetworkConnectionUiItem) {
    val directionColor = if (peer.isOutbound) BisqTheme.colors.primary else BisqTheme.colors.mid_grey30
    val directionLabel =
        if (peer.isOutbound) {
            "mobile.networkInfo.connections.outbound".i18n()
        } else {
            "mobile.networkInfo.connections.inbound".i18n()
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                .background(BisqTheme.colors.dark_grey40)
                .padding(
                    horizontal = BisqUIConstants.ScreenPadding,
                    vertical = BisqUIConstants.ScreenPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(directionColor),
        )

        BisqGap.H1()

        Column(modifier = Modifier.weight(1f)) {
            BisqText.StyledText(
                text = peer.address,
                style = BisqTheme.typography.smallMedium,
                color = BisqTheme.colors.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BisqGap.VQuarter()
            BisqText.XSmallLight(
                text = DateUtils.toMediumDateTime(peer.establishedAtMillis, includeSeconds = true),
                color = BisqTheme.colors.mid_grey20,
            )
        }

        BisqGap.H1()

        Column(horizontalAlignment = Alignment.End) {
            if (peer.isSeed) {
                Box(
                    modifier =
                        Modifier
                            .border(
                                width = 1.dp,
                                color = BisqTheme.colors.mid_grey10,
                                shape = RoundedCornerShape(BisqUIConstants.BorderRadiusSmall),
                            ).padding(
                                horizontal = BisqUIConstants.ScreenPaddingHalf,
                                vertical = BisqUIConstants.ScreenPaddingQuarter,
                            ),
                ) {
                    BisqText.XSmallLight(
                        text = "mobile.networkInfo.connections.seed".i18n(),
                        color = BisqTheme.colors.mid_grey20,
                    )
                }
                BisqGap.VQuarter()
            }
            BisqText.XSmallLight(text = directionLabel, color = BisqTheme.colors.mid_grey20)
        }
    }
}

/**
 * Empty state for a connections list — shown when there are no peers to display.
 *
 * [hintKey] lets each app supply a context-appropriate hint (the node's default speaks about "the node";
 * the Connect app passes a trusted-node variant).
 */
@Composable
fun NetworkConnectionsEmptyState(
    modifier: Modifier = Modifier,
    hintKey: String = "mobile.networkInfo.connections.emptyHint",
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = BisqUIConstants.ScreenPadding2X),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.no_connections),
            contentDescription = null,
            modifier = Modifier.size(BisqUIConstants.ScreenPadding4X),
        )
        BisqGap.V2()
        BisqText.BaseMedium(
            text = "mobile.networkInfo.connections.empty".i18n(),
            color = BisqTheme.colors.mid_grey30,
            textAlign = TextAlign.Center,
        )
        BisqGap.V1()
        BisqText.SmallLight(
            text = hintKey.i18n(),
            color = BisqTheme.colors.mid_grey20,
            textAlign = TextAlign.Center,
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionCardOutboundSeedPreview() {
    BisqTheme.Preview {
        ConnectionCard(
            peer =
                NetworkConnectionUiItem(
                    connectionId = "1",
                    address = "abcd1234efgh5678ijkl.onion:1234",
                    isOutbound = true,
                    isSeed = true,
                    establishedAtMillis = 0L,
                ),
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionCardInboundPreview() {
    BisqTheme.Preview {
        ConnectionCard(
            peer =
                NetworkConnectionUiItem(
                    connectionId = "2",
                    address = "mnop9012qrst3456uvwx.onion:1234",
                    isOutbound = false,
                    isSeed = false,
                    establishedAtMillis = 0L,
                ),
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun NetworkConnectionsEmptyStatePreview() {
    BisqTheme.Preview {
        NetworkConnectionsEmptyState()
    }
}
