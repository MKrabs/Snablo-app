package de.mkrabs.snablo.app.data.prefs

/** Simple key/value preferences — platform-specific implementations persist across restarts. */
expect class PlatformPrefs() {
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
}

fun prefsKeyLastLocation() = "last_location_id"

