package com.anix.android.anixlifetracker.database.dao

import androidx.room.*
import com.anix.android.anixlifetracker.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY accountName ASC")
    fun observeActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY accountName ASC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE ownerId = :ownerId AND isActive = 1 ORDER BY accountName ASC")
    fun observeAccountsByOwner(ownerId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE ownerId = :ownerId ORDER BY accountName ASC")
    suspend fun getAccountsForOwnerOnce(ownerId: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)

    @Query("DELETE FROM accounts WHERE ownerId = :ownerId")
    suspend fun deleteAllAccountsForOwner(ownerId: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY occurredAt DESC, createdAt DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ownerId = :ownerId AND deletedAt IS NULL ORDER BY occurredAt DESC LIMIT :limit OFFSET :offset")
    fun observeTransactionsPaginated(ownerId: String, limit: Int, offset: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ownerId = :ownerId AND deletedAt IS NULL ORDER BY occurredAt DESC")
    suspend fun getTransactionsForOwnerOnce(ownerId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND deletedAt IS NULL ORDER BY occurredAt DESC")
    fun observeTransactionsByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND deletedAt IS NULL ORDER BY occurredAt DESC")
    fun observeTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE localTransactionDate BETWEEN :startDate AND :endDate AND deletedAt IS NULL ORDER BY occurredAt DESC")
    fun observeTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET deletedAt = :deletedAtTimestamp WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, deletedAtTimestamp: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDeleteTransaction(id: String)

    @Query("DELETE FROM transactions WHERE ownerId = :ownerId")
    suspend fun deleteAllTransactionsForOwner(ownerId: String)
}

@Dao
interface TransactionSourceRecordDao {
    @Query("SELECT * FROM transaction_source_records WHERE sourceFingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): TransactionSourceRecordEntity?

    @Query("SELECT * FROM transaction_source_records WHERE transactionId = :transactionId")
    suspend fun getByTransactionId(transactionId: String): List<TransactionSourceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSourceRecord(record: TransactionSourceRecordEntity): Long
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND isSystemCategory = 0")
    suspend fun deleteUserCategory(id: String)
}

@Dao
interface CategorizationRuleDao {
    @Query("SELECT * FROM categorization_rules WHERE isActive = 1 ORDER BY priority ASC")
    fun observeActiveRules(): Flow<List<CategorizationRuleEntity>>

    @Query("SELECT * FROM categorization_rules WHERE isActive = 1 ORDER BY priority ASC")
    suspend fun getActiveRules(): List<CategorizationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CategorizationRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<CategorizationRuleEntity>)

    @Update
    suspend fun updateRule(rule: CategorizationRuleEntity)

    @Query("DELETE FROM categorization_rules WHERE id = :id")
    suspend fun deleteRuleById(id: String)
}

@Dao
interface UnifiedReviewItemDao {
    @Query("SELECT * FROM unified_review_items WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingReviewItems(): Flow<List<UnifiedReviewItemEntity>>

    @Query("SELECT * FROM unified_review_items WHERE status = 'PENDING' ORDER BY createdAt DESC")
    suspend fun getPendingReviewItems(): List<UnifiedReviewItemEntity>

    @Query("SELECT * FROM unified_review_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UnifiedReviewItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UnifiedReviewItemEntity)

    @Update
    suspend fun update(item: UnifiedReviewItemEntity)

    @Query("UPDATE unified_review_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM unified_review_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getBudgetById(id: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: String)
}

@Dao
interface RecurringPaymentDao {
    @Query("SELECT * FROM recurring_payments WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun observeActiveRecurringPayments(): Flow<List<RecurringPaymentEntity>>

    @Query("SELECT * FROM recurring_payments WHERE nextDueDate <= :beforeDate AND isActive = 1")
    suspend fun getDueBeforeDate(beforeDate: String): List<RecurringPaymentEntity>

    @Query("SELECT * FROM recurring_payments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: RecurringPaymentEntity)

    @Update
    suspend fun update(payment: RecurringPaymentEntity)

    @Query("DELETE FROM recurring_payments WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface UpcomingObligationDao {
    @Query("SELECT * FROM upcoming_obligations WHERE status = 'PENDING' ORDER BY expectedDate ASC")
    fun observePendingObligations(): Flow<List<UpcomingObligationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obligation: UpcomingObligationEntity)

    @Update
    suspend fun update(obligation: UpcomingObligationEntity)

    @Query("DELETE FROM upcoming_obligations WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface InvestmentAccountDao {
    @Query("SELECT * FROM investment_accounts ORDER BY name ASC")
    fun observeAll(): Flow<List<InvestmentAccountEntity>>

    @Query("SELECT * FROM investment_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InvestmentAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: InvestmentAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<InvestmentAccountEntity>)

    @Update
    suspend fun update(account: InvestmentAccountEntity)

    @Query("DELETE FROM investment_accounts WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface InvestmentInstrumentDao {
    @Query("SELECT * FROM investment_instruments WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveInstruments(): Flow<List<InvestmentInstrumentEntity>>

    @Query("SELECT * FROM investment_instruments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InvestmentInstrumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instrument: InvestmentInstrumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(instruments: List<InvestmentInstrumentEntity>)

    @Update
    suspend fun update(instrument: InvestmentInstrumentEntity)
}

@Dao
interface InvestmentTransactionDao {
    @Query("SELECT * FROM investment_transactions ORDER BY transactionDate DESC")
    fun observeAll(): Flow<List<InvestmentTransactionEntity>>

    @Query("SELECT * FROM investment_transactions WHERE investmentAccountId = :accountId ORDER BY transactionDate DESC")
    fun observeByAccount(accountId: String): Flow<List<InvestmentTransactionEntity>>

    @Query("SELECT * FROM investment_transactions WHERE instrumentId = :instrumentId ORDER BY transactionDate DESC")
    suspend fun getByInstrument(instrumentId: String): List<InvestmentTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: InvestmentTransactionEntity)

    @Update
    suspend fun update(transaction: InvestmentTransactionEntity)

    @Query("DELETE FROM investment_transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface InvestmentHoldingDao {
    @Query("SELECT * FROM investment_holdings ORDER BY currentValueMinor DESC")
    fun observeAll(): Flow<List<InvestmentHoldingEntity>>

    @Query("SELECT * FROM investment_holdings WHERE investmentAccountId = :accountId")
    fun observeByAccount(accountId: String): Flow<List<InvestmentHoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holding: InvestmentHoldingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holdings: List<InvestmentHoldingEntity>)

    @Update
    suspend fun update(holding: InvestmentHoldingEntity)

    @Query("DELETE FROM investment_holdings WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SIPScheduleDao {
    @Query("SELECT * FROM sip_schedules WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun observeActive(): Flow<List<SIPScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sip: SIPScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sips: List<SIPScheduleEntity>)

    @Update
    suspend fun update(sip: SIPScheduleEntity)

    @Query("DELETE FROM sip_schedules WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MarketPriceDao {
    @Query("SELECT * FROM market_prices WHERE instrumentId = :instrumentId ORDER BY priceDate DESC LIMIT 1")
    suspend fun getLatestPrice(instrumentId: String): MarketPriceEntity?

    @Query("SELECT * FROM market_prices WHERE instrumentId = :instrumentId ORDER BY priceDate DESC")
    fun observePriceHistory(instrumentId: String): Flow<List<MarketPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(price: MarketPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<MarketPriceEntity>)
}

@Dao
interface FinancialGoalDao {
    @Query("SELECT * FROM financial_goals WHERE isActive = 1 ORDER BY targetDate ASC")
    fun observeActiveGoals(): Flow<List<FinancialGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: FinancialGoalEntity)

    @Update
    suspend fun update(goal: FinancialGoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE category = :category ORDER BY dueDate ASC")
    fun observeRemindersByCategory(category: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun observePendingReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)
}
