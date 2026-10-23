# Plan 6 — Android Notification-Based Financial Transaction Collection

> **Project:** AnixLifeTracker  
> **Implementation sequence:** Plan 6 of 17  
> **Scope:** Authorized Android financial-notification monitoring, transaction extraction, source deduplication, persistence, and review.

# TASK: Implement Automatic Financial Transaction Collection Using Android Notification Listener

You are a senior Android and Kotlin Multiplatform engineer implementing Plan 6 of AnixLifeTracker.

Previous plans established KMP architecture, shared financial models, Room database, transaction repositories, backend synchronization, SMS transaction extraction, and a shared financial parser. Implement an Android Notification Listener to detect transactions from banking and payment applications.

**Reuse Plan 5 ingestion infrastructure. Do not build a parallel transaction processing system.**

---

## 1. Read Existing Project Documentation

Inspect:

- `docs/ARCHITECTURE.md`
- `docs/MODULE_STRUCTURE.md`
- `docs/DATABASE_SCHEMA.md`
- `docs/FINANCIAL_MODELS.md`
- `docs/TRANSACTION_PARSING.md`
- `docs/SMS_INTEGRATION.md`
- `docs/SECURITY.md`
- `docs/AGENT_GUIDELINES.md`
- `docs/IMPLEMENTATION_STATUS.md`

Study existing repositories, parsers, `TransactionCandidate`, provenance records, review queue, permissions, and synchronization. Extend them instead of rewriting.

## 2. Objective

Support, where available and authorized:

1. Banking app notifications.
2. UPI payment notifications.
3. Wallet notifications.
4. Credit-card purchase alerts.
5. Refund notifications.
6. Failed/pending payment events (without creating confirmed spending).
7. Investment payment confirmations.
8. Incoming payment notifications.

Detect relevant notifications, extract amount/direction/merchant/source/reference/date, normalize candidates, filter false positives, deduplicate events, persist valid transactions, send uncertain ones for review, and refresh the financial UI.

Do not assume all financial apps include complete, accurate transaction details.

## 3. Android Notification Listener

Use the documented Android `NotificationListenerService` API and its callback:

```kotlin
override fun onNotificationPosted(sbn: StatusBarNotification)
```

Implement:

- Service declaration with correct binding permission.
- Notification listener service intent filter.
- Connection/disconnection lifecycle handling.
- Notification posted/update processing.
- Access status checks.
- Safe handling of malformed and empty notifications.

### Manifest

Declare the listener service with `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`, using the relevant notification listener service action.

**Important:** This is a special user-granted notification-access setting, not an ordinary runtime permission. `POST_NOTIFICATIONS` is not required merely to read notifications through an authorized listener.

Do not use AccessibilityService, screen scraping, hidden APIs, or notification-content restriction bypasses.

## 4. User Authorization

Create an onboarding flow:

1. Explain the scope of notification access and which data is processed.
2. Explain that the user selects allowed financial apps.
3. Present **Enable Financial Notification Tracking**.
4. Open the appropriate Android Notification Access Settings page.
5. Upon return, inspect actual access status and service connection state.
6. Let the user explicitly enable monitoring for selected apps.

Access granted must not imply monitoring is enabled. Notification tracking requires **separate in-app consent** and an app allowlist. Default to monitoring no apps until selected. Let users disable tracking globally or per app.

## 5. Supported Application Management

Prepare configurable profiles for services such as Google Pay, PhonePe, Paytm, supported banking apps, and credit-card apps.

- Do not invent package names.
- Verify package identities from Android package data/documentation.
- Treat format compatibility as app/version-specific.
- Provide an allowlist and parser profile mapping.

Suggested configuration fields:

```text
id
packageName
displayName
isInstalled
isEnabled
parserProfileId
lastProcessedAt
```

Users must be able to view installed supported apps, enable/disable monitoring, and see status if an app is absent.

