# AnixLifeTracker - Shared Reminders Specification (Plan 8)

## 1. Overview

AnixLifeTracker provides a universal shared reminder domain across Android, Desktop JVM, and Web. Reminders support confirmed financial obligations, recurring payments, and manual reminders.

---

## 2. Core Concepts & Data Structure

- **Reminder Definition**: High-level specification storing title, notes, source link (`OBLIGATION`, `RECURRING_PAYMENT`, `MANUAL`), category, priority, schedule version, and server revision.
- **Reminder Schedule**: Recurrence rules supporting `ONE_OFF`, `DAILY`, `WEEKLY`, `MONTHLY`, and `YEARLY` cadences, timezone policies (`Asia/Kolkata` default), and date-only policies (`LOCAL_DAY_END`).
- **Reminder Occurrence**: Deterministic instance ID (`${reminderId}_${scheduledLocalDate}`) capturing scheduled local datetime, status (`UPCOMING`, `DUE`, `OVERDUE`, `COMPLETED`, `SKIPPED`), and snooze time.
- **Occurrence Action**: Stable operation log tracking user mutations (`COMPLETE`, `SKIP`, `SNOOZE`, `DISMISS`, `UNDO`) with sync status.
- **In-App Inbox**: Dedicated UI feed for due dues, budget threshold warnings, and spending anomaly alerts.
- **Preferences**: In-app visibility, quiet hour preferences, timezone settings, and channel preferences.

---

## 3. Recurrence & Timezone Policies

1. **Monthly Anchors (29/30/31)**: Evaluates to the last valid day of shorter months (e.g., Feb 28/29, April 30) while preserving the original anchor day for subsequent months.
2. **Leap Years & DST**: Timezone calculations preserve local day boundaries without unexpected UTC offset shifts.
3. **Date-only Policy**: Date-only dues default to `LOCAL_DAY_END` (23:59:59 local time) to prevent premature overdue flagging.

---

## 4. Platform Capabilities

- **Android, Desktop, Web**: Shared Compose UI for managing reminders, occurrence actions, in-app inbox items, and preferences.
- **Outgoing Notifications**: All OS, browser, and remote notification delivery is explicitly **DISABLED** in this release across all platforms.
