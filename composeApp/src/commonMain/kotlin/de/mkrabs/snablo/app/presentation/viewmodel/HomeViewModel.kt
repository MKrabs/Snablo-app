package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.LedgerRepository
import de.mkrabs.snablo.app.data.repository.ShelfRepository
import de.mkrabs.snablo.app.domain.model.LedgerEntry
import de.mkrabs.snablo.app.domain.model.SlotMapping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val recentTransactions: List<LedgerEntry> = emptyList(),
    val slots: List<SlotMapping> = emptyList(),
    val selectedLocationId: String? = null,
    val error: String? = null
)

class HomeViewModel(
    private val ledgerRepository: LedgerRepository,
    private val shelfRepository: ShelfRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadForUser(userId: String, locationId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load history and compute balance
                val historyResult = ledgerRepository.getHistory(userId)
                val entries = historyResult.getOrElse { emptyList() }
                val balance = entries.sumOf { it.amount }

                // Load slots for given location or selected
                val locationToUse = locationId ?: _uiState.value.selectedLocationId
                val slots = if (locationToUse != null) {
                    val slotRes = shelfRepository.getSlotMappings(locationToUse)
                    slotRes.getOrElse { emptyList() }
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    balance = balance,
                    recentTransactions = entries.take(10),
                    slots = slots,
                    selectedLocationId = locationToUse,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun selectLocation(locationId: String) {
        _uiState.value = _uiState.value.copy(selectedLocationId = locationId)
        // reload slots for the selected location
        viewModelScope.launch {
            try {
                val slotRes = shelfRepository.getSlotMappings(locationId)
                val slots = slotRes.getOrElse { emptyList() }
                _uiState.value = _uiState.value.copy(slots = slots)
            } catch (_: Exception) { /* ignore for now */ }
        }
    }
}
