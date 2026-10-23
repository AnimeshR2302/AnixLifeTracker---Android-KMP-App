# Plan 7 — Finances Processing, Analytics and Planning

> Project: AnixLifeTracker — Android, Desktop JVM and Web; iOS later.
> Prerequisite: Plans 1–6 are already implemented. Work against the actual current code.
> Replaces: Original Plans 8, 9 and 10. Original Plan 7 (email and statement integration) is removed.
> Execution: Code changes only. No builds, tests or verifications will be run while implementing this plan.

## Ready-to-use implementation prompt

You are extending the existing AnixLifeTracker Kotlin Multiplatform application in Android Studio. Implement this entire plan through source-code and documentation changes. Preserve existing modules, UI conventions, repositories, authentication, persistence and Android collectors. Adapt the proposed names to existing contracts rather than creating duplicate infrastructure. Do not implement investment management or market intelligence.

### 1. Execution rules and existing context

Read relevant source files and existing documentation before editing: architecture, module structure, technology stack, database schema, financial models, data flow, authentication, synchronization, SMS integration, notification integration, parsing, security and implementation status. Source inspection needed to make edits is allowed. The existing implementation is authoritative; previous prompts are historical context.

Do not run Gradle tasks, builds, compilation, Gradle sync, tests, lint, static-analysis tasks, runtime launches, previews, manual verification, benchmarks or deployment commands. Do not add or expand unit/integration tests in this scope. Do not invoke commands that indirectly trigger those activities. Existing test files may remain untouched. Report implementation as code changes completed, with builds/tests/verifications explicitly not run. Do not claim correctness or runtime success from source changes alone.

Treat the updated sync policy below as overriding automatic upload/reconnect/background-sync instructions in Plan 4 and earlier documents. Keep financial notifications from third-party apps as an authorized INPUT source from Plan 6; do not confuse that collection with notifications OUTPUT by AnixLifeTracker. Do not remove either existing Android collector.

### 2. Revised scope and sequence

1. Plan 7: Shared transaction processing, deduplication, reconciliation, editing, dashboards, budgets, recurring expenses, forecasts and the revised sync behavior.
2. Plan 8: Reminder management, due-item generation and notification infrastructure with notification delivery disabled for now.
3. Plan 9: Encryption, permissions, privacy, error handling and performance hardening; no unit/integration testing or deployment preparation.

Email/Gmail access, PDF/CSV bank-statement import and associated onboarding are excluded. Investment holdings, SIP management, pricing, market/news APIs and investment intelligence will be planned later. Preserve already-existing models/placeholders without adding those features. A detected investment debit may still be classified as an investment cash outflow without implementing a portfolio.

### 3. Mandatory cross-platform sync contract

Use the existing authenticated backend as the shared exchange point. Desktop/Web cannot directly read Android storage or force a disconnected phone to upload. All financial calculations operate on each client's local available dataset and expose its freshness.

| Trigger | Android | Desktop/Web |
|---|---|---|
| New SMS/notification or local Android edit | Persist/process locally; enqueue normalized changes; do not upload | No automatic delivery or refresh |
| App opens, session restores or foreground resumes | Pull backend changes automatically; preserve pending Android changes; do not upload them | Use cached data; retrieve latest data through Sync |
| Sync button | Explicitly publish eligible local changes and retrieve backend changes | Publish queued local edits, then retrieve latest backend snapshot/deltas |
| Desktop/Web Save | Not applicable | Persist locally and immediately attempt an authenticated backend write; show Pending if offline/failure |
| Reconnect | Do not trigger Android upload | Retry pending desktop/web user saves while app is open; no automatic retrieval of Android data |
| Background/time interval/socket event | No financial upload, polling or live subscription | No polling, live subscription or background refresh |

An Android foreground pull may finish only after connectivity/authentication permits it. On failure keep local data usable, display stale/error state and allow Retry/Sync. If the app remains foreground and offline, allow a bounded retry of that pending foreground pull on reconnect; do not turn it into continuous polling or an upload. Coalesce repeated lifecycle events into one pull. Never promise instant updates without connectivity.

