package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing SMS integration settings, review candidates, and transaction ingestion
 */
interface SmsRepository {
    fun observeSmsSettings(): Flow<SmsSettingsModel>
    suspend fun getSmsSettings(): SmsSettingsModel
    suspend fun updateSmsSettings(settings: SmsSettingsModel)

    fun observePendingReviewCandidates(): Flow<List<SmsReviewCandidateModel>>
    suspend fun getPendingReviewCandidates(): List<SmsReviewCandidateModel>

    suspend fun ingestMessage(message: FinancialMessage): IngestionResult
    suspend fun ingestCandidate(candidate: TransactionCandidate): IngestionResult

    suspend fun confirmReviewCandidate(candidateId: String, correctedCandidate: TransactionCandidate): IngestionResult
    suspend fun rejectReviewCandidate(candidateId: String)
    suspend fun clearPendingReviewQueue()

    suspend fun importHistoricalMessages(
        messages: List<FinancialMessage>,
        onProgress: ((scanned: Int, financial: Int, imported: Int, skipped: Int) -> Unit)? = null
    ): HistoricalImportSummary
}
