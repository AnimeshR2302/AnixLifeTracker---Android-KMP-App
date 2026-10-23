package com.anix.android.anixlifetracker.usecase

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.repository.SmsRepository
import com.anix.android.anixlifetracker.sms.collector.SmsCollector
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class ProcessIncomingSmsUseCase(
    private val smsRepository: SmsRepository
) {
    suspend operator fun invoke(message: FinancialMessage): IngestionResult {
        val settings = smsRepository.getSmsSettings()
        if (!settings.isSmsFeatureEnabled || !settings.isConsentGiven || !settings.liveMonitoringEnabled) {
            return IngestionResult.Rejected("SMS feature disabled or consent not given")
        }
        return smsRepository.ingestMessage(message)
    }
}

class ImportHistoricalSmsUseCase(
    private val smsRepository: SmsRepository,
    private val smsCollector: SmsCollector
) {
    suspend operator fun invoke(
        daysRange: Int,
        onProgress: ((scanned: Int, financial: Int, imported: Int, skipped: Int) -> Unit)? = null
    ): HistoricalImportSummary {
        val settings = smsRepository.getSmsSettings()
        if (!settings.isSmsFeatureEnabled || !settings.isConsentGiven) {
            return HistoricalImportSummary()
        }

        val endMillis = Clock.System.now().toEpochMilliseconds()
        val startMillis = endMillis - (daysRange.toLong() * 24 * 60 * 60 * 1000)

        val messages = smsCollector.readHistoricalMessages(startMillis, endMillis)
        return smsRepository.importHistoricalMessages(messages, onProgress)
    }
}

class ManageSmsSettingsUseCase(
    private val smsRepository: SmsRepository
) {
    fun observeSettings(): Flow<SmsSettingsModel> = smsRepository.observeSmsSettings()

    suspend fun updateSettings(settings: SmsSettingsModel) {
        smsRepository.updateSmsSettings(settings)
    }
}

class ReviewSmsCandidatesUseCase(
    private val smsRepository: SmsRepository
) {
    fun observePendingCandidates(): Flow<List<SmsReviewCandidateModel>> =
        smsRepository.observePendingReviewCandidates()

    suspend fun confirmCandidate(
        candidateId: String,
        correctedCandidate: TransactionCandidate
    ): IngestionResult = smsRepository.confirmReviewCandidate(candidateId, correctedCandidate)

    suspend fun rejectCandidate(candidateId: String) =
        smsRepository.rejectReviewCandidate(candidateId)

    suspend fun clearAll() = smsRepository.clearPendingReviewQueue()
}
