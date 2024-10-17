package network.bisq.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform