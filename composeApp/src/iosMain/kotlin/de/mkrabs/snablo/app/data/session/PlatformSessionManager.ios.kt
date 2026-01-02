package de.mkrabs.snablo.app.data.session

import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session
import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults

private const val KEY_SESSION_JSON = "snablo_session_json"

private class IosUserDefaultsSessionManager : SessionManager {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveSession(session: Session) {
        defaults.setObject(encodeSession(session), forKey = KEY_SESSION_JSON)
    }

    override suspend fun getSession(): Session? {
        val raw = defaults.stringForKey(KEY_SESSION_JSON) ?: return null
        return decodeSession(raw)
    }

    override suspend fun clearSession() {
        defaults.removeObjectForKey(KEY_SESSION_JSON)
    }

    override suspend fun hasValidSession(): Boolean {
        val session = getSession() ?: return false
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
        return session.token.expiresAt > now
    }

    override suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken? {
        val session = getSession() ?: return null
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
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

actual fun platformSessionManager(): SessionManager = IosUserDefaultsSessionManager()
