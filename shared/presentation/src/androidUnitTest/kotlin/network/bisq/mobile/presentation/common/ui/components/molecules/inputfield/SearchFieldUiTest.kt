package network.bisq.mobile.presentation.common.ui.components.molecules.inputfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import network.bisq.mobile.test.presentation.compose.BisqComposeUiTestBase
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFieldUiTest : BisqComposeUiTestBase() {
    /**
     * The clear button is drawn in an overlay the field's own `Layout` pins to the right edge, so a
     * caller that fixes the width used to stretch it across the whole field and leave the icon
     * centred on top of the text. Read through the unmerged tree: the merged node is the button,
     * which is what stretches, and the question is where the icon inside it ends up.
     */
    @Test
    fun `the clear button stays at the right edge when the caller fixes the width`() {
        setTestContent {
            BisqSearchField(
                value = "bitcoin",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val fieldRight = composeTestRule.onRoot().getUnclippedBoundsInRoot().right
        val iconRight =
            composeTestRule
                .onNodeWithContentDescription("close", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
                .right

        assertTrue(
            "clear icon ends at $iconRight, too far from the field's right edge at $fieldRight",
            fieldRight - iconRight < 24.dp,
        )
    }
}
