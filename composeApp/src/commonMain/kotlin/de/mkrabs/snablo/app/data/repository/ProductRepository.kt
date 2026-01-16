package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.PocketBaseApi
import de.mkrabs.snablo.app.data.model.Category
import de.mkrabs.snablo.app.data.model.Product

class ProductRepository(
    private val api: PocketBaseApi
) {
    suspend fun getProducts(): Result<List<Product>> = api.getProducts()

    suspend fun getProduct(id: String): Result<Product> = api.getProduct(id)

    suspend fun getCategories(): Result<List<Category>> = api.getCategories()

    suspend fun createProduct(product: Product): Result<Product> = api.createProduct(product)

    suspend fun updateProduct(id: String, product: Product): Result<Product> = api.updateProduct(id, product)
}
