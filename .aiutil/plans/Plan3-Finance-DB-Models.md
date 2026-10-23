# TASK: Implement AnixLifeTracker Financial Database and Data Models

You are implementing Plan 3 of the AnixLifeTracker project.

Plans 1 and 2 have established:

- Application architecture.
- KMP project structure.
- Android, Desktop, and Web targets.
- Shared Compose Multiplatform UI.
- Navigation.
- Dependency injection.
- MVVM presentation architecture.

Before modifying code, read:

- docs/ARCHITECTURE.md
- docs/MODULE_STRUCTURE.md
- docs/TECH_STACK.md
- docs/AGENT_GUIDELINES.md
- docs/IMPLEMENTATION_STATUS.md

Do not recreate or unnecessarily modify functionality implemented in previous plans.

## 1. Objective

Implement a persistent local database for the financial and investment management features.

The database must support:

1. Financial accounts.
2. Income and expenses.
3. Financial transactions.
4. Transaction categories.
5. Budgets.
6. Recurring payments.
7. Upcoming expenses.
8. Investment accounts.
9. Investment holdings.
10. Investment transactions.
11. SIPs.
12. Market prices.
13. Financial goals.
14. Future synchronization.
15. Future automatic transaction ingestion.

The immediate objective is database implementation, repository access, and manual CRUD functionality.

Do not implement automatic SMS reading, bank integrations, or market API calls yet.

## 2. Database Technology

Use:

- Room 3.0.3 or a newer compatible stable 3.x release.
- Kotlin Multiplatform.
- SQLite.
- Kotlin Coroutines.
- Flow.
- KSP.
- Room schema export and migrations.

Use the Room 3 namespace:

`androidx.room3`

Do not mix Room 2 and Room 3 APIs.

Use the official Android Developers documentation for current KMP Room setup.

### Platform Storage

**Android**

Use a persistent application-private database location.

Configure the appropriate SQLite driver.

**Desktop**

Use a persistent, user-specific application data directory.

Do not store the production database in a temporary directory or project build directory.

Support Linux immediately, with portable directory resolution for Windows and macOS.

**Web**

Use Room 3's WebAssembly-compatible APIs.

Investigate and configure:

- `WebWorkerSQLiteDriver`.
- SQLite WebAssembly.
- Web Worker integration.
- Origin Private File System (OPFS).

The web driver requires a worker implementing the supported communication protocol.

Use the official AndroidX sample worker or a properly maintained implementation.

Inspect its licensing and integration requirements.

Test the actual web persistence behavior.

Do not assume adding the `sqlite-web` dependency alone completes the setup.

If OPFS or required browser capabilities are unavailable, display an explicit storage error. Do not silently claim that volatile in-memory data is persistent.

Do not store real financial information in browser storage until the required application security and privacy controls are established.

## 3. Financial Data Modeling Rules

This is a financial application, so data correctness is critical.

Follow these principles.

### Monetary Precision

Never use Float or Double for stored monetary amounts or financial accounting.

For currency amounts, use integer minor units where appropriate.

Example:

```kotlin
data class Money(
    val amountMinor: Long,
    val currencyCode: String
)
```

For INR:

₹125.50 is stored as 12550 paise.

Support currency metadata because different currencies have different minor-unit conventions.

For fractional investments and NAV values, use an exact decimal representation supporting sufficient precision.

Store investment units and prices using validated fixed-point decimal values or canonical decimal strings with explicit scale rules.

Do not use binary floating-point values for accounting calculations.

### Identifiers

Use stable, globally unique identifiers.

Prefer UUID-style identifiers that can be generated across all supported platforms.

Do not rely on database auto-increment IDs for future synchronization.

### Timestamps

Store transaction timestamps in a consistent UTC-based format.

Preserve:

- Original transaction timestamp.
- Relevant local calendar date.
- Source-provided timezone where available.
- Record creation timestamp.
- Last modification timestamp.

Do not treat a local calendar date as equivalent to a UTC timestamp.

### Data Ownership

Design every financial record for future association with a user and synchronization.

Do not depend on authentication being implemented yet.

