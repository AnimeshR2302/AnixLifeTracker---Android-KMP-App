package com.anix.android.anixlifetracker.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anix.android.anixlifetracker.model.ParseConfidence
import com.anix.android.anixlifetracker.model.TransactionDirection
import com.anix.android.anixlifetracker.model.TransactionType

@Entity(
    tableName = "sms_review_candidates",
    indices = [
        Index(value = ["status"]),
        Index(value = ["sourceFingerprint"], unique = true)
    ]
)
data class SmsReviewCandidateEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,
    val currencyCode: String,
    val direction: TransactionDirection,
    val transactionType: TransactionType,
    val accountRefMasked: String?,
    val institutionName: String?,
    val merchantName: String?,
    val description: String?,
    val transactionReference: String?,
    val occurredAt: String,
    val localTransactionDate: String,
    val availableBalanceMinor: Long?,
    val confidenceScore: Double,
    val parseStatus: ParseConfidence,
    val rejectionReason: String?,
    val rawContentSnippet: String?,
    val sourceFingerprint: String,
    val sender: String,
    val suggestedAccountId: String?,
    val status: String,
    val createdAt: String
)

@Entity(
    tableName = "sms_settings"
)
data class SmsSettingsEntity(
    @PrimaryKey val id: String = "sms_settings_root",
    val isSmsFeatureEnabled: Boolean,
    val isConsentGiven: Boolean,
    val liveMonitoringEnabled: Boolean,
    val autoSaveHighConfidence: Boolean,
    val importDateRangeDays: Int,
    val lastImportTimestamp: String?,
    val totalImportedCount: Int,
    val updatedAt: String
)
