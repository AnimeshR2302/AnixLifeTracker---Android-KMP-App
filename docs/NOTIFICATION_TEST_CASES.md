# Notification Reading Test Cases & Fixtures

> **Suite**: `app/shared/test/NotificationParserTest.kt`  
> **Scope**: Unit, Integration, Deduplication, and Parsing Verification  

---

## 1. Unit Test Scenarios

### Test Case 1: UPI Debit Notification (Google Pay)
- **Input Title**: `Payment Successful`
- **Input Text**: `₹350 paid to Cafe Coffee Day using HDFC Bank •••• 4821. UPI Ref: 123456789012`
- **Package**: `com.google.android.apps.nfc.phone`
- **Expected Outcome**:
  - `status`: `HIGH_CONFIDENCE`
  - `amount`: `350.00 INR`
  - `direction`: `DEBIT`
  - `transactionType`: `EXPENSE`
  - `accountRefMasked`: `•••• 4821`
  - `institutionName`: `Google Pay`
  - `merchantName`: `Cafe Coffee Day`
  - `transactionReference`: `123456789012`

### Test Case 2: Money Received Notification (PhonePe)
- **Input Title**: `Money Received`
- **Input Text**: `You received ₹2,500 from Rahul. Credited to SBI Account •••• 9102. Ref: UTR998877.`
- **Package**: `com.phonepe.app`
- **Expected Outcome**:
  - `status`: `HIGH_CONFIDENCE`
  - `amount`: `2500.00 INR`
  - `direction`: `CREDIT`
  - `transactionType`: `INCOME`
  - `accountRefMasked`: `•••• 9102`
  - `institutionName`: `PhonePe`
  - `transactionReference`: `UTR998877`

### Test Case 3: Pending Payment Rejection
- **Input Title**: `Payment Pending`
- **Input Text**: `Your payment of ₹799 is being processed by your bank.`
- **Package**: `net.one97.paytm`
- **Expected Outcome**:
  - `status`: `REJECTED`
  - `rejectionReason`: Contains `"pending"`

### Test Case 4: Failed Transaction Rejection
- **Input Title**: `Payment Failed`
- **Input Text**: `₹1,000 could not be transferred due to insufficient funds.`
- **Package**: `com.phonepe.app`
- **Expected Outcome**:
  - `status`: `REJECTED`
  - `rejectionReason`: Contains `"failed"`

### Test Case 5: Refund Notification
- **Input Title**: `Refund Completed`
- **Input Text**: `Refund of ₹1,200.00 processed to ICICI Bank •••• 9102.`
- **Package**: `com.csam.icici.bank.imobile`
- **Expected Outcome**:
  - `status`: `HIGH_CONFIDENCE`
  - `amount`: `1200.00 INR`
  - `direction`: `CREDIT`
  - `transactionType`: `REFUND`
  - `institutionName`: `ICICI Bank`

---

## 2. Integration & Lifecycle Scenarios

1. **Access Revocation**: User revokes system notification access -> App detects `isAccessGranted = false` and shows `Access Required` status badge without crashing.
2. **Package Disabling**: User disables Google Pay in app selection dialog -> Google Pay notifications are rejected with `"Package not in allowlist"`.
3. **Cross-Source Match**: Transaction received via SMS with UTR `123456789012`, followed by Notification with same UTR -> Notification attaches secondary source provenance record to the existing transaction ID.