## 6. Notification Processing Architecture

```text
Android Notification Manager
             |
             v
NotificationListenerService
             |
             v
     App Allowlist Filter
             |
             v
 Notification Content Extractor
             |
             v
  Shared Financial Message Parser
             |
             v
 Normalized Transaction Candidate
             |
             v
 Validation + Source Deduplication
             |
             v
 Financial Transaction Repository
             |
             v
        Room Database
             |
             v
     Flow -> ViewModel -> UI
```

Use shared Plan 5 contracts, with compatible extensions where necessary. The source type must distinguish SMS from NOTIFICATION. Keep transaction business logic in shared code; keep Android OS APIs in `androidMain`.

## 7. Notification Content Extraction

Extract supported public fields only:

- Source package name.
- Notification title.
- Notification text.
- Expanded/big text where available.
- Supported multi-line text.
- Notification posted time.
- Notification key/ID.
- Relevant documented metadata.

Example platform-neutral model:

```kotlin
data class FinancialNotification(
    val sourcePackage: String,
    val title: String?,
    val content: String?,
    val postedAt: Long,
    val notificationIdentifier: String?
)
```

Adapt to existing models. Handle grouped, empty, private, hidden, or redacted content without fabricating missing details. Convert relevant content to `FinancialMessage` for the shared parser. Drop unrelated notification content immediately.

## 8. Notification Filtering

### Potential transaction events

- Payment successful.
- Amount debited/credited.
- Money received.
- Card charged.
- Refund completed.
- Transfer completed.

### Do not automatically create confirmed transactions for

- Payment pending/processing.
- Payment requested.
- Failed/declined transactions.
- Balance inquiry.
- Promotions/cashback advertisements.
- Loan offers.
- OTP/login/security alerts.

Example: **Your payment of ₹500 is being processed** must not be counted as a completed expense. Require enough evidence of transaction success; amount text alone is insufficient.

## 9. Transaction Parsing

Reuse/extend Plan 5's financial parser.

### Example A — UPI Payment

```text
Application: UPI Payment App
Payment Successful
₹350 paid to Cafe Coffee Day
```

Expected:

```text
Amount: 350.00 INR
Direction: DEBIT
Transaction Type: EXPENSE
Merchant: Cafe Coffee Day
Source: NOTIFICATION
```

### Example B — Money Received

```text
Application: UPI Payment App
Money Received
You received ₹2,500 from Rahul
```

Expected:

```text
Amount: 2500.00 INR
Direction: CREDIT
Payer: Rahul
Source: NOTIFICATION
```

Do not automatically classify incoming payment as salary/taxable income.

### Example C — Failed Transaction

```text
Payment Failed
₹1,000 could not be transferred
```

Expected:

```text
Create Confirmed Transaction: NO
Reason: Failed Transaction
```

### Example D — Pending Payment

```text
Payment Pending
₹799 payment is processing
```

Expected:

```text
Create Confirmed Transaction: NO
Status: PENDING
```

A pending payment request is not a posted bank transaction.

## 10. Transaction Classification

Extract when available:

- Amount and ISO currency.
- Debit/credit.
- Merchant/payee/payer.
- Account reference.
- Timestamp.
- Payment status and method.
- Reference number.
- Source application.

Use existing domain classifications. A debit to a mutual fund may be an investment rather than spending; an incoming transfer from another personal account may not be income. Route uncertainty to review.

## 11. Duplicate Notification Handling

Financial apps may repost or update the same notification, change payment status, or issue multiple notices for one transaction.

Use available metadata:

- Originating package.
- Notification identifier (not assumed globally stable).
- Timestamp.
- Normalized content.
- Financial reference number.
- Deterministic source fingerprint.

Required behavior:

1. Pending notification creates no confirmed expense.
2. Updated successful notification creates a single transaction.
3. Repeated successful notification creates no duplicates.
4. Same notification key with changed status/content is re-evaluated rather than incorrectly skipped.