Use stable local ownership identifiers and document how they will map to authenticated user identities in Plan 4.

## 4. Implement Financial Account Models

Create an Account model representing:

- Savings accounts.
- Current accounts.
- Cash.
- Wallets.
- Credit cards.
- Other financial accounts.

Recommended fields:

```text
id
ownerId
accountName
accountType
institutionName
accountReferenceMasked
currencyCode
openingBalanceMinor
balanceSnapshotMinor
balanceSnapshotTimestamp
isActive
createdAt
updatedAt
syncStatus
```

Use explicit account types.

Avoid storing complete account numbers unless necessary.

Do not store bank credentials, PINs, or card security codes.

Provide support for multiple accounts belonging to one user.

### Balance Handling

A transaction ledger and an externally reported balance are not necessarily equivalent.

Preserve balance snapshots independently.

Never assume that incomplete imported transaction history represents the complete current balance.

## 5. Implement Transaction Models

Create a Transaction model.

Recommended fields:

```text
id
ownerId
accountId
amountMinor
currencyCode
direction
transactionType
categoryId
merchantName
description
transactionReference
occurredAt
localTransactionDate
status
transferGroupId
sourceType
createdAt
updatedAt
deletedAt
syncStatus
```

Define appropriate enums or sealed models.

### Transaction Direction

- CREDIT
- DEBIT

### Transaction Type

- INCOME
- EXPENSE
- TRANSFER
- INVESTMENT
- REFUND
- FEE
- ADJUSTMENT

### Transaction Status

- PENDING
- POSTED
- REVERSED

### Transaction Source

- MANUAL
- SMS
- NOTIFICATION
- EMAIL
- BANK_STATEMENT
- BANK_API

Distinguish transaction direction from financial meaning.

Examples:

**Salary credit**
- Direction: CREDIT
- Type: INCOME

**Food purchase**
- Direction: DEBIT
- Type: EXPENSE

**Transfer to another personal account**
- Direction: DEBIT on source account
- Type: TRANSFER

**Credit-card bill payment**
- Type: TRANSFER when both accounts are tracked

**Mutual fund purchase**
- Direction: DEBIT from funding account
- Type: INVESTMENT

**Merchant refund**
- Direction: CREDIT
- Type: REFUND

Transfers and investment purchases must not inflate ordinary spending calculations.

Provide a mechanism to represent linked transfers and related financial records.

Use atomic database transactions when creating or modifying multiple related records.

## 6. Transaction Provenance and Duplicate Prevention

Different sources may report the same transaction.

Example:

- Bank SMS.
- PhonePe notification.
- Bank statement.

Design a separate TransactionSourceRecord or equivalent provenance entity.

It should support:

```text
id
transactionId
sourceType
sourceIdentifier
sourceFingerprint
receivedAt
processingStatus
createdAt
```

Allow multiple source records to reference a single transaction.

Implement idempotent insertion for repeated ingestion of the same source event using a suitable unique source identifier or fingerprint.

However, do not assume two transactions with the same amount and date are duplicates.

Detailed cross-source transaction matching will be implemented in Plan 8.

Keep this plan's deduplication limited to clearly identifiable repeated source events.

Avoid storing complete raw SMS messages or emails unnecessarily.

## 7. Implement Categories

Create TransactionCategory.

Recommended fields:

```text
id
ownerId
name
parentCategoryId
categoryType
iconIdentifier
isSystemCategory
isActive
createdAt
updatedAt
```

Support:

- Default categories.
- User-created categories.
- Parent and child categories.
- Income categories.
- Expense categories.

Initial categories may include:

**Income**
- Salary.
- Freelance.
- Interest.
- Dividend.
- Other income.

**Expenses**
- Food.
- Groceries.
- Shopping.
- Rent.
- Utilities.
- Transportation.
- Healthcare.
- Entertainment.
- Subscriptions.
- Other expenses.

Provide migration-safe identifiers for system categories.

Users must be able to change a transaction's category.

Deleting a category must not accidentally delete historical transactions.

## 8. Implement Budget Models

Create Budget.

Recommended fields:

```text
id
ownerId
name
categoryId
limitAmountMinor
currencyCode
periodType
startDate
endDate
isActive
createdAt
updatedAt
```

Support:

- Overall monthly budget.
- Category-wise budget.
- Custom date ranges.

Design repository queries for:

- Budget limits.
- Actual expenses.
- Remaining budget.
- Budget utilization.

Do not implement advanced budget analysis yet.

That belongs to Plans 9 and 10.

## 9. Implement Recurring Payments

Create RecurringPayment.

Recommended fields:

```text
id
ownerId
accountId
name
categoryId
expectedAmountMinor
currencyCode
frequency
startDate
nextDueDate
endDate
isVariableAmount
isActive
createdAt
updatedAt
```

Support frequencies such as:

- Daily.
- Weekly.
- Monthly.
- Quarterly.
- Yearly.
- Custom schedule.

Examples:

- Rent.
- Electricity.
- Internet.
- Credit-card payment.
- Insurance.
- Subscriptions.
- EMI.

Design a model that distinguishes a recurring payment rule from the actual transaction created when payment occurs.

Do not automatically create a confirmed financial transaction just because a recurring due date arrives.

## 10. Implement Upcoming Financial Obligations

Create a model for expected future cash outflows and inflows.

Required capabilities:

- Expected amount.
- Expected payment date.
- Related account.
- Related recurring rule.
- Payment status.
- Optional linked actual transaction.

Future analysis must be able to distinguish:

1. Confirmed expenses.
2. Scheduled expenses.
3. Predicted expenses.

Predictions must not be stored as confirmed transactions.

## 11. Implement Investment Models

Investments require separate accounting models.

Do not represent every investment as an ordinary expense.

### 11.1 InvestmentAccount

Supports:

- Broker accounts.
- Mutual fund accounts.
- Other investment platforms.

Fields should include:

```text
id
ownerId
name
provider
accountType
currencyCode
createdAt
updatedAt
```

### 11.2 InvestmentInstrument

Represents an investable asset.

Fields should include:

```text
id
instrumentType
name
symbol
isin
amfiCode
exchange
currencyCode
isActive
```

Support types:

- MUTUAL_FUND
- STOCK
- ETF
- GOLD
- BOND
- OTHER

Do not force fixed deposits and recurring deposits into market-price-based asset valuation.

Create suitable deposit-specific models for those products, including principal, interest rate, contribution schedule, and maturity.

### 11.3 InvestmentTransaction

Fields should include:

```text
id
ownerId
investmentAccountId
instrumentId
transactionType
quantity
price
grossAmount
fees
taxes
transactionDate
linkedFinancialTransactionId
createdAt
updatedAt
```

Support:

- BUY
- SELL
- DIVIDEND
- REINVESTMENT
- CORPORATE_ACTION
- ADJUSTMENT

Store quantities and monetary values with appropriate exact precision.

### 11.4 InvestmentHolding

Represent current holdings.

Design holdings to be derived from investment transactions where possible.

Do not maintain multiple conflicting authoritative representations of the same holdings.

Support:

- Total units.
- Average acquisition cost.
- Current value.
- Unrealized gains.
- Realized gains where applicable.

Calculated fields can be produced by repositories or use cases rather than redundantly stored.

### 11.5 SIP

Create SIPSchedule.

Required fields:

```text
id
ownerId
investmentAccountId
instrumentId
contributionAmountMinor
currencyCode
frequency
startDate
nextDueDate
endDate
isActive
createdAt
updatedAt
```

Support multiple SIPs for a single investment instrument.

Keep scheduled SIP contributions separate from confirmed investment purchases.

### 11.6 MarketPrice

Create a historical price model.

Support:

```text
instrumentId
priceDate
price
currencyCode
source
fetchedAt
```

Include appropriate unique constraints and indexes.

Handle duplicate updates for the same instrument, date, and source.

### 11.7 Financial Goals

Create FinancialGoal.

Fields should support:

- Goal name.
- Target amount.
- Currency.
- Target date.
- Associated investment accounts.
- Current progress.
- Active status.

Detailed forecasting will be implemented later.

## 12. Reminder and Alert Models

