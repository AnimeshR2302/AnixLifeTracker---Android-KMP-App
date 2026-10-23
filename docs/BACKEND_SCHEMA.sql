-- AnixLifeTracker PostgreSQL / Supabase Backend Database Schema & Migration v1
-- Implements Row Level Security (RLS) for multi-tenant per-user isolation

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. FINANCE TABLES
-- ============================================================================

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    account_name TEXT NOT NULL,
    account_type TEXT NOT NULL,
    institution_name TEXT NOT NULL,
    account_reference_masked TEXT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    opening_balance_minor BIGINT NOT NULL DEFAULT 0,
    balance_snapshot_minor BIGINT NOT NULL DEFAULT 0,
    balance_snapshot_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sync_status TEXT NOT NULL DEFAULT 'SYNCED'
);

ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY accounts_owner_policy ON accounts FOR ALL USING (auth.uid()::text = owner_id);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    account_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    direction TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    category_id TEXT,
    merchant_name TEXT,
    description TEXT,
    transaction_reference TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    local_transaction_date DATE NOT NULL,
    status TEXT NOT NULL DEFAULT 'POSTED',
    transfer_group_id TEXT,
    source_type TEXT NOT NULL DEFAULT 'MANUAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    sync_status TEXT NOT NULL DEFAULT 'SYNCED'
);

ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY transactions_owner_policy ON transactions FOR ALL USING (auth.uid()::text = owner_id);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    name TEXT NOT NULL,
    parent_category_id TEXT,
    category_type TEXT NOT NULL,
    icon_identifier TEXT NOT NULL DEFAULT 'category',
    is_system_category BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
CREATE POLICY categories_owner_policy ON categories FOR ALL USING (auth.uid()::text = owner_id OR is_system_category = TRUE);

-- Budgets
CREATE TABLE IF NOT EXISTS budgets (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    name TEXT NOT NULL,
    category_id TEXT REFERENCES categories(id) ON DELETE SET NULL,
    limit_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    period_type TEXT NOT NULL DEFAULT 'MONTHLY',
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
CREATE POLICY budgets_owner_policy ON budgets FOR ALL USING (auth.uid()::text = owner_id);

-- Recurring Payments
CREATE TABLE IF NOT EXISTS recurring_payments (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    account_id TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category_id TEXT,
    expected_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    frequency TEXT NOT NULL,
    start_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    end_date DATE,
    is_variable_amount BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE recurring_payments ENABLE ROW LEVEL SECURITY;
CREATE POLICY recurring_payments_owner_policy ON recurring_payments FOR ALL USING (auth.uid()::text = owner_id);

-- Upcoming Obligations
CREATE TABLE IF NOT EXISTS upcoming_obligations (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    account_id TEXT REFERENCES accounts(id) ON DELETE SET NULL,
    recurring_payment_id TEXT REFERENCES recurring_payments(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    expected_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    expected_date DATE NOT NULL,
    obligation_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    linked_transaction_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE upcoming_obligations ENABLE ROW LEVEL SECURITY;
CREATE POLICY upcoming_obligations_owner_policy ON upcoming_obligations FOR ALL USING (auth.uid()::text = owner_id);

-- Financial Goals
CREATE TABLE IF NOT EXISTS financial_goals (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    name TEXT NOT NULL,
    target_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    target_date DATE NOT NULL,
    associated_account_ids_json TEXT NOT NULL DEFAULT '[]',
    current_progress_minor BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE financial_goals ENABLE ROW LEVEL SECURITY;
CREATE POLICY financial_goals_owner_policy ON financial_goals FOR ALL USING (auth.uid()::text = owner_id);

-- ============================================================================
-- 2. INVESTMENT TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS investment_accounts (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    name TEXT NOT NULL,
    provider TEXT NOT NULL,
    account_type TEXT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE investment_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY investment_accounts_owner_policy ON investment_accounts FOR ALL USING (auth.uid()::text = owner_id);

CREATE TABLE IF NOT EXISTS investment_instruments (
    id TEXT PRIMARY KEY,
    instrument_type TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT,
    isin TEXT,
    amfi_code TEXT,
    exchange TEXT,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Instruments table is public read-only for authenticated users
ALTER TABLE investment_instruments ENABLE ROW LEVEL SECURITY;
CREATE POLICY investment_instruments_read_policy ON investment_instruments FOR SELECT USING (auth.role() = 'authenticated');

CREATE TABLE IF NOT EXISTS investment_transactions (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    investment_account_id TEXT NOT NULL REFERENCES investment_accounts(id) ON DELETE RESTRICT,
    instrument_id TEXT NOT NULL REFERENCES investment_instruments(id) ON DELETE RESTRICT,
    transaction_type TEXT NOT NULL,
    quantity_fixed_point BIGINT NOT NULL,
    price_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    gross_amount_minor BIGINT NOT NULL,
    fees_minor BIGINT NOT NULL DEFAULT 0,
    taxes_minor BIGINT NOT NULL DEFAULT 0,
    transaction_date DATE NOT NULL,
    linked_financial_transaction_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE investment_transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY investment_transactions_owner_policy ON investment_transactions FOR ALL USING (auth.uid()::text = owner_id);

CREATE TABLE IF NOT EXISTS sip_schedules (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    investment_account_id TEXT NOT NULL REFERENCES investment_accounts(id) ON DELETE CASCADE,
    instrument_id TEXT NOT NULL REFERENCES investment_instruments(id) ON DELETE RESTRICT,
    contribution_amount_minor BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    frequency TEXT NOT NULL,
    start_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE sip_schedules ENABLE ROW LEVEL SECURITY;
CREATE POLICY sip_schedules_owner_policy ON sip_schedules FOR ALL USING (auth.uid()::text = owner_id);

-- ============================================================================
-- 3. REMINDERS & SYNCHRONIZATION METADATA
-- ============================================================================

CREATE TABLE IF NOT EXISTS reminders (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    title TEXT NOT NULL,
    description TEXT,
    due_date TIMESTAMPTZ NOT NULL,
    category TEXT NOT NULL DEFAULT 'PERSONAL',
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    linked_entity_type TEXT,
    linked_entity_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE reminders ENABLE ROW LEVEL SECURITY;
CREATE POLICY reminders_owner_policy ON reminders FOR ALL USING (auth.uid()::text = owner_id);

CREATE TABLE IF NOT EXISTS sync_checkpoints (
    entity_type TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    last_sync_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_sync_revision BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE sync_checkpoints ENABLE ROW LEVEL SECURITY;
CREATE POLICY sync_checkpoints_owner_policy ON sync_checkpoints FOR ALL USING (auth.uid()::text = owner_id);

CREATE TABLE IF NOT EXISTS sync_tombstones (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT auth.uid()::text,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    deleted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE sync_tombstones ENABLE ROW LEVEL SECURITY;
CREATE POLICY sync_tombstones_owner_policy ON sync_tombstones FOR ALL USING (auth.uid()::text = owner_id);