## 12. Interaction with SMS Transactions

One transaction may generate:

```text
₹500 payment
    +-- Bank SMS
    +-- UPI app notification
    +-- Bank app notification
```

Reuse transaction provenance. In this plan:

- Deduplicate identical notification source events.
- Match across SMS/notification only when a dependable shared transaction reference exists.
- Attach additional provenance to certain matches.
- Flag uncertain possible duplicates for review.
- Do not merge transactions based only on matching amount/time.

Full cross-source matching and reconciliation belongs to **Plan 8**. Do not implement inconsistent per-source ad hoc logic.

## 13. Source Trust and Security

Treat all notification content as untrusted input, even when the originating package is allowed.

- Allowlist packages.
- Validate inputs and length.
- Handle malformed payloads without crashing.
- Never execute textual instructions found in notifications.
- Never automatically open URLs in notifications.
- Do not assume text claiming a bank name makes the event authoritative.

## 14. Background Processing

The service can receive callbacks when the app UI is not visible.

- Keep callbacks lightweight.
- Filter unrelated package IDs quickly.
- Use appropriate asynchronous processing and durable hand-off.
- Avoid long-running work in callbacks.
- Persist accepted event/candidate state when needed.
- Apply idempotent processing.
- Handle process termination, listener reconnection, and access revocation.
- Do not assume the listener runs without interruption.
- Avoid unnecessary permanent foreground services.
- Do not perform expensive network operations in callbacks.

## 15. Listener State Management

Expose states such as:

- `NOT_CONFIGURED`
- `ACCESS_REQUIRED`
- `CONNECTING`
- `ACTIVE`
- `DISCONNECTED`
- `DISABLED`
- `ERROR`

Handle:

1. Access revoked.
2. Listener disconnected/reconnected.
3. App force-stopped.
4. Device restarted.
5. App updated.
6. Selected payment app uninstalled.
7. Notification content unavailable.

Use supported APIs for recovery. Do not silently reopen Settings or bypass consent.

## 16. Android Privacy Restrictions

Modern Android and device manufacturers can hide/redact content or restrict delivery. Support:

- Title-only notifications.
- Missing transaction details.
- Redacted/secret notifications.
- Grouped notifications.
- Work-profile separation.
- OEM background restrictions.

Do not fabricate data or attempt to bypass Android protections. Mark insufficient events unrecognized/needs review and document limitations.

## 17. Database Integration

Use Room models/repositories created previously. Store only necessary details:

- Normalized financial transaction.
- Source type and package.
- Source fingerprint.
- Processing/review state.
- Linked transaction ID.

Avoid storing unrelated notifications or full raw notification bodies. Do not upload raw notification contents. Existing cloud synchronization may upload authorized normalized financial records. Preserve manual user corrections on reprocessing.

## 18. UI Implementation

Add **Finance Settings > Notification Tracking**.

### 18.1 Tracking Screen

Show access and service status, monitoring toggle, installed supported apps, selected apps, latest processed event, total detected, and review count.

### 18.2 Application Selection

Show compatible installed apps, per-app toggles, and status for missing apps.

### 18.3 Detected Transactions

Provide a filtered view showing amount, type, source app, merchant, date/time, category, and processing status.

### 18.4 Review Queue

Reuse Plan 5 review UI for confirm/correct/account selection/reject. Do not fork review logic.

### 18.5 Source Information

Display **Detected from notification**. If a record also has bank-SMS provenance, present one financial transaction with both confirmed sources.

## 19. Shared Parser Integration

```text
SMS Source -----------+
                      |
                      v
              FinancialMessage
                      ^
                      |
Notification Source --+
                      |
                      v
              Financial Parser
                      |
                      v
            TransactionCandidate
                      |
                      v
             Ingestion Pipeline
                      |
                      v
               Room Database
```

