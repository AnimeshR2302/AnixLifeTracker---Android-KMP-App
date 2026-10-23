package com.anix.android.anixlifetracker.model

import kotlinx.datetime.Instant

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER, INVESTMENT, REFUND, FEE, ADJUSTMENT
}

enum class AccountType {
    CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT_WALLET, CASH
}

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val accountNumberMasked: String,
    val balance: Money
)

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Money,
    val type: TransactionType,
    val date: String,
    val accountName: String
)

data class SpendingCategory(
    val name: String,
    val spent: Money,
    val budget: Money,
    val percentage: Int
)

data class InvestmentHolding(
    val id: String,
    val name: String,
    val category: String, // Mutual Fund, Equity, Fixed Deposit, Gold
    val investedAmount: Money,
    val currentAmount: Money,
    val returnPercentage: Double,
    val sipAmount: Money? = null,
    val sipNextDate: String? = null
)

enum class ReminderCategory {
    PAYMENT, SIP, PERSONAL, TRAVEL, EVENT
}

enum class ReminderPriority {
    LOW, MEDIUM, HIGH
}

data class ReminderItem(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val category: ReminderCategory,
    val priority: ReminderPriority,
    val amount: Money? = null,
    val isCompleted: Boolean = false
)

data class TripItem(
    val id: String,
    val destination: String,
    val dates: String,
    val status: String,
    val budget: Money,
    val checklistCount: Int,
    val completedChecklistCount: Int
)

data class EventItem(
    val id: String,
    val title: String,
    val date: String,
    val daysRemaining: Int,
    val location: String,
    val notes: String
)

data class MarketInsight(
    val title: String,
    val summary: String,
    val impact: String // Positive, Neutral, Caution
)

object DemoDataProvider {
    val isDemoData: Boolean = true

    val accounts = listOf(
        Account("acc_1", "HDFC Salary Account", AccountType.CHECKING, "•••• 4821", Money.fromMajor(245000)),
        Account("acc_2", "ICICI Savings Account", AccountType.SAVINGS, "•••• 9102", Money.fromMajor(580000)),
        Account("acc_3", "Axis Bank Credit Card", AccountType.CREDIT_CARD, "•••• 1154", Money.fromMajor(-32450)),
        Account("acc_4", "Zerodha Trading Wallet", AccountType.INVESTMENT_WALLET, "•••• ZE01", Money.fromMajor(12500))
    )

    val totalBalance: Money = Money.fromMajor(792550)
    val monthlyIncome: Money = Money.fromMajor(185000)
    val monthlyExpenses: Money = Money.fromMajor(68400)
    val monthlySavings: Money = Money.fromMajor(116600)
    val totalInvestmentValue: Money = Money.fromMajor(1420000)
    val totalInvestmentInvested: Money = Money.fromMajor(1150000)
    val totalInvestmentGain: Money = Money.fromMajor(270000)
    val investmentReturnPercentage: Double = 23.47

    val transactions = listOf(
        Transaction("tx_1", "Monthly Salary Credit", "Income", Money.fromMajor(185000), TransactionType.INCOME, "2026-02-01", "HDFC Salary Account"),
        Transaction("tx_2", "Grocery Market - Superstore", "Groceries", Money.fromMajor(-4250), TransactionType.EXPENSE, "2026-02-03", "Axis Bank Credit Card"),
        Transaction("tx_3", "Nippon India Small Cap SIP", "Investment", Money.fromMajor(-15000), TransactionType.TRANSFER, "2026-02-05", "HDFC Salary Account"),
        Transaction("tx_4", "Electricity & Utilities Bill", "Utilities", Money.fromMajor(-2800), TransactionType.EXPENSE, "2026-02-06", "HDFC Salary Account"),
        Transaction("tx_5", "Dining Out & Restaurant", "Food & Dining", Money.fromMajor(-1850), TransactionType.EXPENSE, "2026-02-07", "Axis Bank Credit Card"),
        Transaction("tx_6", "Fuel & Petrol Station", "Transportation", Money.fromMajor(-3100), TransactionType.EXPENSE, "2026-02-08", "Axis Bank Credit Card")
    )

