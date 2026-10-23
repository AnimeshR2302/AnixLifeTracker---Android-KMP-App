# AnixLifeTracker - Transaction Parsing Rules

## 1. Deterministic Rule Engine

The parsing engine (`DeterministicFinancialMessageParser`) analyzes SMS text through sequential filters:

### 1. Rejection Filters
- **OTP & Security**: Rejects messages containing `OTP`, `one time password`, `verification code`, `secret code`, `do not share`.
- **Promotional & Loans**: Rejects `apply now`, `pre-approved`, `cashback offer`, `loan up to`.
- **Failed Transactions**: Rejects `failed`, `declined`, `unsuccessful`, `could not be processed`.
- **Pending / In-Progress Payments**: Rejects `payment pending`, `is being processed`, `payment is processing`.
- **Collect Requests**: Rejects `requested money`, `collect request`.

### 2. Field Extraction Rules
- **Monetary Amount**: Parses `INR`, `Rs.`, `Rs`, `₹` values with thousands separators (commas) and optional decimal paise.
- **Available Balance Isolation**: Detects `Avail Bal`, `Bal Rs`, `Available Limit` strings to isolate balance figures and prevent double-counting as transaction amounts.
- **Direction & Intent**:
  - `CREDIT` -> `INCOME` (or `REFUND` if "refund"/"reversal" keywords present).
  - `DEBIT` -> `EXPENSE` (or `INVESTMENT` if "SIP"/"mutual fund" keywords present).
- **Institution Matching**: Matches HDFC, SBI, ICICI, Axis, Kotak, PNB, BOB, Yes, IndusInd, Federal, Standard Chartered, or header shortcodes.
- **Account Masking**: Extracts 3-4 digit account suffixes (`A/C XX1234`, `card ending 5678`).
- **Merchant Extraction**: Extracts counterparty from `at <Merchant>`, `to <Payee>`, `vpa <address>`, `paid to <Name>`.
- **Transaction Reference**: Extracts `UPI Ref`, `Ref No`, or `UTR` identifiers.

---

## 2. Confidence Scoring Criteria

| Condition | Score Contribution |
| :--- | :--- |
| Parsed Valid Monetary Amount | +0.40 |
| Matched Masked Account Reference | +0.20 |
| Extracted Merchant / Payee | +0.20 |
| Extracted Transaction UTR / Ref | +0.10 |
| Recognized Refund / Reversal | +0.10 |

- **`HIGH_CONFIDENCE`** (Score >= 0.85): Automatically saved if auto-save setting enabled and account matched.
- **`NEEDS_REVIEW`** (Score 0.50 - 0.84 or unlinked account): Routed to SMS Review Queue.
- **`UNRECOGNIZED`** / **`REJECTED`** (Score < 0.50 or non-financial): Discarded without saving.
