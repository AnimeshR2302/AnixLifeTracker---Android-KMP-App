# AnixLifeTracker - Privacy Architecture & Data Minimization

## 1. Local-First & Consent Model

AnixLifeTracker treats all financial records, transaction histories, accounts, and reminders as **Strictly Confidential Personal Data**.

- **Optional Local-First Inputs**: Android SMS and selected banking application notifications are local-first, optional inputs requiring explicit in-app user consent.
- **Data Minimization**: Non-financial SMS messages, personal notifications, security codes, and One-Time Passwords (OTPs) are discarded in-memory immediately prior to processing. Raw SMS texts and raw notification contents are **never** stored in database tables or uploaded to cloud servers.
- **Separate Consent Boundaries**: Financial collection consent (reading SMS/notifications locally) and Cloud Sync consent (uploading normalized records to server) are strictly separate.

---

## 2. Privacy Settings & Toggles

1. **SMS Collection Pause/Resume**: Global toggle in SMS settings to halt ingestion and background broadcast processing.
2. **Notification Collection Pause/Resume**: Global toggle and per-app allowlist toggles to control which banking apps are processed.
3. **Cloud Sync Opt-In**: Cloud sync is disabled by default. Enabling cloud sync requires explicit user confirmation via the Cloud Sync Disclosure modal.
4. **Sensitive UI Concealment**: Optional toggle to mask monetary balances and transaction amounts in the UI.

---

## 3. Data Retention & Destruction Workflows

Users maintain full control over their data through four distinct, durable deletion workflows:

| Action | Local Scope | Cloud Scope | Outcome |
| :--- | :--- | :--- | :--- |
| **Clear Local Cache** | In-memory temporary caches & review candidate buffers purged | None | Frees runtime memory; leaves stored ledger intact |
| **Purge Local Data** | All local SQLite database tables for active owner erased | None | Removes offline ledger on device; unsent edits erased |
| **Wipe Cloud Data** | None | Backend database records purged; dataset generation marker incremented | Wipes server backup; prevents stale outboxes from resurrecting data |
| **Delete Account & Data** | Local database & session tokens erased | Full server record wipe | Complete account destruction and logout |

---

## 4. Diagnostics & Log Sanitization

All log outputs pass through `RedactingLogger`:
- Account numbers (10–16 digits) -> `[REDACTED_ACCOUNT]`
- Financial amounts and currencies -> `[REDACTED_AMOUNT]`
- Bearer tokens, passwords, API keys -> `[REDACTED_SENSITIVE]`
- No external telemetry, crash analytics, or tracking SDKs are attached.
