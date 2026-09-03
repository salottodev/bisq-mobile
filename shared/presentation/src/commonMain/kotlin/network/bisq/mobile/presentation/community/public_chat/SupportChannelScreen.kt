package network.bisq.mobile.presentation.community.public_chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.layout.BisqScaffold
import network.bisq.mobile.presentation.common.ui.components.molecules.TopBar

/**
 * The in-app Support channel. It is the shared destination of the two entry points #1746 asks to
 * coexist: the Community hub's pinned "Need help?" quick access, and More → Help. The same thread
 * the Discussions segment renders, on the other domain bisq2 serves through the same channel type —
 * desktop does the same, varying only `chatChannelDomain`
 * (`CommonChatTabController`/`CommonChatTabView`).
 *
 * Support is deliberately not a hub segment: bisq 2 keeps official support institutionally separate
 * from casual chat, so this is a screen of its own with its own back button rather than a tab.
 *
 * `BisqScaffold` rather than `ChatScaffold`, which owns a `ChatInputBottomBar`:
 * [PublicChatThreadContent] renders its own composer, so that scaffold would give the screen two.
 *
 * The title comes from the domain's own i18n key rather than the channel's `channelTitle` — the
 * screen serves one fixed domain, and the channel need not have loaded for the chrome to be right.
 */
@Composable
fun SupportChannelScreen() {
    BisqScaffold(
        topBar = { TopBar("chat.channelDomain.SUPPORT".i18n(), showUserAvatar = false) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PublicChatThread(ChatChannelDomainEnum.SUPPORT)
        }
    }
}
