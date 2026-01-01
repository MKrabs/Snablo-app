package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.domain.model.User

/**
 * Repository for authentication and user operations
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun refresh(): Result<User>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val apiClient: PocketBaseClient
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return when (val result = apiClient.login(email, password)) {
            is ApiResult.Success -> {
                // Save token is handled by auth service
                Result.success(result.data.record.toUser())
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun refresh(): Result<User> {
        return when (val result = apiClient.refresh()) {
            is ApiResult.Success -> {
                // Save token is handled by auth service
                Result.success(result.data.record.toUser())
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun logout() {
        // Session cleanup is handled by auth service
    }
}

