package network.bisq.mobile.domain

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform