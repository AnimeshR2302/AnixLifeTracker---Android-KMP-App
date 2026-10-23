# AnixLifeTracker - Financial Data Modeling Principles

## 1. Monetary Precision Rules

To ensure exact accounting accuracy across multiplatform targets, binary floating-point types (`Float` and `Double`) are **strictly forbidden** for monetary representations and balance calculations.

- **Storage**: Minor units stored as `Long` integers (e.g., ₹125.50 = 12550 paise; $10.99 = 1099 cents).
- **Domain Value Object**: `Money(amountInCents: Long, currencyCode: String = "INR")`.
- **Investment Quantities**: Fixed-point representations scaled by 1,000,000 (6 decimal place unit precision) or string decimal rules.

---

## 2. Transaction Classification Logic

Transactions are defined by both physical money movement (`direction`) and financial intent (`transactionType`):

| Action | Direction | Transaction Type | Category Impact |
| :--- | :--- | :--- | :--- |
| **Salary Credit** | `CREDIT` | `INCOME` | Income Ledger (+) |
| **Grocery Purchase** | `DEBIT` | `EXPENSE` | Expense Ledger (-) |
| **Bank Account Transfer** | `DEBIT` / `CREDIT` | `TRANSFER` | Neutral Cash Flow (0) |
| **Mutual Fund SIP** | `DEBIT` | `INVESTMENT` | Asset Allocation (+) |
| **Merchant Refund** | `CREDIT` | `REFUND` | Expense Reversal (+) |
| **Bank Fee / Charges** | `DEBIT` | `FEE` | Expense Ledger (-) |

---

## 3. Provenance & Ingestion Deduplication

Repeated ingestion of identical events (e.g. Bank SMS + UPI Notification) is prevented using deterministic `sourceFingerprint` hashes calculated from:
`sha256(timestampWindow + normalizedAmount + accountLast4 + counterparty)`

Source provenance is stored separately in `transaction_source_records` table without deleting or overwriting financial records.
