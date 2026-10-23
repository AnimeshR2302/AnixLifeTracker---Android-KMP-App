# Plan 5 — Android SMS-Based Financial Transaction Reading

> **Project:** AnixLifeTracker  
> **Implementation sequence:** Plan 5 of 17  
> **Scope:** Authorized Android financial SMS collection, deterministic parsing, transaction validation, persistence, and review.

# TASK: Implement Automatic Financial Transaction Detection Using Android SMS

You are a senior Android and Kotlin Multiplatform engineer implementing Plan 5 of AnixLifeTracker.

The application already has KMP architecture, Android/Desktop/Web apps, shared UI, Room database, financial models and repositories, and authentication/synchronization architecture. Implement automatic financial transaction collection through Android SMS. This feature must be **Android-specific**, while processing contracts and business logic belong to shared KMP code where practical.

---

## 1. Read Existing Project Documentation

Before implementation, read:

- `docs/ARCHITECTURE.md`
- `docs/MODULE_STRUCTURE.md`
- `docs/DATABASE_SCHEMA.md`
- `docs/FINANCIAL_MODELS.md`
- `docs/SECURITY.md`
- `docs/AGENT_GUIDELINES.md`
- `docs/IMPLEMENTATION_STATUS.md`

Inspect transaction entities, provenance/source records, repositories, and synchronization. Reuse existing components. **Do not build a separate transaction database or duplicate domain models.**

## 2. Objective

Implement a system that can:

1. Detect incoming financial SMS.
2. Read historical financial SMS where permitted.
3. Identify relevant banking messages.
4. Extract monetary amounts.
5. Identify debit/credit direction.
6. Identify merchants/payees.
7. Extract bank/account references.
8. Extract transaction dates.
9. Extract transaction reference numbers.
10. Recognize refunds and reversals.
11. Identify balance information when available.
12. Persist validated financial transactions.
13. Prevent duplicate ingestion of the same SMS.
14. Request user review for ambiguous records.
15. Update finance UI through existing repository flows.

Prioritize common Indian banking SMS formats initially. Keep the parser extensible.

## 3. Android Permissions and Google Play Compliance

Investigate current Android permission behavior and Google Play restrictions. Relevant permissions may include:

- `READ_SMS` — permitted historical inbox access.
- `RECEIVE_SMS` — incoming SMS broadcast handling where allowed.

**SMS permissions are restricted by Google Play.** SMS-based money-management apps may qualify for a policy exception, but eligibility does not guarantee approval. Do not assume the production Play build can request SMS permission.

### Build Variant Strategy

Create an appropriate variant/flavor boundary:

**Standard / Play-safe variant**
- Does not declare restricted SMS permissions.
- Never attempts SMS access.
- Supports manual entry and other ingestion sources.

**Internal / SMS-enabled variant**
- Includes SMS integration for authorized testing or eligible distribution.
- Requests runtime permissions and explicit consent.
- Does not assume sideloading bypasses Android restricted-permission enforcement.

Keep shared parsing/ingestion independent of variants. Do not become the default SMS app merely to obtain permissions. Do not bypass OS/policy restrictions. Unavailable SMS access must fail gracefully without breaking the app.

## 4. User Permission Flow

Create SMS integration settings that explain:

- What SMS information is accessed.
- Why access is needed.
- What fields are extracted.
- Whether normalized data may sync.
- How monitoring is disabled.

Implement:

1. Feature introduction.
2. Explicit consent.
3. Runtime permission request.
4. Actual permission-state verification.
5. Historical import option.
6. Live incoming-message monitoring option.
7. Pause/resume.
8. Permission revocation handling.

Do not spam denied permission prompts. Distinguish **permission granted** from **feature enabled**; one must not automatically imply the other.

## 5. SMS Collection Architecture

```text
Android SMS Provider / Broadcast
               |
               v
         SMS Collector
               |
               v
      Financial SMS Filter
               |
               v
   Shared Financial Parser
               |
               v
 Normalized TransactionCandidate
               |
               v
 Validation / Source Deduplication
               |
               v
    Transaction Repository
               |
               v
         Room Database
               |
               v
       Flow -> UI State
```

Reuse or introduce interfaces as appropriate:

```kotlin
interface FinancialMessageParser {
    fun parse(message: FinancialMessage): TransactionParseResult
}

interface FinancialTransactionIngestor {
    suspend fun ingest(candidate: TransactionCandidate): IngestionResult
}
```

Domain/input models may include `FinancialMessage`, `TransactionCandidate`, `TransactionParseResult`, `ParseConfidence`, and `IngestionResult`. They must not import Android APIs. Android collectors convert raw SMS into these shared input types.

## 6. Historical SMS Reading

Where authorized, use `ContentResolver` with the Android SMS content provider.

Requirements:

