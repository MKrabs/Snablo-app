package de.mkrabs.snablo.app.data.prefs

import android.content.Context
import de.mkrabs.snablo.app.SnabloApplication

actual class PlatformPrefs actual constructor() {
    private val prefs by lazy { SnabloApplication.instance.getSharedPreferences("snablo_prefs", Context.MODE_PRIVATE) }

    actual suspend fun getString(key: String): String? = prefs.getString(key, null)
    actual suspend fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
}

