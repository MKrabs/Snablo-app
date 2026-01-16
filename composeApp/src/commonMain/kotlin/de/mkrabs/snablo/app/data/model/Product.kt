package de.mkrabs.snablo.app.data.model

import de.mkrabs.snablo.app.util.formatPrice
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String? = null,
    val priceCents: Int,
    val stockCount: Int,
    val image: String? = null,
    val categoryId: String? = null,
    val order: Int? = null,
    val isActive: Boolean = true
) {
    /**
     * Preis als formatierter String (z.B. "1,50 €")
     */
    val priceFormatted: String
        get() = formatPrice(priceCents)

    /**
     * Stock-Status für UI-Anzeige
     */
    val stockStatus: StockStatus
        get() = when {
            stockCount <= 0 -> StockStatus.EMPTY
            stockCount == 1 -> StockStatus.LOW
            else -> StockStatus.AVAILABLE
        }
}

enum class StockStatus {
    EMPTY,
    LOW,
    AVAILABLE
}

@Serializable
data class ProductListResponse(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<Product>
)
