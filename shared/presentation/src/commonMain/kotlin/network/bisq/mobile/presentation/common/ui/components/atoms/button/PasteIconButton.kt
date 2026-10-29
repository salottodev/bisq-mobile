package network.bisq.mobile.presentation.common.ui.components.atoms.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.launch
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.PasteIcon
import network.bisq.mobile.presentation.common.ui.utils.readText

@Composable
fun PasteIconButton(
    onPaste: (String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    CompactIconButton(
        onClick = {
            scope.launch {
                clipboard.getClipEntry()?.readText()?.let { text ->
                    onPaste(text)
                }
            }
        },
    ) {
        PasteIcon()
    }
}
