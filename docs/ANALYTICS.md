# AnixLifeTracker - Financial Analytics & Reporting Specification

## 1. Overview

AnixLifeTracker provides shared repository queries and UseCases in `commonMain` to compute financial position metrics, category breakdowns, savings rates, and filterable transaction lists.

---

## 2. Core Metrics & Definitions

| Metric | Formula / Accounting Rule | Description |
| :--- | :--- | :--- |
| **Total Income** | `SUM(eligible_credit_income)` | Confirmed earned income events in active period |
| **Gross Expenses** | `SUM(eligible_debit_spending)` | Confirmed purchase/spending events in active period |
| **Total Refunds** | `SUM(eligible_refund_credits)` | Confirmed refund and reversal credits |
| **Net Expenses** | `Gross Expenses - Total Refunds` | Actual net consumption spending |
| **Net Cash Flow** | `Total Income - Net Expenses` | Net cash surplus or deficit |
| **Investment Outflows** | `SUM(investment_contributions)` | Capital transferred into investment instruments |
| **Savings Rate (%)** | `(Net Cash Flow / Total Income) * 100` | Percentage of income saved. Displays `N/A` if income is zero or unknown |

---

## 3. Account Balances Position

- **Observed Balance**: Directly extracted from bank/card alert notifications or statement snapshots (`isObservedBalance = true`).
- **Derived Balance**: Calculated from initial opening balance +/- net posted ledger transactions.
- **Coverage Limitations**: Card available limit is explicitly distinguished from liquid cash. Unknown starting balances display `Unknown Balance` instead of fictitious computed totals.

---

## 4. Category Breakdown & Prior Period Comparison

- Category spend totals sum net eligible expenses per category.
- Percentage of total spend calculated relative to net total expenses.
- Prior period comparisons use equal elapsed-day windows to prevent partial-month skew.
