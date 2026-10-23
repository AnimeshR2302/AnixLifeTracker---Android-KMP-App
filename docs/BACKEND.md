# AnixLifeTracker - Backend Architecture & Database Specification

## 1. Overview

AnixLifeTracker's backend architecture is built to support **Offline-First**, **Local-First**, and **Cloud-Enabled** operations across Android, Desktop JVM, and Web WasmJS.

The primary backend integration uses **Supabase** (PostgreSQL + Supabase Auth + PostgREST + Row Level Security) or an equivalent Ktor Server API proxy.

---

## 2. Architecture Principles

1. **Local Autonomy**: The client application operates completely independently without internet connectivity. All primary CRUD actions are written to local Room/SQLite storage first.
2. **Per-User Isolation**: Every remote record belongs to an authenticated `owner_id` mapped directly from `auth.uid()`. PostgreSQL Row Level Security (RLS) enforces access control at the database layer.
3. **No Sensitive Data Retention**: Unprocessed SMS messages, bank notification bodies, or plain-text credentials are **never** transmitted to or stored on the backend.
4. **Idempotent Sync Pipelines**: Synchronization push and pull operations use stable string UUIDs (`id`) and monotonic checkpoints to guarantee that repeated synchronization requests never duplicate transactions.

---

## 3. Database Migration & Schema

The backend relational schema mirrors the domain entities in the KMP client.

- Primary DDL script: `docs/BACKEND_SCHEMA.sql`
- Major tables:
  - `accounts`, `transactions`, `categories`, `budgets`, `recurring_payments`, `upcoming_obligations`, `financial_goals`
  - `investment_accounts`, `investment_instruments`, `investment_transactions`, `sip_schedules`
  - `reminders`, `sync_checkpoints`, `sync_tombstones`

### Row Level Security (RLS) Invariant
```sql
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY transactions_owner_policy ON transactions FOR ALL USING (auth.uid()::text = owner_id);
```

---

## 4. API Endpoints

### Authentication Endpoints
- `POST /api/v1/auth/register`: Register email and password, returning JWT user session.
- `POST /api/v1/auth/login`: Authenticate existing user, returning access and refresh tokens.
- `POST /api/v1/auth/reset-password`: Initiate password recovery email flow.
- `POST /api/v1/auth/refresh`: Restore expired session token using valid refresh token.

### Synchronization Endpoints
- `POST /api/v1/sync/push`: Accepts `PendingChangesPayload` containing offline inserts, updates, and tombstones.
- `POST /api/v1/sync/pull`: Accepts client checkpoints and returns server records updated since checkpoint timestamp.

---

## 5. Deployment Setup & Environment Variables

To deploy against a live Supabase project:

```bash
SUPABASE_URL="https://your-project.supabase.co"
SUPABASE_ANON_KEY="your-publishable-anon-key"
```

In client builds, public publishable keys are supplied via build configuration; database credentials and service role keys are strictly omitted from client artifacts.