Create minimal persistent models for:

- General reminders.
- Financial reminders.
- Investment alerts.

Avoid coupling ReminderEntity exclusively to transactions.

Use an extensible reference mechanism so a reminder can relate to:

- A transaction.
- A recurring payment.
- A SIP.
- An investment.
- A travel item.
- An event.

Do not implement scheduling or system notifications yet.

That belongs to Plan 15.

## 13. Database Relationships and Indexing

Implement appropriate:

- Primary keys.
- Foreign keys.
- Unique constraints.
- Indexes.
- Cascade or restricted deletion behavior.

Prioritize queries involving:

- Account ID.
- User ownership.
- Transaction date.
- Category ID.
- Transaction type.
- Investment instrument.
- SIP due date.
- Reminder due date.
- External source identifiers.

Do not use cascading deletion when doing so could accidentally erase historical financial records.

Use soft deletion where appropriate for synchronization and auditability.

Implement database operations atomically where multiple records must remain consistent.

## 14. Create Room Database

Create an AppDatabase class in the approved database module.

Use:

- Room entities.
- DAOs.
- Versioned schemas.
- Platform-specific database builders.
- Room 3-compatible SQLite drivers.

Export and commit database schema definitions.

Begin with database version 1.

Do not use destructive migration as a routine upgrade strategy.

Document the approach for future migrations.

## 15. Implement DAOs

Create DAOs for the major entities.

Required operations include:

- Insert.
- Update.
- Delete or soft-delete.
- Get by ID.
- Get all.
- Filter by account.
- Filter by date range.
- Filter by category.
- Filter by transaction type.
- Observe records through Flow.

Use suspend functions for database mutations.

Use reactive Flow queries where ongoing observation is required.

Avoid exposing Room entities directly to the presentation layer.

## 16. Repository Layer

Implement repositories according to the architecture established in Plan 1.

Examples:

```kotlin
interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>

    suspend fun getAccount(id: String): Account?

    suspend fun addAccount(account: Account)

    suspend fun updateAccount(account: Account)

    suspend fun deleteAccount(id: String)
}
```

```kotlin
interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    fun observeTransactionsByAccount(
        accountId: String
    ): Flow<List<Transaction>>

    suspend fun addTransaction(
        transaction: Transaction
    )

    suspend fun updateTransaction(
        transaction: Transaction
    )

    suspend fun deleteTransaction(id: String)
}
```

Implement equivalent contracts for:

- Categories.
- Budgets.
- Recurring payments.
- Investment accounts.
- Investment transactions.
- SIP schedules.
- Market prices.
- Reminders.

Use explicit mapping between:

Room Entity ↔ Domain Model

Do not expose SQL or Room implementation details to ViewModels.

## 17. Implement Domain Use Cases

Create fundamental use cases as needed:

- AddAccountUseCase.
- GetAccountsUseCase.
- AddTransactionUseCase.
- UpdateTransactionUseCase.
- DeleteTransactionUseCase.
- GetTransactionsUseCase.
- GetTransactionsByDateUseCase.
- GetTransactionsByCategoryUseCase.
- AddBudgetUseCase.
- GetBudgetsUseCase.
- AddInvestmentUseCase.
- GetInvestmentsUseCase.
- AddSIPUseCase.
- GetSIPsUseCase.

Avoid creating trivial pass-through use cases with no meaningful abstraction unless required by the existing architecture.

Place business validation in the domain layer.

Examples:

- Amount must satisfy transaction rules.
- Currency must be valid.
- Referenced account must exist.
- Investment quantity must be valid.
- SIP contribution must be positive.
- Transaction date must be valid.

## 18. Connect Database to Existing UI

Replace the demonstration transaction data introduced in Plan 2 with repository-backed data.

Implement a functional manual transaction flow:

1. User opens Finances.
2. User selects Add Transaction.
3. Transaction form opens.
4. User selects account.
5. User enters amount.
6. User selects income or expense.
7. User selects category.
8. User enters optional description.
9. User selects transaction date.
10. User saves the transaction.
11. Repository persists the record.
12. UI automatically refreshes.

