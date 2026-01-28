package de.mkrabs.snablo.app.data.api.dto

import kotlinx.serialization.Serializable

/**
 * Partial update for a user record.
 *
 * PocketBase supports partial updates via PATCH on /collections/users/records/{id}
 */
@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val emailVisibility: Boolean? = null,
    val verified: Boolean? = null,
    val name: String? = null,
    val avatar: String? = null,
    val role: String? = null,
    val balanceCents: Int? = null
)
