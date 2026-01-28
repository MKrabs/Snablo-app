package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.domain.model.*

/**
 * Repository for shelf/inventory operations
 */
interface ShelfRepository {
    suspend fun getSlotMappings(locationId: String): Result<List<SlotMapping>>

    /**
     * Tries to fetch shelf slots with a resolved itemName/imageUrl.
     *
     * This is primarily for PocketBase schemas using `corners` + `shelves` where `shelves.catalogItemId`
     * is a relation and needs `expand=catalogItemId` to get a human label.
     *
     * Returns empty list when the compatibility collections are not present.
     */
    suspend fun getResolvedShelfSlots(locationId: String): Result<List<ResolvedShelfSlot>>

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
        return try {
            when (val result = apiClient.getSlotMappings(locationId)) {
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
                    // If we got non-empty slot mappings, return them
                    if (slots.isNotEmpty()) return Result.success(slots)
                }
                is ApiResult.Error -> {
                    // fallthrough to try shelves/corners compatibility endpoints
                }
                else -> {
                    // fallthrough
                }
            }

            // Fallback 1: try the 'shelves' collection (some PocketBase schemas use this)
            // Use expand so we can resolve a human-friendly item name in the UI without additional queries.
            when (
                val shelvesResult = apiClient.getShelves(
                    filter = "cornerId=\"$locationId\"",
                    expand = "catalogItemId"
                )
            ) {
                is ApiResult.Success -> {
                    val shelfItems = shelvesResult.data.items
                    if (shelfItems.isNotEmpty()) {
                        val mapped = shelfItems.map { s ->
                            val created = s.created ?: s.createdAt ?: ""
                            val updated = s.updated ?: s.updatedAt
                            SlotMapping(
                                id = s.id,
                                // use the provided locationId param as the logical location if caller passed a corner id
                                locationId = locationId,
                                slotIndex = s.orderIndex,
                                catalogItemId = s.catalogItemId,
                                inventoryCount = s.stockCount,
                                createdAt = created,
                                updatedAt = updated
                            )
                        }
                        return Result.success(mapped)
                    }
                }
                else -> {
                    // continue to next fallback
                }
            }

            // Fallback 2: query corners for the given locationId, then fetch shelves for each corner
            when (val cornersResult = apiClient.getCorners(filter = "locationId=\"$locationId\"")) {
                is ApiResult.Success -> {
                    val corners = cornersResult.data.items
                    if (corners.isNotEmpty()) {
                        val allShelves = mutableListOf<SlotMapping>()
                        for (corner in corners) {
                            when (
                                val sres = apiClient.getShelves(
                                    filter = "cornerId=\"${corner.id}\"",
                                    expand = "catalogItemId"
                                )
                            ) {
                                is ApiResult.Success -> {
                                    allShelves += sres.data.items.map { s ->
                                        val created = s.created ?: s.createdAt ?: ""
                                        val updated = s.updated ?: s.updatedAt
                                        SlotMapping(
                                            id = s.id,
                                            locationId = corner.locationId ?: locationId,
                                            slotIndex = s.orderIndex,
                                            catalogItemId = s.catalogItemId,
                                            inventoryCount = s.stockCount,
                                            createdAt = created,
                                            updatedAt = updated
                                        )
                                    }
                                }
                                else -> continue
                            }
                        }
                        return Result.success(allShelves)
                    }
                }
                else -> {
                    // no corners found or error
                }
            }

            // If nothing found, return empty list (not necessarily an error)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResolvedShelfSlots(locationId: String): Result<List<ResolvedShelfSlot>> {
        return try {
            // Strategy:
            // 1) Try shelves directly: filter by cornerId=locationId (some installs treat "location" as corner)
            // 2) If empty, try corners for locationId and fetch shelves for each corner.
            // In both cases use expand=catalogItemId to get the display name.

            val directShelves = when (
                val shelvesResult = apiClient.getShelves(
                    filter = "cornerId=\"$locationId\"",
                    expand = "catalogItemId"
                )
            ) {
                is ApiResult.Success -> shelvesResult.data.items
                else -> emptyList()
            }

            if (directShelves.isNotEmpty()) {
                return Result.success(directShelves.toResolvedShelfSlots(logicalLocationId = locationId))
            }

            // Try corners -> shelves
            val corners = when (val cornersResult = apiClient.getCorners(filter = "locationId=\"$locationId\"")) {
                is ApiResult.Success -> cornersResult.data.items
                else -> emptyList()
            }

            if (corners.isEmpty()) return Result.success(emptyList())

            val allShelves = mutableListOf<de.mkrabs.snablo.app.data.api.dto.ShelfDto>()
            for (corner in corners) {
                when (
                    val shelvesRes = apiClient.getShelves(
                        filter = "cornerId=\"${corner.id}\"",
                        expand = "catalogItemId"
                    )
                ) {
                    is ApiResult.Success -> allShelves += shelvesRes.data.items
                    else -> {
                        // continue
                    }
                }
            }

            Result.success(allShelves.toResolvedShelfSlots(logicalLocationId = locationId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPrice(locationId: String, catalogItemId: String): Result<Double> {
        // Primary: try location-prices collection
        when (val result = apiClient.getPrice(locationId, catalogItemId)) {
            is ApiResult.Success -> return Result.success(result.data.price)
            is ApiResult.Error -> {
                // fallthrough to try shelves.priceCents as a fallback
            }
            else -> {
                // fallthrough
            }
        }

        // Fallback: some PocketBase setups store price in the `shelves` collection as priceCents
        try {
            val shelvesRes = apiClient.getShelves(page = 1, perPage = 50, filter = "catalogItemId=\"$catalogItemId\"")
            if (shelvesRes is ApiResult.Success) {
                val found = shelvesRes.data.items.firstOrNull { it.priceCents != null }
                if (found != null) {
                    return Result.success(found.priceEuros() ?: throw Exception("Invalid priceCents"))
                }
            }
        } catch (e: Exception) {
            // ignore fallback errors and return original error below
        }

        return Result.failure(Exception("Price not found for location=$locationId, item=$catalogItemId"))
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

internal fun List<de.mkrabs.snablo.app.data.api.dto.ShelfDto>.toResolvedShelfSlots(
    logicalLocationId: String
): List<ResolvedShelfSlot> {
    return map { s ->
        // If expanded, prefer that. Otherwise fall back to ID formatting.
        val expandedItem = s.expand?.catalogItemId
        val itemName = expandedItem?.name ?: de.mkrabs.snablo.app.util.formatItemLabel(s.catalogItemId)
        val imageUrl = de.mkrabs.snablo.app.data.repository.resolveCatalogItemImageUrl(
            expandedItem?.id ?: s.catalogItemId,
            expandedItem?.imageUrl ?: expandedItem?.img
        )

        ResolvedShelfSlot(
            slotId = s.id,
            // We treat the caller's locationId as the "logical" location. (Corner/location mapping differs by schema.)
            locationId = logicalLocationId,
            slotIndex = s.orderIndex,
            catalogItemId = s.catalogItemId,
            itemName = itemName,
            imageUrl = imageUrl,
            inventoryCount = s.stockCount
        )
    }
}