Desktop/Web edits reach Android only after those saves reach the backend. Android retrieves them at its next open/foreground resume or explicit Sync. Saving on Desktop/Web is not live propagation to an already-open Android session. An Android edit made locally remains unpublished until explicit Sync, even if the app restarts.

Provide Sync on all supported platforms, with no requirement to buy or integrate a separate messaging transport. Desktop/Web Sync retrieves the last Android-uploaded data; it cannot retrieve unsent phone records. Include clear copy such as “Last Android upload: …; newer phone records require Sync on Android.” A backend fetch timestamp must not masquerade as an Android data-freshness timestamp.

### 4. Adapt synchronization in code

Reuse the existing outbox, cursors, ownership and conflict handling. Split coordinator operations into pull-only and explicit bidirectional sync, with explicit reasons such as ANDROID_FOREGROUND, USER_SYNC and REMOTE_USER_SAVE. Avoid a generic synchronize() call from lifecycle handlers that accidentally uploads Android data.

Implement the following as shared behavior with platform-specific triggers:

1. Record mutations and durable outbox entries atomically with stable entity IDs, owner IDs, operation IDs and base server revisions.
2. Exclude raw SMS/notification text, OTPs and platform permission settings from synchronized payloads. Synchronize normalized canonical data and minimal non-sensitive provenance only.
3. Use bounded paginated deltas with an opaque server cursor/change sequence, including deletions. Never rely solely on device timestamps or offsets that can skip equal-time writes.
4. Apply each downloaded batch and its cursor transactionally. Resume after interruption without resetting the dataset or duplicating records.
5. Use server-side conditional writes against the base revision, owner-scoped idempotency keys and server-managed revisions. A plain client read-then-upsert is insufficient for conflict safety.
6. Clear outbox entries only for the acknowledged mutation revision/operation. If a record changes during an upload, retain the newer queued edit.
7. On explicit Sync, pull and reconcile before conditional pushes, then retrieve committed backend changes. Block only conflicting entities, not unrelated work.
8. On pull-only, apply clean remote records, merge proven non-overlapping field edits when base information permits, and preserve competing financial edits for review. Never erase local unsent changes.
9. Keep tombstones and prevent stale devices from resurrecting deleted records. A cursor older than retained history requires a resumable baseline download that preserves local pending edits.
10. Remote application must not create an echo outbox entry or retrigger uploads. Server-side writes must enforce account ownership and related-record ownership.
11. Partition queues, caches, cursors and conflicts per signed-in user. Do not upload anonymous/local-only records until the existing explicit account-linking flow permits it.
12. Cancel user-scoped work on logout/account switch; prevent old responses from mutating the new session's database.

Remove/disable financial-upload scheduling from WorkManager, lifecycle, reconnect callbacks, periodic jobs and realtime listeners left by Plan 4. Preserve unrelated collectors and required local processing jobs. Sync remains opt-in and local-only mode remains functional. Store mutation origin rather than inferring it from timestamps.

Expose separate local pending count, failed count, unresolved conflict count, last successful pull, last successful explicit exchange and last Android upload. Distinguish local Save from Saved to backend. Do not display fully Synced when conflicts, unsent local data or partial failures remain. Concurrent Sync clicks must share or serialize one operation.

### 5. Shared processing pipeline

Consolidate Plans 5–6 into one pipeline:

`Authorized source → normalized candidate → validation → source idempotency → cross-source matching → canonical ledger/review → analytics`

Keep source extraction in Android code; put business rules in commonMain. Reuse candidate, financial-message, provenance and review models. If existing collectors already write transactions directly, route them through the shared service without double insertion. Do not replay all historical data automatically.

Persist processing versions and statuses such as ignored, pending, needs review, confirmed, rejected and failed. Enforce required amount/currency/status fields; use exact integer minor units or the project's KMP-compatible decimal representation, never Double for ledger money. Treat unknown accounts, dates and balances as unknown, not invented defaults. Preserve source time and normalized transaction time separately, with explicit timezone rules.

