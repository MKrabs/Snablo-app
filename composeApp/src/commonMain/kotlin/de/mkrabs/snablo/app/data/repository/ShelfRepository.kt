package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.domain.model.*

/**
 * Repository for shelf/inventory operations
 */
interface ShelfRepository {
    suspend fun getSlotMappings(locationId: String): Result<List<SlotMapping>>
    suspend fun getPrice(locationId: String, catalogItemId: String): Result<Double>
    suspend fun getSlotDetails(
        locationId: String,
        slotId: String,
        catalogItems: List<CatalogItem>
    ): Result<ConfirmPurchaseData>
}

class ShelfRepositoryImpl(
    private val apiClient: PocketBaseClient
) : ShelfRepository {
    override suspend fun getSlotMappings(locationId: String): Result<List<SlotMapping>> {
        return when (val result = apiClient.getSlotMappings(locationId)) {
            is ApiResult.Success -> {
                val slots = result.data.items.map { dto ->
                    SlotMapping(
                        id = dto.id,
                        locationId = dto.locationId,
                        slotIndex = dto.slotIndex,
                        catalogItemId = dto.catalogItemId,
                        inventoryCount = dto.inventoryCount,
                        createdAt = dto.created,
                        updatedAt = dto.updated
                    )
                }
                Result.success(slots)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getPrice(locationId: String, catalogItemId: String): Result<Double> {
        return when (val result = apiClient.getPrice(locationId, catalogItemId)) {
            is ApiResult.Success -> Result.success(result.data.price)
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getSlotDetails(
        locationId: String,
        slotId: String,
        catalogItems: List<CatalogItem>
    ): Result<ConfirmPurchaseData> {
        return try {
            // Get slot mapping
            val slotResult = getSlotMappings(locationId)
            val slots = slotResult.getOrThrow()
            val slot = slots.find { it.id == slotId }
                ?: throw Exception("Slot not found")

            // Get catalog item
            val item = catalogItems.find { it.id == slot.catalogItemId }
                ?: throw Exception("Item not found")

            // Get price
            val price = getPrice(locationId, slot.catalogItemId).getOrThrow()

            // Get location (assume we have it from context)
            // For now, create a minimal location - this should be passed in real scenario
            val location = Location(id = locationId, name = "")

            Result.success(
                ConfirmPurchaseData(
                    slotId = slotId,
                    catalogItem = item,
                    location = location,
                    effectivePrice = price,
                    userBalance = 0.0,  // Will be set by caller
                    canAfford = false   // Will be set by caller
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

