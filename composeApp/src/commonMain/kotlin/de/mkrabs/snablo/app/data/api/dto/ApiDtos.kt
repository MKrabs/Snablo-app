package de.mkrabs.snablo.app.data.api.dto

import de.mkrabs.snablo.app.domain.model.LedgerEntry
import de.mkrabs.snablo.app.domain.model.TransactionKind
import de.mkrabs.snablo.app.domain.model.PaymentMethod
import de.mkrabs.snablo.app.domain.model.User
import de.mkrabs.snablo.app.domain.model.UserRole
import kotlinx.serialization.Serializable

// ========== Authentication ==========

@Serializable
data class LoginRequest(
    val identity: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val record: UserDto,
    val token: String
)

@Serializable
data class RefreshResponse(
    val record: UserDto,
    val token: String
)

// ========== User DTO ==========

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String = "USER",
    val globalBalance: Double = 0.0,
    val created: String = "",
    val updated: String? = null
) {
    fun toUser(): User = User(
        id = id,
        email = email,
        name = name,
        role = if (role == "ADMIN") UserRole.ADMIN else UserRole.USER,
        globalBalance = globalBalance,
        createdAt = created,
        updatedAt = updated
    )
}

// ========== Catalog =========

@Serializable
data class CatalogItemDto(
    val id: String,
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val created: String = "",
    val updated: String? = null
)

// ========== Location =========

@Serializable
data class LocationDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val timeZone: String = "Europe/Berlin",
    val created: String = "",
    val updated: String? = null
)

// ========== Slot Mapping =========

@Serializable
data class SlotMappingDto(
    val id: String,
    val locationId: String,
    val slotIndex: Int,
    val catalogItemId: String,
    val inventoryCount: Int = 0,
    val created: String = "",
    val updated: String? = null
)

// ========== Location Price =========

@Serializable
data class LocationPriceDto(
    val id: String,
    val locationId: String,
    val catalogItemId: String,
    val price: Double,
    val validFrom: String = "",
    val validUntil: String? = null,
    val created: String = "",
    val updated: String? = null
)

// ========== Ledger =========

@Serializable
data class LedgerEntryDto(
    val id: String,
    val kind: String,
    val userId: String,
    val amount: Double,
    val paymentMethod: String = "INTERNAL_BALANCE",
    val locationId: String? = null,
    val catalogItemId: String? = null,
    val priceSnapshot: Double? = null,
    val description: String? = null,
    val created: String = "",
    val createdBy: String = "",
    val settledAt: String? = null,
    val isCompensating: Boolean = false,
    val updated: String? = null
) {
    fun toLedgerEntry(): LedgerEntry = LedgerEntry(
        id = id,
        kind = TransactionKind.valueOf(kind),
        userId = userId,
        amount = amount,
        paymentMethod = PaymentMethod.valueOf(paymentMethod),
        locationId = locationId,
        catalogItemId = catalogItemId,
        priceSnapshot = priceSnapshot,
        description = description,
        createdAt = created,
        createdBy = createdBy,
        settledAt = settledAt,
        isCompensating = isCompensating
    )
}

@Serializable
data class CreateLedgerEntryRequest(
    val kind: String,
    val userId: String,
    val amount: Double,
    val paymentMethod: String = "INTERNAL_BALANCE",
    val locationId: String? = null,
    val catalogItemId: String? = null,
    val priceSnapshot: Double? = null,
    val description: String? = null,
    val isCompensating: Boolean = false
)

// ========== NFC Token =========

@Serializable
data class NfcTokenDto(
    val id: String,
    val token: String,
    val slotId: String,
    val createdAt: String = ""
)

// ========== Cash Count =========

@Serializable
data class CashCountDto(
    val id: String,
    val locationId: String,
    val countedCash: Double,
    val expectedCash: Double,
    val drift: Double = 0.0,
    val driftPercentage: Double = 0.0,
    val classification: String = "GOOD",
    val recordedBy: String = "",
    val timestamp: String = "",
    val notes: String? = null,
    val created: String = "",
    val updated: String? = null
)

@Serializable
data class CreateCashCountRequest(
    val locationId: String,
    val countedCash: Double,
    val notes: String? = null
)

// ========== Generic Paginated Response =========

@Serializable
data class PaginatedResponse<T>(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val items: List<T> = emptyList()
)

// ========== Error Response =========

@Serializable
data class ErrorResponse(
    val code: Int,
    val message: String,
    val data: Map<String, String>? = null
)

