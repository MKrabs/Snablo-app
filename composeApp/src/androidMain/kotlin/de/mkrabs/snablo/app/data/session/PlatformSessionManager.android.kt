package de.mkrabs.snablo.app.data.session

import android.content.Context
import android.util.Log
import de.mkrabs.snablo.app.SnabloApplication
import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session

private const val PREFS_NAME = "snablo_session"
private const val KEY_SESSION_JSON = "session_json"

private class AndroidPrefsSessionManager(
    private val context: Context
) : SessionManager {

    private val prefs by lazy {
        Log.d("Snablo", "AndroidPrefsSessionManager: obtaining SharedPreferences")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun saveSession(session: Session) {
        Log.d("Snablo", "AndroidPrefsSessionManager.saveSession - saving session")
        prefs.edit().putString(KEY_SESSION_JSON, encodeSession(session)).apply()
    }

    override suspend fun getSession(): Session? {
        Log.d("Snablo", "AndroidPrefsSessionManager.getSession - reading session")
        val raw = prefs.getString(KEY_SESSION_JSON, null) ?: return null
        return decodeSession(raw)
    }

    override suspend fun clearSession() {
        Log.d("Snablo", "AndroidPrefsSessionManager.clearSession - clearing session")
        prefs.edit().remove(KEY_SESSION_JSON).apply()
    }

    override suspend fun hasValidSession(): Boolean {
        Log.d("Snablo", "AndroidPrefsSessionManager.hasValidSession - checking session")
        val session = getSession() ?: return false
        return session.token.expiresAt > System.currentTimeMillis()
    }

    override suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken? {
        Log.d("Snablo", "AndroidPrefsSessionManager.refreshTokenIfNeeded - checking expiry")
        val session = getSession() ?: return null
        val now = System.currentTimeMillis()
        val minutesUntilExpiry = (session.token.expiresAt - now) / (60 * 1000)

        return if (minutesUntilExpiry < 30) {
            runCatching {
                val newToken = refreshFn()
                saveSession(session.copy(token = newToken))
                newToken
            }.getOrNull()
        } else {
            session.token
        }
    }
}

actual fun platformSessionManager(): SessionManager {
    // Use application context (avoid leaking activity)
    Log.d("Snablo", "platformSessionManager - attempting to get SnabloApplication.instance")
    return runCatching {
        val ctx = SnabloApplication.instance
        Log.d("Snablo", "platformSessionManager - obtained application instance: $ctx")
        AndroidPrefsSessionManager(ctx)
    }.getOrElse {
        Log.w("Snablo", "platformSessionManager - Application.instance not available yet, falling back to InMemorySessionManager: ${it.message}")
        InMemorySessionManager()
    }
}
