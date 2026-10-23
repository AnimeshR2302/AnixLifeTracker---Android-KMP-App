package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<AccountModel>>
    suspend fun getAccount(id: String): AccountModel?
    suspend fun addAccount(account: AccountModel)
    suspend fun updateAccount(account: AccountModel)
    suspend fun deleteAccount(id: String)
}

interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionModel>>
    fun observeTransactionsByAccount(accountId: String): Flow<List<TransactionModel>>
    fun observeTransactionsByCategory(categoryId: String): Flow<List<TransactionModel>>
    fun observeTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransactionModel>>
    suspend fun getTransaction(id: String): TransactionModel?
    suspend fun addTransaction(transaction: TransactionModel)
    suspend fun updateTransaction(transaction: TransactionModel)
    suspend fun deleteTransaction(id: String)
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<TransactionCategoryModel>>
    suspend fun getCategory(id: String): TransactionCategoryModel?
    suspend fun addCategory(category: TransactionCategoryModel)
    suspend fun updateCategory(category: TransactionCategoryModel)
    suspend fun deleteCategory(id: String)
}

interface BudgetRepository {
    fun observeBudgets(): Flow<List<BudgetModel>>
    suspend fun addBudget(budget: BudgetModel)
    suspend fun updateBudget(budget: BudgetModel)
    suspend fun deleteBudget(id: String)
}

interface RecurringPaymentRepository {
    fun observeRecurringPayments(): Flow<List<RecurringPaymentModel>>
    suspend fun addRecurringPayment(payment: RecurringPaymentModel)
    suspend fun updateRecurringPayment(payment: RecurringPaymentModel)
    suspend fun deleteRecurringPayment(id: String)
}

interface UpcomingObligationRepository {
    fun observePendingObligations(): Flow<List<UpcomingObligationModel>>
    suspend fun addObligation(obligation: UpcomingObligationModel)
    suspend fun updateObligation(obligation: UpcomingObligationModel)
    suspend fun deleteObligation(id: String)
}

interface InvestmentRepository {
    fun observeAccounts(): Flow<List<InvestmentAccountModel>>
    fun observeInstruments(): Flow<List<InvestmentInstrumentModel>>
    fun observeTransactions(): Flow<List<InvestmentTransactionModel>>
    fun observeHoldings(): Flow<List<InvestmentHoldingDetailModel>>
    suspend fun addAccount(account: InvestmentAccountModel)
    suspend fun addInstrument(instrument: InvestmentInstrumentModel)
    suspend fun addTransaction(transaction: InvestmentTransactionModel)
    suspend fun updateHolding(holding: InvestmentHoldingDetailModel)
}

interface SIPRepository {
    fun observeSIPs(): Flow<List<SIPScheduleModel>>
    suspend fun addSIP(sip: SIPScheduleModel)
    suspend fun updateSIP(sip: SIPScheduleModel)
    suspend fun deleteSIP(id: String)
}

interface MarketPriceRepository {
    suspend fun getLatestPrice(instrumentId: String): MarketPriceModel?
    fun observePriceHistory(instrumentId: String): Flow<List<MarketPriceModel>>
    suspend fun addPrice(price: MarketPriceModel)
}

interface FinancialGoalRepository {
    fun observeGoals(): Flow<List<FinancialGoalModel>>
    suspend fun addGoal(goal: FinancialGoalModel)
    suspend fun updateGoal(goal: FinancialGoalModel)
    suspend fun deleteGoal(id: String)
}

interface ReminderRepository {
    fun observeReminders(): Flow<List<ReminderRecordModel>>
    fun observeRemindersByCategory(category: String): Flow<List<ReminderRecordModel>>
    suspend fun addReminder(reminder: ReminderRecordModel)
    suspend fun updateReminder(reminder: ReminderRecordModel)
    suspend fun deleteReminder(id: String)

    // Plan 8 Extended Capabilities
    fun observeDefinitions(): Flow<List<ReminderDefinitionModel>>
    fun observeOccurrences(): Flow<List<ReminderOccurrenceModel>>
    fun observeActiveOccurrences(): Flow<List<ReminderOccurrenceModel>>
    suspend fun addDefinition(definition: ReminderDefinitionModel, schedule: ReminderScheduleModel)
    suspend fun updateDefinition(definition: ReminderDefinitionModel, schedule: ReminderScheduleModel? = null)
    suspend fun performOccurrenceAction(occurrenceId: String, actionType: ReminderActionType, ownerId: String = "local_user")
    suspend fun getPreferences(ownerId: String = "local_user"): ReminderPreferencesModel?
    suspend fun savePreferences(preferences: ReminderPreferencesModel)
}
