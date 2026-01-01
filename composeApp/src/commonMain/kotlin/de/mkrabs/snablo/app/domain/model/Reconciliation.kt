package de.mkrabs.snablo.app.domain.model

import kotlinx.serialization.Serializable

enum class DriftClassification {
    GOOD,   // Drift < 5%
    WARN,   // Drift 5% - 10%
    BAD     // Drift > 10%
}

@Serializable
data class CashCount(
    val id: String,
    val locationId: String,
    val countedCash: Double,       // EUR; actual cash counted
    val expectedCash: Double,      // EUR; computed from ledger (cash-affecting entries only)
    val drift: Double = 0.0,       // countedCash - expectedCash (can be negative)
    val driftPercentage: Double = 0.0,  // |drift| / expectedCash * 100 (if expectedCash > 0)
    val classification: DriftClassification = DriftClassification.GOOD, // Derived from driftPercentage
    val recordedBy: String = "",   // Admin user ID
    val timestamp: String = "",    // ISO 8601; when this cash count occurred
    val notes: String? = null,     // Optional admin notes
    val createdAt: String = "",
    val updatedAt: String? = null
) {
    companion object {
        /**
         * Classify drift based on percentage
         */
        fun classifyDrift(percentage: Double): DriftClassification = when {
            percentage < 5.0 -> DriftClassification.GOOD
            percentage <= 10.0 -> DriftClassification.WARN
            else -> DriftClassification.BAD
        }
    }
}

/**
 * Request to record a cash count and settlement
 */
data class CashCountRequest(
    val locationId: String,
    val countedCash: Double,
    val notes: String? = null
)

