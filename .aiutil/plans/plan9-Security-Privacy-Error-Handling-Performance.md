# Plan 9 — Security, Privacy, Error Handling and Performance

> Project: AnixLifeTracker — Android, Desktop JVM and Web; iOS later.
> Prerequisites: Plans 1–8 implemented against the current source tree.
> Replaces: Security/hardening portions of original Plan 16. Unit/integration tests and deployment preparation are excluded.
> Execution: Code changes only. No builds, tests or verifications will be run while implementing this plan.

## Ready-to-use implementation prompt

Harden the existing AnixLifeTracker application through source-code changes for encryption, permission management, privacy, error handling and performance. Reuse the implemented architecture, collectors, processing/analytics, reminder system and backend integration. Preserve the manual cross-device synchronization policy. Do not rebuild the app, introduce excluded features or claim production readiness.

### 1. Implementation-only execution contract

Read relevant current sources and docs: architecture, module structure, financial models, database schema, authentication, synchronization, SMS/notification integration, processing, forecasts, reminders, security and implementation status. Source inspection to determine necessary edits is allowed; do not turn it into an executed verification workflow.

Do not run builds, compilation, Gradle sync/tasks, unit tests, integration tests, instrumentation tests, lint/static analysis, security scanners, dependency-audit tasks, benchmarks, profilers, app launches/previews, manual verifications or deployment commands. Do not create or expand a test suite. Do not add CI/CD, signing/release packaging, store submission, hosting deployment, production infrastructure provisioning or deployment preparation. Do not execute SQL or security migrations against a live backend.

Implement runtime guards and error recovery as application code. Create migration/configuration source where necessary and identify changes that need later application. Existing unrelated test/CI files may remain. Final status must explicitly say that builds, tests and verifications were not run and that runtime/security/performance behavior remains unverified.

### 2. Non-negotiable behavior to preserve

- Android SMS and selected financial-app notification collection remain optional, authorized, local-first inputs.
- Android collected data and local edits queue durably. Only explicit Sync uploads them; opening the app, foreground resume, reconnect and reminders must not upload automatically.
- Android open/session restore/foreground resume triggers an automatic pull-only operation. Committed Desktop/Web edits appear when this completes, subject to connectivity/authentication.
- Desktop/Web Save immediately attempts an authenticated backend write; pending offline saves remain durable and visible. Explicit Sync publishes pending edits and retrieves latest available backend data.
- Desktop/Web data freshness is limited by the last Android upload. No realtime subscriptions, automatic Android-to-other-platform propagation, periodic refresh, push notifications or live-data sync.
- All outgoing reminder notifications remain disabled. Do not request delivery permissions or enable a sender while hardening.
- Local-only mode remains usable without authentication or cloud sync; user accounts and data remain isolated.

Do not add a generic “sync on reconnect” recovery path that violates direction-specific rules. Automatic foreground-pull retries and retries of Desktop/Web user saves must be bounded and must retain their operation reason. Reuse shared policy enforcement rather than scattered booleans.

### 3. Encryption model and truthful capabilities

Document and implement distinct protections for transport, backend storage, local financial storage and credential/key storage. Never claim one provides the others. Do not label provider-side encryption at rest as end-to-end encryption.

Keep the existing authorized-backend architecture for cross-device edits; do not silently convert the system to E2EE without a defined multi-device key-provisioning/recovery protocol. This plan does not introduce an E2EE protocol. If E2EE is already genuinely implemented in current code, preserve it and its key lifecycle rather than replacing it with plaintext backend storage.

For existing standard backend storage, explicitly document that the backend can process stored normalized financial records, subject to its access controls. Financial collection consent and cloud-upload consent are separate.

### 4. Transport and remote access protection

Implement shared network configuration and platform adapters that:

1. Require HTTPS for remote authenticated APIs; any existing explicit local-development exception remains narrowly scoped and never the production default.
2. Retain normal TLS certificate/hostname validation; never install a trust-all manager or disable checks to resolve a network error.
3. Keep provider publishable configuration distinct from secrets. Service-role/database passwords, private keys and privileged credentials never enter Android/Desktop/Web client source or resources.
4. Enforce authenticated owner-based access for every private table and related record, including transactions, provenance links, outbox-facing endpoints, conflicts, reminders, preferences and deletion operations.
5. Derive authoritative ownership from the server identity. Prevent forged foreign keys from linking one user's records to another user's account/source.
6. Use server-managed revisions/conditional writes and owner-scoped idempotency keys. Return conflicts explicitly; do not replace them with unguarded upserts.

