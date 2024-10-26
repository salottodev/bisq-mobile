package network.bisq.mobile

open class Greeting {
    protected val platform = getPlatform()

    open fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}

interface GreetingFactory {
    fun createGreeting(): Greeting
}

class DefaultGreetingFactory : GreetingFactory {
    override fun createGreeting() = Greeting()
}