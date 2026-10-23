# AnixLifeTracker - Offline-First Cross-Platform Synchronization Contract

## 1. Overview

AnixLifeTracker implements a durable, offline-first synchronization contract between **Android**, **Desktop (JVM)**, and **Web (WasmJS)** using the authenticated backend exchange point. All financial entities, reminder definitions, schedules, occurrence actions, inbox items, and preferences follow this unified sync contract.

---

## 2. Revised Synchronization Trigger Matrix

| Trigger Event | Android Action | Desktop / Web Action |
| :--- | :--- | :--- |
| **New SMS / Notification / Local Edit / Reminder Action** | Persist/process locally; enqueue normalized changes; **do not upload** | No automatic delivery or refresh |
| **App Open / Foreground Resume** | **Pull-only**: Automatically retrieve backend updates; preserve pending local Android changes without uploading | Use cached data; retrieve latest data through Sync |
| **Sync Button Click** | **Explicit Sync**: Publish eligible local changes, then retrieve latest backend snapshot/deltas | Publish queued local edits, then retrieve latest backend snapshot/deltas |
| **Desktop / Web Save** | Not applicable | Persist locally; attempt immediate authenticated backend write; show Pending if offline/failure |
| **Reconnect** | Do not trigger Android upload | Retry pending desktop/web user saves while app is open; no automatic retrieval of Android data |
| **Background / Timer / Socket** | **No financial or reminder upload, polling or live subscription** | No polling, live subscription or background refresh |

---

## 3. Platform Sync Contract Guidelines

1. **Explicit Sync Reasons**: `ANDROID_FOREGROUND`, `USER_SYNC`, `REMOTE_USER_SAVE`, `CONNECTIVITY_RESTORE`.
2. **Pull-Only vs Explicit Sync**:
   - `pullOnly`: Pulls remote updates without pushing unsent phone records.
   - `syncExplicit`: Pushes queued local edits conditionally, then pulls committed remote deltas.
3. **Data Freshness Copy**: The UI explicitly displays:  
   `"Last Android upload: <timestamp>; newer phone records require Sync on Android."`
4. **Reminder Sync Payloads**: Includes stable reminder definitions, schedules, occurrence actions, in-app inbox states, and preferences. Local permission states (e.g. Android NotificationListener permission) are strictly excluded from sync.
5. **Exclusion of Raw Text & OTPs**: Raw SMS body, notification text, security OTPs, and platform permission settings are strictly excluded from sync payloads.
6. **Disabled Background Auto-Uploads**: WorkManager periodic upload loops and background auto-polling left from earlier plans are disabled.
