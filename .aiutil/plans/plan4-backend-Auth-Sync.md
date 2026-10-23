# Plan 4 — Backend, Authentication and Cross-Platform Synchronization

> **Project:** AnixLifeTracker  
> **Implementation sequence:** Plan 4 of 17  
> **Scope:** Optional cloud authentication and secure offline-first sync between Android, Desktop JVM, and Web WasmJS.

# TASK: Implement AnixLifeTracker Backend, Authentication, and Cross-Platform Synchronization

You are a senior Kotlin Multiplatform engineer and backend architect working on AnixLifeTracker.

Plans 1–3 have already implemented the application architecture, shared UI, local database, financial models, and repository layer. Your responsibility is to implement authentication, backend integration, and reliable offline-first synchronization. **Do not recreate existing functionality.**

---

## 1. Read Existing Project Context

Before changing code, inspect:

- `docs/ARCHITECTURE.md`
- `docs/MODULE_STRUCTURE.md`
- `docs/TECH_STACK.md`
- `docs/DATABASE_SCHEMA.md`
- `docs/FINANCIAL_MODELS.md`
- `docs/SECURITY.md`
- `docs/AGENT_GUIDELINES.md`
- `docs/IMPLEMENTATION_STATUS.md`

Inspect actual Gradle modules, repository interfaces, Room entities, and dependency injection configuration. Preserve architectural decisions unless they create demonstrable incompatibilities. Document necessary changes.

## 2. Objectives

Implement:

1. User authentication.
2. Secure login and logout.
3. Persistent authenticated sessions.
4. Cross-platform account access.
5. Backend financial data storage.
6. Local-to-cloud synchronization.
7. Cloud-to-local synchronization.
8. Offline data modifications.
9. Automatic synchronization when connectivity returns.
10. Synchronization conflict handling.
11. User data isolation.
12. Synchronization status reporting.
13. Optional cloud synchronization.
14. Support for future background financial monitoring.

The application must remain usable without internet connectivity. Authentication must **not** be required to perform basic local financial tracking.

## 3. Backend Selection

Prefer **Supabase** using:

- PostgreSQL.
- Supabase Authentication.
- Row Level Security (RLS).
- REST/PostgREST APIs.
- Supabase Kotlin Multiplatform SDK 3.x.
- Ktor where required.

Before implementing, verify the selected SDK and dependencies support **Android, Desktop JVM, and Kotlin/WasmJS**. If Plan 1 selected another appropriate provider, follow that decision or document a justified change. Avoid unnecessary self-hosted backend infrastructure. Use a free-tier-compatible architecture where practical; do not assume unlimited free hosting, API calls, or storage.

**Never embed** database passwords, service-role keys, secret API keys, or private signing keys in client applications. Clients may use public/publishable keys only when backend access is secured appropriately. Privileged operations must run on trusted server infrastructure.

## 4. Backend Database Design

Use the Room schema from Plan 3 as the starting point. Implement synchronized backend tables for:

**Finance**
- Financial accounts.
- Transactions.
- Transaction categories.
- Budgets.
- Recurring payments.
- Financial obligations.
- Financial goals.

**Investments**
- Investment accounts.
- Investment transactions.
- SIP schedules.
- User investment records.

**Reminders**
- User reminders.
- Alert preferences.

**Synchronization**
- Device registrations.
- Sync checkpoints or cursors.
- Entity revisions.
- Deletion tombstones where required.

Only synchronize necessary data. Never upload raw SMS messages, unrelated notifications, bank passwords, or authentication secrets. Public market prices and investment reference data should eventually use shared datasets, rather than being duplicated per user.

Implement stable IDs, foreign keys, unique constraints, appropriate indexes, ownership fields, created/updated timestamps, server-managed revision metadata, safe deletion semantics, and versioned migrations. Use exact numeric representations for monetary amounts and investment quantities. Do not use floating-point accounting. Keep backend SQL migrations under version control.

## 5. Authentication System

Implement shared `AuthRepository` and state interfaces.

### 5.1 Registration

- Email registration.
- Password or passwordless email authentication.
- Email verification when configured.
- Validation and user-readable errors.

Choose a secure flow compatible with all three platforms.

### 5.2 Login

