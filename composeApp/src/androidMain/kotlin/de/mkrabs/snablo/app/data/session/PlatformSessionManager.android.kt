package de.mkrabs.snablo.app.data.session

import android.content.Context
import de.mkrabs.snablo.app.SnabloApplication
import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session

private const val PREFS_NAME = "snablo_session"
private const val KEY_SESSION_JSON = "session_json"

private class AndroidPrefsSessionManager(
    private val context: Context
) : SessionManager {

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override suspend fun saveSession(session: Session) {
        prefs.edit().putString(KEY_SESSION_JSON, encodeSession(session)).apply()
    }

    override suspend fun getSession(): Session? {
        val raw = prefs.getString(KEY_SESSION_JSON, null) ?: return null
        return decodeSession(raw)
    }

    override suspend fun clearSession() {
        prefs.edit().remove(KEY_SESSION_JSON).apply()
    }

    override suspend fun hasValidSession(): Boolean {
        val session = getSession() ?: return false
        return session.token.expiresAt > System.currentTimeMillis()
    }

    override suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken? {
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
    val ctx = SnabloApplication.instance
    return AndroidPrefsSessionManager(ctx)
}

