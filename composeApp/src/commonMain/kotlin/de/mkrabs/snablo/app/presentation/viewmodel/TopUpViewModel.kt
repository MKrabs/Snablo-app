package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.LedgerRepository
import de.mkrabs.snablo.app.domain.model.TransactionKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for top-up operations
 */
data class TopUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for top-up operations (cash or digital)
 */
class TopUpViewModel(
    private val ledgerRepository: LedgerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    fun topUp(
        kind: TransactionKind,
        amount: Double,
        locationId: String? = null,
        userId: String = ""  // Should be passed from caller
    ) {
        if (kind != TransactionKind.TOPUP_CASH && kind != TransactionKind.TOPUP_DIGITAL) {
            _uiState.value = _uiState.value.copy(error = "Invalid top-up kind")
            return
        }

        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Amount must be greater than 0")
            return
        }

        if (kind == TransactionKind.TOPUP_CASH && locationId == null) {
            _uiState.value = _uiState.value.copy(error = "Location required for cash top-up")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = ledgerRepository.recordTopUp(
                    userId = userId,
                    amount = amount,
                    kind = kind,
                    locationId = locationId
                )
                result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Top-up failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = TopUpUiState()
    }
}
