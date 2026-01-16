package de.mkrabs.snablo.app.data.model

import de.mkrabs.snablo.app.util.formatPriceWithSign
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val productId: String? = null,
    val amountCents: Int,
    val quantity: Int? = null,
    val unitPriceCentsSnapshot: Int? = null,
    val paymentMethod: PaymentMethod,
    val note: String? = null,
    val created: String? = null
) {
    /**
     * Betrag als formatierter String (z.B. "+5,00 €" oder "-1,50 €")
     */
    val amountFormatted: String
        get() = formatPriceWithSign(amountCents)
}

@Serializable
enum class TransactionType {
    purchase,
    topup,
    correction
}

@Serializable
enum class PaymentMethod {
    cash,
    paypal,
    wero,
    balance
}

@Serializable
data class TransactionListResponse(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<Transaction>
)

/**
 * Request zum Erstellen einer neuen Transaction
 */
@Serializable
data class CreateTransactionRequest(
    val userId: String,
    val type: TransactionType,
    val productId: String? = null,
    val amountCents: Int,
    val quantity: Int? = null,
    val unitPriceCentsSnapshot: Int? = null,
    val paymentMethod: PaymentMethod,
    val note: String? = null
)
