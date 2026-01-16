package de.mkrabs.snablo.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String? = null,
    val role: UserRole,
    val verified: Boolean = false
)

@Serializable
enum class UserRole {
    user,
    admin
}

/**
 * Response von PocketBase Auth-Endpoints
 */
@Serializable
data class AuthResponse(
    val token: String,
    val record: User
)