Ensure shared code does not import Android `Context`, `StatusBarNotification`, `NotificationListenerService`, or other Android OS classes. Those belong only in Android implementation modules.

## 20. Performance Requirements

- Filter source package first.
- Avoid reparsing unchanged notifications.
- Bound memory and queues.
- Avoid unnecessary DB reads and networking.
- Use efficient asynchronous processing.
- Avoid uncontrolled background jobs.
- Handle bursts of notifications without blocking callbacks or exhausting memory.

## 21. Testing Requirements

### Parsing Fixtures

- Debit, credit, refund.
- Failed and pending.
- Promotional, OTP, ambiguous.
- Missing amount, missing merchant, missing reference.
- Multiple amounts.

### Duplicates

- Same notification twice.
- Notification update.
- Pending followed by success.
- Multiple success notifications.
- SMS + notification for one payment.

### Permissions

- Access granted, denied, revoked.
- Tracking off despite access grant.
- Individual app disabled.

### Lifecycle

- Foreground/background delivery.
- Listener disconnected/reconnected.
- Device restart and app update where feasible.

### Database and UI

- Valid record inserted.
- Invalid event ignored.
- UI observes updates.
- User correction survives reprocessing.
- No spurious duplication.

### KMP Regression

- Android compiles/runs.
- Desktop JVM still builds.
- Web WasmJS still builds.
- No Android notification classes in shared common code.

Use Android instrumentation/device testing where feasible. Do not report actual capture as verified from parser unit tests alone.

## 22. Security and Privacy Testing

Verify:

1. Only user-selected apps are monitored.
2. Nonfinancial content is ignored.
3. OTPs are not retained.
4. Raw notification contents are not logged.
5. Unrelated content is never uploaded.
6. Disabling collection stops processing.
7. Revoked access is handled.
8. Untrusted text cannot trigger actions.
9. No Accessibility permission is requested.
10. No hidden API or bypass is used.

## 23. Acceptance Criteria

Plan 6 is complete when:

1. Listener service is properly declared and implemented.
2. User authorization works.
3. Allowed financial apps are selectable.
4. Relevant notifications are identified.
5. Transaction data is parsed accurately.
6. Nonfinancial/pending/failed events do not create confirmed expenses.
7. Repeated source events do not create duplicate transactions.
8. The shared Plan 5 ingestion pipeline is reused.
9. Valid transactions persist in Room.
10. Uncertain events enter the review queue.
11. UI updates automatically.
12. Monitoring/app-selection toggles work.
13. Listener disconnects are handled safely.
14. Background processing follows OS restrictions.
15. Desktop/Web build remains valid.
16. Automated tests pass.

Document unsupported formats and Android-version limitations. When real-device testing is unavailable, distinguish code/test verification from unverified runtime capture.

## 24. Documentation

Create:

```text
docs/
├── NOTIFICATION_INTEGRATION.md
├── SUPPORTED_FINANCIAL_APPS.md
└── NOTIFICATION_TEST_CASES.md
```

Update:

- `docs/TRANSACTION_PARSING.md`
- `docs/DATA_FLOW.md`
- `docs/SECURITY.md`
- `docs/IMPLEMENTATION_STATUS.md`

Document listener design, authorization, app configurations, supported formats, parser behavior, deduplication, lifecycle/background execution, privacy constraints, and known issues.

## 25. Final Deliverables

Report:

1. Created/modified files.
2. Notification listener implementation.
3. Supported app configuration.
4. Shared parser changes.
5. Database/UI changes.
6. Authorization and lifecycle behavior.
7. Build results.
8. Unit/integration results.
9. Actual Android runtime results.
10. Remaining limitations.

Update `docs/IMPLEMENTATION_STATUS.md`. **Do not begin Plan 7.**

**Completion condition:** AnixLifeTracker automatically processes relevant notifications from explicitly selected financial apps on Android, sharing validated transaction records with Desktop and Web through the existing architecture.
