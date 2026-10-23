package com.anix.android.anixlifetracker.repository

import com.anix.android.anixlifetracker.database.AppDatabase
import com.anix.android.anixlifetracker.database.entity.*
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.notification.engine.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(private val db: AppDatabase) : AccountRepository {
    override fun observeAccounts(): Flow<List<AccountModel>> {
        return db.accountDao().observeAllAccounts().map { list -> list.map { it.toModel() } }
    }

    override suspend fun getAccount(id: String): AccountModel? {
        return db.accountDao().getAccountById(id)?.toModel()
    }

    override suspend fun addAccount(account: AccountModel) {
        db.accountDao().insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: AccountModel) {
        db.accountDao().updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(id: String) {
        db.accountDao().deleteAccountById(id)
    }
}

class TransactionRepositoryImpl(private val db: AppDatabase) : TransactionRepository {
    override fun observeTransactions(): Flow<List<TransactionModel>> {
        return db.transactionDao().observeAllTransactions().map { list ->
            list.map { entity ->
                val categoryName = entity.categoryId?.let { db.categoryDao().getCategoryById(it)?.name }
                entity.toModel(categoryName)
            }
        }
    }

    override fun observeTransactionsByAccount(accountId: String): Flow<List<TransactionModel>> {
        return db.transactionDao().observeTransactionsByAccount(accountId).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun observeTransactionsByCategory(categoryId: String): Flow<List<TransactionModel>> {
        return db.transactionDao().observeTransactionsByCategory(categoryId).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun observeTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransactionModel>> {
        return db.transactionDao().observeTransactionsByDateRange(startDate, endDate).map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun getTransaction(id: String): TransactionModel? {
        return db.transactionDao().getTransactionById(id)?.toModel()
    }

    override suspend fun addTransaction(transaction: TransactionModel) {
        db.transactionDao().insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: TransactionModel) {
        db.transactionDao().updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        db.transactionDao().softDeleteTransaction(id, "DELETED")
    }
}

class CategoryRepositoryImpl(private val db: AppDatabase) : CategoryRepository {
    override fun observeCategories(): Flow<List<TransactionCategoryModel>> {
        return db.categoryDao().observeActiveCategories().map { list -> list.map { it.toModel() } }
    }

    override suspend fun getCategory(id: String): TransactionCategoryModel? {
        return db.categoryDao().getCategoryById(id)?.toModel()
    }

    override suspend fun addCategory(category: TransactionCategoryModel) {
        db.categoryDao().insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: TransactionCategoryModel) {
        db.categoryDao().updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        db.categoryDao().deleteUserCategory(id)
    }
}

class BudgetRepositoryImpl(private val db: AppDatabase) : BudgetRepository {
    override fun observeBudgets(): Flow<List<BudgetModel>> {
        return db.budgetDao().observeActiveBudgets().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addBudget(budget: BudgetModel) {
        db.budgetDao().insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: BudgetModel) {
        db.budgetDao().updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(id: String) {
        db.budgetDao().deleteBudgetById(id)
    }
}

class RecurringPaymentRepositoryImpl(private val db: AppDatabase) : RecurringPaymentRepository {
    override fun observeRecurringPayments(): Flow<List<RecurringPaymentModel>> {
        return db.recurringPaymentDao().observeActiveRecurringPayments().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addRecurringPayment(payment: RecurringPaymentModel) {
        db.recurringPaymentDao().insert(payment.toEntity())
    }

    override suspend fun updateRecurringPayment(payment: RecurringPaymentModel) {
        db.recurringPaymentDao().update(payment.toEntity())
    }

    override suspend fun deleteRecurringPayment(id: String) {
        db.recurringPaymentDao().deleteById(id)
    }
}

class UpcomingObligationRepositoryImpl(private val db: AppDatabase) : UpcomingObligationRepository {
    override fun observePendingObligations(): Flow<List<UpcomingObligationModel>> {
        return db.upcomingObligationDao().observePendingObligations().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addObligation(obligation: UpcomingObligationModel) {
        db.upcomingObligationDao().insert(obligation.toEntity())
    }

    override suspend fun updateObligation(obligation: UpcomingObligationModel) {
        db.upcomingObligationDao().update(obligation.toEntity())
    }

    override suspend fun deleteObligation(id: String) {
        db.upcomingObligationDao().deleteById(id)
    }
}

class InvestmentRepositoryImpl(private val db: AppDatabase) : InvestmentRepository {
    override fun observeAccounts(): Flow<List<InvestmentAccountModel>> {
        return db.investmentAccountDao().observeAll().map { list -> list.map { it.toModel() } }
    }

    override fun observeInstruments(): Flow<List<InvestmentInstrumentModel>> {
        return db.investmentInstrumentDao().observeActiveInstruments().map { list -> list.map { it.toModel() } }
    }

    override fun observeTransactions(): Flow<List<InvestmentTransactionModel>> {
        return db.investmentTransactionDao().observeAll().map { list -> list.map { it.toModel() } }
    }

    override fun observeHoldings(): Flow<List<InvestmentHoldingDetailModel>> {
        return db.investmentHoldingDao().observeAll().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addAccount(account: InvestmentAccountModel) {
        db.investmentAccountDao().insert(account.toEntity())
    }

    override suspend fun addInstrument(instrument: InvestmentInstrumentModel) {
        db.investmentInstrumentDao().insert(instrument.toEntity())
    }

    override suspend fun addTransaction(transaction: InvestmentTransactionModel) {
        db.investmentTransactionDao().insert(transaction.toEntity())
    }

    override suspend fun updateHolding(holding: InvestmentHoldingDetailModel) {
        db.investmentHoldingDao().update(holding.toEntity())
    }
}

class SIPRepositoryImpl(private val db: AppDatabase) : SIPRepository {
    override fun observeSIPs(): Flow<List<SIPScheduleModel>> {
        return db.sipScheduleDao().observeActive().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addSIP(sip: SIPScheduleModel) {
        db.sipScheduleDao().insert(sip.toEntity())
    }

    override suspend fun updateSIP(sip: SIPScheduleModel) {
        db.sipScheduleDao().update(sip.toEntity())
    }

    override suspend fun deleteSIP(id: String) {
        db.sipScheduleDao().deleteById(id)
    }
}

class MarketPriceRepositoryImpl(private val db: AppDatabase) : MarketPriceRepository {
    override suspend fun getLatestPrice(instrumentId: String): MarketPriceModel? {
        return db.marketPriceDao().getLatestPrice(instrumentId)?.toModel()
    }

    override fun observePriceHistory(instrumentId: String): Flow<List<MarketPriceModel>> {
        return db.marketPriceDao().observePriceHistory(instrumentId).map { list -> list.map { it.toModel() } }
    }

    override suspend fun addPrice(price: MarketPriceModel) {
        db.marketPriceDao().insert(price.toEntity())
    }
}

class FinancialGoalRepositoryImpl(private val db: AppDatabase) : FinancialGoalRepository {
    override fun observeGoals(): Flow<List<FinancialGoalModel>> {
        return db.financialGoalDao().observeActiveGoals().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addGoal(goal: FinancialGoalModel) {
        db.financialGoalDao().insert(goal.toEntity())
    }

    override suspend fun updateGoal(goal: FinancialGoalModel) {
        db.financialGoalDao().update(goal.toEntity())
    }

    override suspend fun deleteGoal(id: String) {
        db.financialGoalDao().deleteById(id)
    }
}

class ReminderRepositoryImpl(
    private val db: AppDatabase,
    private val coordinator: ReminderCoordinator = DefaultReminderCoordinator(db)
) : ReminderRepository {
    override fun observeReminders(): Flow<List<ReminderRecordModel>> {
        return db.reminderDao().observeAllReminders().map { list -> list.map { it.toModel() } }
    }

    override fun observeRemindersByCategory(category: String): Flow<List<ReminderRecordModel>> {
        return db.reminderDao().observeRemindersByCategory(category).map { list -> list.map { it.toModel() } }
    }

    override suspend fun addReminder(reminder: ReminderRecordModel) {
        db.reminderDao().insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: ReminderRecordModel) {
        db.reminderDao().updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(id: String) {
        db.reminderDao().deleteReminderById(id)
    }

    override fun observeDefinitions(): Flow<List<ReminderDefinitionModel>> {
        return db.reminderDao().observeAllReminders().map { list -> list.map { it.toDefinitionModel() } }
    }

    override fun observeOccurrences(): Flow<List<ReminderOccurrenceModel>> {
        return db.reminderOccurrenceDao().observeAllOccurrences().map { list -> list.map { it.toModel() } }
    }

    override fun observeActiveOccurrences(): Flow<List<ReminderOccurrenceModel>> {
        return db.reminderOccurrenceDao().observeActiveOccurrences().map { list -> list.map { it.toModel() } }
    }

    override suspend fun addDefinition(definition: ReminderDefinitionModel, schedule: ReminderScheduleModel) {
        db.reminderDao().insertReminder(definition.toEntity(dueDate = schedule.localDueDateOrTime))
        db.reminderScheduleDao().insertSchedule(schedule.toEntity())
        coordinator.reconcileReminders(definition.ownerId)
    }

    override suspend fun updateDefinition(definition: ReminderDefinitionModel, schedule: ReminderScheduleModel?) {
        db.reminderDao().updateReminder(definition.toEntity(dueDate = schedule?.localDueDateOrTime ?: ""))
        if (schedule != null) {
            db.reminderScheduleDao().insertSchedule(schedule.toEntity())
        }
        coordinator.reconcileReminders(definition.ownerId)
    }

    override suspend fun performOccurrenceAction(occurrenceId: String, actionType: ReminderActionType, ownerId: String) {
        coordinator.handleOccurrenceAction(occurrenceId, actionType, ownerId)
    }

    override suspend fun getPreferences(ownerId: String): ReminderPreferencesModel? {
        return db.reminderPreferencesDao().getPreferences(ownerId)?.toModel()
    }

    override suspend fun savePreferences(preferences: ReminderPreferencesModel) {
        db.reminderPreferencesDao().savePreferences(preferences.toEntity())
    }
}