- Query relevant SMS inbox fields only.
- Respect a bounded date range.
- Process in batches, not an entire inbox in memory.
- Perform work off the main thread.
- Support progress, cancellation, and errors.

### Import Options

- Last 30 days.
- Last 90 days.
- Last 6 months.
- Last 1 year.
- Custom range.

Do not default to unlimited historical scanning.

### Historical Import Pipeline

```text
User Starts Import
       |
       v
Verify Consent + Permission
       |
       v
Query SMS Provider
       |
       v
Filter Financial Messages
       |
       v
Parse and Validate
       |
       v
Check Source Duplicates
       |
       v
Save Confirmed Transactions
       |
       v
Queue Uncertain Transactions
       |
       v
Show Import Summary
```

Cancellation must not corrupt data. Transactions successfully committed before cancellation may remain, but partial/inconsistent entries must not.

## 7. Incoming SMS Detection

Use documented Android SMS broadcast handling, particularly `Telephony.Sms.Intents.SMS_RECEIVED_ACTION`, where permitted. Do **not** confuse it with `SMS_DELIVER_ACTION` (default-SMS-app flow).

- Extract message data with supported APIs.
- Combine multipart segments correctly.
- Verify consent and runtime permission.
- Keep broadcast callbacks brief.
- Queue/delegate parsing and database work appropriately.
- Respect background execution limits.
- Do not create an always-on foreground service for ordinary SMS ingestion.

## 8. Financial SMS Filtering

Most messages are not financial transactions. Use sender context and message semantics together; do not trust apparent bank sender IDs blindly.

Potential terms: debited, credited, withdrawn, deposited, spent, received, UPI, IMPS, NEFT, RTGS, card payment, refund, reversal, transaction successful.

Reject or safely classify:

- OTPs and login/security codes.
- Marketing and promotional offers.
- Loan advertisements.
- Balance inquiries/standalone balance updates.
- Failed/declined transactions.
- Payment requests that were not completed.
- Future scheduled reminders rather than actual payments.

**Keywords alone are insufficient** to confirm financial activity.

## 9. Financial Transaction Parsing

Use deterministic parsing first: regex, pattern templates, sender-specific profiles where appropriate, vocabulary normalization, and structured extraction results. Do not introduce a mandatory LLM dependency.

### Example A — Debit

Input:

> INR 1,250.00 debited from A/C XX1234 on 23-Oct-2026 at SWIGGY. UPI Ref 987654321.

Expected:

```text
Amount: 1250.00 INR
Direction: DEBIT
Transaction Type: EXPENSE
Account: XX1234
Merchant: SWIGGY
Date: 2026-10-23
Reference: 987654321
Source: SMS
```

### Example B — Credit

Input:

> Your account XX5678 has been credited with INR 45,000.00 via NEFT.

Expected:

```text
Amount: 45000.00 INR
Direction: CREDIT
Account: XX5678
Payment Method: NEFT
Source: SMS
```

Do not assume an arbitrary credit is salary/income.

### Example C — Refund

Input:

> Refund of INR 750 credited to account XX5678.

Expected:

```text
Amount: 750.00 INR
Direction: CREDIT
Transaction Type: REFUND
Source: SMS
```

### Example D — Failed Payment

Input:

> Your UPI payment of INR 500 failed.

Expected:

```text
Create Confirmed Transaction: NO
Reason: Failed Transaction
```

## 10. Parser Requirements

Support:

- INR and ₹ formats.
- Thousands separators and decimals.
- `CR`/`DR` terms.
- Different date formats.
- Missing dates/account references/merchant names.
- Multiple amounts in one message.
- Available-balance information.
- Reference numbers.
- Credit-card and UPI transactions.
- Refunds and reversals.

One SMS may contain both a transaction amount and an available balance: do **not** count the balance as another transaction. Preserve whether timestamp information was stated in the message or inferred from SMS receipt time. Never attach an unknown account to an arbitrary saved account.

## 11. Parsing Confidence and Review

Use explicit processing states:

- `HIGH_CONFIDENCE`
- `NEEDS_REVIEW`
- `UNRECOGNIZED`
- `REJECTED`

Confidence must be linked to parsing/validation evidence, not arbitrary scores. Save high-confidence confirmed transactions automatically only when the user has enabled automatic saving. Route ambiguous candidates to a review queue.

The review UI must let users:

- Inspect extracted fields.
- Correct amount.
- Choose account.
- Correct merchant/category.
- Confirm or reject a candidate.

Do not insert uncertain candidates as confirmed expenses.

## 12. Account Recognition

Use existing account records to match masked account reference, institution, card suffix, or payment-instrument info. Support multiple accounts at the same institution.

For unknown/ambiguous accounts:

1. Attempt only safe matches.
2. Keep mapping unresolved when necessary.
3. Request user confirmation.
4. Allow linking/creating an account.

