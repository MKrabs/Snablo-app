package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.PocketBaseApi
import de.mkrabs.snablo.app.data.model.User
import de.mkrabs.snablo.app.data.session.SessionManager

class AuthRepository(
    private val api: PocketBaseApi,
    private val sessionManager: SessionManager
) {
    val currentUser = sessionManager.currentUser
    val isLoggedIn get() = sessionManager.isLoggedIn
    val isAdmin get() = sessionManager.isAdmin

    suspend fun login(email: String, password: String): Result<User> {
        return api.login(email, password).map { response ->
            sessionManager.setSession(response.token, response.record)
            response.record
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<User> {
        return api.register(email, password, name).map { response ->
            sessionManager.setSession(response.token, response.record)
            response.record
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
