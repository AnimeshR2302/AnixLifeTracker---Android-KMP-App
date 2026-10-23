package com.anix.android.anixlifetracker.model

/**
 * Represents a financial account (Checking, Savings, Wallet, Credit Card, etc.)
 */
data class AccountModel(
    val id: String,
    val ownerId: String = "local_user",
    val accountName: String,
    val accountType: AccountCategoryType,
    val institutionName: String = "",
    val accountReferenceMasked: String = "",
    val currencyCode: String = "INR",
    val openingBalance: Money = Money.zero(currencyCode),
    val balanceSnapshot: Money = Money.zero(currencyCode),
    val balanceSnapshotTimestamp: String = "",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

/**
 * Represents a financial transaction ledger record
 */
data class TransactionModel(
    val id: String,
    val ownerId: String = "local_user",
    val accountId: String,
    val amount: Money,
    val direction: TransactionDirection,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val merchantName: String? = null,
    val description: String? = null,
    val transactionReference: String? = null,
    val occurredAt: String = "",
    val localTransactionDate: String = "",
    val status: TransactionState = TransactionState.POSTED,
    val transferGroupId: String? = null,
    val sourceType: TransactionSourceType = TransactionSourceType.MANUAL,
    val createdAt: String = "",
    val updatedAt: String = "",
    val deletedAt: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

/**
 * Represents provenance and source data for transaction ingestion deduplication
 */
data class TransactionSourceRecordModel(
    val id: String,
    val transactionId: String,
    val sourceType: TransactionSourceType,
    val sourceIdentifier: String? = null,
    val sourceFingerprint: String,
    val rawContentSnippet: String? = null,
    val receivedAt: String = "",
    val processingStatus: String = "PROCESSED",
    val createdAt: String = ""
)

/**
 * Represents a spending or income category
 */
data class TransactionCategoryModel(
    val id: String,
    val ownerId: String = "local_user",
    val name: String,
    val parentCategoryId: String? = null,
    val categoryType: CategoryKind,
    val iconIdentifier: String = "category",
    val isSystemCategory: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents a user budget
 */
data class BudgetModel(
    val id: String,
    val ownerId: String = "local_user",
    val name: String,
    val categoryId: String? = null,
    val limitAmount: Money,
    val periodType: BudgetPeriodType = BudgetPeriodType.MONTHLY,
    val startDate: String = "",
    val endDate: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents a recurring payment rule
 */
data class RecurringPaymentModel(
    val id: String,
    val ownerId: String = "local_user",
    val accountId: String,
    val name: String,
    val categoryId: String? = null,
    val expectedAmount: Money,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val startDate: String = "",
    val nextDueDate: String = "",
    val endDate: String? = null,
    val isVariableAmount: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents an upcoming expected cash inflow or outflow
 */
data class UpcomingObligationModel(
    val id: String,
    val ownerId: String = "local_user",
    val accountId: String? = null,
    val recurringPaymentId: String? = null,
    val title: String,
    val expectedAmount: Money,
    val expectedDate: String,
    val obligationType: ObligationType = ObligationType.SCHEDULED_EXPENSE,
    val status: ObligationStatus = ObligationStatus.PENDING,
    val linkedTransactionId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents an investment broker or fund house account
 */
data class InvestmentAccountModel(
    val id: String,
    val ownerId: String = "local_user",
    val name: String,
    val provider: String = "",
    val accountType: InvestmentAccountType = InvestmentAccountType.BROKER,
    val currencyCode: String = "INR",
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents an investable financial instrument
 */
data class InvestmentInstrumentModel(
    val id: String,
    val instrumentType: InstrumentType,
    val name: String,
    val symbol: String? = null,
    val isin: String? = null,
    val amfiCode: String? = null,
    val exchange: String? = null,
    val currencyCode: String = "INR",
    val isActive: Boolean = true
)

/**
 * Represents an investment purchase, sale, or dividend event
 */
data class InvestmentTransactionModel(
    val id: String,
    val ownerId: String = "local_user",
    val investmentAccountId: String,
    val instrumentId: String,
    val transactionType: InvestmentTransactionType,
    val quantityFixedPoint: Long, // Scaled by 1,000,000 for 6 decimal precision
    val price: Money,
    val grossAmount: Money,
    val fees: Money = Money.zero(price.currencyCode),
    val taxes: Money = Money.zero(price.currencyCode),
    val transactionDate: String = "",
    val linkedFinancialTransactionId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Represents current calculated investment holdings
 */
data class InvestmentHoldingDetailModel(
    val id: String,
    val ownerId: String = "local_user",
    val investmentAccountId: String,
    val instrumentId: String,
    val totalUnitsFixedPoint: Long,
    val averageAcquisitionPrice: Money,
    val currentValue: Money,
    val unrealizedGain: Money,
    val realizedGain: Money,
    val lastCalculatedAt: String = ""
)

/**
 * Represents a systematic investment plan (SIP) mandate
 */
data class SIPScheduleModel(
    val id: String,
    val ownerId: String = "local_user",
    val investmentAccountId: String,
    val instrumentId: String,
    val contributionAmount: Money,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val startDate: String = "",
    val nextDueDate: String = "",
    val endDate: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Historical NAV or price quote for an instrument
 */
data class MarketPriceModel(
    val instrumentId: String,
    val priceDate: String,
    val price: Money,
    val source: String = "MANUAL",
    val fetchedAt: String = ""
)

/**
 * Long-term financial goal
 */
data class FinancialGoalModel(
    val id: String,
    val ownerId: String = "local_user",
    val name: String,
    val targetAmount: Money,
    val targetDate: String,
    val associatedAccountIds: List<String> = emptyList(),
    val currentProgress: Money = Money.zero(targetAmount.currencyCode),
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Decoupled universal reminder model
 */
data class ReminderRecordModel(
    val id: String,
    val ownerId: String = "local_user",
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val category: ReminderCategory,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val linkedEntityType: String? = null,
    val linkedEntityId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
