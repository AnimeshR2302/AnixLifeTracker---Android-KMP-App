# AnixLifeTracker - Permission Hygiene & Capability Management

## 1. Coherent Capability Architecture

Permission states and system capabilities are governed by `PermissionAndCapabilityManager`, which models platform capabilities as distinct states:
- `AVAILABLE`: OS permission granted, in-app consent active, feature enabled, key vault unlocked.
- `PERMISSION_DENIED`: Required Android OS runtime permission denied by user.
- `CONSENT_WITHDRAWN`: OS permission granted, but in-app consent toggled off by user.
- `FEATURE_DISABLED`: In-app feature toggle turned off.
- `KEY_LOCKED`: Encryption key vault locked or unavailable.
- `PLATFORM_UNSUPPORTED`: Non-Android platforms (JVM Desktop, WasmJS Web) where automated OS ingestion is unavailable.

---

## 2. Android Runtime Permissions

1. **`RECEIVE_SMS` / `READ_SMS`**:
   - Used exclusively in restricted Android SMS variant for automated bank transaction parsing.
   - Evaluated on app open and foreground resume.
   - Denial does **not** break manual financial entry, analytics, or viewing saved offline data.

2. **`BIND_NOTIFICATION_LISTENER_SERVICE`**:
   - Used by `FinancialNotificationListenerService` to capture payment alerts from user-authorized apps.
   - Requires explicit Android Notification Access permission + in-app consent + app allowlist selection.

3. **`POST_NOTIFICATIONS`**:
   - Strictly reserved for local reminder alerts. All outgoing push notifications remain disabled in current version.

---

## 3. Non-Android Platform Handling

- **Desktop (JVM) & Web (WasmJS)**:
  - Do not display fake SMS or Notification Listener permission dialogs.
  - Display informative notice: *"Automated SMS & notification financial collection originates on Android devices. Collected transactions sync after Android Sync."*
