package de.mkrabs.snablo.app.domain.model

import kotlinx.serialization.Serializable

enum class TransactionKind {
    PURCHASE,              // Digital snack purchase
    TOP_UP_CASH,          // Cash deposited into balance
    TOP_UP_DIGITAL,       // Digital transfer to balance (record-only)
    LOGGED_CASH_PAYMENT,  // User paid cash for snack, logged in-app
    REFUND_CASH,          // Admin refunded cash
    REFUND_DIGITAL,       // Admin recorded digital refund
    ADJUSTMENT            // Admin adjustment (correction, etc.)
}

enum class PaymentMethod {
    INTERNAL_BALANCE,  // Spent from Snablo balance
    CASH              // Paid directly with cash
}

@Serializable
data class LedgerEntry(
    val id: String,                  // PocketBase record ID
    val kind: TransactionKind,       // Type of transaction
    val userId: String,              // Who is affected
    val amount: Double,              // EUR; can be negative (refund/undo)
    val paymentMethod: PaymentMethod = PaymentMethod.INTERNAL_BALANCE, // Payment method
    val locationId: String? = null,  // Required for cash movements; null for digital-only
    val catalogItemId: String? = null,      // Set if purchase/logged-cash-payment
    val priceSnapshot: Double? = null,      // Effective price at time of purchase
    val description: String? = null,        // Optional: reason, admin notes
    val createdAt: String = "",            // ISO 8601 timestamp
    val createdBy: String = "",            // User ID who created entry (or system)
    val settledAt: String? = null,         // Timestamp of cash count that settled this entry (null if unsettled)
    val isCompensating: Boolean = false    // True if this is an undo/correction entry
)

/**
 * Represents a pending undo operation
 */
data class PendingUndo(
    val originalEntryId: String,
    val expiresAt: Long  // Milliseconds since epoch
)

