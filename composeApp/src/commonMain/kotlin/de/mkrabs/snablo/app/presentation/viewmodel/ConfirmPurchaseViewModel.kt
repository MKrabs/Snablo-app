package de.mkrabs.snablo.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.repository.LedgerRepository
import de.mkrabs.snablo.app.data.repository.ShelfRepository
import de.mkrabs.snablo.app.domain.model.CatalogItem
import de.mkrabs.snablo.app.domain.model.ConfirmPurchaseData
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.PendingUndo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for purchase confirmation
 */
data class ConfirmPurchaseUiState(
    val isLoading: Boolean = false,
    val purchaseData: ConfirmPurchaseData? = null,
    val isConfirmed: Boolean = false,
    val showUndoWindow: Boolean = false,
    val undoTimeRemaining: Int = 0,  // Seconds
    val error: String? = null
)

/**
 * ViewModel for purchase confirmation and undo flow
 */
class ConfirmPurchaseViewModel(
    private val shelfRepository: ShelfRepository,
    private val ledgerRepository: LedgerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfirmPurchaseUiState())
    val uiState: StateFlow<ConfirmPurchaseUiState> = _uiState.asStateFlow()

    private var pendingUndo: PendingUndo? = null
    private val UNDO_WINDOW_SECONDS = 10

    fun loadPurchaseData(
        locationId: String,
        slotId: String,
        catalogItems: List<CatalogItem>,
        userBalance: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val dataResult = shelfRepository.getSlotDetails(locationId, slotId, catalogItems)
                val data = dataResult.getOrThrow()
                val updatedData = data.copy(
                    userBalance = userBalance,
                    canAfford = userBalance >= data.effectivePrice
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    purchaseData = updatedData,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load purchase data"
                )
            }
        }
    }

    fun confirmPurchase(userId: String) {
        val data = _uiState.value.purchaseData ?: return

        if (!data.canAfford) {
            _uiState.value = _uiState.value.copy(error = "Insufficient balance")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = ledgerRepository.recordPurchase(
                    userId = userId,
                    price = data.effectivePrice,
                    locationId = data.location.id,
                    catalogItemId = data.catalogItem.id
                )
                val entry = result.getOrThrow()
                pendingUndo = PendingUndo(
                    originalEntryId = entry.id,
                    expiresAt = System.currentTimeMillis() + (UNDO_WINDOW_SECONDS * 1000L)
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConfirmed = true,
                    showUndoWindow = true,
                    undoTimeRemaining = UNDO_WINDOW_SECONDS,
                    error = null
                )
                startUndoCountdown()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Purchase failed"
                )
            }
        }
    }

    private fun startUndoCountdown() {
        viewModelScope.launch {
            repeat(UNDO_WINDOW_SECONDS) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    undoTimeRemaining = UNDO_WINDOW_SECONDS - (it + 1)
                )
            }
            _uiState.value = _uiState.value.copy(showUndoWindow = false)
            pendingUndo = null
        }
    }

    fun undo(userId: String) {
        val undo = pendingUndo ?: return
        val now = System.currentTimeMillis()

        if (now > undo.expiresAt) {
            _uiState.value = _uiState.value.copy(error = "Undo window expired")
            return
        }

        val data = _uiState.value.purchaseData ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = ledgerRepository.recordUndo(
                    originalEntryId = undo.originalEntryId,
                    amount = data.effectivePrice,
                    userId = userId
                )
                result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showUndoWindow = false,
                    isConfirmed = false,
                    error = "Purchase undone"
                )
                pendingUndo = null
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Undo failed: ${e.message}"
                )
            }
        }
    }

    fun canUndo(): Boolean {
        val undo = pendingUndo ?: return false
        return System.currentTimeMillis() <= undo.expiresAt
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = ConfirmPurchaseUiState()
        pendingUndo = null
    }
}

