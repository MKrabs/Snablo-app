package de.mkrabs.snablo.app.domain.model

import kotlinx.serialization.Serializable

enum class LedgerEntryType {
    BALANCE_ENTRY,
    CASH_MOVEMENT;

    companion object {
        fun fromApi(value: String?): LedgerEntryType {
            return when (value?.uppercase()) {
                "CASH_MOVEMENT" -> CASH_MOVEMENT
                else -> BALANCE_ENTRY
            }
        }
    }
}

enum class TransactionKind {
    PURCHASE_DIGITAL,
    PURCHASE_CASH_LOGGED,
    TOPUP_CASH,
    TOPUP_DIGITAL,
    REFUND_CASH,
    REFUND_DIGITAL,
    ADJUSTMENT_BALANCE,
    ADJUSTMENT_CASH;

    companion object {
        fun fromApi(value: String?): TransactionKind {
            return when (value?.uppercase()) {
                "PURCHASE", "PURCHASE_DIGITAL" -> PURCHASE_DIGITAL
                "PURCHASE_CASH_LOGGED", "LOGGED_CASH_PAYMENT" -> PURCHASE_CASH_LOGGED
                "TOP_UP_CASH", "TOPUP_CASH" -> TOPUP_CASH
                "TOP_UP_DIGITAL", "TOPUP_DIGITAL" -> TOPUP_DIGITAL
                "REFUND_CASH" -> REFUND_CASH
                "REFUND_DIGITAL" -> REFUND_DIGITAL
                "ADJUSTMENT_BALANCE", "ADJUSTMENT" -> ADJUSTMENT_BALANCE
                "ADJUSTMENT_CASH" -> ADJUSTMENT_CASH
                else -> ADJUSTMENT_BALANCE
            }
        }
    }
}

enum class PaymentMethod {
    CASH,
    PAYPAL,
    WERO,
    INTERNAL_BALANCE;

    companion object {
        fun fromApi(value: String?): PaymentMethod {
            return when (value?.uppercase()) {
                "CASH" -> CASH
                "PAYPAL" -> PAYPAL
                "WERO" -> WERO
                "INTERNAL_BALANCE" -> INTERNAL_BALANCE
                else -> PAYPAL
            }
        }
    }
}

@Serializable
data class LedgerEntry(
    val id: String,                  // PocketBase record ID
    val entryType: LedgerEntryType,   // Broad category
    val kind: TransactionKind,       // Type of transaction
    val userId: String? = null,      // Who is affected
    val locationId: String? = null,  // Required for cash movements; null for digital-only
    val shelfId: String? = null,
    val catalogItemIdSnapshot: String? = null,
    val quantity: Int? = null,
    val unitPriceCentsSnapshot: Int? = null,
    val amountCents: Int,            // Signed cents; negative reduces balance
    val cashAffectsExpectedCash: Boolean,
    val paymentMethod: PaymentMethod,
    val note: String? = null,
    val createdAt: String = "",      // ISO 8601 timestamp
    val updatedAt: String? = null
) {
    val amountEuros: Double
        get() = amountCents / 100.0
}

/**
 * Represents a pending undo operation
 */
data class PendingUndo(
    val originalEntryId: String,
    val expiresAt: Long  // Milliseconds since epoch
)
