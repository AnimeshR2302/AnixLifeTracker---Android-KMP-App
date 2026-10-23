# Supported Financial Applications Directory

> **System**: AnixLifeTracker Notification & Payment App Directory  
> **Package Mapping**: Pre-configured profiles for Indian UPI, Wallet, Credit Card, and Bank Applications  

---

## 1. Supported Application Profiles

| Application Name | Package Name | Default Category Hint | Parser Profile ID |
| :--- | :--- | :--- | :--- |
| **Google Pay** | `com.google.android.apps.nfc.phone` | Wallet / Checking | `upi_gpay` |
| **PhonePe** | `com.phonepe.app` | Wallet / Checking | `upi_phonepe` |
| **Paytm** | `net.one97.paytm` | Wallet / Savings | `upi_paytm` |
| **CRED** | `com.dreamplug.android.cred` | Credit Card | `cc_cred` |
| **HDFC Bank MobileBanking** | `com.snapwork.hdfc` | Checking / Account | `bank_hdfc` |
| **SBI YONO** | `com.sbi.lotusintouch` | Savings / Account | `bank_sbi` |
| **ICICI iMobile** | `com.csam.icici.bank.imobile` | Checking / Account | `bank_icici` |
| **Axis Mobile** | `com.axis.mobile` | Checking / Account | `bank_axis` |
| **Kotak Mobile Banking** | `com.msf.ksecure` | Savings / Account | `bank_kotak` |
| **PNB One** | `com.pnb.one` | Savings / Account | `bank_pnb` |
| **Jupiter Money** | `me.jupiter.app` | Checking / Neobank | `neobank_jupiter` |
| **Slice** | `org.altruist.slice` | Credit Card | `cc_slice` |
| **Navi UPI** | `com.naviapp` | Wallet | `upi_navi` |
| **Amazon Pay** | `in.amazon.mShop.android.shopping` | Wallet | `upi_amazon` |

---

## 2. Parsing Patterns & Characteristics

### Google Pay (`com.google.android.apps.nfc.phone`)
- **Title**: `Paid ₹<amount> to <merchant>` or `Payment Successful`
- **Body**: `₹<amount> paid to <merchant> using <bank_name> •••• <last4>. UPI Ref: <12_digit_ref>`

### PhonePe (`com.phonepe.app`)
- **Title**: `Payment Successful` or `Money Received`
- **Body**: `Paid ₹<amount> to <merchant>. Debited from Bank Account •••• <last4>` or `You received ₹<amount> from <payer>`

### Paytm (`net.one97.paytm`)
- **Title**: `Paid ₹<amount>`
- **Body**: `Sent ₹<amount> to <merchant> from Paytm Wallet / <bank_name> •••• <last4>`

### CRED (`com.dreamplug.android.cred`)
- **Title**: `Bill Paid Successfully`
- **Body**: `₹<amount> paid towards <card_name> Credit Card`
