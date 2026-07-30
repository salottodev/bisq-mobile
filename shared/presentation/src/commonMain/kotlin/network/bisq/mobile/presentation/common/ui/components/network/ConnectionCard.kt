package network.bisq.mobile.presentation.common.ui.components.network

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bisqapps.shared.presentation.generated.resources.Res
import bisqapps.shared.presentation.generated.resources.no_connections
import network.bisq.mobile.domain.utils.ByteUnitUtil
import network.bisq.mobile.domain.utils.DateUtils
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.button.CopyIconButton
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.ArrowDownIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqHDivider
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import org.jetbrains.compose.resources.painterResource
import kotlin.math.round

/**
 * UI model for one peer connection row, shared by the Node and Connect apps.
 *
 * Both apps map their own domain snapshot to this (`NodePeerInfo` on the node, `ConnectionDto`
 * on the client) so [ConnectionCard] stays app-agnostic.
 *
 * [metrics] is `null` when per-peer traffic metrics are not available for this source yet — today
 * that is the Connect app, whose trusted-node `ConnectionDto` does not carry them. When null the
 * card renders without RTT and without the expandable Sent/Received section (no placeholder data).
 */
data class NetworkConnectionUiItem(
    val connectionId: String,
    val address: String,
    val isOutbound: Boolean,
    val isSeed: Boolean,
    val establishedAtMillis: Long,
    val metrics: ConnectionMetricsUiItem? = null,
)

/**
 * Per-peer traffic metrics read off the connection.
 *
 * [rttMillis] is `null` until a round-trip has actually been measured (handshake / request-response),
 * rendered as "–".
 */
data class ConnectionMetricsUiItem(
    val rttMillis: Long?,
    val sentBytes: Long,
    val sentMessageCount: Long,
    val receivedBytes: Long,
    val receivedMessageCount: Long,
)

private const val ADDRESS_HEAD_CHARS = 10
private const val ADDRESS_TAIL_CHARS = 10

/**
 * A single peer connection row.
 *
 * Row 1: direction dot + address (middle-truncated, with a copy icon) + RTT.
 * Row 2: established timestamp + optional seed badge + inbound/outbound label + expand chevron.
 * Expanded: Sent / Received (bytes + message count), in a 2-column layout.
 *
 * RTT, the chevron and the expandable section only render when [NetworkConnectionUiItem.metrics] is
 * present; otherwise the card collapses to a static direction/address/timestamp row (Connect today).
 *
 * [initiallyExpanded] exists so previews/tests can render the expanded state without simulating a tap.
 */
@Composable
fun ConnectionCard(
    peer: NetworkConnectionUiItem,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    val metrics = peer.metrics
    val expandable = metrics != null
    // rememberSaveable (not remember) so the expanded state survives the card being scrolled out of the
    // LazyColumn and disposed; the stable connectionId item key lets it be restored on scroll-back.
    var isExpanded by rememberSaveable(peer.connectionId) { mutableStateOf(initiallyExpanded) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "connectionCardChevronRotation",
    )
    val chevronDescription =
        if (isExpanded) "mobile.action.hide".i18n() else "mobile.action.show".i18n()

    val directionColor = if (peer.isOutbound) BisqTheme.colors.primary else BisqTheme.colors.mid_grey30
    val directionLabel =
        if (peer.isOutbound) {
            "mobile.networkInfo.connections.outbound".i18n()
        } else {
            "mobile.networkInfo.connections.inbound".i18n()
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                .background(BisqTheme.colors.dark_grey40)
                .let { if (expandable) it.clickable { isExpanded = !isExpanded } else it }
                .padding(
                    horizontal = BisqUIConstants.ScreenPadding,
                    vertical = BisqUIConstants.ScreenPadding,
                ).testTag("connection_card_${peer.connectionId}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(directionColor),
            )
            BisqGap.H1()
            BisqText.StyledText(
                text = truncateMiddle(peer.address),
                style = BisqTheme.typography.smallMedium,
                color = BisqTheme.colors.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CopyIconButton(value = peer.address, showToast = false)
            if (metrics != null) {
                BisqGap.H1()
                BisqText.XSmallMedium(
                    text = formatRtt(metrics.rttMillis),
                    color = if (metrics.rttMillis == null) BisqTheme.colors.mid_grey20 else BisqTheme.colors.white,
                )
            }
        }

        BisqGap.VQuarter()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BisqText.XSmallLight(
                text = DateUtils.toMediumDateTime(peer.establishedAtMillis, includeSeconds = true),
                color = BisqTheme.colors.mid_grey20,
                modifier = Modifier.weight(1f),
            )
            if (peer.isSeed) {
                SeedBadge()
                BisqGap.HHalf()
            }
            BisqText.XSmallLight(text = directionLabel, color = BisqTheme.colors.mid_grey20)
            if (expandable) {
                BisqGap.HHalf()
                // Down = "there is more below", rotated to up = "tap to close". Rotating one asset
                // rather than swapping in a second keeps both states pixel-identical; there is no
                // matching 12dp up-chevron drawable to swap to.
                ArrowDownIcon(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .rotate(chevronRotation)
                            // The asset's own "Down arrow icon" description is meaningless once
                            // rotated, and rotation is invisible to accessibility services; replace
                            // it with the localized affordance, as AdvancedOptionsDrawer does.
                            .clearAndSetSemantics { contentDescription = chevronDescription },
                )
            }
        }

        if (metrics != null && isExpanded) {
            ConnectionCardExpandedSection(metrics)
        }
    }
}

