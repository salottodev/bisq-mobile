package network.bisq.mobile.presentation.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.bisq.mobile.presentation.ui.theme.BisqTheme

@Composable
fun BisqScrollLayout(
    innerPadding: PaddingValues = PaddingValues(top = 48.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement ?: Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(color = BisqTheme.colors.backgroundColor)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}