- Login screen.
- Credential validation.
- Loading, success, and error states.
- Session creation and restoration.

### 5.3 Logout

- Clear authentication state.
- Terminate/revoke session where supported.
- Clear locally stored credentials.
- Stop synchronization until authentication resumes.
- **Do not delete local financial records merely because the user logs out.**

### 5.4 Password Recovery

Implement provider-supported account recovery.

### 5.5 Session Handling

- Session restoration.
- Token refresh.
- Expired-session handling.
- Observable authentication state.
- Reauthentication when needed.

Never store passwords locally or log tokens. Use platform-appropriate credential/session storage. On Android and Desktop, prefer available OS-backed protection; on Web, account for XSS exposure and limitations of browser storage.

## 6. Local-First and Cloud-Enabled Modes

### Mode A — Local Only

User can create accounts, enter transactions, track expenses, and manage investments without uploading personal financial information.

### Mode B — Cloud Synchronization

User explicitly opts in. After authentication:

1. Examine local records.
2. Retrieve existing remote records.
3. Reconcile differences.
4. Upload safe pending local changes.
5. Download remote changes.
6. Update local Room storage.
7. Allow other devices to synchronize.

Explain **what financial information will be uploaded** before initial sync. Do not silently opt users into cloud sync.

## 7. Synchronization Architecture

Implement dedicated contracts:

```text
Local Room Database
        |
        v
   Sync Coordinator
        |
        +-- Local Change Tracker
        +-- Remote Data Source
        +-- Conflict Resolver
        +-- Sync Metadata Repository
        |
        v
   Backend Database
```

Suggested interfaces (adapt to existing architecture):

```kotlin
interface SyncRepository {
    suspend fun sync(): SyncResult
    fun observeSyncStatus(): Flow<SyncStatus>
}

interface SyncCoordinator {
    suspend fun synchronize(): SyncResult
}
```

Do not leak Supabase SDK types into the domain layer.

## 8. Local Change Tracking

Track persistent states:

- `SYNCED`
- `PENDING_UPLOAD`
- `PENDING_DELETE`
- `CONFLICT`
- `SYNC_ERROR`

Track creation, updates, and deletion using stable IDs and durable sync metadata. State must survive crashes and restarts; do not rely on in-memory flags. Deleted records must not be resurrected because another device still has a stale version.

## 9. Synchronization Algorithm

```text
Synchronization Trigger
          |
          v
Check Authentication
          |
          v
Check Connectivity
          |
          v
Upload Pending Changes
          |
          v
Fetch Remote Changes
          |
          v
Detect Conflicts
          |
          v
Resolve or Flag Conflicts
          |
          v
Apply Local Database Updates
          |
          v
Update Sync Checkpoint
          |
          v
Emit Synchronization Status
```

Requirements:

- Idempotent operations.
- Bounded batches.
- Retry with exponential backoff.
- Partial failure recovery.
- Cancellation support.
- Persistent checkpoints.
- No duplicated financial transactions.
- No silently lost pending changes.

Persist checkpoints only once the corresponding changes have been committed. Prefer server-managed revisions or authoritative timestamps rather than device clocks alone.

## 10. Conflict Resolution

Example: Device A changes a transaction category while Device B changes the amount offline.

The system must detect competing updates. Preserve conflicting financial values and do **not** silently apply last-write-wins to monetary records. Auto-merge only when provably safe; otherwise flag for user review. Maintain sufficient conflict metadata for recovery. Simpler rules can be used for nonfinancial preferences.

## 11. Multi-Device Data Ownership

A user may sign in from Android, Linux Desktop, Web, and later iPhone. Enforce consistent account identity and record ownership.

### Row Level Security

- Implement ownership RLS on every private table.
- Derive ownership from authenticated server identity, not arbitrary client-supplied `ownerId` values.
- Test cross-user read, insert, update, and delete rejection.
- Keep administrator credentials out of builds.

## 12. Local Data Ownership Migration

A local-only user may have data before creating an account. When that user signs in:

1. Detect existing local records.
2. Explain how import/sync works.
3. Obtain explicit confirmation.
4. Associate data with the authenticated identity safely.
5. Retrieve existing remote records.
6. Reconcile before upload.
7. Preserve originals and prevent duplicate imports.

