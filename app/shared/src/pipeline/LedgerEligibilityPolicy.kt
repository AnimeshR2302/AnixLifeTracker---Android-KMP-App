package com.anix.android.anixlifetracker.pipeline

import com.anix.android.anixlifetracker.model.TransactionDirection
import com.anix.android.anixlifetracker.model.TransactionModel
import com.anix.android.anixlifetracker.model.TransactionState
import com.anix.android.anixlifetracker.model.TransactionType

object LedgerEligibilityPolicy {

    /**
     * Confirmed purchases and expenses count as spending.
     * Failed, pending, rejected, duplicate, own-account transfer, credit-card repayment,
     * and investment outflow events DO NOT count as consumption spending.
     */
    fun isEligibleSpending(transaction: TransactionModel): Boolean {
        if (transaction.deletedAt != null) return false
        if (transaction.status != TransactionState.POSTED) return false
        if (transaction.transferGroupId != null) return false
        if (transaction.direction != TransactionDirection.DEBIT) return false

        return when (transaction.transactionType) {
            TransactionType.EXPENSE, TransactionType.FEE, TransactionType.ADJUSTMENT -> true
            TransactionType.INCOME, TransactionType.TRANSFER, TransactionType.INVESTMENT, TransactionType.REFUND -> false
        }
    }

    /**
     * Confirmed earned income counts towards total income.
     * Transfers, refunds, investment redemptions, and credit card repayments DO NOT count as income.
     */
    fun isEligibleIncome(transaction: TransactionModel): Boolean {
        if (transaction.deletedAt != null) return false
        if (transaction.status != TransactionState.POSTED) return false
        if (transaction.transferGroupId != null) return false
        if (transaction.direction != TransactionDirection.CREDIT) return false

        return when (transaction.transactionType) {
            TransactionType.INCOME -> true
            TransactionType.EXPENSE, TransactionType.TRANSFER, TransactionType.INVESTMENT, TransactionType.REFUND, TransactionType.FEE, TransactionType.ADJUSTMENT -> false
        }
    }

    /**
     * Investment contributions affect liquidity and are reported separately from consumption spending.
     */
    fun isInvestmentOutflow(transaction: TransactionModel): Boolean {
        if (transaction.deletedAt != null) return false
        if (transaction.status != TransactionState.POSTED) return false
        return transaction.transactionType == TransactionType.INVESTMENT && transaction.direction == TransactionDirection.DEBIT
    }

    /**
     * Own-account transfers affect individual account cash flows but neither total income nor spending.
     */
    fun isOwnAccountTransfer(transaction: TransactionModel): Boolean {
        if (transaction.deletedAt != null) return false
        return transaction.transferGroupId != null || transaction.transactionType == TransactionType.TRANSFER
    }

    /**
     * Refunds and reversals adjust gross spending transparently.
     */
    fun isRefundOrReversal(transaction: TransactionModel): Boolean {
        if (transaction.deletedAt != null) return false
        return transaction.transactionType == TransactionType.REFUND || transaction.status == TransactionState.REVERSED
    }
}
