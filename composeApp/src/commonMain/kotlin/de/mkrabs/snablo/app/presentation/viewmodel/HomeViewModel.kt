package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.prefs.PlatformPrefs
import de.mkrabs.snablo.app.data.prefs.prefsKeyLastLocation
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
    val inventoryCount: Int,
    val imageUrl: String? = null
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
    private val catalogRepository: CatalogRepository,
    private val prefs: PlatformPrefs = PlatformPrefs()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun addBalance(amountEuro: Int) {
        if (amountEuro <= 0) return
        _uiState.value = _uiState.value.copy(balance = _uiState.value.balance + amountEuro.toDouble())
    }

    fun loadForUser(userId: String, locationId: String? = null, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = if (!isRefresh) true else _uiState.value.isLoading,
                isRefreshing = if (isRefresh) true else _uiState.value.isRefreshing,
                error = null
            )
            try {
                // Load all locations ("corners") and catalog items for name mapping
                val locations = catalogRepository.getLocations().getOrElse { emptyList() }

                // determine which location to use: explicit param -> prefs -> first from server
                val resolvedLocationId = when {
                    !locationId.isNullOrBlank() -> locationId
                    else -> prefs.getString(prefsKeyLastLocation()) ?: locations.firstOrNull()?.id
                }

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

                // Legacy: load slots for selected location (optional) using resolvedLocationId
                val slots = if (resolvedLocationId != null) {
                    shelfRepository.getSlotMappings(resolvedLocationId).getOrElse { emptyList() }
                } else {
                    emptyList()
                }

                // persist resolved location
                if (!resolvedLocationId.isNullOrBlank()) prefs.putString(prefsKeyLastLocation(), resolvedLocationId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    balance = 0.0, // keep existing balance logic or compute elsewhere
                    recentTransactions = emptyList(),
                    corners = corners,
                    slots = slots,
                    selectedLocationId = resolvedLocationId,
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
                prefs.putString(prefsKeyLastLocation(), locationId)
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
    ): CornerUi {
        // Prefer resolved shelf slots (PocketBase `expand=catalogItemId`) to avoid cryptic IDs in UI.
        val resolvedSlots = shelfRepository.getResolvedShelfSlots(location.id).getOrElse { emptyList() }
        if (resolvedSlots.isNotEmpty()) {
            val shelfUis = coroutineScope {
                resolvedSlots
                    .sortedBy { it.slotIndex }
                    .map { slot ->
                        async {
                            val price = shelfRepository.getPrice(location.id, slot.catalogItemId).getOrNull()
                            CornerShelfSlotUi(
                                slotId = slot.slotId,
                                slotIndex = slot.slotIndex,
                                catalogItemId = slot.catalogItemId,
                                itemName = slot.itemName,
                                price = price,
                                inventoryCount = slot.inventoryCount,
                                imageUrl = slot.imageUrl
                            )
                        }
                    }
                    .awaitAll()
            }
            return CornerUi(location = location, shelves = shelfUis)
        }

        val slotMappings = shelfRepository.getSlotMappings(location.id).getOrElse { emptyList() }
        if (slotMappings.isEmpty()) {
            // If a location has no shelves, we still may want to show the location itself.
            // We'll consider it a "corner" with empty shelves.
            return CornerUi(location = location, shelves = emptyList())
        }

        // Show all shelves sorted by index so the UI renders them in order.
        val slotsToShow = slotMappings.sortedBy { it.slotIndex }

        // Fetch prices concurrently; if some prices missing, show null.
        val shelfUis = coroutineScope {
            slotsToShow
                .map { slot ->
                    async {
                        var item = itemById[slot.catalogItemId]
                        // If not present in the preloaded map, try to refresh catalog items once
                        if (item == null) {
                            val refreshed = catalogRepository.getCatalogItems().getOrElse { emptyList() }
                            item = refreshed.find { it.id == slot.catalogItemId }
                        }
                        val itemName = item?.name ?: de.mkrabs.snablo.app.util.formatItemLabel(slot.catalogItemId)
                        val price = shelfRepository.getPrice(location.id, slot.catalogItemId).getOrNull()
                        CornerShelfSlotUi(
                            slotId = slot.id,
                            slotIndex = slot.slotIndex,
                            catalogItemId = slot.catalogItemId,
                            itemName = itemName,
                            price = price,
                            inventoryCount = slot.inventoryCount,
                            imageUrl = item?.imageUrl
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
