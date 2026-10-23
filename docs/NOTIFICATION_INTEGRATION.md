# Android Financial Notification Integration

> **Module**: `app/shared/src/notification/` & `app/shared/src@android/notification/`  
> **Target Platform**: Android (Kotlin Multiplatform with graceful JVM / Web fallbacks)  
> **API**: Android `NotificationListenerService`  

---

## 1. Architecture Overview

AnixLifeTracker leverages the official Android `NotificationListenerService` API to intercept financial transactions from explicitly user-selected payment and banking applications (such as Google Pay, PhonePe, Paytm, CRED, HDFC, SBI, ICICI, and Axis Bank).

```text
Android System Notification Manager
                 |
                 v
FinancialNotificationListenerService (androidMain)
                 |
                 v
   In-App Allowlist Filter (User Consent & App Config)
                 |
                 v
    FinancialNotification Model
                 |
                 v
  Shared Deterministic Message Parser
                 |
                 v
   Normalized TransactionCandidate
                 |
                 v
Deduplication & Cross-Source Reference Matching (SMS + Notification)
                 |
                 v
NotificationRepositoryImpl -> Room Database (Version 4)
                 |
                 v
         Flow -> ViewModel -> Compose UI
```

---

## 2. Onboarding & User Consent Flow

Notification access is a special, user-granted system privacy setting (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`). Granting access at the system level **does not enable monitoring**.

In-app consent and allowlisting operate as follows:

1. **User Disclosure**: `NotificationConsentDialog` presents the scope of notification reading, strict local processing guarantee, and privacy safeguards.
2. **Settings Direct**: Clicking "Agree & Open Settings" launches `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
3. **Application Selection**: `NotificationAppSelectionDialog` displays installed supported financial applications. By default, **no app is monitored** until explicitly toggled ON by the user.
4. **Live Monitoring Toggle**: Users can globally pause or resume notification monitoring at any time.

---

## 3. Strict Security & Non-Financial Filtering

All notification payloads are treated as untrusted input.

- **Non-Financial Drop**: Discarded immediately without logging or storing.
- **Security & OTPs**: Messages containing OTPs, 2FA codes, login alerts, or PINs are rejected by `DeterministicFinancialMessageParser`.
- **Pending / In-Progress Payments**: Notifications indicating `"Payment Pending"`, `"is being processed"`, or collect requests are flagged as pending and **do not create confirmed transactions**.
- **Failed Payments**: Notifications indicating `"Payment Failed"` or `"Declined"` are rejected and discarded.
- **Local On-Device Execution**: Notification parsing is 100% on-device. Raw notification contents are never logged or transmitted over the network.

---

## 4. Source Fingerprinting & Deduplication

Each incoming notification generates a deterministic source fingerprint:

```kotlin
val prefix = if (sender.contains(".")) "NOTIF" else "SMS"
val rawKey = "$prefix:$sender:${timestampMillis / 60000}:$amountCents:${direction.name}:${accountRef ?: ""}:${referenceNo ?: ""}"
val fingerprint = "fp_${rawKey.hashCode()}"
```

### Cross-Source Deduplication (SMS + Notification)
When a transaction produces both a Bank SMS and a Payment App Notification (e.g. UPI Ref `123456789012`):
1. The parser extracts the transaction reference (`referenceNo`).
2. The repository checks if a transaction with matching `transactionReference` already exists in Room.
3. If matched, a new `TransactionSourceRecordEntity` (`sourceType = NOTIFICATION`) is attached to the existing transaction record without creating a duplicate expense or income.
