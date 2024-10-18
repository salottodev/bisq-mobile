package network.bisq.mobile.android.node

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.App
import network.bisq.mobile.Greeting
import network.bisq.mobile.GreetingFactory
import network.bisq.mobile.GreetingProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GreetingProvider.factory = AndroidNodeGreetingFactory()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}