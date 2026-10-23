package com.anix.android.anixlifetracker.usecase

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.repository.*
import kotlinx.coroutines.flow.Flow

class AddAccountUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(account: AccountModel) {
        require(account.accountName.isNotBlank()) { "Account name cannot be blank." }
        require(account.currencyCode.isNotBlank()) { "Account currency code must be specified." }
        accountRepository.addAccount(account)
    }
}

class GetAccountsUseCase(private val accountRepository: AccountRepository) {
    operator fun invoke(): Flow<List<AccountModel>> {
        return accountRepository.observeAccounts()
    }
}

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(transaction: TransactionModel) {
        require(transaction.amount.amountInCents != 0L) { "Transaction amount cannot be zero." }
        require(transaction.accountId.isNotBlank()) { "Transaction must be assigned to an account." }
        
        val account = accountRepository.getAccount(transaction.accountId)
        requireNotNull(account) { "Referenced financial account does not exist." }

        transactionRepository.addTransaction(transaction)

        // Atomically update account balance snapshot
        val updatedBalance = if (transaction.direction == TransactionDirection.CREDIT) {
            account.balanceSnapshot + transaction.amount
        } else {
            account.balanceSnapshot - transaction.amount
        }
        accountRepository.updateAccount(account.copy(balanceSnapshot = updatedBalance))
    }
}

class UpdateTransactionUseCase(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transaction: TransactionModel) {
        require(transaction.id.isNotBlank()) { "Transaction ID is required for update." }
        require(transaction.amount.amountInCents != 0L) { "Transaction amount cannot be zero." }
        transactionRepository.updateTransaction(transaction)
    }
}

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(transactionId: String) {
        val existingTx = transactionRepository.getTransaction(transactionId)
        if (existingTx != null) {
            val account = accountRepository.getAccount(existingTx.accountId)
            if (account != null) {
                // Revert account balance adjustment
                val revertedBalance = if (existingTx.direction == TransactionDirection.CREDIT) {
                    account.balanceSnapshot - existingTx.amount
                } else {
                    account.balanceSnapshot + existingTx.amount
                }
                accountRepository.updateAccount(account.copy(balanceSnapshot = revertedBalance))
            }
        }
        transactionRepository.deleteTransaction(transactionId)
    }
}

class GetTransactionsUseCase(private val transactionRepository: TransactionRepository) {
    operator fun invoke(): Flow<List<TransactionModel>> {
        return transactionRepository.observeTransactions()
    }
}

class GetTransactionsByDateUseCase(private val transactionRepository: TransactionRepository) {
    operator fun invoke(startDate: String, endDate: String): Flow<List<TransactionModel>> {
        require(startDate <= endDate) { "Start date must be before or equal to end date." }
        return transactionRepository.observeTransactionsByDateRange(startDate, endDate)
    }
}

class GetTransactionsByCategoryUseCase(private val transactionRepository: TransactionRepository) {
    operator fun invoke(categoryId: String): Flow<List<TransactionModel>> {
        return transactionRepository.observeTransactionsByCategory(categoryId)
    }
}

class AddBudgetUseCase(private val budgetRepository: BudgetRepository) {
    suspend operator fun invoke(budget: BudgetModel) {
        require(budget.name.isNotBlank()) { "Budget name cannot be blank." }
        require(budget.limitAmount.amountInCents > 0) { "Budget limit must be greater than zero." }
        budgetRepository.addBudget(budget)
    }
}

class GetBudgetsUseCase(private val budgetRepository: BudgetRepository) {
    operator fun invoke(): Flow<List<BudgetModel>> {
        return budgetRepository.observeBudgets()
    }
}

class AddInvestmentUseCase(private val investmentRepository: InvestmentRepository) {
    suspend operator fun invoke(transaction: InvestmentTransactionModel) {
        require(transaction.quantityFixedPoint > 0) { "Investment unit quantity must be positive." }
        require(transaction.price.amountInCents > 0) { "Investment unit price must be positive." }
        investmentRepository.addTransaction(transaction)
    }
}

class GetInvestmentsUseCase(private val investmentRepository: InvestmentRepository) {
    operator fun invoke(): Flow<List<InvestmentHoldingDetailModel>> {
        return investmentRepository.observeHoldings()
    }
}

class AddSIPUseCase(private val sipRepository: SIPRepository) {
    suspend operator fun invoke(sip: SIPScheduleModel) {
        require(sip.contributionAmount.amountInCents > 0) { "SIP contribution amount must be positive." }
        sipRepository.addSIP(sip)
    }
}

class GetSIPsUseCase(private val sipRepository: SIPRepository) {
    operator fun invoke(): Flow<List<SIPScheduleModel>> {
        return sipRepository.observeSIPs()
    }
}
