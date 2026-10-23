package com.anix.android.anixlifetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anix.android.anixlifetracker.database.dao.*
import com.anix.android.anixlifetracker.database.entity.*

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        TransactionSourceRecordEntity::class,
        CategoryEntity::class,
        CategorizationRuleEntity::class,
        UnifiedReviewItemEntity::class,
        BudgetEntity::class,
        RecurringPaymentEntity::class,
        UpcomingObligationEntity::class,
        InvestmentAccountEntity::class,
        InvestmentInstrumentEntity::class,
        InvestmentTransactionEntity::class,
        InvestmentHoldingEntity::class,
        SIPScheduleEntity::class,
        MarketPriceEntity::class,
        FinancialGoalEntity::class,
        ReminderEntity::class,
        ReminderScheduleEntity::class,
        ReminderOccurrenceEntity::class,
        ReminderActionEntity::class,
        InAppInboxItemEntity::class,
        ReminderPreferencesEntity::class,
        SyncCheckpointEntity::class,
        SyncTombstoneEntity::class,
        SyncConflictEntity::class,
        SmsReviewCandidateEntity::class,
        SmsSettingsEntity::class,
        NotificationSettingsEntity::class,
        NotificationAppConfigEntity::class,
        NotificationReviewCandidateEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionSourceRecordDao(): TransactionSourceRecordDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categorizationRuleDao(): CategorizationRuleDao
    abstract fun unifiedReviewItemDao(): UnifiedReviewItemDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringPaymentDao(): RecurringPaymentDao
    abstract fun upcomingObligationDao(): UpcomingObligationDao
    abstract fun investmentAccountDao(): InvestmentAccountDao
    abstract fun investmentInstrumentDao(): InvestmentInstrumentDao
    abstract fun investmentTransactionDao(): InvestmentTransactionDao
    abstract fun investmentHoldingDao(): InvestmentHoldingDao
    abstract fun sipScheduleDao(): SIPScheduleDao
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun financialGoalDao(): FinancialGoalDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderScheduleDao(): ReminderScheduleDao
    abstract fun reminderOccurrenceDao(): ReminderOccurrenceDao
    abstract fun reminderActionDao(): ReminderActionDao
    abstract fun inAppInboxDao(): InAppInboxDao
    abstract fun reminderPreferencesDao(): ReminderPreferencesDao
    abstract fun syncDao(): SyncDao
    abstract fun smsReviewCandidateDao(): SmsReviewCandidateDao
    abstract fun smsSettingsDao(): SmsSettingsDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao
    abstract fun notificationAppConfigDao(): NotificationAppConfigDao
    abstract fun notificationReviewCandidateDao(): NotificationReviewCandidateDao
}
