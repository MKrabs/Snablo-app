package de.mkrabs.snablo.app.data.api

import de.mkrabs.snablo.app.data.model.*
import de.mkrabs.snablo.app.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * PocketBase API Client
 */
class PocketBaseApi(
    private val httpClient: HttpClient,
    private val sessionManager: SessionManager
) {
    // TODO: Aus Config/Environment laden
    private val baseUrl = "http://10.0.2.2:8090" // Android Emulator localhost

    private fun HttpRequestBuilder.addAuth() {
        sessionManager.token?.let { token ->
            header("Authorization", "Bearer $token")
        }
    }

    // ==================== AUTH ====================

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/collections/users/auth-with-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("identity" to email, "password" to password))
        }.body()
    }

    suspend fun register(email: String, password: String, name: String): Result<AuthResponse> = runCatching {
        // Erst User erstellen
        httpClient.post("$baseUrl/api/collections/users/records") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "email" to email,
                "password" to password,
                "passwordConfirm" to password,
                "name" to name,
                "role" to "user"
            ))
        }.body<User>()

        // Dann einloggen
        login(email, password).getOrThrow()
    }

    suspend fun getCurrentUser(): Result<User> = runCatching {
        httpClient.get("$baseUrl/api/collections/users/auth-refresh") {
            addAuth()
        }.body<AuthResponse>().record
    }

    // ==================== PRODUCTS ====================

    suspend fun getProducts(): Result<List<Product>> = runCatching {
        httpClient.get("$baseUrl/api/collections/products/records") {
            addAuth()
            parameter("filter", "isActive=true")
            parameter("sort", "order,name")
        }.body<ProductListResponse>().items
    }

    suspend fun getProduct(id: String): Result<Product> = runCatching {
        httpClient.get("$baseUrl/api/collections/products/records/$id") {
            addAuth()
        }.body()
    }

    suspend fun createProduct(product: Product): Result<Product> = runCatching {
        httpClient.post("$baseUrl/api/collections/products/records") {
            addAuth()
            contentType(ContentType.Application.Json)
            setBody(product)
        }.body()
    }

    suspend fun updateProduct(id: String, product: Product): Result<Product> = runCatching {
        httpClient.patch("$baseUrl/api/collections/products/records/$id") {
            addAuth()
            contentType(ContentType.Application.Json)
            setBody(product)
        }.body()
    }

    // ==================== CATEGORIES ====================

    suspend fun getCategories(): Result<List<Category>> = runCatching {
        httpClient.get("$baseUrl/api/collections/categories/records") {
            addAuth()
            parameter("sort", "order,label")
        }.body<CategoryListResponse>().items
    }

    // ==================== TRANSACTIONS ====================

    suspend fun getMyTransactions(): Result<List<Transaction>> = runCatching {
        val userId = sessionManager.currentUser.value?.id
            ?: throw IllegalStateException("Nicht eingeloggt")

        httpClient.get("$baseUrl/api/collections/transactions/records") {
            addAuth()
            parameter("filter", "userId='$userId'")
            parameter("sort", "-created")
        }.body<TransactionListResponse>().items
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> = runCatching {
        httpClient.post("$baseUrl/api/collections/transactions/records") {
            addAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Berechnet das aktuelle Guthaben des Users
     */
    suspend fun getBalance(): Result<Int> = runCatching {
        val transactions = getMyTransactions().getOrThrow()
        transactions.sumOf { it.amountCents }
    }

    // ==================== ADMIN ====================

    suspend fun getAllTransactions(): Result<List<Transaction>> = runCatching {
        httpClient.get("$baseUrl/api/collections/transactions/records") {
            addAuth()
            parameter("sort", "-created")
        }.body<TransactionListResponse>().items
    }
}
