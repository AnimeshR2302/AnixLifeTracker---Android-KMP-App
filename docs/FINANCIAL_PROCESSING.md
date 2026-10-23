# AnixLifeTracker - Shared Financial Processing Pipeline & Rules

## 1. Overview

AnixLifeTracker consolidates transaction candidates from all authorized input sources (SMS, Notification, Manual, Statement) into a unified shared processing pipeline in `commonMain`.

```text
Authorized Input Source (SMS / Notification / Manual)
                     |
                     v
        Normalized Transaction Candidate
                     |
                     v
          Validation & Idempotency Check
                     |
                     v
  Cross-Source Reference Matching & Deduplication
                     |
                     v
         Deterministic Rule Categorization
                     |
                     v
  Auto-Post to Ledger OR Queue for Unified Review
```

---

## 2. Shared Ingestion Pipeline Steps

1. **Source Extraction**: Ingest candidates from platform collectors (SMS receiver, NotificationListener, Manual entry).
2. **Idempotency Check**: Compute source fingerprint (`NOTIF:package:timestamp:amount:direction` or `SMS:sender:timestamp:amount:direction`). Check `transaction_source_records` table to prevent re-processing duplicate events.
3. **Cross-Source Reference Matching**: Match payment references (UTR / UPI Ref / Card Ref). If a transaction with matching reference exists, append a new `TransactionSourceRecordEntity` (`processingStatus = CONFIRMED_LINKED`) without creating a duplicate canonical transaction.
4. **Account Association**: Match masked account reference (`•••• 4821`) or institution name against active user accounts.
5. **Categorization Engine**: Run deterministic ordered rules from `categorization_rules` table. Fallback to keyword heuristics (dining, groceries, shopping, rent, utilities, transport, healthcare, subscriptions).
6. **Posting or Unified Review Queue**:
   - High-confidence parsed candidates + matched account + autoSave enabled -> Auto-posted to `transactions` table.
   - Uncertain or missing account -> Saved to `unified_review_items` / review queue for user confirmation.

---

## 3. Accounting & Ledger Eligibility Policies

- **Spending / Consumption**: Only confirmed purchases count towards spending (`POSTED`, `DEBIT`, `EXPENSE`/`FEE`/`ADJUSTMENT`). Pending/failed/rejected/duplicate/transfer events DO NOT count as spending.
- **Credit Card Repayments**: Settles liability; not counted as new consumption expenses.
- **Own-Account Transfers**: Linked via `transferGroupId`. Affects individual account cash flow but does NOT count towards total income or spending.
- **Investment Outflows**: Contribution to investment accounts/mutual funds tracked separately from consumption spending.
- **Refunds & Reversals**: Linked to original transaction where known. Displays gross expenses, refunds, and net expenses transparently.
- **User Overrides**: Manual user edits set `isUserOverridden = true`. User-overridden fields are stored and preserved against subsequent re-parsing or enrichment.
