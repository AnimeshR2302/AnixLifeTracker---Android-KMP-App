# AnixLifeTracker - SMS Test Cases Suite

## 1. Test Scenarios

| Test Case ID | Category | Sample Input Text | Expected Result |
| :--- | :--- | :--- | :--- |
| **TC-SMS-01** | HDFC Debit | `INR 1,250.00 debited from A/C XX1234 on 23-Oct-2026 at SWIGGY. UPI Ref 987654321.` | Amount: ₹1,250.00, DEBIT, SWIGGY, Ref: 987654321, HIGH_CONFIDENCE |
| **TC-SMS-02** | SBI NEFT Credit | `Your account XX5678 has been credited with INR 45,000.00 via NEFT on 15-Nov-2026.` | Amount: ₹45,000.00, CREDIT, INCOME, Ref extracted, HIGH_CONFIDENCE |
| **TC-SMS-03** | ICICI Refund | `Refund of INR 750.00 credited to account XX5678 for order 12345.` | Amount: ₹750.00, CREDIT, REFUND, HIGH_CONFIDENCE |
| **TC-SMS-04** | OTP Rejection | `Your OTP for HDFC Bank transaction is 482190. Do not share.` | REJECTED (OTP Filter) |
| **TC-SMS-05** | Failed Payment | `Your UPI payment of INR 500 to Swiggy failed.` | REJECTED (Failed Payment Filter) |
| **TC-SMS-06** | Marketing Loan | `Get a pre-approved personal loan up to Rs 500000. Click here.` | REJECTED (Promotional Filter) |
| **TC-SMS-07** | Balance Isolation | `A/C XX1234 debited by INR 250. Avail Bal INR 10,000.00.` | Amount: ₹250.00 (NOT ₹10,000.00), Bal: ₹10,000.00 |