Add/correct versioned backend policy/migration source files as needed. Do not report policies as active or verified when they have only been written.

### 5. Local encryption and platform key storage

Reuse the actual KMP persistence stack selected in Plans 1–3. Introduce or complete a shared sensitive-storage/key-provider boundary with platform-specific capabilities. Use established cryptographic libraries already compatible with the actual targets; never implement custom cipher algorithms or put Android/JVM crypto classes in commonMain.

Protect canonical financial data and sensitive auxiliary storage: pending mutations, conflict copies, provenance metadata, reminder notes, temporary migration copies and persisted session state. Encrypting only the main transaction table is insufficient.

**Android**

- Protect wrapping/encryption keys with Android Keystore through an Android adapter. Use a compatible existing encrypted-database solution or application-level authenticated encryption when the chosen database cannot support native encryption.
- For field/blob encryption use a versioned envelope with an approved authenticated cipher (for example AES-GCM), a fresh unique nonce per key/message, ciphertext and necessary key/version identifiers. Bind appropriate owner/record context as authenticated associated data.
- Do not hardcode keys, store them beside ciphertext unprotected or reuse nonces. Handle device-key invalidation, app reinstall and backup restore without deleting the original data silently.
- Keep collection requirements in mind: a biometric prompt on every database access would prevent unattended authorized ingestion. Separate optional UI lock policy from background-storage access; when keys are unavailable, pause protected processing or use an explicitly secure durable staging path rather than dropping into plaintext.

**Desktop JVM**

- Use an existing suitable OS credential/key-store adapter where available. Do not assume Android Keystore or a universal desktop keyring.
- Where an OS vault is unavailable, expose that capability and use a supported passphrase-protected encrypted vault with an established password KDF and per-vault salt; never silently persist an unprotected replacement key.
- Scope unlock state to the session, clear references when locking/logging out where practical and avoid promising guaranteed memory wiping on managed runtimes.

**Web**

- Use supported browser cryptography through a Wasm-compatible adapter; do not assume a JVM/native encrypted database works in a browser.
- Minimize persisted sensitive data and tokens. For encrypted local persistence, use the existing compatible storage plus authenticated encryption and a deliberate unlock/key-storage policy.
- A browser-stored/non-exportable key does not protect against malicious same-origin script invoking it. A key persisted alongside encrypted IndexedDB data is not equivalent to a separately unlocked vault; document the actual threat boundary.
- If secure unlock/persistence is unavailable, prefer memory-only sensitive caching with an explicit user-facing limitation over plaintext fallback. Preserve pending edits through a supported encrypted queue when persistence is enabled; if only memory mode is available, disclose that unsaved offline edits cannot survive closing the tab and prevent a false Saved claim.
- Account for storage denial/quota/eviction and private browsing. Do not silently lose pending saves or mislabel them synchronized.

Expose Supported, Locked, Unavailable, KeyInvalidated and MigrationRequired states rather than fake encryption success. Do not claim full-database encryption if only selected payload fields are encrypted; identify remaining plaintext metadata and minimize it.

### 6. Encryption migration and recovery source changes

Design a restart-safe migration from existing plaintext storage where required:

1. Track schema/encryption/key-envelope versions explicitly and preserve stable record IDs, owner IDs, pending mutations, revisions, conflicts and sync cursors.
2. Write encrypted copies transactionally or in bounded restartable batches; keep source data recoverable until each migration stage commits.
3. Keep original/temporary files private and clean them up only after committed migration progress permits it. Do not promise forensic erasure on flash storage.
4. Handle unavailable keys or authentication failures with locked/recovery state. Never create a new empty database over recoverable encrypted data.
5. Retain outbox writes made during supported migration transitions or serialize migration against edits; prevent half-migrated records from uploading.
6. Distinguish recoverable backend-synced data from local-only data and unsent edits. A server restore cannot recover records that were never uploaded.
7. Do not silently reset sync cursors or replay historic alerts as new transactions when encryption changes.

