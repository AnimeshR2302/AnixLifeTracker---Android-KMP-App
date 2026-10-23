# AnixLifeTracker - Notification Engine Specification (Plan 8)

## 1. Overview & Delivery Policy

The Notification Engine handles in-app due state evaluation, financial reminder generation, and inbox insights.

> **CRITICAL RELEASE POLICY**: All outgoing system notifications, OS banners, browser pushes, remote alerts, email dispatch, and scheduled alarm background services are strictly **DISABLED** on all platforms.

---

## 2. Notification Delivery Gateway

Outgoing notification dispatches pass through `NotificationDeliveryGateway`:

- **Active Provider**: `DisabledNotificationDeliveryGateway`
- **Capability Status**: `isEnabled = false`, `statusExplanation = "Delivery is not available in this version"`
- **Result**: `NotificationDeliveryResult.Disabled("Delivery is not available in this version")`

---

## 3. Financial Ingestion vs. Outgoing Delivery

- **Incoming NotificationListenerService (Plan 6)**: Continues to collect financial transaction alerts on authorized Android devices.
- **Outgoing Notification Engine**: Inactive; setting switches in UI show explicit notices explaining that outgoing delivery is disabled.

---

## 4. In-App Inbox & Insights

Due state and budget/anomaly insights are stored locally and displayed in the shared Compose In-App Inbox when the application is open.

- **Budget Thresholds**: Crossing 80% limit generates a warning insight; exceeding limit generates a critical insight.
- **Spending Anomalies**: Transactions exceeding 2.5x baseline average produce an in-app insight with reason and deviation details.
