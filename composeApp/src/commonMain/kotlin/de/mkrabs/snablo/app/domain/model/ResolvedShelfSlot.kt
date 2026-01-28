package de.mkrabs.snablo.app.domain.model

/**
 * A shelf slot that already has a resolved display label/image.
 *
 * This is useful when the backend schema is `shelves` + `catalog_items` and we fetch
 * everything in one request using PocketBase `expand=catalogItemId`.
 */
data class ResolvedShelfSlot(
    val slotId: String,
    val locationId: String,
    val slotIndex: Int,
    val catalogItemId: String,
    val itemName: String,
    val imageUrl: String? = null,
    val inventoryCount: Int = 0
)
