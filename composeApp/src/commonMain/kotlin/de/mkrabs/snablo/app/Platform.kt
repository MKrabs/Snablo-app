package de.mkrabs.snablo.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform