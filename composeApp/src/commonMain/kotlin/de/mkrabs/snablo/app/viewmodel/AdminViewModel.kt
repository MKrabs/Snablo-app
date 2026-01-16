package de.mkrabs.snablo.app.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.mkrabs.snablo.app.data.model.Category
import de.mkrabs.snablo.app.data.model.Product
import de.mkrabs.snablo.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val productRepository: ProductRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            productRepository.getProducts()
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(products = products)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }

            productRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updateProductStock(product: Product, newStock: Int) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            productRepository.updateProduct(
                product.id,
                product.copy(stockCount = newStock)
            )
                .onSuccess {
                    loadData()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
}

data class AdminUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null
)
