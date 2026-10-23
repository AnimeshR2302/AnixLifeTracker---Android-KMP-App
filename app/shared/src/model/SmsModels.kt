package com.anix.android.anixlifetracker.model

/**
 * Raw input model representing an incoming or historical SMS message
 */
data class FinancialMessage(
    val id: String,
    val sender: String,
    val body: String,
    val timestampMillis: Long,
    val rawMessageId: String? = null
)

/**
 * Parsing confidence classification
 */
enum class ParseConfidence {
    HIGH_CONFIDENCE,
    NEEDS_REVIEW,
    UNRECOGNIZED,
    REJECTED
}

/**
 * Normalized transaction candidate parsed from an SMS message
 */
data class TransactionCandidate(
    val id: String,
    val amount: Money,
    val direction: TransactionDirection,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val accountRefMasked: String? = null,
    val institutionName: String? = null,
    val merchantName: String? = null,
    val description: String? = null,
    val transactionReference: String? = null,
    val occurredAt: String = "",
    val localTransactionDate: String = "",
    val availableBalance: Money? = null,
    val confidenceScore: Double = 1.0,
    val parseStatus: ParseConfidence = ParseConfidence.HIGH_CONFIDENCE,
    val rejectionReason: String? = null,
    val rawContentSnippet: String? = null,
    val sourceFingerprint: String = "",
    val sender: String = "",
    val suggestedAccountId: String? = null
)

/**
 * Result of parsing a raw financial message
 */
data class TransactionParseResult(
    val status: ParseConfidence,
    val candidate: TransactionCandidate? = null,
    val rejectionReason: String? = null
)

/**
 * Result of attempting transaction ingestion into the system
 */
sealed interface IngestionResult {
    data class Confirmed(val transactionId: String) : IngestionResult
    data class QueuedForReview(val candidateId: String) : IngestionResult
    data class DuplicateSkipped(val fingerprint: String) : IngestionResult
    data class Rejected(val reason: String) : IngestionResult
    data class Failed(val error: String) : IngestionResult
}

/**
 * User preference settings for SMS integration
 */
data class SmsSettingsModel(
    val isSmsFeatureEnabled: Boolean = false,
    val isConsentGiven: Boolean = false,
    val liveMonitoringEnabled: Boolean = false,
    val autoSaveHighConfidence: Boolean = true,
    val importDateRangeDays: Int = 30,
    val lastImportTimestamp: String? = null,
    val totalImportedCount: Int = 0
)

/**
 * Pending SMS transaction candidate awaiting user review
 */
data class SmsReviewCandidateModel(
    val id: String,
    val candidate: TransactionCandidate,
    val createdAt: String = "",
    val status: String = "PENDING"
)

/**
 * Detailed metrics from a historical SMS import run
 */
data class HistoricalImportSummary(
    val scannedCount: Int = 0,
    val financialFoundCount: Int = 0,
    val importedCount: Int = 0,
    val duplicatesSkippedCount: Int = 0,
    val needsReviewCount: Int = 0,
    val errorCount: Int = 0
)
