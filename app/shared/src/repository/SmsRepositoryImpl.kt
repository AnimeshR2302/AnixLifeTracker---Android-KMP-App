package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.toEntity
import com.anix.android.anixlifetracker.database.entity.toModel
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.pipeline.FinancialProcessingPipeline
import com.anix.android.anixlifetracker.sms.parser.DeterministicFinancialMessageParser
import com.anix.android.anixlifetracker.sms.parser.FinancialMessageParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class SmsRepositoryImpl(
    private val db: AppDatabase,
    private val parser: FinancialMessageParser = DeterministicFinancialMessageParser(),
    private val pipeline: FinancialProcessingPipeline = FinancialProcessingPipeline(db)
) : SmsRepository {

    override fun observeSmsSettings(): Flow<SmsSettingsModel> {
        return db.smsSettingsDao().observeSettings().map { entity ->
            entity?.toModel() ?: SmsSettingsModel()
        }
    }

    override suspend fun getSmsSettings(): SmsSettingsModel {
        return db.smsSettingsDao().getSettings()?.toModel() ?: SmsSettingsModel()
    }

    override suspend fun updateSmsSettings(settings: SmsSettingsModel) {
        db.smsSettingsDao().insertOrUpdate(settings.toEntity())
    }

    override fun observePendingReviewCandidates(): Flow<List<SmsReviewCandidateModel>> {
        return db.smsReviewCandidateDao().observePendingCandidates().map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun getPendingReviewCandidates(): List<SmsReviewCandidateModel> {
        val pending = db.smsReviewCandidateDao().observePendingCandidates().firstOrNull() ?: emptyList()
        return pending.map { it.toModel() }
    }

    override suspend fun ingestMessage(message: FinancialMessage): IngestionResult {
        val parseResult = parser.parse(message)
        if (parseResult.status == ParseConfidence.REJECTED || parseResult.status == ParseConfidence.UNRECOGNIZED) {
            return IngestionResult.Rejected(parseResult.rejectionReason ?: "Message not recognized as financial transaction")
        }

        val candidate = parseResult.candidate
            ?: return IngestionResult.Failed("Parser produced null candidate")

        return ingestCandidate(candidate)
    }

    override suspend fun ingestCandidate(candidate: TransactionCandidate): IngestionResult {
        val settings = getSmsSettings()
        val result = pipeline.processCandidate(
            candidate = candidate,
            sourceType = TransactionSourceType.SMS,
            sourceIdentifier = candidate.sender,
            autoSaveHighConfidence = settings.autoSaveHighConfidence
        )
        if (result is IngestionResult.Confirmed) {
            updateSmsSettings(settings.copy(totalImportedCount = settings.totalImportedCount + 1))
        }
        return result
    }

    override suspend fun confirmReviewCandidate(
        candidateId: String,
        correctedCandidate: TransactionCandidate
    ): IngestionResult {
        db.smsReviewCandidateDao().getCandidateById(candidateId)
            ?: return IngestionResult.Failed("Review candidate not found")

        val result = ingestCandidate(correctedCandidate)
        db.smsReviewCandidateDao().deleteCandidateById(candidateId)
        return result
    }

    override suspend fun rejectReviewCandidate(candidateId: String) {
        db.smsReviewCandidateDao().deleteCandidateById(candidateId)
    }

    override suspend fun clearPendingReviewQueue() {
        db.smsReviewCandidateDao().clearPendingCandidates()
    }

    override suspend fun importHistoricalMessages(
        messages: List<FinancialMessage>,
        onProgress: ((scanned: Int, financial: Int, imported: Int, skipped: Int) -> Unit)?
    ): HistoricalImportSummary {
        var scanned = 0
        var financial = 0
        var imported = 0
        var skipped = 0
        var review = 0
        var errors = 0

        for (msg in messages) {
            scanned++
            val result = ingestMessage(msg)
            when (result) {
                is IngestionResult.Confirmed -> {
                    financial++
                    imported++
                }
                is IngestionResult.QueuedForReview -> {
                    financial++
                    review++
                }
                is IngestionResult.DuplicateSkipped -> {
                    financial++
                    skipped++
                }
                is IngestionResult.Rejected -> {
                    // Non financial or rejected
                }
                is IngestionResult.Failed -> {
                    errors++
                }
            }

            onProgress?.invoke(scanned, financial, imported, skipped)
        }

        val settings = getSmsSettings()
        val nowIso = Clock.System.now().toString()
        updateSmsSettings(settings.copy(lastImportTimestamp = nowIso))

        return HistoricalImportSummary(
            scannedCount = scanned,
            financialFoundCount = financial,
            importedCount = imported,
            duplicatesSkippedCount = skipped,
            needsReviewCount = review,
            errorCount = errors
        )
    }
}