Do not save full account identifiers unnecessarily.

## 13. Transaction Classification

Initial classifications:

| Message / event | Initial classification |
|---|---|
| Restaurant payment | Expense |
| Clear salary credit | Income |
| Merchant refund | Refund |
| ATM withdrawal | Cash withdrawal / transfer candidate |
| Credit-card bill payment | Transfer candidate |
| SIP deduction | Investment candidate |
| Internal bank transfer | Transfer candidate |

Not every debit is an expense; not every credit is income. Complex cross-account reconciliation belongs to Plan 8.

## 14. SMS Source Deduplication

Implement idempotent processing for repeated observations of the **same SMS event**. Use available source identity plus a stable fingerprint derived from sender, timestamp, normalized relevant contents, etc.

- Reuse Plan 3 transaction provenance records.
- Do not create duplicate records for re-read events.
- Do not equate transactions solely because amount and date match.
- Leave probabilistic cross-source matching for Plan 8.

## 15. Database Integration

Use existing repositories to store:

- Validated normalized transactions.
- Source provenance.
- Parsing status.
- Account mapping.
- Review status.

Use proper database migrations for required schema changes. Avoid storing complete raw SMS unless strictly necessary; if transient raw content is needed, justify and limit its lifetime. Do not synchronize raw SMS to the backend. Sync normalized financial information only when cloud sync is explicitly enabled. Preserve user corrections through later reprocessing.

## 16. UI Integration

Create a Finance Settings > SMS Integration experience.

### SMS Dashboard

Show feature status, actual permission status, total imported, last import, import action, live monitoring toggle, and review count.

### Historical Import

Show date selector, scanned count, financial messages found, imported count, duplicates skipped, needs-review count, errors, and progress/cancel action.

### Transaction Detail

Display **Detected from SMS** and normalized provenance without exposing unrelated sensitive content.

### Manual Correction

Allow editing parsed transactions and retain user correction provenance. Reprocessing must not silently overwrite manually corrected fields.

## 17. Privacy and Security

- Explicit consent and least-privilege access.
- Stop collection when disabled.
- Do not retain unrelated messages or OTPs.
- Never log raw SMS bodies.
- Never upload raw SMS.
- Minimize identifiers and retention.
- Apply parser resource/input-length limits.
- Avoid sensitive values in crash reports.
- Never attempt permission bypasses or access to other devices.
- Document feature-off, delete, and revocation behavior.

## 18. Testing Requirements

Create synthetic SMS fixtures for:

- Multiple Indian bank styles.
- UPI debit/credit.
- Card purchase.
- Salary credit.
- Refund and reversal.
- Failed transaction.
- Balance-only message.
- OTP/marketing.
- Ambiguous message.
- Multipart delivery.
- Repeated import and repeated broadcast.
- Unknown formats.
- Multiple money amounts in one message.

Test:

1. Permission granted, denied, revoked.
2. Historical import and cancellation.
3. Duplicate imports.
4. Incoming SMS detection.
5. Accurate persistence.
6. Account mapping and review flows.
7. UI Flow updates.
8. Offline operation.
9. Standard flavor **merged manifest** contains no restricted SMS permissions.
10. Android emulator/device behavior where available.

Do not claim device-level capture was verified solely through parser unit tests.

## 19. Acceptance Criteria

Plan 5 is complete when:

1. SMS tracking can be enabled or disabled.
2. Permission handling is correct.
3. Historical import works when authorized.
4. Incoming SMS processing works when authorized.
5. Financial messages are identified.
6. Money amounts and debit/credit direction are parsed accurately.
7. Failed/nonfinancial events create no confirmed transactions.
8. Repeat processing of a source event creates no duplicate.
9. Ambiguous candidates enter review.
10. Valid transactions persist in Room.
11. UI updates automatically.
12. Desktop and Web still compile and function.
13. Standard SMS-disabled variant remains usable.
14. Relevant automated tests pass.

If the environment lacks SMS access, complete parsing, mock tests, unavailable-feature behavior, and builds; mark real-device verification as pending.

## 20. Documentation and Final Output

Create:

```text
docs/
├── SMS_INTEGRATION.md
├── TRANSACTION_PARSING.md
├── SMS_PRIVACY.md
└── SMS_TEST_CASES.md
```

Update `docs/SECURITY.md` and `docs/IMPLEMENTATION_STATUS.md`.

Report:

1. Components implemented.
2. Permissions and variants.
3. Supported SMS formats.
4. Parsing rules.
5. Test results.
6. Database/UI changes.
7. Android runtime verification.
8. Remaining limitations.

**Do not begin Plan 6.**

**Completion condition:** AnixLifeTracker can extract and record transactions from user-authorized financial SMS where permitted, while preserving privacy and Android/Desktop/Web compatibility.
