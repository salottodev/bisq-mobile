package network.bisq.mobile.presentation.common.ui.components.atoms.button

import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.QrCodeIcon
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Test
import kotlin.test.assertEquals

class CompactIconButtonUiTest : BisqComposeUiTestBase() {
    @Test
    fun `tapping the icon invokes the handler`() {
        var clicks = 0
        setTestContent {
            CompactIconButton(onClick = { clicks++ }) { QrCodeIcon() }
        }

        composeTestRule.onNodeWithContentDescription("QR code icon").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `button has the compact trailing icon size`() {
        setTestContent {
            CompactIconButton(onClick = {}) { QrCodeIcon() }
        }

        composeTestRule.onNode(hasClickAction()).assertWidthIsEqualTo(BisqUIConstants.ScreenPadding2X)
    }
}
