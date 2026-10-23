# AnixLifeTracker - Financial Forecasting & Unusual Spending Detection

## 1. Overview

AnixLifeTracker uses explainable deterministic and statistical models in `commonMain` rather than external LLM APIs for month-end expense estimation, cash flow forecasting, and anomaly detection.

---

## 2. Month-End Expense Forecast Model

```text
Projected Month-End Expenses = Actual Eligible Spending So Far 
                             + Unpaid Known Obligations (Recurring Bills) 
                             + Estimated Remaining Variable Spending
```

- **Variable Baseline**: Excludes known recurring payments from historical daily average to prevent double-counting.
- **Scenario Horizon**:
  - **Low Scenario**: 90% of projected total
  - **Base Scenario**: 100% of projected total
  - **High Scenario**: 115% of projected total
- **Confidence Level**: `PRELIMINARY_LOW_DATA` (<7 elapsed days in month) or `MODERATE_TO_HIGH` (>=7 days).

---

## 3. Cash Flow Forecast Model

```text
Projected End Balance = Trustworthy Starting Balance + Expected Inflows - Expected Outflows
```

- If starting balance is unknown, the model displays projected net cash movement with explicit explanation rather than inventing an absolute ending balance.

---

## 4. Spending Anomaly Detection

- Evaluates transactions relative to a minimum history baseline (e.g. 2.5x higher than average merchant/category transaction amount).
- Generates a `SpendingAnomaly` review signal in `FinancesScreen` and `DashboardScreen`. Anomaly flags serve as user review signals, not automatic fraud assertions.