Implement migration logic/source only; do not execute it now. Any passphrase recovery limitation or platform key reset must be stated clearly in the product's recovery flow.

### 7. Session lifecycle and account isolation

Harden the current AuthRepository rather than creating another authentication stack:

- Use supported session persistence and token refresh with one refresh operation shared across concurrent callers.
- Never log passwords, bearer/refresh tokens or authorization headers.
- Prevent refresh loops on revoked/invalid credentials; retain local financial data and pending edits while requesting reauthentication.
- Partition local stores, encrypted keys, cursors, requests and cached UI state by account. Late responses from an old session must be discarded.
- Logout cancels user-scoped work and clears credentials. Retaining local financial records must not make them visible to the next user. Lock/isolate them; do not destroy unsent edits without an explicit destructive action.
- Keep anonymous-data linking opt-in and idempotent; never merge two signed-in users' datasets automatically.
- For Web, preserve the established auth approach and its supported CSRF/XSS/session defenses. Do not assume HttpOnly cookie protection is available to a token-based public client without the corresponding backend architecture.

### 8. Permission and capability management

Implement a single coherent settings/state layer with separate states for OS permission, in-app consent, feature enabled, source allowlist and actual service availability.

- SMS access: preserve the existing restricted-permission variant boundary and local permission checks. No restricted SMS declarations/requests in the standard variant.
- Notification reading: require explicit in-app consent plus Android notification-access grant and selected app allowlist. Revocation or disable stops processing promptly.
- Re-check access on Android foreground resume and before protected collection. Handle denied, revoked, restricted, unavailable and disconnected states without repeatedly reopening Settings or requesting permission.
- Desktop/Web do not show fake SMS/listener permission controls. Explain that financial collection originates on Android and data becomes available after Android Sync.
- No outgoing notification permission prompts while delivery is disabled. Notification-listener access and POST_NOTIFICATIONS remain separate concepts.
- Disable/cancel queued source processing when consent is withdrawn; do not process retained raw events after revocation unless a specific retained-data policy explicitly permits it.
- Account for key-locked state separately from permission denial, so the user sees the correct recovery action.

Permission denial must not break manual financial entry, analytics, reminder management or viewing previously authorized local data.

### 9. Privacy controls and data minimization

Implement usable controls for:

1. Pause/resume SMS and financial-notification collection, globally and per allowed app where applicable.
2. Separate cloud opt-in, a clear description of normalized fields uploaded and explicit Sync.
3. Local data retention for minimal source metadata/review items; discard unrelated messages and OTP/security content. Never upload raw SMS/notification bodies.
4. Clear local cache, remove local financial data, delete cloud financial data and delete account, as distinct actions with clear consequences for unsent edits.
5. Sensitive UI concealment/optional app lock where compatible with existing design, plus generic previews if delivery is enabled in a future release.
6. Sanitized diagnostics without financial descriptions, full account references, exact balances, raw payloads, tokens or keys. Do not add external telemetry uploads by default.

Implement deletion as an authenticated durable workflow. A cloud wipe must propagate to other devices without their stale outboxes resurrecting deleted data; use an account dataset generation/reset marker or equivalent server-enforced policy. An offline deletion request must say Pending rather than claiming remote data was erased. Local cache clearing must not accidentally enqueue cloud deletion. Preserve enough non-sensitive tombstone/reset metadata to honor deletion safely, with documented retention bounds.

Do not promise deletion from provider backups where the app has no immediate control. Do not introduce account-sharing/invitation features or external data exports in this plan.

### 10. Error taxonomy and recovery code

Use shared typed errors/results and platform mappings. Avoid catch-all success fallbacks and raw exception messages in UI. Cover:

