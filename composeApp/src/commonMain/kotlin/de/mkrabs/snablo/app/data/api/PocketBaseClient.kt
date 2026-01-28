package de.mkrabs.snablo.app.data.api

import de.mkrabs.snablo.app.data.api.dto.*
import de.mkrabs.snablo.app.data.session.SessionManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * PocketBase API client for all backend interactions
 */
class PocketBaseClient(
    private val httpClient: HttpClient,
    private val sessionManager: SessionManager
) {
    private suspend fun getAuthHeader(): Map<String, String> {
        val session = sessionManager.getSession()
        return if (session != null) {
            mapOf("Authorization" to "Bearer ${session.token.token}")
        } else {
            emptyMap()
        }
    }

    // ========== Authentication ==========

    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return try {
            val response = httpClient.post(PocketBaseConfig.authUrl) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body<LoginResponse>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(
                code = 500,
                message = "Login failed: ${e.message}",
                exception = e
            )
        }
    }

    suspend fun refresh(): ApiResult<RefreshResponse> {
        return try {
            val headers = getAuthHeader()
            val response = httpClient.post(PocketBaseConfig.refreshUrl) {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)
            }.body<RefreshResponse>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(
                code = 401,
                message = "Token refresh failed: ${e.message}",
                exception = e
            )
        }
    }

    // ========== Catalog ==========

    suspend fun getCatalogItems(page: Int = 1, perPage: Int = 50): ApiResult<PaginatedResponse<CatalogItemDto>> {
        return try {
            val response = httpClient.get("${PocketBaseConfig.catalogItemsUrl}?page=$page&perPage=$perPage") {
                contentType(ContentType.Application.Json)
            }.body<PaginatedResponse<CatalogItemDto>>()
            println("Fetched catalog items: $response")
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch catalog items", exception = e)
        }
    }

    suspend fun getCatalogItemById(id: String): ApiResult<CatalogItemDto> {
        return try {
            val response = httpClient.get("${PocketBaseConfig.catalogItemsUrl}/$id") {
                contentType(ContentType.Application.Json)
            }.body<CatalogItemDto>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 404, message = "Catalog item not found: ${e.message}", exception = e)
        }
    }

    // ========== Locations ==========

    suspend fun getLocations(): ApiResult<PaginatedResponse<LocationDto>> {
        return try {
            val response = httpClient.get(PocketBaseConfig.locationsUrl) {
                contentType(ContentType.Application.Json)
            }.body<PaginatedResponse<LocationDto>>()
            println("Fetched locations: $response")
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch locations", exception = e)
        }
    }

    // ========== Slot Mappings ==========

    suspend fun getSlotMappings(locationId: String): ApiResult<PaginatedResponse<SlotMappingDto>> {
        return try {
            val filter = """filter=locationId="$locationId""""
            val response = httpClient.get("${PocketBaseConfig.slotMappingsUrl}?$filter") {
                contentType(ContentType.Application.Json)
            }.body<PaginatedResponse<SlotMappingDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch slot mappings", exception = e)
        }
    }

    // ========== Prices ==========

    suspend fun getPrice(locationId: String, catalogItemId: String): ApiResult<LocationPriceDto> {
        return try {
            val filter = """filter=locationId="$locationId" && catalogItemId="$catalogItemId" && validUntil=null"""
            val response = httpClient.get("${PocketBaseConfig.locationPricesUrl}?$filter") {
                contentType(ContentType.Application.Json)
            }.body<PaginatedResponse<LocationPriceDto>>()

            val price = response.items.firstOrNull()
                ?: throw ApiException(404, "Price not found for location=$locationId, item=$catalogItemId")
            ApiResult.Success(price)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            ApiResult.Error(code = e.code, message = e.message.orEmpty(), exception = e)
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch price", exception = e)
        }
    }

    // ========== Ledger ==========

    suspend fun createLedgerEntry(entry: CreateLedgerEntryRequest): ApiResult<LedgerEntryDto> {
        return try {
            val headers = getAuthHeader()
            val response = httpClient.post(PocketBaseConfig.ledgerEntriesUrl) {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)
                setBody(entry)
            }.body<LedgerEntryDto>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to create ledger entry", exception = e)
        }
    }

    suspend fun getLedgerEntries(
        userId: String,
        page: Int = 1,
        perPage: Int = 50
    ): ApiResult<PaginatedResponse<LedgerEntryDto>> {
        return try {
            val headers = getAuthHeader()
            val filter = """filter=userId="$userId""""
            val response =
                httpClient.get("${PocketBaseConfig.ledgerEntriesUrl}?$filter&sort=-created&page=$page&perPage=$perPage") {
                    headers.forEach { (k, v) -> header(k, v) }
                    contentType(ContentType.Application.Json)
                }.body<PaginatedResponse<LedgerEntryDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch ledger entries", exception = e)
        }
    }

    // ========== NFC Tokens ==========

    suspend fun resolveNfcToken(token: String): ApiResult<NfcTokenDto> {
        return try {
            val response = httpClient.get("${PocketBaseConfig.nfcTokensUrl}/$token") {
                contentType(ContentType.Application.Json)
            }.body<NfcTokenDto>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 404, message = "NFC token not found", exception = e)
        }
    }

    // ========== Cash Counts ==========

    suspend fun createCashCount(request: CreateCashCountRequest): ApiResult<CashCountDto> {
        return try {
            val headers = getAuthHeader()
            val response = httpClient.post(PocketBaseConfig.cashCountsUrl) {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<CashCountDto>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to create cash count", exception = e)
        }
    }

    suspend fun getCashCounts(locationId: String, limit: Int = 10): ApiResult<PaginatedResponse<CashCountDto>> {
        return try {
            val headers = getAuthHeader()
            val filter = """filter=locationId="$locationId""""
            val response = httpClient.get("${PocketBaseConfig.cashCountsUrl}?$filter&sort=-timestamp&perPage=$limit") {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)
            }.body<PaginatedResponse<CashCountDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch cash counts", exception = e)
        }
    }

    // ========== Shelves (compat) ==========

    suspend fun getShelves(
        page: Int = 1,
        perPage: Int = 50,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null
    ): ApiResult<PaginatedResponse<ShelfDto>> {
        return try {
            val url = buildString {
                append(PocketBaseConfig.shelvesUrl)
                append("?page=$page&perPage=$perPage")
                if (!filter.isNullOrBlank()) append("&filter=${filter}")
                if (!expand.isNullOrBlank()) append("&expand=${expand}")
                if (!fields.isNullOrBlank()) append("&fields=${fields}")
            }
            val response = httpClient.get(url) { contentType(ContentType.Application.Json) }
                .body<PaginatedResponse<ShelfDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch shelves", exception = e)
        }
    }

    // ========== Corners (compat) ==========

    suspend fun getCorners(
        page: Int = 1,
        perPage: Int = 50,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null
    ): ApiResult<PaginatedResponse<CornerDto>> {
        return try {
            val url = buildString {
                append(PocketBaseConfig.cornersUrl)
                append("?page=$page&perPage=$perPage")
                if (!filter.isNullOrBlank()) append("&filter=${filter}")
                if (!expand.isNullOrBlank()) append("&expand=${expand}")
                if (!fields.isNullOrBlank()) append("&fields=${fields}")
            }
            val response = httpClient.get(url) { contentType(ContentType.Application.Json) }
                .body<PaginatedResponse<CornerDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch corners", exception = e)
        }
    }

    // ========== Users ==========

    suspend fun getUsersList(
        page: Int = 1,
        perPage: Int = 30,
        sort: String? = null,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null,
        skipTotal: Boolean? = null
    ): ApiResult<PaginatedResponse<UserDto>> {
        return try {
            val headers = getAuthHeader()
            val response = httpClient.get(PocketBaseConfig.usersUrl) {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)

                parameter("page", page)
                parameter("perPage", perPage)
                if (!sort.isNullOrBlank()) parameter("sort", sort)
                if (!filter.isNullOrBlank()) parameter("filter", filter)
                if (!expand.isNullOrBlank()) parameter("expand", expand)
                if (!fields.isNullOrBlank()) parameter("fields", fields)
                if (skipTotal != null) parameter("skipTotal", skipTotal)
            }.body<PaginatedResponse<UserDto>>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch users: ${e.message}", exception = e)
        }
    }

    suspend fun getUsersFullList(
        sort: String? = null,
        filter: String? = null,
        expand: String? = null,
        fields: String? = null
    ): ApiResult<List<UserDto>> {
        return try {
            val all = mutableListOf<UserDto>()
            var page = 1
            val perPage = 200
            while (true) {
                val pageResult = getUsersList(
                    page = page,
                    perPage = perPage,
                    sort = sort,
                    filter = filter,
                    expand = expand,
                    fields = fields,
                    skipTotal = true
                )
                when (pageResult) {
                    is ApiResult.Success -> {
                        val items = pageResult.data.items
                        all.addAll(items)
                        if (items.size < perPage) break
                        page++
                    }
                    is ApiResult.Error -> return pageResult
                    else -> return ApiResult.Error(code = 500, message = "Unknown error")
                }
            }
            ApiResult.Success(all)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to fetch users full list: ${e.message}", exception = e)
        }
    }

    suspend fun getFirstUser(
        filter: String,
        expand: String? = null,
        fields: String? = null
    ): ApiResult<UserDto> {
        // PocketBase JS SDK uses skipTotal=true by default for getFirstListItem.
        // Equivalent: request with perPage=1 and return first item.
        return when (
            val list = getUsersList(
                page = 1,
                perPage = 1,
                filter = filter,
                expand = expand,
                fields = fields,
                skipTotal = true
            )
        ) {
            is ApiResult.Success -> {
                val first = list.data.items.firstOrNull()
                if (first != null) ApiResult.Success(first) else ApiResult.Error(404, "No user found")
            }
            is ApiResult.Error -> list
            else -> ApiResult.Error(500, "Unknown error")
        }
    }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): ApiResult<UserDto> {
        return try {
            val headers = getAuthHeader()
            val response = httpClient.patch("${PocketBaseConfig.usersUrl}/$userId") {
                headers.forEach { (k, v) -> header(k, v) }
                contentType(ContentType.Application.Json)
                setBody<UpdateUserRequest>(request)
            }.body<UserDto>()
            ApiResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(code = 500, message = "Failed to update user: ${e.message}", exception = e)
        }
    }
}
