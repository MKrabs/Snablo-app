package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.ReconciliationRepository
import de.mkrabs.snablo.app.domain.model.CashCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for reconciliation (cash counts)
 */
data class ReconciliationUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val cashCounts: List<CashCount> = emptyList(),
    val lastCashCount: CashCount? = null,
    val error: String? = null
)

/**
 * ViewModel for reconciliation and cash count operations
 */
class ReconciliationViewModel(
    private val reconciliationRepository: ReconciliationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReconciliationUiState())
    val uiState: StateFlow<ReconciliationUiState> = _uiState.asStateFlow()

    fun loadCashCounts(locationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = reconciliationRepository.getCashCounts(locationId)
                val counts = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cashCounts = counts,
                    lastCashCount = counts.firstOrNull(),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load cash counts"
                )
            }
        }
    }

    fun recordCashCount(
        locationId: String,
        countedCash: Double,
        notes: String? = null
    ) {
        if (countedCash < 0) {
            _uiState.value = _uiState.value.copy(error = "Counted cash cannot be negative")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = reconciliationRepository.recordCashCount(
                    locationId = locationId,
                    countedCash = countedCash,
                    notes = notes
                )
                val newCashCount = result.getOrThrow()

                // Update the list with the new count
                val updatedCounts = listOf(newCashCount) + _uiState.value.cashCounts
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    cashCounts = updatedCounts,
                    lastCashCount = newCashCount,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to record cash count"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = ReconciliationUiState()
    }
}

