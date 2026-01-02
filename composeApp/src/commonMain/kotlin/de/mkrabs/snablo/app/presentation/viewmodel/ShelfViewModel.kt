package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.CatalogRepository
import de.mkrabs.snablo.app.data.repository.ShelfRepository
import de.mkrabs.snablo.app.domain.model.CatalogItem
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.SlotMapping
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for location and shelf selection
 */
data class ShelfUiState(
    val isLoading: Boolean = false,
    val locations: List<Location> = emptyList(),
    val catalogItems: List<CatalogItem> = emptyList(),
    val selectedLocation: Location? = null,
    val slots: List<SlotMapping> = emptyList(),
    val prices: Map<String, Double?> = emptyMap(), // slotId -> price
    val error: String? = null
)

/**
 * ViewModel for shelf/inventory operations
 */
class ShelfViewModel(
    private val catalogRepository: CatalogRepository,
    private val shelfRepository: ShelfRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShelfUiState())
    val uiState: StateFlow<ShelfUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val locationsResult = catalogRepository.getLocations()
                val itemsResult = catalogRepository.getCatalogItems()

                val locations = locationsResult.getOrNull() ?: emptyList()
                val items = itemsResult.getOrNull() ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    locations = locations,
                    catalogItems = items,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun selectLocation(location: Location) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedLocation = location, isLoading = true, error = null)
            try {
                val slotsResult = shelfRepository.getSlotMappings(location.id)
                val slots = slotsResult.getOrNull() ?: emptyList()

                // fetch prices for each slot concurrently (use catalogItemId + locationId)
                val priceDeferred = slots.map { slot ->
                    async {
                        val priceRes = shelfRepository.getPrice(location.id, slot.catalogItemId)
                        slot.id to priceRes.getOrNull()
                    }
                }
                val pricesList = priceDeferred.awaitAll().toMap()

                _uiState.value = _uiState.value.copy(
                    slots = slots,
                    prices = pricesList,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load shelf"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
