# AnixLifeTracker - Local & Transport Encryption Specification

## 1. Encryption Model & Capabilities

AnixLifeTracker enforces distinct encryption layers across transport, backend storage, local database storage, and platform key vaults:

| Domain | Encryption Protocol | Key Vault / Provider |
| :--- | :--- | :--- |
| **Transport** | TLS 1.3 / HTTPS for remote APIs | Platform HTTPS Client Engines (CIO, Android Ktor) |
| **Backend Storage** | PostgreSQL at rest + Owner-scoped Row Level Security (`owner_id`) | Managed Cloud DB / Supabase Vault |
| **Local Database** | Room SQLite encrypted at rest via SQLCipher / Encrypted SQLite | Platform Key Vault (`KeyStorage`) |
| **Key Vault (Android)** | 256-bit AES wrapped keys | Android KeyStore (`KeyGenParameterSpec.PURPOSE_ENCRYPT | DECRYPT`) |
| **Key Vault (JVM)** | Master key with PBKDF2 / SHA-256 derivation | OS Keyring / Salted Local Vault (`~/.anixlifetracker/vault.key`) |
| **Key Vault (WasmJS)** | WebCrypto `SubtleCrypto` unexportable session keys | Browser Web Crypto Engine / In-Memory Session Storage |

---

## 2. Envelope Structure for Sensitive Payload Fields

Sensitive fields, temporary review items, and outbox mutations use a versioned envelope (`EncryptedEnvelope`):
- **Algorithm**: AES-256-GCM
- **Nonce**: 12-byte unique IV generated per encryption invocation
- **Version Tag**: `keyVersion = 1L`
- **Authenticated Associated Data (AAD)**: `${ownerId}:${recordType}` bound to prevent ciphertext substitution across records or accounts.

---

## 3. Encryption Migration Strategy (`EncryptionMigrationManager`)

- **Restart-Safe**: Tracks schema version, encryption envelope version, and migration progress.
- **Key Loss / Locking**: If the platform key is unavailable or invalidated (e.g. device security reset), the app enters `EncryptionStatus.LOCKED` rather than deleting or overwriting the database file.
- **Outbox Integrity**: Local pending edits are preserved in locked/migration states and released only when unlocked.
