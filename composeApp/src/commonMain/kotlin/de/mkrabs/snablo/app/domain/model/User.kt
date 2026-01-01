package de.mkrabs.snablo.app.domain.model

import kotlinx.serialization.Serializable

enum class UserRole {
    USER,    // Regular colleague (buyer)
    ADMIN    // Location operator (can restock, reconcile, record cash counts)
}

@Serializable
data class User(
    val id: String,              // PocketBase record ID
    val email: String,           // Unique identifier
    val name: String,            // Display name
    val role: UserRole = UserRole.USER,  // USER or ADMIN
    val globalBalance: Double = 0.0,     // In EUR; can be negative (floor: -5 EUR)
    val createdAt: String = "",          // ISO 8601 timestamp
    val updatedAt: String? = null        // Last modification timestamp
)

/**
 * Session token holder for authentication
 */
data class AuthToken(
    val token: String,
    val expiresAt: Long  // Milliseconds since epoch
)

data class Session(
    val user: User,
    val token: AuthToken
)

