package de.mkrabs.snablo.app.data.session

import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session
import kotlinx.browser.localStorage

private const val KEY_SESSION_JSON = "snablo_session_json"

private class WebLocalStorageSessionManager : SessionManager {
    override suspend fun saveSession(session: Session) {
        localStorage.setItem(KEY_SESSION_JSON, encodeSession(session))
    }

    override suspend fun getSession(): Session? {
        val raw = localStorage.getItem(KEY_SESSION_JSON) ?: return null
        return decodeSession(raw)
    }

    override suspend fun clearSession() {
        localStorage.removeItem(KEY_SESSION_JSON)
    }

    override suspend fun hasValidSession(): Boolean {
        val session = getSession() ?: return false
        return session.token.expiresAt > kotlin.js.Date.now().toLong()
    }

    override suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken? {
        val session = getSession() ?: return null
        val now = kotlin.js.Date.now().toLong()
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

actual fun platformSessionManager(): SessionManager = WebLocalStorageSessionManager()
