package de.mkrabs.snablo.app.data.repository

import de.mkrabs.snablo.app.data.api.ApiResult
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.api.dto.CreateCashCountRequest
import de.mkrabs.snablo.app.domain.model.CashCount
import de.mkrabs.snablo.app.domain.model.DriftClassification

/**
 * Repository for reconciliation and cash count operations
 */
interface ReconciliationRepository {
    suspend fun recordCashCount(locationId: String, countedCash: Double, notes: String? = null): Result<CashCount>
    suspend fun getCashCounts(locationId: String): Result<List<CashCount>>
}

class ReconciliationRepositoryImpl(
    private val apiClient: PocketBaseClient
) : ReconciliationRepository {
    override suspend fun recordCashCount(
        locationId: String,
        countedCash: Double,
        notes: String?
    ): Result<CashCount> {
        val request = CreateCashCountRequest(
            locationId = locationId,
            countedCash = countedCash,
            notes = notes
        )
        return when (val result = apiClient.createCashCount(request)) {
            is ApiResult.Success -> {
                val dto = result.data
                val classification = CashCount.classifyDrift(dto.driftPercentage)
                Result.success(
                    CashCount(
                        id = dto.id,
                        locationId = dto.locationId,
                        countedCash = dto.countedCash,
                        expectedCash = dto.expectedCash,
                        drift = dto.drift,
                        driftPercentage = dto.driftPercentage,
                        classification = classification,
                        recordedBy = dto.recordedBy,
                        timestamp = dto.timestamp,
                        notes = dto.notes,
                        createdAt = dto.created,
                        updatedAt = dto.updated
                    )
                )
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun getCashCounts(locationId: String): Result<List<CashCount>> {
        return when (val result = apiClient.getCashCounts(locationId)) {
            is ApiResult.Success -> {
                val counts = result.data.items.map { dto ->
                    val classification = CashCount.classifyDrift(dto.driftPercentage)
                    CashCount(
                        id = dto.id,
                        locationId = dto.locationId,
                        countedCash = dto.countedCash,
                        expectedCash = dto.expectedCash,
                        drift = dto.drift,
                        driftPercentage = dto.driftPercentage,
                        classification = classification,
                        recordedBy = dto.recordedBy,
                        timestamp = dto.timestamp,
                        notes = dto.notes,
                        createdAt = dto.created,
                        updatedAt = dto.updated
                    )
                }
                Result.success(counts)
            }
            is ApiResult.Error -> Result.failure(Exception(result.message))
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}

