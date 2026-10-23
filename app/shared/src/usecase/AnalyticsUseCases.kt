package com.anix.android.anixlifetracker.usecase

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.pipeline.LedgerEligibilityPolicy
import com.anix.android.anixlifetracker.repository.AccountRepository
import com.anix.android.anixlifetracker.repository.CategoryRepository
import com.anix.android.anixlifetracker.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class FinancialSummaryResult(
    val totalIncome: Money = Money.zero(),
    val grossExpenses: Money = Money.zero(),
    val totalRefunds: Money = Money.zero(),
    val netExpenses: Money = Money.zero(),
    val netCashFlow: Money = Money.zero(),
    val investmentOutflows: Money = Money.zero(),
    val currencyCode: String = "INR"
)

data class AccountBalanceSummary(
    val account: AccountModel,
    val isObserved: Boolean,
    val lastObservedTimestamp: String?
)

data class CategorySpendTotal(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Money,
    val percentageOfTotal: Double,
    val transactionCount: Int
)

data class SavingsRateResult(
    val savingsAmount: Money,
    val savingsRatePercentage: Double?,
    val isAvailable: Boolean,
    val explanation: String
)

class GetFinancialSummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(currencyCode: String = "INR"): Flow<FinancialSummaryResult> {
        return transactionRepository.observeTransactions().map { transactions ->
            var incomeCents = 0L
            var grossExpenseCents = 0L
            var refundCents = 0L
            var investmentCents = 0L

            transactions.filter { it.amount.currencyCode == currencyCode }.forEach { tx ->
                when {
                    LedgerEligibilityPolicy.isEligibleIncome(tx) -> incomeCents += tx.amount.amountInCents
                    LedgerEligibilityPolicy.isEligibleSpending(tx) -> grossExpenseCents += tx.amount.amountInCents
                    LedgerEligibilityPolicy.isRefundOrReversal(tx) -> refundCents += tx.amount.amountInCents
                    LedgerEligibilityPolicy.isInvestmentOutflow(tx) -> investmentCents += tx.amount.amountInCents
                }
            }

            val netExpenseCents = (grossExpenseCents - refundCents).coerceAtLeast(0L)
            val netCashFlowCents = incomeCents - netExpenseCents

            FinancialSummaryResult(
                totalIncome = Money(incomeCents, currencyCode),
                grossExpenses = Money(grossExpenseCents, currencyCode),
                totalRefunds = Money(refundCents, currencyCode),
                netExpenses = Money(netExpenseCents, currencyCode),
                netCashFlow = Money(netCashFlowCents, currencyCode),
                investmentOutflows = Money(investmentCents, currencyCode),
                currencyCode = currencyCode
            )
        }
    }
}

class GetAccountBalancesUseCase(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): Flow<List<AccountBalanceSummary>> {
        return accountRepository.observeAccounts().map { accounts ->
            accounts.map { acc ->
                AccountBalanceSummary(
                    account = acc,
                    isObserved = acc.institutionName.isNotBlank() && acc.balanceSnapshotTimestamp.isNotBlank(),
                    lastObservedTimestamp = acc.balanceSnapshotTimestamp.ifBlank { null }
                )
            }
        }
    }
}

class GetCategoryBreakdownUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(currencyCode: String = "INR"): Flow<List<CategorySpendTotal>> {
        return combine(
            transactionRepository.observeTransactions(),
            categoryRepository.observeCategories()
        ) { transactions, categories ->
            val catMap = categories.associateBy { it.id }
            val spendingTxns = transactions.filter {
                it.amount.currencyCode == currencyCode && LedgerEligibilityPolicy.isEligibleSpending(it)
            }

            val totalGrossSpend = spendingTxns.sumOf { it.amount.amountInCents }
            if (totalGrossSpend == 0L) return@combine emptyList()

            val grouped = spendingTxns.groupBy { it.categoryId ?: "uncategorized" }

            grouped.map { (catId, txs) ->
                val sumCents = txs.sumOf { it.amount.amountInCents }
                val catName = catMap[catId]?.name ?: if (catId == "uncategorized") "Uncategorized" else "Other"
                val pct = (sumCents.toDouble() / totalGrossSpend.toDouble()) * 100.0

                CategorySpendTotal(
                    categoryId = catId,
                    categoryName = catName,
                    totalAmount = Money(sumCents, currencyCode),
                    percentageOfTotal = pct,
                    transactionCount = txs.size
                )
            }.sortedByDescending { it.totalAmount.amountInCents }
        }
    }
}

class GetSavingsRateUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(currencyCode: String = "INR"): Flow<SavingsRateResult> {
        return transactionRepository.observeTransactions().map { transactions ->
            var incomeCents = 0L
            var grossExpenseCents = 0L
            var refundCents = 0L

            transactions.filter { it.amount.currencyCode == currencyCode }.forEach { tx ->
                when {
                    LedgerEligibilityPolicy.isEligibleIncome(tx) -> incomeCents += tx.amount.amountInCents
                    LedgerEligibilityPolicy.isEligibleSpending(tx) -> grossExpenseCents += tx.amount.amountInCents
                    LedgerEligibilityPolicy.isRefundOrReversal(tx) -> refundCents += tx.amount.amountInCents
                }
            }

            val netExpenseCents = (grossExpenseCents - refundCents).coerceAtLeast(0L)
            val savingsCents = incomeCents - netExpenseCents

            if (incomeCents <= 0L) {
                SavingsRateResult(
                    savingsAmount = Money(savingsCents, currencyCode),
                    savingsRatePercentage = null,
                    isAvailable = false,
                    explanation = "Savings rate unavailable: Zero or unknown income recorded for period."
                )
            } else {
                val rate = (savingsCents.toDouble() / incomeCents.toDouble()) * 100.0
                SavingsRateResult(
                    savingsAmount = Money(savingsCents, currencyCode),
                    savingsRatePercentage = rate,
                    isAvailable = true,
                    explanation = "Savings rate calculated against eligible income denominator."
                )
            }
        }
    }
}

class GetFilteredTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        query: String = "",
        categoryId: String? = null,
        accountId: String? = null,
        sourceType: TransactionSourceType? = null
    ): Flow<List<TransactionModel>> {
        return transactionRepository.observeTransactions().map { txs ->
            txs.filter { tx ->
                val matchesQuery = query.isBlank() ||
                        (tx.description?.contains(query, ignoreCase = true) == true) ||
                        (tx.merchantName?.contains(query, ignoreCase = true) == true) ||
                        (tx.amount.formattedString().contains(query))
                val matchesCategory = categoryId.isNullOrBlank() || tx.categoryId == categoryId
                val matchesAccount = accountId.isNullOrBlank() || tx.accountId == accountId
                val matchesSource = sourceType == null || tx.sourceType == sourceType

                matchesQuery && matchesCategory && matchesAccount && matchesSource
            }
        }
    }
}