### 6. Deduplication and reconciliation

Separate source-event idempotency from financial-transaction identity:

- Stable source fingerprints prevent replaying the same source event; notification status/content revisions remain eligible for processing.
- Match dependable payment references within the proper owner/account/currency context. A reference is not assumed globally unique.
- Multiple sources for one purchase attach to one canonical transaction. Preserve provenance and confidence.
- Amount/time/merchant similarity produces a possible-match review item, not an automatic merge. Two equal payments close together can be real transactions.
- A reference reused on the two legs of an own-account transfer must link those legs rather than collapse them into one account entry.
- Pending/failed/payment-request events do not become posted expenses. Pending-to-success updates yield one posted financial event.
- Manual edits survive reparsing and later source enrichment. Store user-overridden fields; never overwrite them silently.
- Rejected events remain suppressed on replay, unless the user explicitly requests reconsideration.

Implement link/unlink, merge/unmerge or an equivalent reversible correction model. Suppress duplicate canonical rows from aggregates without destroying original evidence. Reconcile provisional account mappings and balance observations through review. Do not fabricate a bank balance from partial alerts. An account with no known starting balance displays unknown balance rather than a misleading computed total.

### 7. Accounting classifications and shared ledger rules

Separate debit/credit direction from economic classification: expense, income, own-account transfer, refund/reversal, investment contribution/redemption, credit-card repayment, adjustment and unknown.

Use one shared eligibility policy for every screen and forecast:

- Confirmed purchases count as spending; failed/pending/rejected/duplicate events do not.
- Own-account transfers affect account cash flows but neither total income nor spending. Keep both account legs and avoid double-counting consolidated cash flow.
- Card purchases count once as expenses. Bank-to-card repayments settle the liability and are not new expenses.
- Investment contributions affect liquidity and are reported separately from consumption. Do not interpret a redemption as earned income automatically.
- Refunds/reversals link to the original transaction where known, including partial refunds. Display gross expenses, refunds and net expenses transparently; do not alter closed history silently. Make transaction-date attribution consistent and documented.
- Unknown incoming credits are not automatically salary; unknown debits are not automatically consumption.
- Cross-currency totals remain separate until an explicit rate/source/date policy exists. Do not sum INR and USD directly.

Allow manual account/transaction creation, edits, category changes, notes, review and deletion on all three platforms. Canonical user edits and deletion tombstones synchronize; raw device sources and local permission state do not. Preserve a minimal change history needed for conflicts and reversals.

### 8. Categorization and review UI

Implement deterministic ordered rules using normalized merchants, descriptions and account/payment types. Support editable category mappings, manual overrides, Uncategorized fallback, rule priority and confidence. Do not add an external AI dependency or send private messages to a model.

Provide one shared review queue for ambiguous duplicates, account mapping, transaction status, classifications, refunds, transfers and conflicts. Show safe normalized evidence, uncertainty, available actions and consequences. User decisions create durable canonical mutations. Handle conflicting deletion/edit actions without silently discarding either side.

### 9. Dashboard and analytics

Build shared repository queries/use cases and Compose UI for:

1. Income, gross/net expenses, refunds, net cash flow and separate investment outflows.
2. Account balances with observed/derived/unknown status, last balance observation and coverage limitations; do not infer card available credit as cash.
3. Category totals, merchant totals, daily/weekly/monthly trends and comparable prior periods.
4. Savings amount/rate with a documented eligible-income denominator and unavailable state for zero/unknown income; do not present investment transfers as consumption.
5. Transaction list with search, pagination, date/account/category/source/status filters and detail editing.
6. Unreviewed/uncategorized/conflicting counts and data freshness indicators.

Use a common period-boundary and user-timezone policy, including equal elapsed-day comparisons for partial months. Do not treat missing days as confirmed zero spending. Charts require text totals, currency labels and empty states. Editing a canonical transaction must invalidate relevant local aggregates through repository flows without requiring network sync.

