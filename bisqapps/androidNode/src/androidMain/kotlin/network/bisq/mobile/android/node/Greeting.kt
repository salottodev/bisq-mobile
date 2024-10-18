package network.bisq.mobile.android.node

import network.bisq.mobile.Greeting
import network.bisq.mobile.GreetingFactory

class AndroidNodeGreeting : Greeting() {
    override fun greet(): String {
        return "Hello Node, ${platform.name}!"
    }
}
class AndroidNodeGreetingFactory : GreetingFactory {
    override fun createGreeting() = AndroidNodeGreeting()
}