package network.bisq.mobile.presentation.common.ui.components.molecules.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import network.bisq.mobile.data.replicated.chat.ChatMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.ClosedEyeIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.CopyIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.DeleteIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.EditIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.EyeIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.FlagIcon
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.ReplyIcon
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage

/**
 * Every leading icon here sits next to a menu item that already spells out what it does, so
 * announcing it would read each item twice. Re-declares the 24.dp the icons carry as their own
 * default, which passing a modifier replaces.
 */
private val decorativeIcon = Modifier.size(24.dp).clearAndSetSemantics { }

/** [CopyIcon] alone defaults to no size at all, so giving it [decorativeIcon] would resize it. */
private val decorativeCopyIcon = Modifier.clearAndSetSemantics { }

/**
 * @param onEditMessage null keeps Edit out of the menu, and likewise [onDeleteMessage] for Delete.
 *   Nullable rather than a boolean because that is what stops a public-chat-only action leaking into
 *   the trade and private menus, where bisq2 has no edit or delete endpoint at all and a rendered item
 *   would be an affordance that cannot work. Same idiom as
 *   [UsernameMessageDeliveryAndDate]'s nullable delivery-info flow.
 */
@Composable
fun ChatMessageContextMenu(
    message: ChatMessage<*>,
    isIgnored: Boolean,
    onSetShowMenu: (Boolean) -> Unit,
    onAddReaction: (ReactionEnum) -> Unit,
    showMenu: Boolean = false,
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {},
    onIgnoreUser: () -> Unit = {},
    onUndoIgnoreUser: () -> Unit = {},
    onReportUser: () -> Unit = {},
    onEditMessage: (() -> Unit)? = null,
    onDeleteMessage: (() -> Unit)? = null,
) {
    val isPeersMessage = !message.isMyMessage
    Surface {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onSetShowMenu(false) },
            containerColor = BisqTheme.colors.dark_grey40,
        ) {
            ChatReactionInput(
                onAddReaction = { reaction ->
                    onAddReaction(reaction)
                    onSetShowMenu(false)
                },
            )

            HorizontalDivider(
                color = BisqTheme.colors.dark_grey50,
                thickness = 2.dp,
            )

            if (isPeersMessage) {
                DropdownMenuItem(
                    text = { BisqText.SmallRegular("chat.message.reply".i18n()) },
                    leadingIcon = { ReplyIcon(modifier = decorativeIcon) },
                    onClick = {
                        onReply()
                        onSetShowMenu(false)
                    },
                )
            }
            if (!isPeersMessage && onEditMessage != null) {
                DropdownMenuItem(
                    text = { BisqText.SmallRegular("action.edit".i18n()) },
                    leadingIcon = { EditIcon(modifier = decorativeIcon) },
                    onClick = {
                        onEditMessage()
                        onSetShowMenu(false)
                    },
                )
            }
            DropdownMenuItem(
                text = { BisqText.SmallRegular("action.copyToClipboard".i18n()) },
                leadingIcon = { CopyIcon(modifier = decorativeCopyIcon) },
                onClick = {
                    onCopy()
                },
            )
            if (isPeersMessage) {
                if (isIgnored) {
                    DropdownMenuItem(
                        text = { BisqText.SmallRegular("user.profileCard.userActions.undoIgnore".i18n()) },
                        leadingIcon = { EyeIcon(modifier = decorativeIcon) },
                        onClick = {
                            onUndoIgnoreUser()
                            onSetShowMenu(false)
                        },
                    )
                } else {
                    DropdownMenuItem(
                        text = { BisqText.SmallRegular("chat.message.contextMenu.ignoreUser".i18n()) },
                        leadingIcon = { ClosedEyeIcon(modifier = decorativeIcon) },
                        onClick = {
                            onIgnoreUser()
                            onSetShowMenu(false)
                        },
                    )
                }

                DropdownMenuItem(
                    text = { BisqText.SmallRegular("chat.message.contextMenu.reportUser".i18n()) },
                    leadingIcon = { FlagIcon(modifier = decorativeIcon) },
                    onClick = {
                        onReportUser()
                        onSetShowMenu(false)
                    },
                )
            }
            if (!isPeersMessage && onDeleteMessage != null) {
                DropdownMenuItem(
                    text = { BisqText.SmallRegular("action.delete".i18n()) },
                    leadingIcon = { DeleteIcon(modifier = decorativeIcon) },
                    onClick = {
                        onDeleteMessage()
                        onSetShowMenu(false)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChatMessageContextMenuPreview() {
    BisqTheme.Preview {
        ChatMessageContextMenu(
            message = mockMessage(),
            showMenu = true,
            onSetShowMenu = {},
            onAddReaction = {},
            isIgnored = false,
        )
    }
}

@Preview
@Composable
private fun ChatMessageContextMenuIgnoredPreview() {
    BisqTheme.Preview {
        ChatMessageContextMenu(
            message = mockMessage(),
            showMenu = true,
            onSetShowMenu = {},
            onAddReaction = {},
            isIgnored = true,
        )
    }
}

@ExcludeFromCoverage
private fun mockMessage(): BisqEasyOpenTradeMessage {
    val myUserProfile = createMockUserProfile("Bob")
    val peerUserProfile = createMockUserProfile("Alice")

    return createMockBisqEasyOpenTradeMessage(
        id = "msg456",
        text = "Sure! Let's proceed with the payment.",
        senderUserProfile = peerUserProfile,
        myUserProfile = myUserProfile,
        tradeId = "trade123",
    )
}
