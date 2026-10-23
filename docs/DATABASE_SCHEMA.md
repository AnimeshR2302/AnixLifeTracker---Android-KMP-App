# AnixLifeTracker - Database Schema Specification (v6)

## 1. Overview

AnixLifeTracker uses **Room 3 KMP** with SQLite to provide offline-first, multiplatform persistent storage across Android, Desktop (JVM), and Web (WasmJS).

- **Database Name**: `anix_life_tracker.db`
- **Current Version**: `6`
- **Package**: `com.anix.android.anixlifetracker.database`

---

## 2. Table Schemas

### 2.1 `accounts`
Stores financial accounts (Checking, Savings, Credit Cards, Wallets, Cash).

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Stable UUID |
| `ownerId` | `TEXT` | `NOT NULL` | Local user ownership ID |
| `accountName` | `TEXT` | `NOT NULL` | Account display name |
| `accountType` | `TEXT` | `NOT NULL` | Enum: `SAVINGS`, `CHECKING`, `CREDIT_CARD`, `CASH`, `WALLET`, etc. |
| `institutionName` | `TEXT` | `NOT NULL` | Bank/Institution name |
| `accountReferenceMasked` | `TEXT` | `NOT NULL` | Masked reference (e.g. `•••• 4821`) |
| `currencyCode` | `TEXT` | `NOT NULL` | ISO currency code (default: `INR`) |
| `openingBalanceMinor` | `INTEGER` | `NOT NULL` | Minor monetary units (e.g. paise/cents) |
| `balanceSnapshotMinor` | `INTEGER` | `NOT NULL` | Latest balance in minor units |
| `balanceSnapshotTimestamp` | `TEXT` | `NOT NULL` | UTC ISO timestamp |
| `isObservedBalance` | `INTEGER` | `NOT NULL` | 1 if directly observed from alert/statement, 0 if derived |
| `lastObservationTimestamp` | `TEXT` | `NULLABLE` | Last observed balance timestamp |
| `isActive` | `INTEGER` | `NOT NULL` | Active status (1/0) |
| `createdAt` | `TEXT` | `NOT NULL` | Record creation timestamp |
| `updatedAt` | `TEXT` | `NOT NULL` | Record update timestamp |
| `syncStatus` | `TEXT` | `NOT NULL` | Enum: `LOCAL_ONLY`, `SYNCED`, `PENDING_SYNC` |

Indexes: `ownerId`, `accountType`.

---

### 2.2 `reminders` (Definitions)
Stores universal reminder definitions.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Stable ID |
| `ownerId` | `TEXT` | `NOT NULL` | Ownership ID |
| `title` | `TEXT` | `NOT NULL` | Reminder title |
| `description` | `TEXT` | `NULLABLE` | Notes / Description |
| `dueDate` | `TEXT` | `NOT NULL` | Scheduled date string |
| `category` | `TEXT` | `NOT NULL` | `PAYMENT`, `SIP`, `PERSONAL`, `TRAVEL`, `EVENT` |
| `priority` | `TEXT` | `NOT NULL` | `LOW`, `MEDIUM`, `HIGH` |
| `isCompleted` | `INTEGER` | `NOT NULL` | Completion flag |
| `linkedEntityType` | `TEXT` | `NULLABLE` | Source entity type |
| `linkedEntityId` | `TEXT` | `NULLABLE` | Source entity ID |
| `createdAt` | `TEXT` | `NOT NULL` | Creation timestamp |
| `updatedAt` | `TEXT` | `NOT NULL` | Update timestamp |
| `sourceType` | `TEXT` | `NOT NULL` | `MANUAL`, `OBLIGATION`, `RECURRING_PAYMENT`, etc. |
| `isEnabled` | `INTEGER` | `NOT NULL` | Enabled flag |
| `scheduleVersion` | `INTEGER` | `NOT NULL` | Version integer |
| `serverRevision` | `INTEGER` | `NOT NULL` | Backend revision |
| `syncStatus` | `TEXT` | `NOT NULL` | Sync status |

---

### 2.3 `reminder_schedules`
Stores recurrence rules associated with reminder definitions.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Schedule ID |
| `reminderId` | `TEXT` | `NOT NULL, FK -> reminders(id) CASCADE` | Foreign key to definition |
| `isRecurring` | `INTEGER` | `NOT NULL` | 1 if recurring |
| `localDueDateOrTime` | `TEXT` | `NOT NULL` | Due date or local ISO time |
| `ianaTimezone` | `TEXT` | `NOT NULL` | IANA Timezone string |
| `dateOnlyPolicy` | `TEXT` | `NOT NULL` | `EXACT_TIME`, `LOCAL_DAY_END`, `LOCAL_DAY_START` |
| `cadence` | `TEXT` | `NOT NULL` | `ONE_OFF`, `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` |
| `anchorDay` | `INTEGER` | `NULLABLE` | Anchor day for monthly cadence |
| `endDate` | `TEXT` | `NULLABLE` | End date bound |
| `endCount` | `INTEGER` | `NULLABLE` | End count bound |
| `leadTimesDays` | `INTEGER` | `NOT NULL` | Lead time in days |
| `missedDatePolicy` | `TEXT` | `NOT NULL` | `SKIP_MISSED`, `GENERATE_LATEST`, `GENERATE_ALL` |
| `scheduleVersion` | `INTEGER` | `NOT NULL` | Version integer |
| `updatedAt` | `TEXT` | `NOT NULL` | Update timestamp |

