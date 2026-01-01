package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.api.dto.CreateLedgerEntryRequest
import de.mkrabs.snablo.app.domain.model.*

/**
 * Repository for ledger/transaction operations
 */
interface LedgerRepository {
    suspend fun recordPurchase(
        userId: String,
        price: Double,
        locationId: String,
        catalogItemId: String
    ): Result<LedgerEntry>

    suspend fun recordTopUp(
        userId: String,
        amount: Double,
        kind: TransactionKind,
        locationId: String? = null
    ): Result<LedgerEntry>

    suspend fun recordUndo(originalEntryId: String, amount: Double, userId: String): Result<LedgerEntry>

    suspend fun getHistory(userId: String): Result<List<LedgerEntry>>
}

class LedgerRepositoryImpl(
    private val apiClient: PocketBaseClient
) : LedgerRepository {
    override suspend fun recordPurchase(
        userId: String,
        price: Double,
        locationId: String,
        catalogItemId: String
    ): Result<LedgerEntry> {
        val request = CreateLedgerEntryRequest(
            kind = "PURCHASE",
            userId = userId,
            amount = -price,  // Negative for spending
            paymentMethod = "INTERNAL_BALANCE",
            locationId = locationId,
            catalogItemId = catalogItemId,
            priceSnapshot = price
        )
        return when (val result = apiClient.createLedgerEntry(request)) {
            is ApiResult.Success -> Result.success(result.data.toLedgerEntry())
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun recordTopUp(
        userId: String,
        amount: Double,
        kind: TransactionKind,
        locationId: String?
    ): Result<LedgerEntry> {
        if (kind != TransactionKind.TOP_UP_CASH && kind != TransactionKind.TOP_UP_DIGITAL) {
            return Result.failure(Exception("Invalid top-up kind"))
        }

        val request = CreateLedgerEntryRequest(
            kind = kind.name,
            userId = userId,
            amount = amount,  // Positive for adding
            paymentMethod = if (kind == TransactionKind.TOP_UP_CASH) "CASH" else "INTERNAL_BALANCE",
            locationId = locationId
        )
        return when (val result = apiClient.createLedgerEntry(request)) {
            is ApiResult.Success -> Result.success(result.data.toLedgerEntry())
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun recordUndo(originalEntryId: String, amount: Double, userId: String): Result<LedgerEntry> {
        val request = CreateLedgerEntryRequest(
            kind = "PURCHASE",
            userId = userId,
            amount = amount,  // Opposite sign (positive to undo negative purchase)
            paymentMethod = "INTERNAL_BALANCE",
            isCompensating = true,
            description = "Undo of entry $originalEntryId"
        )
        return when (val result = apiClient.createLedgerEntry(request)) {
            is ApiResult.Success -> Result.success(result.data.toLedgerEntry())
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getHistory(userId: String): Result<List<LedgerEntry>> {
        return when (val result = apiClient.getLedgerEntries(userId)) {
            is ApiResult.Success -> {
                val entries = result.data.items.map { it.toLedgerEntry() }
                Result.success(entries)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}

