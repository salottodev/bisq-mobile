package network.bisq.mobile.presentation.common.ui.components.network

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.button.CopyIconButton
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage

private const val KEY_ID_KEEP_CHARS = 16

/**
 * Screen-level identity strip rendered once above a connections list: the node identity these
 * connections belong to.
 *
 * [keyId] is the local node's own key id (Node app) or the trusted node's key id (Connect). [nodeTag]
 * is the identity tag; it is rendered only when non-null — Connect does not receive a nodeTag over the
 * websocket yet, so it passes null and only the key id shows. The whole header self-hides when [keyId]
 * is null (e.g. before the node identity resolves, or while the Connect link is down).
 *
 * Deliberately terse (truncated key id + copy icon): the Node app's "My Node" sub-page already shows
 * the key id in depth — this is a lightweight contextual echo, not a duplicate of that screen.
 */
@Composable
fun ConnectionsIdentityHeader(
    keyId: String?,
    nodeTag: String?,
    modifier: Modifier = Modifier,
) {
    if (keyId == null) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BisqText.XSmallLight(
            text = "mobile.networkInfo.connections.identity.keyId".i18n(),
            color = BisqTheme.colors.mid_grey20,
        )
        BisqGap.HHalf()
        BisqText.StyledText(
            text = truncateLeading(keyId, KEY_ID_KEEP_CHARS),
            style = BisqTheme.typography.xsmallMedium,
            color = BisqTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        CopyIconButton(value = keyId, showToast = false)
        if (nodeTag != null) {
            BisqGap.H1()
            BisqText.XSmallLight(
                text = "mobile.networkInfo.connections.identity.nodeTag".i18n(),
                color = BisqTheme.colors.mid_grey20,
            )
            BisqGap.HHalf()
            BisqText.XSmallMedium(text = nodeTag, color = BisqTheme.colors.white)
        }
    }
}

/** Leading truncation for the key id — no meaningful tail to preserve, unlike an address's port. */
private fun truncateLeading(
    value: String,
    keepChars: Int,
): String {
    if (value.length <= keepChars + 1) return value
    return "${value.take(keepChars)}…"
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionsIdentityHeaderPreview() {
    BisqTheme.Preview {
        ConnectionsIdentityHeader(
            keyId = "9f8e7d6c5b4a3928170615243f3e2d1c0b9a8f7e6d5c4b3a2918f7e6",
            nodeTag = "default",
        )
    }
}

@ExcludeFromCoverage
@Preview
@Composable
private fun ConnectionsIdentityHeaderKeyIdOnlyPreview() {
    BisqTheme.Preview {
        ConnectionsIdentityHeader(
            keyId = "02a1c98f4b7e29d3f18a",
            nodeTag = null,
        )
    }
}
