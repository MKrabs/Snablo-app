package de.mkrabs.snablo.app.data.prefs

import kotlinx.browser.localStorage

actual class PlatformPrefs actual constructor() {
    actual suspend fun getString(key: String): String? = localStorage.getItem(key)
    actual suspend fun putString(key: String, value: String) { localStorage.setItem(key, value) }
}

