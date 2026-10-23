# AnixLifeTracker - Unified Error Taxonomy & Recovery Protocol

## 1. Structured Error Hierarchy (`AppError`)

AnixLifeTracker uses typed error categories (`AppError`) wrapped in `AppResult<T>`:

| Category | Typical Scenarios | Recovery Action |
| :--- | :--- | :--- |
| `AppError.Network.Offline` | Device disconnected from internet | Work saved locally; state set to Offline; explicit sync retries allowed |
| `AppError.Network.Timeout` | Slow backend response | Automatic exponential backoff with jitter |
| `AppError.Auth.SessionExpired` | JWT access/refresh token invalid | Prompt re-authentication; local financial data and pending edits preserved |
| `AppError.Forbidden.OwnershipMismatch` | Record belongs to another owner | Permanently block mutation; log security warning via `RedactingLogger` |
| `AppError.Validation.ParsingFailed` | Malformed SMS or notification format | Route to review queue as low confidence; preserve raw snippet locally |
| `AppError.Conflict.RecordVersionMismatch` | Concurrent edits across devices | Route to `SyncConflict` resolution UI |
| `AppError.RateLimit` | HTTP 429 Too Many Requests | Respect `Retry-After` header; backoff with jitter |
| `AppError.Storage.DiskFull` | Local device storage exhausted | Visibly fail mutation; prevent marking record as saved |
| `AppError.Encryption.KeyUnavailable` | Android KeyStore or JVM vault locked | Transition to `EncryptionStatus.LOCKED`; prompt passphrase/unlock |
| `AppError.Collector.PermissionDenied` | SMS or Notification permission revoked | Pause collector; display capability status in Settings |

---

## 2. Coroutine Concurrency Invariants

- **Cancellation Exception**: `CancellationException` is never caught or swallowed in `try-catch` blocks.
- **Single-Flight Coordinator**: Token refreshes, sync sessions, and historical SMS imports use `Mutex` / single-flight execution to prevent concurrent overlapping jobs.
- **Thread Offloading**: Heavy JSON parsing, regex extraction, database writes, and crypto operations are offloaded to `Dispatchers.Default` / `Dispatchers.IO`.
