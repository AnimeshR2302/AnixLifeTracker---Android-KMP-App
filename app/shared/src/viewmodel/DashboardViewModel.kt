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

data class DashboardUiState(
    val totalBalance: Money = DemoDataProvider.totalBalance,
    val monthlyIncome: Money = DemoDataProvider.monthlyIncome,
    val monthlyExpenses: Money = DemoDataProvider.monthlyExpenses,
    val netExpenses: Money = Money.zero(),
    val refunds: Money = Money.zero(),
    val investmentOutflows: Money = Money.zero(),
    val monthlySavings: Money = DemoDataProvider.monthlySavings,
    val savingsRatePercentage: Double? = null,
    val expenseForecast: MonthEndExpenseForecastResult? = null,
    val spendingAnomalies: List<SpendingAnomaly> = emptyList(),
    val recentTransactions: List<Transaction> = DemoDataProvider.transactions.take(4),
    val spendingCategories: List<SpendingCategory> = DemoDataProvider.spendingCategories,
    val portfolioValue: Money = Money(12500000),
    val portfolioReturnPercentage: Double = 12.4,
    val upcomingReminders: List<ReminderItem> = DemoDataProvider.reminders.take(3),
    val marketInsights: List<MarketInsight> = DemoDataProvider.marketInsights,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val accountRepository: AccountRepository = AccountRepositoryImpl(DatabaseManager.getDatabase()),
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(DatabaseManager.getDatabase()),
    private val recurringRepository: RecurringPaymentRepository = RecurringPaymentRepositoryImpl(DatabaseManager.getDatabase())
) : ViewModel() {

    private val summaryUseCase = GetFinancialSummaryUseCase(transactionRepository)
    private val savingsRateUseCase = GetSavingsRateUseCase(transactionRepository)
    private val forecastUseCase = GetMonthEndExpenseForecastUseCase(transactionRepository, recurringRepository)
    private val anomaliesUseCase = GetSpendingAnomaliesUseCase(transactionRepository)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDatabaseData()
    }

    private fun observeDatabaseData() {
        viewModelScope.launch {
            launch {
                accountRepository.observeAccounts()
                    .catch { }
                    .collect { dbAccounts ->
                        if (dbAccounts.isNotEmpty()) {
                            var totalCents = 0L
                            dbAccounts.forEach { totalCents += it.balanceSnapshot.amountInCents }
                            _uiState.value = _uiState.value.copy(totalBalance = Money(totalCents))
                        }
                    }
            }

            launch {
                transactionRepository.observeTransactions()
                    .catch { }
                    .collect { dbTransactions ->
                        if (dbTransactions.isNotEmpty()) {
                            val converted = dbTransactions.take(5).map { tx ->
                                Transaction(
                                    id = tx.id,
                                    title = tx.description ?: tx.transactionType.name,
                                    category = tx.categoryName ?: "General",
                                    amount = tx.amount,
                                    type = tx.transactionType,
                                    date = tx.localTransactionDate,
                                    accountName = tx.accountId
                                )
                            }
                            _uiState.value = _uiState.value.copy(recentTransactions = converted)
                        }
                    }
            }

            launch {
                summaryUseCase().catch { }.collect { summary ->
                    _uiState.value = _uiState.value.copy(
                        monthlyIncome = summary.totalIncome,
                        monthlyExpenses = summary.grossExpenses,
                        netExpenses = summary.netExpenses,
                        refunds = summary.totalRefunds,
                        investmentOutflows = summary.investmentOutflows,
                        monthlySavings = summary.netCashFlow
                    )
                }
            }

            launch {
                savingsRateUseCase().catch { }.collect { savings ->
                    _uiState.value = _uiState.value.copy(
                        savingsRatePercentage = savings.savingsRatePercentage
                    )
                }
            }

            launch {
                forecastUseCase().catch { }.collect { forecast ->
                    _uiState.value = _uiState.value.copy(expenseForecast = forecast)
                }
            }

            launch {
                anomaliesUseCase().catch { }.collect { anomalies ->
                    _uiState.value = _uiState.value.copy(spendingAnomalies = anomalies)
                }
            }
        }
    }

    fun refreshData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