@Composable
private fun SeedBadge() {
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
}

@Composable
private fun ConnectionCardExpandedSection(metrics: ConnectionMetricsUiItem) {
    BisqHDivider(verticalPadding = BisqUIConstants.ScreenPaddingHalf)

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            BisqText.XSmallLight(
                text = "mobile.networkInfo.connections.sent".i18n(),
                color = BisqTheme.colors.mid_grey20,
            )
            BisqGap.VQuarter()
            BisqText.SmallRegular(
                text = formatIoLine(metrics.sentBytes, metrics.sentMessageCount),
                color = BisqTheme.colors.white,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            BisqText.XSmallLight(
                text = "mobile.networkInfo.connections.received".i18n(),
                color = BisqTheme.colors.mid_grey20,
            )
            BisqGap.VQuarter()
            BisqText.SmallRegular(
                text = formatIoLine(metrics.receivedBytes, metrics.receivedMessageCount),
                color = BisqTheme.colors.white,
            )
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

/**
 * Head + "…" + tail truncation, keeping the trailing `:port` (the one part of an onion address that
 * varies meaningfully across a user's own nodes) that plain tail-ellipsis would discard.
 */
private fun truncateMiddle(
    value: String,
    headChars: Int = ADDRESS_HEAD_CHARS,
    tailChars: Int = ADDRESS_TAIL_CHARS,
): String {
    if (value.length <= headChars + tailChars + 1) return value
    return "${value.take(headChars)}…${value.takeLast(tailChars)}"
}

/** "184 ms" / "1.2 s" / "–" when unmeasured. No color-coding: Tor latency has no vetted threshold. */
private fun formatRtt(rttMillis: Long?): String =
    when {
        rttMillis == null -> "–"
        rttMillis < 1000 -> "$rttMillis ms"
        else -> "${round(rttMillis / 100.0) / 10.0} s"
    }

/** "12.1 KB · 340 msgs" — plural on the message count. */
private fun formatIoLine(
    bytes: Long,
    messageCount: Long,
): String {
    val key =
        if (messageCount == 1L) {
            "mobile.networkInfo.connections.ioData.1"
        } else {
            "mobile.networkInfo.connections.ioData.*"
        }
    return key.i18n(ByteUnitUtil.formatBytesPrecise(bytes, decimals = 1), messageCount)
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
                    metrics =
                        ConnectionMetricsUiItem(
                            rttMillis = 184L,
                            sentBytes = 12_400L,
                            sentMessageCount = 340L,
                            receivedBytes = 18_900L,
                            receivedMessageCount = 512L,
                        ),
                ),
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionCardExpandedPreview() {
    BisqTheme.Preview {
        ConnectionCard(
            peer =
                NetworkConnectionUiItem(
                    connectionId = "2",
                    address = "wns3jrgvyxjafp7sazk4nzgcpndinftwotgxu4tdpg6ov5xshhbc4qd.onion:1893",
                    isOutbound = true,
                    isSeed = false,
                    establishedAtMillis = 0L,
                    metrics =
                        ConnectionMetricsUiItem(
                            rttMillis = null,
                            sentBytes = 0L,
                            sentMessageCount = 0L,
                            receivedBytes = 0L,
                            receivedMessageCount = 1L,
                        ),
                ),
            initiallyExpanded = true,
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionCardNoMetricsPreview() {
    BisqTheme.Preview {
        ConnectionCard(
            peer =
                NetworkConnectionUiItem(
                    connectionId = "3",
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
