package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.api.dto.UpdateUserRequest
import de.mkrabs.snablo.app.domain.model.User

/**
 * Repository for user profile operations
 */
interface UserRepository {
    suspend fun listUsers(
        page: Int = 1,
        perPage: Int = 30,
        sort: String? = null,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null,
        skipTotal: Boolean? = null
    ): Result<de.mkrabs.snablo.app.data.api.dto.PaginatedResponse<User>>

    suspend fun getAllUsers(
        sort: String? = null,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null
    ): Result<List<User>>

    suspend fun getFirstUser(
        filter: String,
        expand: String? = null,
        fields: String? = null
    ): Result<User>

    suspend fun updateUser(
        userId: String,
        name: String? = null,
        email: String? = null,
        emailVisibility: Boolean? = null,
        verified: Boolean? = null,
        avatar: String? = null,
        role: String? = null,
        balanceCents: Int? = null
    ): Result<User>
}

class UserRepositoryImpl(
    private val apiClient: PocketBaseClient
) : UserRepository {
    override suspend fun listUsers(
        page: Int,
        perPage: Int,
        sort: String?,
        filter: String?,
        expand: String?,
        fields: String?,
        skipTotal: Boolean?
    ): Result<de.mkrabs.snablo.app.data.api.dto.PaginatedResponse<User>> {
        return when (
            val result = apiClient.getUsersList(
                page = page,
                perPage = perPage,
                sort = sort,
                filter = filter,
                expand = expand,
                fields = fields,
                skipTotal = skipTotal
            )
        ) {
            is ApiResult.Success -> {
                val mapped = de.mkrabs.snablo.app.data.api.dto.PaginatedResponse(
                    page = result.data.page,
                    perPage = result.data.perPage,
                    totalItems = result.data.totalItems,
                    items = result.data.items.map { it.toUser() }
                )
                Result.success(mapped)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getAllUsers(
        sort: String?,
        filter: String?,
        expand: String?,
        fields: String?
    ): Result<List<User>> {
        return when (val result = apiClient.getUsersFullList(sort = sort, filter = filter, expand = expand, fields = fields)) {
            is ApiResult.Success -> Result.success(result.data.map { it.toUser() })
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getFirstUser(filter: String, expand: String?, fields: String?): Result<User> {
        return when (val result = apiClient.getFirstUser(filter = filter, expand = expand, fields = fields)) {
            is ApiResult.Success -> Result.success(result.data.toUser())
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun updateUser(
        userId: String,
        name: String?,
        email: String?,
        emailVisibility: Boolean?,
        verified: Boolean?,
        avatar: String?,
        role: String?,
        balanceCents: Int?
    ): Result<User> {
        val request = UpdateUserRequest(
            name = name,
            email = email,
            emailVisibility = emailVisibility,
            verified = verified,
            avatar = avatar,
            role = role,
            balanceCents = balanceCents
        )

        return when (val result = apiClient.updateUser(userId, request)) {
            is ApiResult.Success -> Result.success(result.data.toUser())
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}
