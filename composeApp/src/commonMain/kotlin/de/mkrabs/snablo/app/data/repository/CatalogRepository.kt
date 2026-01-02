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
        // Primary: try standard locations collection
        when (val result = apiClient.getLocations()) {
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
                if (locations.isNotEmpty()) return Result.success(locations)
            }
            is ApiResult.Error -> {
                // fallthrough to try other sources
            }
            else -> {
                // fallthrough
            }
        }

        // Fallback 1: try corners collection (if available)
        when (val cornersResult = apiClient.getCorners()) {
            is ApiResult.Success -> {
                val corners = cornersResult.data.items
                if (corners.isNotEmpty()) {
                    val mapped = corners.map { c ->
                        Location(
                            id = c.id,
                            name = c.name,
                            address = null,
                            timeZone = "Europe/Berlin",
                            createdAt = c.created ?: c.createdAt ?: "",
                            updatedAt = c.updated ?: c.updatedAt
                        )
                    }
                    return Result.success(mapped)
                }
            }
            else -> {
                // fallthrough
            }
        }

        // Fallback 2: derive locations from shelves grouping by cornerId
        when (val shelvesRes = apiClient.getShelves(page = 1, perPage = 200)) {
            is ApiResult.Success -> {
                val shelfItems = shelvesRes.data.items
                if (shelfItems.isNotEmpty()) {
                    val ids = shelfItems.mapNotNull { it.cornerId }.distinct()
                    val inferred = ids.map { id ->
                        val short = if (id.length > 6) id.take(6) else id
                        Location(
                            id = id,
                            name = "Corner $short",
                            address = null,
                            timeZone = "Europe/Berlin",
                            createdAt = "",
                            updatedAt = null
                        )
                    }
                    return Result.success(inferred)
                }
            }
            else -> {
                // no shelves or error
            }
        }

        // Nothing found
        return Result.success(emptyList())
    }
}
