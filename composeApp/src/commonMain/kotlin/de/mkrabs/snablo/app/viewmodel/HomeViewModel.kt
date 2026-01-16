package de.mkrabs.snablo.app.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.mkrabs.snablo.app.data.model.PaymentMethod
import de.mkrabs.snablo.app.data.model.Product
import de.mkrabs.snablo.app.data.model.Transaction
import de.mkrabs.snablo.app.data.model.User
import de.mkrabs.snablo.app.data.repository.AuthRepository
import de.mkrabs.snablo.app.data.repository.ProductRepository
import de.mkrabs.snablo.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    val isAdmin: Boolean get() = authRepository.isAdmin

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Produkte laden
            productRepository.getProducts()
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(products = products)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }

            // Balance laden
            transactionRepository.getBalance()
                .onSuccess { balance ->
                    _uiState.value = _uiState.value.copy(balanceCents = balance)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun purchase(product: Product) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true)

            transactionRepository.purchase(product)
                .onSuccess {
                    // Balance neu laden
                    loadData()
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        purchaseSuccess = "\"${product.name}\" gekauft!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = error.message
                    )
                }
        }
    }

    fun topUp(amountCents: Int, method: PaymentMethod) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            transactionRepository.topUp(amountCents, method)
                .onSuccess {
                    loadData()
                    _uiState.value = _uiState.value.copy(
                        topUpSuccess = "Guthaben aufgeladen!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            purchaseSuccess = null,
            topUpSuccess = null,
            error = null
        )
    }

    fun logout() {
        authRepository.logout()
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val products: List<Product> = emptyList(),
    val balanceCents: Int = 0,
    val purchaseSuccess: String? = null,
    val topUpSuccess: String? = null,
    val error: String? = null
) {
    val balanceFormatted: String
        get() = de.mkrabs.snablo.app.util.formatBalance(balanceCents)
}
