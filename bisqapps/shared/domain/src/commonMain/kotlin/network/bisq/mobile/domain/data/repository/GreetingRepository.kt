package network.bisq.mobile.domain.data.repository

import network.bisq.mobile.domain.DefaultGreetingFactory
import network.bisq.mobile.domain.GreetingFactory

// TODO generalize repository behaviour, what it can and can't do, what it depends on (data sources?)
// for now as a quick example just uses an in mem factory
class GreetingRepository(private val greetingFactory: GreetingFactory = DefaultGreetingFactory()) {
    // Secondary constructor for Swift compatibility
    constructor() : this(DefaultGreetingFactory())

    fun getValue() = greetingFactory.createGreeting().greet()
}