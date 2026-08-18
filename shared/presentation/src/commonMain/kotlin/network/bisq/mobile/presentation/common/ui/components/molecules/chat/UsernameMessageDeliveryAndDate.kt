package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.priv.PrivateChatMessage
import network.bisq.mobile.data.replicated.network.confidential.ack.MessageDeliveryInfoVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.rememberDebouncedClick
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants

/**
 * @param onLongClick forwarded from the enclosing message bubble, for the same reason
 *   [ProfileIconAndText] takes one: this row consumes the pointer-down, and a plain `clickable`
 *   still fires `onClick` on release after an arbitrarily long press — so without re-dispatching,
 *   long-pressing a peer's name would navigate to their profile instead of opening the context menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsernameMessageDeliveryAndDate(
    message: PrivateChatMessage<*>,
    onResendMessage: (String) -> Unit,
    userNameProvider: suspend (String) -> String,
    messageDeliveryInfoByPeersProfileId: StateFlow<Map<String, MessageDeliveryInfoVO>>,
    onPeerProfileClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    var showInfo by remember { mutableStateOf(false) }

    // Debounced because it navigates: a double tap would otherwise push two profile destinations.
    // Only the peer branch needs it — the delivery popup is idempotent, and keeping the plain
    // `clickable` preserves the row's ripple, which `debouncedClickable` suppresses by default.
    val openPeerProfile = rememberDebouncedClick { onPeerProfileClick() }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .semantics(mergeDescendants = true) {}
                    // Always tappable, but to different ends: my own messages open the
                    // delivery-status popup, a peer's opens their profile.
                    .combinedClickable(
                        role = Role.Button,
                        onLongClick = onLongClick,
                        onClick = {
                            if (message.isMyMessage) {
                                showInfo = true
                            } else {
                                openPeerProfile()
                            }
                        },
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val date = @Composable {
                BisqText.XSmallLightGrey(
                    modifier =
                        Modifier
                            .widthIn(max = this@BoxWithConstraints.maxWidth * 0.4f),
                    text = message.dateString,
                )
            }
            val username = @Composable {
                BisqText.BaseRegular(
                    modifier =
                        Modifier
                            .widthIn(max = this@BoxWithConstraints.maxWidth * 0.6f),
                    text = message.senderUserName,
                )
            }

            if (message.isMyMessage) {
                Spacer(Modifier.weight(1f))
                date()
                Spacer(Modifier.width(BisqUIConstants.ScreenPaddingHalfQuarter))
                MessageDeliveryBox(
                    onResendMessage = onResendMessage,
                    userNameProvider = userNameProvider,
                    messageDeliveryInfoByPeersProfileId = messageDeliveryInfoByPeersProfileId,
                    showInfo,
                    onDismissMenu = {
                        showInfo = false
                    },
                )
                username()
            } else {
                username()
                Spacer(Modifier.width(BisqUIConstants.ScreenPaddingHalfQuarter))
                date()
            }
        }
    }
}

@Preview
@Composable
private fun UsernameMessageDeliveryAndDate_MyMessagePreview() {
    BisqTheme.Preview {
        val myUserProfile = createMockUserProfile("Bob [Marvelously-Extraneous-Elephant-234345435]")
        val peerUserProfile =
            createMockUserProfile("Alice [Marvelously-Extraneous-Elephant-234345435]")

        val message =
            createMockBisqEasyOpenTradeMessage(
                id = "msg123",
                text = "Hello!",
                senderUserProfile = myUserProfile,
                myUserProfile = myUserProfile,
                tradeId = "trade123",
            )

        UsernameMessageDeliveryAndDate(
            message = message,
            onResendMessage = {},
            userNameProvider = { it },
            messageDeliveryInfoByPeersProfileId = MutableStateFlow(emptyMap()),
            onPeerProfileClick = {},
        )
    }
}

@Preview
@Composable
private fun UsernameMessageDeliveryAndDate_PeerMessagePreview() {
    BisqTheme.Preview {
        val myUserProfile = createMockUserProfile("Bob [Marvelously-Extraneous-Elephant-234345435]")
        val peerUserProfile =
            createMockUserProfile("Alice")

        val message =
            createMockBisqEasyOpenTradeMessage(
                id = "msg456",
                text = "Hi there!",
                senderUserProfile = peerUserProfile,
                myUserProfile = myUserProfile,
                tradeId = "trade123",
            )

        UsernameMessageDeliveryAndDate(
            message = message,
            onResendMessage = {},
            userNameProvider = { it },
            messageDeliveryInfoByPeersProfileId = MutableStateFlow(emptyMap()),
            onPeerProfileClick = {},
        )
    }
}
