package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.PocketBaseApi
import de.mkrabs.snablo.app.data.model.*
import de.mkrabs.snablo.app.data.session.SessionManager

class TransactionRepository(
    private val api: PocketBaseApi,
    private val sessionManager: SessionManager
) {
    suspend fun getMyTransactions(): Result<List<Transaction>> = api.getMyTransactions()

    suspend fun getBalance(): Result<Int> = api.getBalance()

    /**
     * Erstellt einen Kauf (zieht vom Guthaben ab)
     */
    suspend fun purchase(product: Product, quantity: Int = 1): Result<Transaction> {
        val userId = sessionManager.currentUser.value?.id
            ?: return Result.failure(IllegalStateException("Nicht eingeloggt"))

        val totalCents = product.priceCents * quantity

        return api.createTransaction(
            CreateTransactionRequest(
                userId = userId,
                type = TransactionType.purchase,
                productId = product.id,
                amountCents = -totalCents, // Negativ = Guthaben sinkt
                quantity = quantity,
                unitPriceCentsSnapshot = product.priceCents,
                paymentMethod = PaymentMethod.balance
            )
        )
    }

    /**
     * Erstellt eine Aufladung (erhöht Guthaben)
     */
    suspend fun topUp(amountCents: Int, method: PaymentMethod, note: String? = null): Result<Transaction> {
        val userId = sessionManager.currentUser.value?.id
            ?: return Result.failure(IllegalStateException("Nicht eingeloggt"))

        return api.createTransaction(
            CreateTransactionRequest(
                userId = userId,
                type = TransactionType.topup,
                amountCents = amountCents, // Positiv = Guthaben steigt
                paymentMethod = method,
                note = note
            )
        )
    }

    /**
     * Admin: Alle Transaktionen abrufen
     */
    suspend fun getAllTransactions(): Result<List<Transaction>> = api.getAllTransactions()
}
