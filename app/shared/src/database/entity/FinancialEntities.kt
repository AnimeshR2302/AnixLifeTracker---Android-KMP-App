package com.anix.android.anixlifetracker.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anix.android.anixlifetracker.model.*

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["accountType"])
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val accountName: String,
    val accountType: AccountCategoryType,
    val institutionName: String,
    val accountReferenceMasked: String,
    val currencyCode: String,
    val openingBalanceMinor: Long,
    val balanceSnapshotMinor: Long,
    val balanceSnapshotTimestamp: String,
    val isObservedBalance: Boolean = false,
    val lastObservationTimestamp: String? = null,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["localTransactionDate"]),
        Index(value = ["transactionType"]),
        Index(value = ["occurredAt"]),
        Index(value = ["ownerId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val accountId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val direction: TransactionDirection,
    val transactionType: TransactionType,
    val categoryId: String?,
    val merchantName: String?,
    val description: String?,
    val transactionReference: String?,
    val occurredAt: String,
    val localTransactionDate: String,
    val status: TransactionState,
    val transferGroupId: String?,
    val refundParentTransactionId: String? = null,
    val isUserOverridden: Boolean = false,
    val userOverriddenFieldsJson: String? = null,
    val processingStatus: String = "CONFIRMED",
    val sourceType: TransactionSourceType,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val syncStatus: SyncStatus
)

@Entity(
    tableName = "transaction_source_records",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["sourceFingerprint"], unique = true)
    ]
)
data class TransactionSourceRecordEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val sourceType: TransactionSourceType,
    val sourceIdentifier: String?,
    val sourceFingerprint: String,
    val rawContentSnippet: String?,
    val receivedAt: String,
    val processingStatus: String,
    val createdAt: String
)

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["parentCategoryId"]),
        Index(value = ["categoryType"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val parentCategoryId: String?,
    val categoryType: CategoryKind,
    val iconIdentifier: String,
    val isSystemCategory: Boolean,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "categorization_rules",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["priority"])
    ]
)
data class CategorizationRuleEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val priority: Int,
    val merchantPattern: String?,
    val descriptionPattern: String?,
    val accountTypePattern: String?,
    val targetCategoryId: String,
    val confidenceScore: Double,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "unified_review_items",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["status"])
    ]
)
data class UnifiedReviewItemEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val candidateId: String,
    val sourceType: TransactionSourceType,
    val title: String,
    val subtitle: String,
    val candidateJson: String,
    val matchType: String,
    val suggestedAction: String,
    val confidenceScore: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["categoryId"])
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val categoryId: String?,
    val limitAmountMinor: Long,
    val currencyCode: String,
    val periodType: BudgetPeriodType,
    val startDate: String,
    val endDate: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "recurring_payments",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["accountId"]),
        Index(value = ["nextDueDate"])
    ]
)
data class RecurringPaymentEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val accountId: String,
    val name: String,
    val categoryId: String?,
    val expectedAmountMinor: Long,
    val currencyCode: String,
    val frequency: RecurrenceFrequency,
    val startDate: String,
    val nextDueDate: String,
    val endDate: String?,
    val isVariableAmount: Boolean,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "upcoming_obligations",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["expectedDate"]),
        Index(value = ["recurringPaymentId"])
    ]
)
data class UpcomingObligationEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val accountId: String?,
    val recurringPaymentId: String?,
    val title: String,
    val expectedAmountMinor: Long,
    val currencyCode: String,
    val expectedDate: String,
    val obligationType: ObligationType,
    val status: ObligationStatus,
    val linkedTransactionId: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "investment_accounts",
    indices = [
        Index(value = ["ownerId"])
    ]
)
data class InvestmentAccountEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val provider: String,
    val accountType: InvestmentAccountType,
    val currencyCode: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "investment_instruments",
    indices = [
        Index(value = ["instrumentType"]),
        Index(value = ["isin"]),
        Index(value = ["symbol"])
    ]
)
data class InvestmentInstrumentEntity(
    @PrimaryKey val id: String,
    val instrumentType: InstrumentType,
    val name: String,
    val symbol: String?,
    val isin: String?,
    val amfiCode: String?,
    val exchange: String?,
    val currencyCode: String,
    val isActive: Boolean
)

@Entity(
    tableName = "investment_transactions",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["investmentAccountId"]),
        Index(value = ["instrumentId"]),
        Index(value = ["transactionDate"])
    ]
)
data class InvestmentTransactionEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val investmentAccountId: String,
    val instrumentId: String,
    val transactionType: InvestmentTransactionType,
    val quantityFixedPoint: Long,
    val priceMinor: Long,
    val currencyCode: String,
    val grossAmountMinor: Long,
    val feesMinor: Long,
    val taxesMinor: Long,
    val transactionDate: String,
    val linkedFinancialTransactionId: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "investment_holdings",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["investmentAccountId"]),
        Index(value = ["instrumentId"])
    ]
)
data class InvestmentHoldingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val investmentAccountId: String,
    val instrumentId: String,
    val totalUnitsFixedPoint: Long,
    val averageAcquisitionPriceMinor: Long,
    val currentValueMinor: Long,
    val currencyCode: String,
    val unrealizedGainMinor: Long,
    val realizedGainMinor: Long,
    val lastCalculatedAt: String
)

@Entity(
    tableName = "sip_schedules",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["investmentAccountId"]),
        Index(value = ["instrumentId"]),
        Index(value = ["nextDueDate"])
    ]
)
data class SIPScheduleEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val investmentAccountId: String,
    val instrumentId: String,
    val contributionAmountMinor: Long,
    val currencyCode: String,
    val frequency: RecurrenceFrequency,
    val startDate: String,
    val nextDueDate: String,
    val endDate: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "market_prices",
    primaryKeys = ["instrumentId", "priceDate", "source"],
    indices = [
        Index(value = ["instrumentId"]),
        Index(value = ["priceDate"])
    ]
)
data class MarketPriceEntity(
    val instrumentId: String,
    val priceDate: String,
    val priceMinor: Long,
    val currencyCode: String,
    val source: String,
    val fetchedAt: String
)

@Entity(
    tableName = "financial_goals",
    indices = [
        Index(value = ["ownerId"])
    ]
)
data class FinancialGoalEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val targetAmountMinor: Long,
    val currencyCode: String,
    val targetDate: String,
    val associatedAccountIdsJson: String,
    val currentProgressMinor: Long,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["dueDate"]),
        Index(value = ["category"])
    ]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val dueDate: String,
    val category: ReminderCategory,
    val priority: ReminderPriority,
    val isCompleted: Boolean,
    val linkedEntityType: String?,
    val linkedEntityId: String?,
    val createdAt: String,
    val updatedAt: String,
    val sourceType: String = "MANUAL",
    val isEnabled: Boolean = true,
    val scheduleVersion: Long = 1L,
    val serverRevision: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

@Entity(tableName = "sync_checkpoints")
data class SyncCheckpointEntity(
    @PrimaryKey val entityType: String,
    val lastSyncTimestamp: String,
    val lastSyncRevision: Long,
    val updatedAt: String
)

@Entity(
    tableName = "sync_tombstones",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["entityType"])
    ]
)
data class SyncTombstoneEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val ownerId: String,
    val deletedAt: String,
    val syncStatus: SyncStatus
)

@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["entityType"])
    ]
)
data class SyncConflictEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val ownerId: String,
    val localDataJson: String,
    val remoteDataJson: String,
    val conflictReason: String,
    val isResolved: Boolean,
    val createdAt: String
)
