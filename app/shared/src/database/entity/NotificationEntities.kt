package com.anix.android.anixlifetracker.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anix.android.anixlifetracker.model.ParseConfidence
import com.anix.android.anixlifetracker.model.TransactionDirection
import com.anix.android.anixlifetracker.model.TransactionType

@Entity(
    tableName = "notification_settings"
)
data class NotificationSettingsEntity(
    @PrimaryKey val id: String = "notification_settings_root",
    val isNotificationFeatureEnabled: Boolean,
    val isConsentGiven: Boolean,
    val liveMonitoringEnabled: Boolean,
    val autoSaveHighConfidence: Boolean,
    val lastProcessedTimestamp: String?,
    val totalImportedCount: Int,
    val updatedAt: String
)

@Entity(
    tableName = "notification_app_configs",
    indices = [
        Index(value = ["packageName"], unique = true)
    ]
)
data class NotificationAppConfigEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isEnabled: Boolean,
    val parserProfileId: String,
    val lastProcessedAt: String?,
    val updatedAt: String
)

@Entity(
    tableName = "notification_review_candidates",
    indices = [
        Index(value = ["status"]),
        Index(value = ["sourceFingerprint"], unique = true),
        Index(value = ["sourcePackage"])
    ]
)
data class NotificationReviewCandidateEntity(
    @PrimaryKey val id: String,
    val sourcePackage: String,
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
