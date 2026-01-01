package de.mkrabs.snablo.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CatalogItem(
    val id: String,
    val name: String,              // e.g., "Snickers", "Orange Juice"
    val category: String,          // e.g., "Candy", "Beverage"
    val imageUrl: String? = null,  // Optional image
    val description: String? = null,
    val createdAt: String = "",
    val updatedAt: String? = null
)

@Serializable
data class Location(
    val id: String,
    val name: String,              // e.g., "Main Office - 1st Floor"
    val address: String? = null,
    val timeZone: String = "Europe/Berlin",
    val createdAt: String = "",
    val updatedAt: String? = null
)

enum class StockState {
    EMPTY,  // 0 items
    ONE,    // 1-2 items
    MANY    // 3+ items
}

@Serializable
data class SlotMapping(
    val id: String,
    val locationId: String,
    val slotIndex: Int,            // 0-based position on shelf (row, col encoded as int)
    val catalogItemId: String,     // Which snack is in this slot
    val inventoryCount: Int = 0,   // Current count of items in slot
    val createdAt: String = "",
    val updatedAt: String? = null
) {
    val stockState: StockState
        get() = when {
            inventoryCount == 0 -> StockState.EMPTY
            inventoryCount <= 2 -> StockState.ONE
            else -> StockState.MANY
        }
}

@Serializable
data class LocationPrice(
    val id: String,
    val locationId: String,
    val catalogItemId: String,
    val price: Double,             // EUR; e.g., 1.00
    val validFrom: String = "",    // ISO 8601 timestamp (when price becomes effective)
    val validUntil: String? = null, // ISO 8601 timestamp (null = currently active)
    val createdAt: String = "",
    val updatedAt: String? = null
)

/**
 * Contains full details needed for purchase confirmation
 */
data class ConfirmPurchaseData(
    val slotId: String,                    // SlotMapping.id
    val catalogItem: CatalogItem,
    val location: Location,
    val effectivePrice: Double,            // Current price for this item at this location
    val userBalance: Double,               // User's current global balance
    val canAfford: Boolean                 // userBalance >= effectivePrice
)

/**
 * NFC token resolution result
 */
data class NfcTokenResolution(
    val tokenId: String,
    val slotId: String,
    val locationId: String
)

