# AnixLifeTracker - Source-Level Performance Improvements

## 1. Hotspot Optimizations

1. **Pre-filtering Notification & SMS Payloads**:
   - Package name allowlists, feature enablement, and OTP detection are evaluated **before** string concatenation or parser execution in `FinancialNotificationListenerService` and `FinancialSmsReceiver`.
2. **Room Database Indexing**:
   - Indexes added on `ownerId`, `accountId`, `categoryId`, `localTransactionDate`, `transactionType`, `occurredAt`, `syncStatus`, `sourceFingerprint`, `scheduledLocalDateTime`, and `packageName`.
3. **Paginated Transaction Queries**:
   - Large transaction ledger queries support limit/offset pagination (`observeTransactionsPaginated`) to maintain 60 FPS Compose rendering on low-end devices.
4. **Structured Thread Offloading**:
   - Database operations, SMS ingestion, regex parsing, and sync payloads execute on `Dispatchers.IO` / `Dispatchers.Default`. Compose UI code remains non-blocking.
5. **Single-Flight Work Coalescing**:
   - Token refreshes and sync executions run through `Mutex` single-flight coordinators, preventing redundant API calls during rapid network re-connections.