---

### 2.4 `reminder_occurrences`
Stores calculated occurrences derived from reminder schedules.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Deterministic occurrence ID |
| `reminderId` | `TEXT` | `NOT NULL` | Reminder definition ID |
| `ownerId` | `TEXT` | `NOT NULL` | Owner ID |
| `scheduleVersion` | `INTEGER` | `NOT NULL` | Schedule version |
| `scheduledLocalDateTime` | `TEXT` | `NOT NULL` | Scheduled datetime string |
| `resolvedInstant` | `TEXT` | `NOT NULL` | Resolved instant ISO |
| `state` | `TEXT` | `NOT NULL` | `UPCOMING`, `DUE`, `OVERDUE`, `COMPLETED`, `SKIPPED`, `CANCELLED` |
| `snoozeUntilISO` | `TEXT` | `NULLABLE` | Snooze until ISO timestamp |
| `amountMinor` | `INTEGER` | `NULLABLE` | Optional amount in minor units |
| `currencyCode` | `TEXT` | `NULLABLE` | ISO currency code |
| `createdAt` | `TEXT` | `NOT NULL` | Creation timestamp |
| `updatedAt` | `TEXT` | `NOT NULL` | Update timestamp |
| `syncStatus` | `TEXT` | `NOT NULL` | Sync status |

---

### 2.5 `reminder_actions`
Stores user action logs targeting reminder occurrences.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Stable operation ID |
| `occurrenceId` | `TEXT` | `NOT NULL` | Target occurrence ID |
| `reminderId` | `TEXT` | `NOT NULL` | Reminder definition ID |
| `ownerId` | `TEXT` | `NOT NULL` | Owner ID |
| `actionType` | `TEXT` | `NOT NULL` | `COMPLETE`, `SKIP`, `SNOOZE`, `DISMISS`, `UNDO` |
| `effectiveTimeISO` | `TEXT` | `NOT NULL` | Effective timestamp |
| `baseRevision` | `INTEGER` | `NOT NULL` | Base schedule revision |
| `syncStatus` | `TEXT` | `NOT NULL` | Sync status |
| `conflictState` | `TEXT` | `NULLABLE` | Conflict state details |

---

### 2.6 `in_app_inbox_items`
Stores in-app inbox items for due dues, budget warnings, and spending anomaly alerts.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `PRIMARY KEY` | Inbox item ID |
| `ownerId` | `TEXT` | `NOT NULL` | Owner ID |
| `reminderId` | `TEXT` | `NULLABLE` | Linked reminder ID |
| `occurrenceId` | `TEXT` | `NULLABLE` | Linked occurrence ID |
| `linkedEntityType` | `TEXT` | `NULLABLE` | Linked source entity type |
| `linkedEntityId` | `TEXT` | `NULLABLE` | Linked source entity ID |
| `title` | `TEXT` | `NOT NULL` | Display title |
| `subtitle` | `TEXT` | `NOT NULL` | Subtitle / Reason |
| `severity` | `TEXT` | `NOT NULL` | `INFO`, `WARNING`, `CRITICAL` |
| `reason` | `TEXT` | `NOT NULL` | Reason explanation |
| `sourceRevision` | `INTEGER` | `NOT NULL` | Source revision |
| `freshnessISO` | `TEXT` | `NOT NULL` | Freshness ISO timestamp |
| `isRead` | `INTEGER` | `NOT NULL` | Read status |
| `isDismissed` | `INTEGER` | `NOT NULL` | Dismissed status |
| `isInvalidated` | `INTEGER` | `NOT NULL` | Invalidated status |
| `createdAt` | `TEXT` | `NOT NULL` | Creation timestamp |
| `updatedAt` | `TEXT` | `NOT NULL` | Update timestamp |

---

### 2.7 `reminder_preferences`
Stores user notification and display preferences.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `ownerId` | `TEXT` | `PRIMARY KEY` | Owner ID |
| `inAppVisibility` | `INTEGER` | `NOT NULL` | In-app visibility toggle |
| `quietHoursStart` | `TEXT` | `NULLABLE` | Quiet hours start string |
| `quietHoursEnd` | `TEXT` | `NULLABLE` | Quiet hours end string |
| `userTimezone` | `TEXT` | `NOT NULL` | User IANA timezone |
| `displaySettingsJson` | `TEXT` | `NOT NULL` | Display settings JSON |
| `enabledChannelsJson` | `TEXT` | `NOT NULL` | Enabled channels JSON |
| `updatedAt` | `TEXT` | `NOT NULL` | Update timestamp |
