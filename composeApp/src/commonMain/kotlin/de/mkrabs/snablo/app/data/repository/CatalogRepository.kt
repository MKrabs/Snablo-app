package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.domain.model.*

/**
 * Repository for user and catalog operations
 */
interface CatalogRepository {
    suspend fun getCatalogItems(): Result<List<CatalogItem>>
    suspend fun getLocations(): Result<List<Location>>
}

class CatalogRepositoryImpl(
    private val apiClient: PocketBaseClient
) : CatalogRepository {
    override suspend fun getCatalogItems(): Result<List<CatalogItem>> {
        return when (val result = apiClient.getCatalogItems()) {
            is ApiResult.Success -> {
                val items = result.data.items.map { dto ->
                    CatalogItem(
                        id = dto.id,
                        name = dto.name,
                        category = dto.category,
                        imageUrl = dto.imageUrl,
                        description = dto.description,
                        createdAt = dto.created,
                        updatedAt = dto.updated
                    )
                }
                Result.success(items)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getLocations(): Result<List<Location>> {
        return when (val result = apiClient.getLocations()) {
            is ApiResult.Success -> {
                val locations = result.data.items.map { dto ->
                    Location(
                        id = dto.id,
                        name = dto.name,
                        address = dto.address,
                        timeZone = dto.timeZone,
                        createdAt = dto.created,
                        updatedAt = dto.updated
                    )
                }
                Result.success(locations)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}