### 10. Budgets, recurring obligations and financial planning

Implement editable per-category/overall budgets, period, currency, rollover choice, spent/remaining amounts and forecast overspend status. Define how refunds and period boundaries affect budgets.

Detect recurring patterns from confirmed history using normalized payee/category, amount tolerance, cadence and date windows. Require enough observations; expose confidence/evidence and propose a recurring obligation for confirmation. Do not auto-create duplicates or assume a monthly bill is paid merely because its date passed.

Support manually entered rent, bills, subscriptions, loan repayments and other obligations. Include next due date, cadence, variable/fixed amount, expected range, timezone, optional end date and active/paused state. Match actual payments conservatively; support skipped/paid/cancelled occurrences. Avoid double-counting an observed payment and its predicted occurrence. Add generic reminder-candidate interfaces for Plan 8 without scheduling notifications in this plan.

### 11. Forecasting and unusual spending

Use explainable shared deterministic/statistical calculations rather than an LLM:

- Month-end expense estimate: actual eligible spending plus unpaid known obligations plus estimated remaining variable spending. Remove known recurring portions from the variable baseline to avoid counting them twice.
- Cash-flow forecast: trustworthy starting balance + expected inflows − expected outflows, including transfers/investments where relevant to selected accounts. If opening balance is unknown, show projected net movement and explain why absolute balance is unavailable.
- Distinguish confirmed schedules, user-entered assumptions and inferred estimates. Include horizon, as-of time, history coverage, method version and confidence/low/base/high scenarios with explicit assumptions. Do not label heuristic ranges calibrated probabilities.
- Handle sparse history, partial months, outliers and interrupted collection. Prefer insufficient-data states over fabricated precision.
- Flag unusual category/merchant spending relative to a minimum-history baseline, with thresholds and cooldown/deduplication hooks for Plan 8. An anomaly is a review signal, not evidence of fraud.

Synchronize user budgets, obligations, confirmed recurrence rules, goals/assumptions and corrections. Recalculate derived aggregates/forecasts locally from the available dataset using common rules; avoid syncing stale chart totals as financial truth.

### 12. KMP implementation boundaries

Reuse actual modules and the established dependency-injection design. Suggested responsibilities:

- commonMain: processing rules, ledger policies, sync state machine, conflict contracts, analytics, recurring detection, forecasts, repository interfaces, shared ViewModels/UI and serializable DTOs.
- androidMain: existing collectors, secure storage adapters, foreground lifecycle pull trigger and explicit sync action wiring.
- desktopMain: existing JVM persistence/network/credential adapters and Save/Sync UI wiring.
- wasmJsMain or existing web target: actual browser persistence and networking adapters, Save/Sync wiring and browser lifecycle cancellation.

Do not import Android/JVM-only APIs into common or Wasm code. Reuse the web storage adapter already selected; do not assume Android Room/native encryption APIs exist in a browser. Keep money, dates and serialization portable. iOS gets extension points only; no new target is required now.

### 13. Source-change deliverables

Make versioned local/backend schema migration source changes for necessary canonical records, provenance links, processing states, outbox revisions, tombstones, recurrence/budget data and conflict records. Do not execute migrations against a live backend. Add repository mappings, shared use cases, UI state and platform adapters through the existing architecture.

Update docs/FINANCIAL_PROCESSING.md, docs/ANALYTICS.md, docs/FINANCIAL_FORECASTING.md, docs/SYNCHRONIZATION.md, docs/DATABASE_SCHEMA.md and docs/IMPLEMENTATION_STATUS.md. Correct obsolete roadmap references: full cross-source processing is now Plan 7, reminders Plan 8 and hardening Plan 9. Record the revised sync trigger matrix and disabled automatic upload jobs.

Finish with a concise list of source changes, migration/configuration files requiring later application, limitations and deferred work. Explicitly state: “No builds, tests or verifications were run, as requested.” Do not begin Plan 8 automatically.
