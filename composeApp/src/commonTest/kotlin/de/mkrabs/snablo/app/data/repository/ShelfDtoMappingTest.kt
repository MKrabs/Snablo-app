package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.dto.CatalogItemDto
import de.mkrabs.snablo.app.data.api.dto.ShelfDto
import de.mkrabs.snablo.app.data.api.dto.ShelfExpandDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ShelfDtoMappingTest {

    @Test
    fun toResolvedShelfSlots_prefersExpandedCatalogItemName() {
        val shelf = ShelfDto(
            id = "s1",
            cornerId = "c1",
            catalogItemId = "item123",
            orderIndex = 2,
            stockCount = 5,
            expand = ShelfExpandDto(
                catalogItemId = CatalogItemDto(
                    id = "item123",
                    name = "Snickers",
                    img = "snickers.jpg"
                )
            )
        )

        val resolved = listOf(shelf).toResolvedShelfSlots(logicalLocationId = "loc1")
        assertEquals(1, resolved.size)
        assertEquals("Snickers", resolved.first().itemName)
        assertEquals("loc1", resolved.first().locationId)
        assertEquals("snickers.jpg", resolved.first().imageUrl)
        assertEquals(5, resolved.first().inventoryCount)
    }
}
