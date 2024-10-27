package network.bisq.mobile.android.node

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.domain.data.repository.GreetingRepository
import network.bisq.mobile.presentation.MainPresenter
import network.bisq.mobile.presentation.ui.App

class MainActivity : ComponentActivity() {
    // TODO use a DI framework to provide implementations
    private val greetingRepository = GreetingRepository(AndroidNodeGreetingFactory())
    private val presenter = MainPresenter(greetingRepository)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attachView(this)

        setContent {
            App(presenter)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.detachView()
        presenter.onDestroy()
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(MainPresenter(GreetingRepository(AndroidNodeGreetingFactory())))
}