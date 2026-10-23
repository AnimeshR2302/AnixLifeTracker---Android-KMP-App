# AnixLifeTracker - Authentication System & Security

## 1. Overview

AnixLifeTracker provides optional, secure cloud authentication. Authentication is **never required** to use core local financial tracking capabilities on any platform.

---

## 2. Authentication Lifecycle & States

```
                 [ App Launch ]
                       |
                       v
            [ Session Storage Check ]
             /                     \
    (Session Valid)         (No Session / Expired)
           |                          |
           v                          v
  [ AuthState.Authenticated ]   [ AuthState.Unauthenticated ]
           |                          |
           +----- (Local-First Mode) -+
```

### Supported States (`AuthState`)
- `Unauthenticated`: Application operates in local-first offline mode. Data is owned by `"local_user"`.
- `Loading`: Authentication attempt or session restoration in progress.
- `Authenticated(session)`: User has verified JWT access token. Cloud synchronization enabled upon consent.
- `Error(message)`: Authentication failure banner with actionable user guidance.

---

## 3. Account Operations

### 3.1 Registration
Users register using email and password (minimum 6 characters) or passwordless link. Upon successful registration, a `UserSession` is generated.

### 3.2 Login & Session Persistence
On sign in, `AuthRepository` receives `UserSession` containing:
- `accessToken`: Standard JWT used for backend API authorization.
- `refreshToken`: Token used to refresh expired sessions silently.
- `expiresAtEpochSec`: Token expiry time.
- `user`: User profile containing `id`, `email`, and `createdAt`.

Session tokens are persisted via `PersistentSessionStorage`.

### 3.3 Logout Invariant (Requirement 5.3)
When a user signs out:
1. Auth state transitions to `AuthState.Unauthenticated`.
2. Cloud sync is disabled.
3. Active `accessToken` and `refreshToken` are wiped from `SessionStorage`.
4. **CRITICAL REQUIREMENT**: Local financial records (Accounts, Transactions, Budgets, Reminders) are **strictly preserved** in Room storage. Records are never deleted merely because the user logged out.

---

## 4. Local Data Ownership Migration (Local-to-Cloud Transition)

When an existing local user signs in for the first time:
1. Application detects existing local entities tagged with `ownerId = "local_user"`.
2. Displays explicit **Cloud Data Disclosure Modal** outlining what data will be synced.
3. Upon user confirmation, `SyncDao.migrateAccountsOwner(newOwnerId)` updates `ownerId` from `"local_user"` to `user.id`.
4. Sets local entity `syncStatus = "PENDING_UPLOAD"` to safely queue records for initial cloud synchronization without record duplication.