    val spendingCategories = listOf(
        SpendingCategory("Groceries & Supplies", Money.fromMajor(18200), Money.fromMajor(25000), 72),
        SpendingCategory("Utilities & Bills", Money.fromMajor(12500), Money.fromMajor(15000), 83),
        SpendingCategory("Food & Dining", Money.fromMajor(11800), Money.fromMajor(15000), 78),
        SpendingCategory("Transportation", Money.fromMajor(8400), Money.fromMajor(12000), 70),
        SpendingCategory("Entertainment & Shopping", Money.fromMajor(17500), Money.fromMajor(20000), 87)
    )

    val investmentHoldings = listOf(
        InvestmentHolding("inv_1", "Parag Parikh Flexi Cap Fund", "Mutual Fund", Money.fromMajor(450000), Money.fromMajor(585000), 30.0, Money.fromMajor(20000), "2026-02-15"),
        InvestmentHolding("inv_2", "Nippon India Small Cap Fund", "Mutual Fund", Money.fromMajor(300000), Money.fromMajor(410000), 36.67, Money.fromMajor(15000), "2026-02-15"),
        InvestmentHolding("inv_3", "HDFC Index S&P BSE Sensex", "Index Fund", Money.fromMajor(250000), Money.fromMajor(285000), 14.0, Money.fromMajor(10000), "2026-02-20"),
        InvestmentHolding("inv_4", "Sovereign Gold Bond 2023", "Gold", Money.fromMajor(150000), Money.fromMajor(140000), -6.67, null, null)
    )

    val marketInsights = listOf(
        MarketInsight("Equity Markets All-Time High", "Broad index gains led by banking and IT sectors.", "Positive"),
        MarketInsight("RBI Interest Rate Stance Neutral", "Repo rates maintained; inflation trajectory stable.", "Neutral"),
        MarketInsight("SIP Inflow Record High", "Retail mutual fund investments cross ₹20,000 Cr mark.", "Positive")
    )

    val reminders = listOf(
        ReminderItem("rem_1", "Axis Credit Card Payment Due", "Pay total bill amount to avoid interest charges.", "2026-02-12", ReminderCategory.PAYMENT, ReminderPriority.HIGH, Money.fromMajor(32450)),
        ReminderItem("rem_2", "Mutual Fund SIP Deductions", "Automated SIP debit for Parag Parikh & Nippon Funds.", "2026-02-15", ReminderCategory.SIP, ReminderPriority.HIGH, Money.fromMajor(35000)),
        ReminderItem("rem_3", "Car Health Checkup & Oil Change", "Schedule service at authorized center.", "2026-02-18", ReminderCategory.PERSONAL, ReminderPriority.MEDIUM),
        ReminderItem("rem_4", "Flight Check-In for Bengaluru Trip", "Web check-in opens 48 hours prior to departure.", "2026-02-22", ReminderCategory.TRAVEL, ReminderPriority.MEDIUM),
        ReminderItem("rem_5", "Aniversary Celebration RSVP", "Confirm venue and gift arrangements.", "2026-02-28", ReminderCategory.EVENT, ReminderPriority.LOW)
    )

    val trips = listOf(
        TripItem("trip_1", "Bengaluru Tech Summit & Meetup", "Feb 23 - Feb 26, 2026", "Confirmed", Money.fromMajor(25000), 5, 3),
        TripItem("trip_2", "Goa Weekend Family Retreat", "Mar 15 - Mar 18, 2026", "Planning", Money.fromMajor(60000), 8, 2)
    )

    val events = listOf(
        EventItem("evt_1", "Annual Performance Review", "2026-02-25", 15, "Office HQ / Conference Room B", "Prepare accomplishments deck and goal roadmap."),
        EventItem("evt_2", "Family Life Insurance Renewal", "2026-03-10", 28, "Online Portal", "Verify premium receipt and tax exemption certificate.")
    )
}
