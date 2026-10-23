package com.anix.android.anixlifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.repository.*
import com.anix.android.anixlifetracker.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class FinancesUiState(
    val accounts: List<AccountModel> = emptyList(),
    val accountSummaries: List<AccountBalanceSummary> = emptyList(),
    val transactions: List<TransactionModel> = emptyList(),
    val categories: List<TransactionCategoryModel> = emptyList(),
    val summary: FinancialSummaryResult = FinancialSummaryResult(),
    val categoryBreakdown: List<CategorySpendTotal> = emptyList(),
    val savingsRate: SavingsRateResult = SavingsRateResult(Money.zero(), null, false, "Calculating"),
    val budgetStatuses: List<BudgetStatusModel> = emptyList(),
    val expenseForecast: MonthEndExpenseForecastResult? = null,
    val cashFlowForecast: CashFlowForecastResult? = null,
    val spendingAnomalies: List<SpendingAnomaly> = emptyList(),
    val selectedCategoryFilter: String? = null,
    val isAddTransactionDialogOpen: Boolean = false,
    val isAddAccountDialogOpen: Boolean = false,
    val isAddBudgetDialogOpen: Boolean = false,
    val transactionToDelete: TransactionModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FinancesViewModel(
    private val accountRepository: AccountRepository = AccountRepositoryImpl(DatabaseManager.getDatabase()),
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(DatabaseManager.getDatabase()),
    private val categoryRepository: CategoryRepository = CategoryRepositoryImpl(DatabaseManager.getDatabase()),
    private val budgetRepository: BudgetRepository = BudgetRepositoryImpl(DatabaseManager.getDatabase()),
    private val recurringRepository: RecurringPaymentRepository = RecurringPaymentRepositoryImpl(DatabaseManager.getDatabase()),
    private val addTransactionUseCase: AddTransactionUseCase = AddTransactionUseCase(transactionRepository, accountRepository),
    private val deleteTransactionUseCase: DeleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, accountRepository),
    private val addAccountUseCase: AddAccountUseCase = AddAccountUseCase(accountRepository),
    private val addBudgetUseCase: AddBudgetUseCase = AddBudgetUseCase(budgetRepository)
) : ViewModel() {

    private val summaryUseCase = GetFinancialSummaryUseCase(transactionRepository)
    private val accountBalancesUseCase = GetAccountBalancesUseCase(accountRepository)
    private val categoryBreakdownUseCase = GetCategoryBreakdownUseCase(transactionRepository, categoryRepository)
    private val savingsRateUseCase = GetSavingsRateUseCase(transactionRepository)
    private val manageBudgetsUseCase = ManageBudgetsUseCase(budgetRepository, transactionRepository)
    private val forecastUseCase = GetMonthEndExpenseForecastUseCase(transactionRepository, recurringRepository)
    private val cashFlowUseCase = GetCashFlowForecastUseCase(accountRepository, transactionRepository, recurringRepository)
    private val anomaliesUseCase = GetSpendingAnomaliesUseCase(transactionRepository)

    private val _uiState = MutableStateFlow(FinancesUiState(isLoading = true))
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                accountBalancesUseCase().catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { summaries ->
                        _uiState.value = _uiState.value.copy(
                            accounts = summaries.map { it.account },
                            accountSummaries = summaries,
                            isLoading = false
                        )
                    }
            }

            launch {
                categoryRepository.observeCategories()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { categories ->
                        _uiState.value = _uiState.value.copy(categories = categories)
                    }
            }

            launch {
                transactionRepository.observeTransactions()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { transactions ->
                        _uiState.value = _uiState.value.copy(transactions = transactions)
                    }
            }

            launch {
                summaryUseCase().catch { }.collect { sum ->
                    _uiState.value = _uiState.value.copy(summary = sum)
                }
            }

            launch {
                categoryBreakdownUseCase().catch { }.collect { breakdown ->
                    _uiState.value = _uiState.value.copy(categoryBreakdown = breakdown)
                }
            }

            launch {
                savingsRateUseCase().catch { }.collect { savings ->
                    _uiState.value = _uiState.value.copy(savingsRate = savings)
                }
            }

            launch {
                manageBudgetsUseCase.observeBudgetStatuses().catch { }.collect { budgets ->
                    _uiState.value = _uiState.value.copy(budgetStatuses = budgets)
                }
            }

            launch {
                forecastUseCase().catch { }.collect { forecast ->
                    _uiState.value = _uiState.value.copy(expenseForecast = forecast)
                }
            }

            launch {
                cashFlowUseCase().catch { }.collect { cashFlow ->
                    _uiState.value = _uiState.value.copy(cashFlowForecast = cashFlow)
                }
            }

            launch {
                anomaliesUseCase().catch { }.collect { anomalies ->
                    _uiState.value = _uiState.value.copy(spendingAnomalies = anomalies)
                }
            }
        }
    }

    fun selectCategoryFilter(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = categoryId)
    }

    fun toggleAddTransactionDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddTransactionDialogOpen = isOpen)
    }

    fun toggleAddAccountDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddAccountDialogOpen = isOpen)
    }

    fun toggleAddBudgetDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddBudgetDialogOpen = isOpen)
    }

    fun confirmDeleteTransaction(transaction: TransactionModel?) {
        _uiState.value = _uiState.value.copy(transactionToDelete = transaction)
    }

    fun addTransaction(
        accountId: String,
        amountInCents: Long,
        type: TransactionType,
        direction: TransactionDirection,
        categoryId: String?,
        description: String?,
        localDate: String
    ) {
        viewModelScope.launch {
            try {
                val newTx = TransactionModel(
                    id = "tx_${Clock.System.now().toEpochMilliseconds()}",
                    ownerId = "local_user",
                    accountId = accountId,
                    amount = Money(amountInCents),
                    direction = direction,
                    transactionType = type,
                    categoryId = categoryId,
                    description = description,
                    occurredAt = localDate,
                    localTransactionDate = localDate,
                    sourceType = TransactionSourceType.MANUAL
                )
                addTransactionUseCase(newTx)
                _uiState.value = _uiState.value.copy(isAddTransactionDialogOpen = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transactionId)
                _uiState.value = _uiState.value.copy(transactionToDelete = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun addAccount(
        name: String,
        type: AccountCategoryType,
        institutionName: String,
        accountReferenceMasked: String,
        openingBalanceInCents: Long
    ) {
        viewModelScope.launch {
            try {
                val newAccount = AccountModel(
                    id = "acc_${Clock.System.now().toEpochMilliseconds()}",
                    ownerId = "local_user",
                    accountName = name,
                    accountType = type,
                    institutionName = institutionName,
                    accountReferenceMasked = accountReferenceMasked,
                    openingBalance = Money(openingBalanceInCents),
                    balanceSnapshot = Money(openingBalanceInCents)
                )
                addAccountUseCase(newAccount)
                _uiState.value = _uiState.value.copy(isAddAccountDialogOpen = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun addBudget(
        name: String,
        limitInCents: Long,
        categoryId: String?
    ) {
        viewModelScope.launch {
            try {
                val newBudget = BudgetModel(
                    id = "bud_${Clock.System.now().toEpochMilliseconds()}",
                    ownerId = "local_user",
                    name = name,
                    categoryId = categoryId,
                    limitAmount = Money(limitInCents),
                    periodType = BudgetPeriodType.MONTHLY
                )
                addBudgetUseCase(newBudget)
                _uiState.value = _uiState.value.copy(isAddBudgetDialogOpen = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
