package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.LedgerRepository
import de.mkrabs.snablo.app.domain.model.LedgerEntry
import de.mkrabs.snablo.app.domain.model.TransactionKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for balance and history view
 */
data class BalanceUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val history: List<LedgerEntry> = emptyList(),
    val error: String? = null,
    val selectedFilterKind: TransactionKind? = null
)

/**
 * ViewModel for balance and transaction history
 */
class BalanceViewModel(
    private val ledgerRepository: LedgerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BalanceUiState())
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    fun loadHistory(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = ledgerRepository.getHistory(userId)
                val entries = result.getOrThrow()
                val balance = entries.sumOf { it.amountCents } / 100.0
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    balance = balance,
                    history = entries,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load history"
                )
            }
        }
    }

    fun setFilter(kind: TransactionKind?) {
        _uiState.value = _uiState.value.copy(selectedFilterKind = kind)
    }

    fun getFilteredHistory(): List<LedgerEntry> {
        val history = _uiState.value.history
        val filter = _uiState.value.selectedFilterKind
        return if (filter != null) {
            history.filter { entry ->
                when (filter) {
                    TransactionKind.PURCHASE_DIGITAL ->
                        entry.kind == TransactionKind.PURCHASE_DIGITAL || entry.kind == TransactionKind.PURCHASE_CASH_LOGGED
                    TransactionKind.TOPUP_DIGITAL ->
                        entry.kind == TransactionKind.TOPUP_DIGITAL
                    TransactionKind.TOPUP_CASH ->
                        entry.kind == TransactionKind.TOPUP_CASH
                    else -> entry.kind == filter
                }
            }
        } else {
            history
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
