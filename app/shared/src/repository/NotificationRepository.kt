package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing notification integration settings, supported application allowlist,
 * review candidates, and transaction ingestion from Android notifications.
 */
interface NotificationRepository {
    fun observeSettings(): Flow<NotificationSettingsModel>
    suspend fun getSettings(): NotificationSettingsModel
    suspend fun updateSettings(settings: NotificationSettingsModel)

    fun observeSupportedApps(): Flow<List<FinancialAppProfile>>
    suspend fun getSupportedApps(): List<FinancialAppProfile>
    suspend fun setAppEnabledStatus(packageName: String, isEnabled: Boolean)

    fun observePendingReviewCandidates(): Flow<List<NotificationReviewCandidateModel>>
    suspend fun getPendingReviewCandidates(): List<NotificationReviewCandidateModel>

    suspend fun ingestNotification(notification: FinancialNotification): IngestionResult
    suspend fun ingestCandidate(candidate: TransactionCandidate, sourcePackage: String): IngestionResult

    suspend fun confirmReviewCandidate(candidateId: String, correctedCandidate: TransactionCandidate): IngestionResult
    suspend fun rejectReviewCandidate(candidateId: String)
    suspend fun clearPendingReviewQueue()
}
