package de.mkrabs.snablo.app.data.auth

import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.repository.AuthRepository
import de.mkrabs.snablo.app.data.session.SessionManager
import de.mkrabs.snablo.app.domain.model.AuthToken
import de.mkrabs.snablo.app.domain.model.Session
import de.mkrabs.snablo.app.domain.model.User
import kotlin.math.max

/**
 * Service for managing authentication flow
 */
class AuthService(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val apiClient: PocketBaseClient
) {
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Call login endpoint
            val loginResult = apiClient.login(email, password)

            when (loginResult) {
                is de.mkrabs.snablo.app.data.api.ApiResult.Success -> {
                    val response = loginResult.data
                    val user = response.record.toUser()

                    // Parse JWT expiry (simplified: assume 1 hour from now)
                    // In production, extract from JWT payload
                    val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour

                    val token = AuthToken(
                        token = response.token,
                        expiresAt = expiresAt
                    )

                    val session = Session(user = user, token = token)
                    sessionManager.saveSession(session)

                    Result.success(user)
                }
                is de.mkrabs.snablo.app.data.api.ApiResult.Error -> {
                    Result.failure(Exception(loginResult.message))
                }
                else -> Result.failure(Exception("Unknown login error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
                        val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour
                        val newToken = AuthToken(
                            token = response.token,
                            expiresAt = expiresAt
                        )
                        val newSession = session.copy(user = user, token = newToken)
                        sessionManager.saveSession(newSession)
                    }

                    Result.success(user)
                }
                is de.mkrabs.snablo.app.data.api.ApiResult.Error -> {
                    Result.failure(Exception(refreshResult.message))
                }
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
                // Try to refresh
                val refreshResult = refreshToken()
                if (refreshResult.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Session expired and refresh failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

