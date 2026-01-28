package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.api.dto.CreateLedgerEntryRequest
import de.mkrabs.snablo.app.domain.model.*
import de.mkrabs.snablo.app.util.eurosToCents

/**
 * Repository for ledger/transaction operations
 */
interface LedgerRepository {
    suspend fun recordPurchase(
        userId: String,
        unitPrice: Double,
        locationId: String,
        catalogItemId: String,
        shelfId: String,
        quantity: Int = 1
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
        unitPrice: Double,
        locationId: String,
        catalogItemId: String,
        shelfId: String,
        quantity: Int
    ): Result<LedgerEntry> {
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }
        if (shelfId.isBlank()) {
            return Result.failure(IllegalArgumentException("shelfId is blank"))
        }
        val unitPriceCents = eurosToCents(unitPrice)
        val amountCents = -unitPriceCents * quantity
        val request = CreateLedgerEntryRequest(
            entryType = LedgerEntryType.BALANCE_ENTRY.name,
            kind = TransactionKind.PURCHASE_DIGITAL.name,
            userId = userId,
            amountCents = amountCents,
            cashAffectsExpectedCash = false,
            paymentMethod = PaymentMethod.PAYPAL.name,
            locationId = locationId,
            shelfId = shelfId,
            catalogItemIdSnapshot = catalogItemId,
            quantity = quantity,
            unitPriceCentsSnapshot = unitPriceCents
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
        if (kind != TransactionKind.TOPUP_CASH && kind != TransactionKind.TOPUP_DIGITAL) {
            return Result.failure(Exception("Invalid top-up kind"))
        }
        val entryType = if (kind == TransactionKind.TOPUP_CASH) {
            LedgerEntryType.CASH_MOVEMENT
        } else {
            LedgerEntryType.BALANCE_ENTRY
        }
        val cashAffectsExpectedCash = kind == TransactionKind.TOPUP_CASH
        val paymentMethod = if (kind == TransactionKind.TOPUP_CASH) {
            PaymentMethod.CASH
        } else {
            PaymentMethod.PAYPAL
        }
        val request = CreateLedgerEntryRequest(
            entryType = entryType.name,
            kind = kind.name,
            userId = userId,
            amountCents = eurosToCents(amount),
            cashAffectsExpectedCash = cashAffectsExpectedCash,
            paymentMethod = paymentMethod.name,
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
            entryType = LedgerEntryType.BALANCE_ENTRY.name,
            kind = TransactionKind.ADJUSTMENT_BALANCE.name,
            userId = userId,
            amountCents = eurosToCents(amount),
            cashAffectsExpectedCash = false,
            paymentMethod = PaymentMethod.PAYPAL.name,
            note = "Undo of entry $originalEntryId"
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
