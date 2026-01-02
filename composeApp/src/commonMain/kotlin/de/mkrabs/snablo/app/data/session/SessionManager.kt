package de.mkrabs.snablo.app.data.session

import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session

/**
 * Manages user session: token storage, retrieval, and validation
 */
interface SessionManager {
    suspend fun saveSession(session: Session)
    suspend fun getSession(): Session?
    suspend fun clearSession()
    suspend fun hasValidSession(): Boolean
    suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken?
}

/**
 * In-memory session manager for commonMain (will be overridden in androidMain)
 */
class InMemorySessionManager : SessionManager {
    private var currentSession: Session? = null

    override suspend fun saveSession(session: Session) {
        currentSession = session
    }

    override suspend fun getSession(): Session? = currentSession

    override suspend fun clearSession() {
        currentSession = null
    }

    override suspend fun hasValidSession(): Boolean {
        val session = currentSession ?: return false
        val now = System.currentTimeMillis()
        return session.token.expiresAt > now
    }

    override suspend fun refreshTokenIfNeeded(refreshFn: suspend () -> AuthToken): AuthToken? {
        val session = currentSession ?: return null
        val now = System.currentTimeMillis()
        val minutesUntilExpiry = (session.token.expiresAt - now) / (60 * 1000)

        // Refresh if less than 30 minutes until expiry
        return if (minutesUntilExpiry < 30) {
            try {
                val newToken = refreshFn()
                currentSession = session.copy(token = newToken)
                newToken
            } catch (e: Exception) {
                null
            }
        } else {
            session.token
        }
    }
}

/**
 * Default SessionManager used by the app.
 *
 * Note: this keeps the user logged in for the lifetime of the running app.
 * For true persistence across app restarts, add a platform-specific implementation.
 */
fun defaultSessionManager(): SessionManager = InMemorySessionManager()
