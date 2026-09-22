package network.bisq.mobile.presentation.common.ui.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Reports the window hosting the composition, so a screen test on `createComposeRule()` can
 * assert that the screen applied [SecureScreenEffect].
 */
@Composable
fun CaptureHostWindow(onWindow: (Window) -> Unit) {
    val context = LocalContext.current
    SideEffect { onWindow(context.findHostActivity().window) }
}

val Window.isSecure: Boolean
    get() = attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0

private tailrec fun Context.findHostActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findHostActivity()
        else -> error("Composition is not hosted in an Activity")
    }
