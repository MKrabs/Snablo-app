package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.CatalogRepository
import de.mkrabs.snablo.app.data.repository.LedgerRepository
import de.mkrabs.snablo.app.data.repository.ShelfRepository
import de.mkrabs.snablo.app.domain.model.CatalogItem
import de.mkrabs.snablo.app.domain.model.LedgerEntry
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.SlotMapping
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI model for a single shelf slot to show inside a corner card.
 */
data class CornerShelfSlotUi(
    val slotId: String,
    val slotIndex: Int,
    val catalogItemId: String,
    val itemName: String,
    val price: Double?,
    val inventoryCount: Int
)

/**
 * UI model for a "corner" to show on the dashboard.
 * For now, a corner maps 1:1 to a Location.
 */
data class CornerUi(
    val location: Location,
    val shelves: List<CornerShelfSlotUi>
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val recentTransactions: List<LedgerEntry> = emptyList(),

    // "Corners" (locations) section
    val corners: List<CornerUi> = emptyList(),

    // Legacy: selected location slots for quick actions
    val slots: List<SlotMapping> = emptyList(),
    val selectedLocationId: String? = null,

    val error: String? = null,

    // Debugging support
    val locationsDebugText: String? = null,
    val isLocationsRefreshing: Boolean = false,

    // Global refresh indicator for pull-to-refresh
    val isRefreshing: Boolean = false
)

class HomeViewModel(
    private val ledgerRepository: LedgerRepository,
    private val shelfRepository: ShelfRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadForUser(userId: String, locationId: String? = null, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = if (!isRefresh) true else _uiState.value.isLoading,
                isRefreshing = if (isRefresh) true else _uiState.value.isRefreshing,
                error = null
            )
            try {
                // Load history and compute balance
                val historyResult = ledgerRepository.getHistory(userId)
                val entries = historyResult.getOrElse { emptyList() }
                val balance = entries.sumOf { it.amount }

                // Load all locations ("corners") and catalog items for name mapping
                val locations = catalogRepository.getLocations().getOrElse { emptyList() }
                val catalogItems = catalogRepository.getCatalogItems().getOrElse { emptyList() }
                val itemById = catalogItems.associateBy { it.id }

                val corners: List<CornerUi> = if (locations.isEmpty()) {
                    emptyList()
                } else {
                    // Build each corner concurrently; keep it small and robust
                    coroutineScope {
                        locations
                            .map { loc -> async { buildCornerUi(loc, itemById) } }
                            .awaitAll()
                            .filterNotNull()
                    }
                }

                // Legacy: load slots for selected location (optional)
                val locationToUse = locationId ?: _uiState.value.selectedLocationId
                val slots = if (locationToUse != null) {
                    shelfRepository.getSlotMappings(locationToUse).getOrElse { emptyList() }
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    balance = balance,
                    recentTransactions = entries.take(10),
                    corners = corners,
                    slots = slots,
                    selectedLocationId = locationToUse,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun selectLocation(locationId: String) {
        _uiState.value = _uiState.value.copy(selectedLocationId = locationId)
        viewModelScope.launch {
            try {
                val slots = shelfRepository.getSlotMappings(locationId).getOrElse { emptyList() }
                _uiState.value = _uiState.value.copy(slots = slots)
            } catch (_: Exception) {
                // ignore for now
            }
        }
    }

    private suspend fun buildCornerUi(
        location: Location,
        itemById: Map<String, CatalogItem>
    ): CornerUi? {
        val slotMappings = shelfRepository.getSlotMappings(location.id).getOrElse { emptyList() }
        if (slotMappings.isEmpty()) {
            // If a location has no shelves, we still may want to show the location itself.
            // We'll consider it a "corner" with empty shelves.
            return CornerUi(location = location, shelves = emptyList())
        }

        // Limit shelves displayed in card to keep UI snappy.
        val slotsToShow = slotMappings.sortedBy { it.slotIndex }.take(6)

        // Fetch prices concurrently; if some prices missing, show null.
        val shelfUis = coroutineScope {
            slotsToShow
                .map { slot ->
                    async {
                        val item = itemById[slot.catalogItemId]
                        val itemName = item?.name ?: slot.catalogItemId
                        val price = shelfRepository.getPrice(location.id, slot.catalogItemId).getOrNull()
                        CornerShelfSlotUi(
                            slotId = slot.id,
                            slotIndex = slot.slotIndex,
                            catalogItemId = slot.catalogItemId,
                            itemName = itemName,
                            price = price,
                            inventoryCount = slot.inventoryCount
                        )
                    }
                }
                .awaitAll()
        }

        return CornerUi(location = location, shelves = shelfUis)
    }

    fun refreshLocationsDebug() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLocationsRefreshing = true)
            try {
                val locations = catalogRepository.getLocations().getOrElse { emptyList() }
                val text = if (locations.isEmpty()) {
                    "No locations returned"
                } else {
                    locations.joinToString(separator = "\n") { loc -> "${loc.id}: ${loc.name}" }
                }
                _uiState.value = _uiState.value.copy(
                    isLocationsRefreshing = false,
                    locationsDebugText = text
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLocationsRefreshing = false,
                    locationsDebugText = "ERROR: ${e.message ?: e::class.simpleName}"
                )
            }
        }
    }
}