| Failure | Required behavior |
|---|---|
| Offline/timeout | Preserve local data and pending work; show cached freshness; retry only allowed operation types |
| Expired/revoked session | Bounded token refresh then reauthentication; no endless loop or data wipe |
| Forbidden ownership | Permanent blocked state with safe message; no privilege escalation or endless retry |
| Validation/parsing | Ignore invalid sources or route to review; retain minimal safe diagnostic reason |
| Write conflict | Preserve local/remote versions and show resolution UI |
| Rate limit/server failure | Bounded backoff with jitter and Retry-After where available; keep operation IDs |
| Disk full/browser quota | Fail the mutation visibly; never show Saved for an uncommitted record |
| Key unavailable/decryption failure | Locked/recovery state; no plaintext fallback, silent reset or silent data skip |
| Partial sync/cancellation | Retain unacknowledged operations; commit cursors only with applied data |
| Collector disconnect/revocation | Pause collection and show capability state; preserve existing ledger |
| Bad schema/data payload | Stop affected batch safely, retain cursor and recovery metadata; do not corrupt valid records |

Use structured concurrency, cancellation propagation, timeouts and a single-flight coordinator. Do not swallow CancellationException. Prevent overlapping refresh/sync/ingestion jobs from producing duplicate records. Distinguish no-data, stale-data, loading, partial-success and failure states in shared ViewModels.

Retries must be idempotent and bounded, preserve the same operation identity, and stop on permanent failures. A pull retry must never become an Android upload. Local validation/runtime schema guards are code requirements, not permission to execute tests or verification now.

### 11. Performance source improvements

Improve identifiable hotspots from source inspection without claiming measured speedups:

- Filter incoming notification packages/consent before content parsing; bound payload size, parser work, queues and durable hand-offs.
- Use bounded concurrency and batched database writes; do not block Android callbacks or UI threads with parsing, crypto, SQL or networking.
- Add indexes for owner/date/account/category/status, source fingerprints, references, pending revisions, reminder due-state and sync sequence where relevant. Avoid indexing encrypted payload fields as if plaintext search still works; retain only deliberately chosen minimal metadata or use supported protected indexes with documented leakage.
- Perform paginated transaction queries and narrow aggregations; avoid loading the entire ledger for every dashboard recomposition.
- Update affected aggregates/recurrence/forecasts incrementally and invalidate caches by canonical data revision, filters, timezone and calculation version. A category correction must not leave totals stale.
- Batch post-sync recalculation, apply backpressure to source bursts and prevent unbounded Flow collectors/recomposition work.
- Limit reminder generation horizon and overdue paging. No per-minute server polling or always-running background services.
- Keep network payloads minimal, use bounded delta batches and resume cursors. Reuse HTTP clients; avoid per-row requests and full-dataset refresh after every Save.
- Keep Wasm-heavy work off rendering paths where the current architecture supports it; chunk cooperative work rather than assuming JVM threads exist in browsers.
- Keep all optimized paths consistent with exact-money calculations, user overrides, encryption and crash-safe outbox semantics. Do not trade away financial correctness for caching.

Document intended improvements and unmeasured assumptions. Do not run benchmarks/profilers or describe any speedup as established.

### 12. KMP source boundaries

Keep commonMain responsible for policies, typed errors, retry decisions, sync triggers, privacy settings, financial invariants and shared UI states. Put cryptography/key-store capabilities, OS permissions, platform scheduling/lifecycle, network engines and storage adapters in their actual platform modules. Reuse existing dependencies; do not pick an Android-only encryption solution for every target.

Use capability-based UI and explicit unsupported states. Do not expose Android classes in common/Web code or pretend browser lifecycle guarantees match Android services. Leave future iOS extension points; adding its target is out of scope.

### 13. Documentation and final handoff

Update docs/SECURITY.md, docs/PRIVACY.md, docs/PERMISSIONS.md, docs/ENCRYPTION.md, docs/ERROR_HANDLING.md, docs/PERFORMANCE.md, docs/SYNCHRONIZATION.md and docs/IMPLEMENTATION_STATUS.md. Create only missing relevant documents and preserve project conventions.

Record the actual encryption coverage per platform, remaining plaintext metadata, key/unlock/recovery behavior, trust model, account/deletion isolation, disabled output notifications, manual upload policy and unexecuted migration/configuration requirements. Mark this as code hardening implemented with runtime outcomes unverified; do not describe the app as security-audited, tested or production-ready.

Finish with changed files/components, source-level migrations and configuration changes, material limitations and deferred investment planning. Explicitly state: “No builds, tests or verifications were run, as requested.” No deployment preparation is included. Stop after Plan 9.
