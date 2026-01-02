package de.mkrabs.snablo.app.data.api

/**
 * Configuration for PocketBase API
 */
object PocketBaseConfig {
    // Read from environment variable POCKETBASE_URL or use default
    var baseUrl: String = System.getenv("POCKETBASE_URL") ?: "https://pocketbase.mkrabs.de"
    var collectionPrefix: String = "/api/collections"

    // PocketBase v0.35+: records are under /api/collections/{collection}/records
    private fun recordsUrl(collection: String): String = "$baseUrl$collectionPrefix/$collection/records"

    val authUrl: String get() = "$baseUrl$collectionPrefix/users/auth-with-password"
    val refreshUrl: String get() = "$baseUrl$collectionPrefix/users/refresh"

    val usersUrl: String get() = recordsUrl("users")
    val catalogItemsUrl: String get() = recordsUrl("catalog-items")
    val locationsUrl: String get() = recordsUrl("locations")
    val slotMappingsUrl: String get() = recordsUrl("slot-mappings")
    val locationPricesUrl: String get() = recordsUrl("location-prices")
    val ledgerEntriesUrl: String get() = recordsUrl("ledger-entries")
    val nfcTokensUrl: String get() = recordsUrl("nfc-tokens")
    val cashCountsUrl: String get() = recordsUrl("cash-counts")
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
