package com.anix.android.anixlifetracker.usecase

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.pipeline.LedgerEligibilityPolicy
import com.anix.android.anixlifetracker.repository.AccountRepository
import com.anix.android.anixlifetracker.repository.BudgetRepository
import com.anix.android.anixlifetracker.repository.RecurringPaymentRepository
import com.anix.android.anixlifetracker.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class BudgetStatusModel(
    val budget: BudgetModel,
    val spentAmount: Money,
    val remainingAmount: Money,
    val percentageUsed: Double,
    val isOverbudget: Boolean
)

data class ForecastScenarios(
    val lowScenario: Money,
    val baseScenario: Money,
    val highScenario: Money
)

data class MonthEndExpenseForecastResult(
    val actualSpendingSoFar: Money,
    val unpaidKnownObligations: Money,
    val estimatedRemainingVariableSpending: Money,
    val projectedMonthEndExpenses: Money,
    val confidenceLevel: String,
    val scenarios: ForecastScenarios,
    val methodologyNotes: String
)

data class CashFlowForecastResult(
    val startingBalance: Money,
    val isStartingBalanceKnown: Boolean,
    val expectedInflows: Money,
    val expectedOutflows: Money,
    val projectedEndBalance: Money,
    val projectedNetMovement: Money,
    val explanation: String
)

data class SpendingAnomaly(
    val id: String,
    val merchantName: String,
    val categoryName: String,
    val actualAmount: Money,
    val baselineAverageAmount: Money,
    val deviationRatio: Double,
    val detectedAtISO: String,
    val anomalyDescription: String
)

/**
 * Generic decoupled reminder candidate interface exposed for Plan 8 integration
 */
interface ReminderCandidate {
    val candidateId: String
    val title: String
    val dueDateISO: String
    val category: String
    val expectedAmount: Money?
}

class ManageBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    fun observeBudgetStatuses(currencyCode: String = "INR"): Flow<List<BudgetStatusModel>> {
        return combine(
            budgetRepository.observeBudgets(),
            transactionRepository.observeTransactions()
        ) { budgets, transactions ->
            val spendingTxns = transactions.filter {
                it.amount.currencyCode == currencyCode && LedgerEligibilityPolicy.isEligibleSpending(it)
            }

            budgets.map { budget ->
                val matchingTxns = spendingTxns.filter { tx ->
                    budget.categoryId == null || tx.categoryId == budget.categoryId
                }
                val spentCents = matchingTxns.sumOf { it.amount.amountInCents }
                val limitCents = budget.limitAmount.amountInCents
                val remainingCents = limitCents - spentCents
                val pct = if (limitCents > 0) (spentCents.toDouble() / limitCents.toDouble()) * 100.0 else 0.0

                BudgetStatusModel(
                    budget = budget,
                    spentAmount = Money(spentCents, currencyCode),
                    remainingAmount = Money(remainingCents, currencyCode),
                    percentageUsed = pct,
                    isOverbudget = remainingCents < 0
                )
            }
        }
    }
}

class GetMonthEndExpenseForecastUseCase(
    private val transactionRepository: TransactionRepository,
    private val recurringRepository: RecurringPaymentRepository
) {
    operator fun invoke(
        daysInMonth: Int = 30,
        currentDayOfMonth: Int = 10,
        currencyCode: String = "INR"
    ): Flow<MonthEndExpenseForecastResult> {
        return combine(
            transactionRepository.observeTransactions(),
            recurringRepository.observeRecurringPayments()
        ) { transactions, recurring ->
            val eligibleTxns = transactions.filter {
                it.amount.currencyCode == currencyCode && LedgerEligibilityPolicy.isEligibleSpending(it)
            }

            val actualSpentCents = eligibleTxns.sumOf { it.amount.amountInCents }

            val unpaidObligationsCents = recurring
                .filter { it.isActive && it.expectedAmount.currencyCode == currencyCode }
                .sumOf { it.expectedAmount.amountInCents }

            val elapsedDays = currentDayOfMonth.coerceAtLeast(1)
            val remainingDays = (daysInMonth - elapsedDays).coerceAtLeast(0)

            val dailyVariableRateCents = if (elapsedDays > 0) actualSpentCents / elapsedDays else 0L
            val estRemainingVariableCents = dailyVariableRateCents * remainingDays

            val projectedTotalCents = actualSpentCents + unpaidObligationsCents + estRemainingVariableCents

            val lowCents = (projectedTotalCents * 0.90).toLong()
            val baseCents = projectedTotalCents
            val highCents = (projectedTotalCents * 1.15).toLong()

            MonthEndExpenseForecastResult(
                actualSpendingSoFar = Money(actualSpentCents, currencyCode),
                unpaidKnownObligations = Money(unpaidObligationsCents, currencyCode),
                estimatedRemainingVariableSpending = Money(estRemainingVariableCents, currencyCode),
                projectedMonthEndExpenses = Money(projectedTotalCents, currencyCode),
                confidenceLevel = if (elapsedDays >= 7) "MODERATE_TO_HIGH" else "PRELIMINARY_LOW_DATA",
                scenarios = ForecastScenarios(
                    lowScenario = Money(lowCents, currencyCode),
                    baseScenario = Money(baseCents, currencyCode),
                    highScenario = Money(highCents, currencyCode)
                ),
                methodologyNotes = "Actual spent + unpaid recurring obligations + (daily variable baseline * remaining days). Known recurring excluded from variable baseline."
            )
        }
    }
}

class GetCashFlowForecastUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val recurringRepository: RecurringPaymentRepository
) {
    operator fun invoke(currencyCode: String = "INR"): Flow<CashFlowForecastResult> {
        return combine(
            accountRepository.observeAccounts(),
            transactionRepository.observeTransactions(),
            recurringRepository.observeRecurringPayments()
        ) { accounts, transactions, recurring ->
            val matchingAccounts = accounts.filter { it.currencyCode == currencyCode }
            var startingCents = 0L
            var isKnown = matchingAccounts.isNotEmpty()

            matchingAccounts.forEach { acc ->
                startingCents += acc.balanceSnapshot.amountInCents
            }

            var expectedInflowCents = 0L
            var expectedOutflowCents = 0L

            recurring.filter { it.isActive && it.expectedAmount.currencyCode == currencyCode }.forEach { rec ->
                expectedOutflowCents += rec.expectedAmount.amountInCents
            }

            transactions.filter { it.amount.currencyCode == currencyCode }.take(10).forEach { tx ->
                if (LedgerEligibilityPolicy.isEligibleIncome(tx)) {
                    expectedInflowCents += tx.amount.amountInCents
                }
            }

            val projectedNetCents = expectedInflowCents - expectedOutflowCents
            val projectedEndCents = startingCents + projectedNetCents

            CashFlowForecastResult(
                startingBalance = Money(startingCents, currencyCode),
                isStartingBalanceKnown = isKnown,
                expectedInflows = Money(expectedInflowCents, currencyCode),
                expectedOutflows = Money(expectedOutflowCents, currencyCode),
                projectedEndBalance = Money(projectedEndCents, currencyCode),
                projectedNetMovement = Money(projectedNetCents, currencyCode),
                explanation = if (isKnown)
                    "Cash flow forecast calculated from starting balance, expected inflows, and upcoming obligations."
                else "Starting account balance unknown. Displaying projected net cash movement."
            )
        }
    }
}

class GetSpendingAnomaliesUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(currencyCode: String = "INR"): Flow<List<SpendingAnomaly>> {
        return transactionRepository.observeTransactions().map { transactions ->
            val spendingTxns = transactions.filter {
                it.amount.currencyCode == currencyCode && LedgerEligibilityPolicy.isEligibleSpending(it)
            }

            if (spendingTxns.size < 5) return@map emptyList()

            val avgCents = spendingTxns.map { it.amount.amountInCents }.average()
            val anomalies = mutableListOf<SpendingAnomaly>()

            spendingTxns.forEach { tx ->
                val amount = tx.amount.amountInCents
                if (avgCents > 0 && amount > avgCents * 2.5) {
                    val ratio = amount.toDouble() / avgCents
                    anomalies.add(
                        SpendingAnomaly(
                            id = "anom_${tx.id}",
                            merchantName = tx.merchantName ?: tx.description ?: "Merchant",
                            categoryName = tx.categoryName ?: "General",
                            actualAmount = tx.amount,
                            baselineAverageAmount = Money(avgCents.toLong(), currencyCode),
                            deviationRatio = ratio,
                            detectedAtISO = tx.occurredAt,
                            anomalyDescription = "Spending amount is ${(ratio).toString().take(4)}x higher than average transaction baseline."
                        )
                    )
                }
            }
            anomalies.sortedByDescending { it.actualAmount.amountInCents }
        }
    }
}
