package de.mkrabs.snablo.app.data.api

/**
 * Configuration for PocketBase API
 */
object PocketBaseConfig {
    // Read from environment variable POCKETBASE_URL or use default
    var baseUrl: String = System.getenv("POCKETBASE_URL") ?: "https://pocketbase.mkrabs.de"
    var collectionPrefix: String = "/api/collections"
    var recordsPrefix: String = "/api/records"

    val authUrl: String get() = "$baseUrl$collectionPrefix/users/auth-with-password"
    val refreshUrl: String get() = "$baseUrl$collectionPrefix/users/refresh"
    val usersUrl: String get() = "$baseUrl$recordsPrefix/users"
    val catalogItemsUrl: String get() = "$baseUrl$recordsPrefix/catalog-items"
    val locationsUrl: String get() = "$baseUrl$recordsPrefix/locations"
    val slotMappingsUrl: String get() = "$baseUrl$recordsPrefix/slot-mappings"
    val locationPricesUrl: String get() = "$baseUrl$recordsPrefix/location-prices"
    val ledgerEntriesUrl: String get() = "$baseUrl$recordsPrefix/ledger-entries"
    val nfcTokensUrl: String get() = "$baseUrl$recordsPrefix/nfc-tokens"
    val cashCountsUrl: String get() = "$baseUrl$recordsPrefix/cash-counts"
}

/**
 * Sealed class for API results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String, val exception: Throwable? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Exception for API errors
 */
class ApiException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

