package com.anix.android.anixlifetracker.model

enum class AccountCategoryType {
    CHECKING,
    SAVINGS,
    CURRENT,
    CASH,
    WALLET,
    CREDIT_CARD,
    INVESTMENT_WALLET,
    OTHER
}

enum class TransactionDirection {
    CREDIT,
    DEBIT
}

enum class TransactionKind {
    INCOME,
    EXPENSE,
    TRANSFER,
    INVESTMENT,
    REFUND,
    FEE,
    ADJUSTMENT
}

enum class TransactionState {
    PENDING,
    POSTED,
    REVERSED
}

enum class TransactionSourceType {
    MANUAL,
    SMS,
    NOTIFICATION,
    EMAIL,
    BANK_STATEMENT,
    BANK_API
}

enum class CategoryKind {
    INCOME,
    EXPENSE,
    TRANSFER,
    SYSTEM
}

enum class BudgetPeriodType {
    MONTHLY,
    WEEKLY,
    QUARTERLY,
    YEARLY,
    CUSTOM
}

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    CUSTOM
}

enum class ObligationType {
    CONFIRMED_EXPENSE,
    SCHEDULED_EXPENSE,
    PREDICTED_EXPENSE
}

enum class ObligationStatus {
    PENDING,
    PAID,
    SKIPPED,
    CANCELLED
}

enum class InvestmentAccountType {
    BROKER,
    MUTUAL_FUND_HOUSE,
    RETIREMENT,
    OTHER
}

enum class InstrumentType {
    MUTUAL_FUND,
    STOCK,
    ETF,
    GOLD,
    BOND,
    OTHER
}

enum class InvestmentTransactionType {
    BUY,
    SELL,
    DIVIDEND,
    REINVESTMENT,
    CORPORATE_ACTION,
    ADJUSTMENT
}

enum class SyncStatus {
    LOCAL_ONLY,
    SYNCED,
    PENDING_SYNC,
    PENDING_UPLOAD,
    PENDING_DELETE,
    CONFLICT,
    SYNC_ERROR
}
