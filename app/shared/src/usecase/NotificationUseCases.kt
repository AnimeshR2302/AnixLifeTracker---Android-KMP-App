package com.anix.android.anixlifetracker.usecase

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class ProcessIncomingNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notification: FinancialNotification): IngestionResult {
        val settings = repository.getSettings()
        if (!settings.isNotificationFeatureEnabled || !settings.isConsentGiven || !settings.liveMonitoringEnabled) {
            return IngestionResult.Rejected("Notification feature disabled or consent not granted")
        }
        return repository.ingestNotification(notification)
    }
}

class ManageNotificationSettingsUseCase(
    private val repository: NotificationRepository
) {
    fun observeSettings(): Flow<NotificationSettingsModel> = repository.observeSettings()

    suspend fun updateSettings(settings: NotificationSettingsModel) {
        repository.updateSettings(settings)
    }
}

class ManageSupportedAppsUseCase(
    private val repository: NotificationRepository
) {
    fun observeSupportedApps(): Flow<List<FinancialAppProfile>> = repository.observeSupportedApps()

    suspend fun setAppEnabled(packageName: String, isEnabled: Boolean) {
        repository.setAppEnabledStatus(packageName, isEnabled)
    }
}

class ReviewNotificationCandidatesUseCase(
    private val repository: NotificationRepository
) {
    fun observePendingCandidates(): Flow<List<NotificationReviewCandidateModel>> =
        repository.observePendingReviewCandidates()

    suspend fun confirmCandidate(
        candidateId: String,
        correctedCandidate: TransactionCandidate
    ): IngestionResult = repository.confirmReviewCandidate(candidateId, correctedCandidate)

    suspend fun rejectCandidate(candidateId: String) =
        repository.rejectReviewCandidate(candidateId)

    suspend fun clearAll() = repository.clearPendingReviewQueue()
}
