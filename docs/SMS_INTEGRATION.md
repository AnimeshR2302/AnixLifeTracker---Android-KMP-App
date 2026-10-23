# AnixLifeTracker - SMS Integration Specification

## 1. Overview

AnixLifeTracker provides automated financial transaction collection through Android SMS parsing while maintaining privacy, KMP platform independence, and Google Play policy compliance.

---

## 2. Architecture & Data Flow

```text
Android Telephony SMS Provider / BroadcastReceiver
                    |
                    v
              SmsCollector
                    |
                    v
     FinancialMessageParser (Deterministic)
                    |
                    v
         TransactionCandidate
                    |
                    v
      SmsRepository (Deduplication)
      /                          \
(High Confidence & Auto-Save)   (Needs Review or Unlinked Account)
    /                              \
Room Transactions Table     Room SMS Review Queue Table
```

### Component Breakdown
1. **`SmsCollector` (KMP Expect/Actual)**:
   - On Android: Queries `Telephony.Sms.CONTENT_URI` (`content://sms/inbox`) for historical import or processes incoming broadcasts.
   - On Desktop/Web: Returns empty results gracefully without throwing exceptions.
2. **`FinancialSmsReceiver` (Android BroadcastReceiver)**:
   - Listens for `android.provider.Telephony.SMS_RECEIVED`.
   - Reconstructs multipart SMS segments and delegates parsing off the main thread.
3. **`DeterministicFinancialMessageParser`**:
   - Parses monetary amounts, debit/credit directions, accounts, institutions, merchants, and transaction references using deterministic regex rules.
4. **`SmsRepository`**:
   - Deduplicates using `sourceFingerprint` against `transaction_source_records` and `sms_review_candidates`.
   - Matches masked account references against active accounts.
   - Saves confirmed records to `transactions` or queues candidates in `sms_review_candidates`.

---

## 3. Google Play Permission Strategy

- **`READ_SMS` & `RECEIVE_SMS`**: Declared with `android.hardware.telephony` (`required="false"`).
- Runtime permission checks guard all collection flows (`isSmsPermissionGranted()`).
- Unavailability of SMS permissions or sideload restrictions fail gracefully without crashing the app.