Never automatically merge two different users' data. When switching accounts on the same device, isolate or lock previous users' data rather than exposing it to the new user.

## 13. Platform-Specific Synchronization

**Android**
- Use WorkManager for eligible background sync.
- Trigger sync after reconnect where feasible.
- Support manual foreground sync.
- Respect OS execution limits.

**Desktop JVM**
- Sync on startup and after local changes.
- Optionally sync periodically while running.
- Handle network interruptions gracefully.

**Web WasmJS**
- Sync while the application is open.
- Support manual sync and reconnect handling.
- Do not promise background sync when the browser is closed.

Keep synchronization/business logic in common code and schedulers platform-specific.

## 14. Security and Privacy

Implement or explicitly document:

- HTTPS/TLS for network connections.
- Secure handling of sessions and token expiration.
- Least-privilege data access.
- Redaction of secrets and financial data from logs.
- Sensitive-data minimization.
- Isolation of local users.
- Account/data deletion flows.
- Privacy disclosures for uploads.

Distinguish encryption in transit, provider encryption at rest, and true end-to-end encryption. **Do not call this end-to-end encrypted** without implementing and testing actual E2EE.

## 15. UI Integration

**Authentication screens:** sign in, sign up, password recovery, and errors.

**Settings:** account information, sign in/out, enable/disable sync, sync now, last success, current status, pending changes, errors.

**Sync indicators:** Synced, Syncing, Offline, Pending Changes, Sync Failed, Conflict Detected.

Do not display **Synced** unless synchronization has completed successfully.

## 16. API and Network Layer

Use shared KMP-compatible networking interfaces:

- Authentication client.
- Remote repository implementation.
- Serializable DTOs and mapping.
- Network error mapping.
- Retry and timeouts.
- Request cancellation.

Do not expose backend DTOs directly to UI. Avoid endless retries for permanent authentication or authorization failures.

## 17. Testing Requirements

### Authentication
- Registration success/failure.
- Valid/invalid login.
- Session restoration and expiration.
- Token refresh.
- Logout.
- Password recovery.

### Synchronization
- First sync.
- Offline create/edit/delete.
- Reconnect sync.
- Repeated sync.
- Interrupted sync.
- Duplicate upload requests.
- Concurrent updates and conflicts.
- Account switching.

### Security
- Unauthorized request rejection.
- Cross-user data isolation and RLS enforcement.
- Expired/invalid token handling.
- No secrets in client build artifacts.

### Platforms
- Verify Android, Desktop, and Web separately.
- Use local test backend and synthetic financial fixtures.
- Do not upload real personal financial data during testing.

## 18. Acceptance Criteria

Plan 4 is complete when:

1. Authentication works on supported platforms.
2. Local-only mode remains functional.
3. Cloud sync is opt-in.
4. Accounts and transactions sync correctly.
5. Offline changes are retained.
6. Repeated sync does not duplicate records.
7. Deletions propagate correctly.
8. Financial conflicts are never silently lost.
9. Per-user data isolation is tested.
10. Status displays accurately.
11. Tests pass for configured targets.

If backend credentials or infrastructure are unavailable, implement local configuration, SQL migrations, repository integration, and automated tests; clearly identify unverified live-backend requirements. Do not fabricate credentials or report unrun tests as passing.

## 19. Documentation

Create/update:

```text
docs/
├── BACKEND.md
├── AUTHENTICATION.md
├── SYNCHRONIZATION.md
├── SECURITY.md
└── IMPLEMENTATION_STATUS.md
```

Document setup, environment variables, schema migrations, Auth setup, RLS, sync algorithm, conflict resolution, platform behavior, and testing.

## 20. Final Deliverables

Report:

1. Backend architecture.
2. Authentication implementation.
3. Sync implementation.
4. Database migrations.
5. UI changes.
6. Files created/changed.
7. Build results.
8. Automated tests.
9. Security limitations.
10. Remaining setup requirements.

Update `docs/IMPLEMENTATION_STATUS.md`. **Do not begin Plan 5.**

**Completion condition:** Financial information can be managed offline and, when explicitly enabled, synchronized securely across Android, Desktop, and Web without duplication or silent data loss.
