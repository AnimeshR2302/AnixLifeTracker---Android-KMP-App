# AnixLifeTracker - Data Flow & Pipeline Architecture

## 1. Multi-Source Financial Ingestion Pipeline

Financial transactions are captured from multiple disparate channels and converted into normalized accounting records.

```mermaid
sequenceDiagram
    autonumber
    participant Source as Source (SMS/Notification/PDF/Manual)
    participant Extractor as Platform Extractor
    participant Normalizer as Normalization Engine
    participant Deduper as Deduplication Engine
    participant Categorizer as Rule & AI Categorizer
    participant DB as Room KMP Local DB
    participant UI as Active ViewModels & UI

    Source->>Extractor: Pass raw payload / string
    Extractor->>Normalizer: Extract RawTransactionRecord
    Normalizer->>Deduper: Compute deduplication hash
    Deduper->>DB: Query hash within time window (±10 mins)
    alt Transaction exists
        Deduper->>DB: Update provenance metadata (add secondary source)
    else New transaction
        Deduper->>Categorizer: Pass normalized record
        Categorizer->>Categorizer: Apply category rules / patterns
        Categorizer->>DB: Insert confirmed TransactionEntity
        DB-->>UI: Reactive Flow emits updated list
    end
```

### Deduplication Hash Strategy
To prevent duplicate records when an expense triggers both an SMS and a Push Notification, a deterministic deduplication hash is generated:

$$\text{Hash} = \text{SHA256}(\text{accountLast4} + \text{amountCents} + \text{transactionType} + \text{dateBucket(10 min)} + \text{normalizedMerchant})$$

If a hash match occurs within a 10-minute time window, the ingestion engine treats the new payload as a secondary source confirmation rather than a new transaction.

---

## 2. Investment & Portfolio Tracking Flow

The investment engine manages asset holdings, calculates return metrics, and projects future wealth trajectories.

```mermaid
flowchart TD
    subgraph Market Data Layer
        API[External Financial API / Nav Fetcher]
        Cache[Market Price Cache Table]
    end

    subgraph Ledger Layer
        TxTable[Investment Transactions Table]
        HoldingsTable[Holding Units & Balance Table]
    end

    subgraph Analytics Engine
        XIRR[XIRR & Absolute Return Calculator]
        Projections[Probabilistic Scenario Engine]
    end

    subgraph Presentation
        PortfolioUI[Portfolio Dashboard UI]
    end

    API -->|Fetch Latest NAV/Price| Cache
    TxTable -->|Aggregate Buy/Sell/SIP| HoldingsTable
    HoldingsTable & Cache -->|Compute Current Valuation| XIRR
    XIRR -->|Net Asset Value & CAGR| PortfolioUI
    HoldingsTable & XIRR -->|Generate Min/Likely/Max Trajectories| Projections
    Projections -->|Probabilistic Fan Chart| PortfolioUI
```

### Key Rules
- **Probabilistic Projections**: Projections are calculated as range distributions (e.g., 8% Bear, 12% Expected, 15% Bull) and presented with explicit disclosures that market returns are not guaranteed.
- **Exact Accounting**: Unit quantities and NAV prices are stored with 6-decimal precision (`unitsMicro: Long` or high-precision fixed point) to eliminate cumulative rounding drift.

---

## 3. Universal Reminder Engine Flow

Reminders operate across financial, travel, personal, and AI alert domains using a unified state flow.

```mermaid
sequenceDiagram
    autonumber
    participant System as Feature Module / User
    participant Repo as ReminderRepository
    participant DB as Local Database
    participant Scheduler as Platform Scheduler (WorkManager / AlarmManager)
    participant Notifier as Local Notification Presenter

    System->>Repo: Create/Update Reminder (Title, Date, Category, LinkedEntityRef)
    Repo->>DB: Insert/Update ReminderEntity
    Repo->>Scheduler: Schedule trigger time with OS
    Note over Scheduler: Time elapses to trigger time
    Scheduler->>Notifier: Present OS Local Notification
    Notifier->>User: Display alert with action buttons (Snooze / Complete)
    User->>Repo: Tap "Complete"
    Repo->>DB: Mark isCompleted = true
    DB-->>System: UI flow updates status
```

---

## 4. Sync Metadata & Offline Mutation Queue Flow

When cloud synchronization is enabled (Plan 4+), mutations follow an offline-first transactional queue:

```mermaid
flowchart LR
    A[User Action / Edit] --> B[Write to Local DB]
    B --> C[Append to SyncMutationQueue]
    C --> D{Is Network Online?}
    D -- Yes --> E[Process Mutation Queue via Ktor API]
    D -- No --> F[Retain Queue in DB]
    E -- Success --> G[Mark Mutation Executed]
    E -- Conflict --> H[Apply Conflict Resolution Rule]
    F -- Reconnected --> E
```

### Conflict Resolution Strategy
- **Client Wins for Local User Edits**: Direct manual edits by the primary user override background inferences.
- **Timestamp Versioning**: Entities track `lastModifiedTimestamp` and `version: Long`. Server reconciliation uses Last-Write-Wins (LWW) with server-side validation.