Implement basic account creation so users can assign transactions to accounts.

Support editing and deleting manually entered transactions.

Display confirmation before destructive user actions.

Provide correct empty states when no financial data exists.

Do not display fabricated balances or portfolio returns.

Investment and SIP repositories must be implemented and tested, but full investment UI functionality will be added in Plan 11.

## 19. Dependency Injection

Register:

- Database instance.
- DAOs.
- Repository implementations.
- Use cases.
- Required ViewModels.

Use constructor injection.

Handle platform-specific database initialization through separate implementations.

Keep business logic independent of platform context.

Ensure database initialization failures are surfaced as meaningful application errors.

## 20. Testing Requirements

Create tests for:

### Database

- Insert transaction.
- Update transaction.
- Delete transaction.
- Read transaction.
- Filter transactions.
- Observe Flow updates.
- Persist account information.
- Persist investment information.
- Handle foreign-key constraints.

### Financial Correctness

- Exact monetary precision.
- Negative amount validation.
- Currency validation.
- Transfer accounting.
- Refund handling.
- Investment contribution classification.
- Credit-card payment classification.
- Duplicate source-event insertion.
- Transactions with identical amounts and dates that are actually distinct.

### Data Integrity

- Stable identifiers.
- Transaction relationships.
- Atomic updates.
- Referential integrity.
- Safe deletion.
- Schema validation.

### Platform Persistence

Verify on Android, Desktop, and Web:

1. Create a financial account.
2. Add a transaction.
3. Close or reload the application.
4. Reopen it.
5. Confirm the data remains available.
6. Edit the transaction.
7. Confirm the updated data persists.

For Web, explicitly verify OPFS persistence and database worker initialization in a supported browser.

Do not claim tests passed without executing them.

## 21. Security Restrictions

- Do not store passwords or bank PINs.
- Do not store unnecessary complete bank account numbers.
- Do not log sensitive transaction details.
- Do not use actual user financial records as test fixtures.
- Do not assume local databases are encrypted automatically.
- Do not store API keys directly in code.
- Do not introduce plaintext cloud backups.
- Do not upload any financial data to external services.

Use synthetic test data.

Document local-storage security limitations, especially for Web.

If production-grade storage encryption cannot be completed in this plan, document it as a required security task before real financial data is used.

## 22. Documentation

Update:

- docs/ARCHITECTURE.md
- docs/MODULE_STRUCTURE.md
- docs/IMPLEMENTATION_STATUS.md

Create:

```text
docs/DATABASE_SCHEMA.md
docs/DATABASE_MIGRATIONS.md
docs/FINANCIAL_MODELS.md
```

Include:

- Entity definitions.
- Relationship diagrams.
- Database constraints.
- Indexes.
- Repository APIs.
- Monetary precision rules.
- Transaction classifications.
- Data migration strategy.
- Platform persistence details.

## 23. Acceptance Criteria

Plan 3 is complete when:

1. Room 3 is configured correctly.
2. Shared data models compile across selected targets.
3. Database storage works on Android.
4. Database storage works on Desktop.
5. Database storage works on Web.
6. CRUD operations work.
7. Financial repositories are implemented.
8. Investment repositories are implemented.
9. Relevant domain use cases are implemented.
10. Finances UI displays real locally stored transactions.
11. Manual transactions can be created, edited, and deleted.
12. Database persistence survives application restarts.
13. Financial data classifications are correct.
14. Tests pass.
15. Database documentation is updated.

If a platform cannot be verified, explicitly state the reason and leave its acceptance criterion incomplete.

## 24. Final Implementation Report

After completing the task, provide:

1. Created and modified files.
2. Database entities implemented.
3. Repository interfaces and implementations.
4. DAOs implemented.
5. UI integration changes.
6. Database schema version.
7. Platform-specific storage implementation.
8. Build and test results.
9. Outstanding security considerations.
10. Remaining work for Plan 4.

Update `IMPLEMENTATION_STATUS.md`.

**Completion condition:** AnixLifeTracker has a functional, persistent, multiplatform financial data layer ready for automatic transaction ingestion, financial analytics, investment tracking, and future cloud synchronization.