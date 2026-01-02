package de.mkrabs.snablo.app.data.auth

import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.repository.AuthRepository
import de.mkrabs.snablo.app.data.session.SessionManager
import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session
import de.mkrabs.snablo.app.domain.model.User

/**
 * Service for managing authentication flow
 */
class AuthService(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val apiClient: PocketBaseClient
) {
    suspend fun login(email: String, password: String): Result<User> {
        // keep original implementation (delegate to apiClient)
        return try {
            val loginResult = apiClient.login(email, password)
            when (loginResult) {
                is de.mkrabs.snablo.app.data.api.ApiResult.Success -> {
                    val response = loginResult.data
                    val user = response.record.toUser()
                    val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000)
                    val token = AuthToken(token = response.token, expiresAt = expiresAt)
                    val session = Session(user = user, token = token)
                    sessionManager.saveSession(session)
                    Result.success(user)
                }
                is de.mkrabs.snablo.app.data.api.ApiResult.Error -> Result.failure(Exception(loginResult.message))
                else -> Result.failure(Exception("Unknown login error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Minimal stub for SSO to avoid unresolved references; platform-specific overrides should replace this
    suspend fun loginWithMicrosoft(): Result<User> {
        return Result.failure(Exception("Microsoft SSO not implemented"))
    }

    suspend fun refreshToken(): Result<User> {
        return try {
            val refreshResult = apiClient.refresh()
            when (refreshResult) {
                is de.mkrabs.snablo.app.data.api.ApiResult.Success -> {
                    val response = refreshResult.data
                    val user = response.record.toUser()
                    val session = sessionManager.getSession()
                    if (session != null) {
                        val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000)
                        val newToken = AuthToken(token = response.token, expiresAt = expiresAt)
                        val newSession = session.copy(user = user, token = newToken)
                        sessionManager.saveSession(newSession)
                    }
                    Result.success(user)
                }
                is de.mkrabs.snablo.app.data.api.ApiResult.Error -> Result.failure(Exception(refreshResult.message))
                else -> Result.failure(Exception("Unknown refresh error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun getCurrentUser(): User? {
        return sessionManager.getSession()?.user
    }

    suspend fun hasValidSession(): Boolean {
        return sessionManager.hasValidSession()
    }

    suspend fun ensureValidToken(): Result<Unit> {
        return try {
            if (sessionManager.hasValidSession()) {
                Result.success(Unit)
            } else {
                val refreshResult = refreshToken()
                if (refreshResult.isSuccess) Result.success(Unit) else Result.failure(Exception("Session expired and refresh failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
