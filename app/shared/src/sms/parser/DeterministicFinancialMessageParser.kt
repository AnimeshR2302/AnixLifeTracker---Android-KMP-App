package com.anix.android.anixlifetracker.sms.parser

import com.anix.android.anixlifetracker.model.*
import kotlin.time.Clock
import kotlinx.datetime.Instant

/**
 * Deterministic regex and rule-based parser for Indian financial SMS
 */
class DeterministicFinancialMessageParser : FinancialMessageParser {

    override fun parse(message: FinancialMessage): TransactionParseResult {
        val text = message.body.trim()
        val lowerText = text.lowercase()

        // 1. Non-Financial / Reject Filters
        if (isOtpOrSecurityMessage(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.REJECTED,
                rejectionReason = "OTP or 2FA security code"
            )
        }

        if (isPromotionalOrLoan(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.REJECTED,
                rejectionReason = "Marketing or promotional loan message"
            )
        }

        if (isFailedTransaction(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.REJECTED,
                rejectionReason = "Failed or declined transaction"
            )
        }

        if (isPendingTransaction(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.REJECTED,
                rejectionReason = "Pending or processing payment request"
            )
        }

        if (isPaymentRequest(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.REJECTED,
                rejectionReason = "Incomplete collect or payment request"
            )
        }

        // 2. Financial Keywords Check
        if (!hasFinancialKeywords(lowerText)) {
            return TransactionParseResult(
                status = ParseConfidence.UNRECOGNIZED,
                rejectionReason = "No financial transaction keywords detected"
            )
        }

        // 3. Extract Available Balance (so it is separated and not confused with transaction amount)
        val availableBalance = extractAvailableBalance(text)

        // 4. Extract Primary Transaction Amount
        val transactionAmount = extractTransactionAmount(text, availableBalance)
            ?: return TransactionParseResult(
                status = ParseConfidence.UNRECOGNIZED,
                rejectionReason = "Could not parse monetary transaction amount"
            )

        // 5. Determine Direction and Transaction Type
        val direction = determineDirection(lowerText)
        val isRefundOrReversal = isRefund(lowerText)
        val transactionType = when {
            isRefundOrReversal -> TransactionType.REFUND
            lowerText.contains("sip") || lowerText.contains("mutual fund") -> TransactionType.INVESTMENT
            direction == TransactionDirection.CREDIT -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 6. Extract Account Reference & Institution
        val accountRef = extractAccountReference(text)
        val institution = extractInstitutionName(message.sender, text)

        // 7. Extract Merchant / Counterparty
        val merchant = extractMerchant(text)

        // 8. Extract Reference Number (UTR / UPI Ref)
        val referenceNo = extractReferenceNumber(text)

        // 9. Date Extraction or Fallback
        val occurredAtIso = extractDateOrFallback(text, message.timestampMillis)
        val localDate = occurredAtIso.substringBefore("T")

        // 10. Generate Source Fingerprint for Deduplication
        val rawSnippet = if (text.length > 200) text.take(200) + "..." else text
        val fingerprint = generateSourceFingerprint(
            sender = message.sender,
            timestampMillis = message.timestampMillis,
            amountCents = transactionAmount.amountInCents,
            direction = direction,
            accountRef = accountRef,
            referenceNo = referenceNo
        )

        // 11. Calculate Confidence Score
        val confidence = calculateConfidence(
            hasAccount = accountRef != null,
            hasMerchant = merchant != null,
            hasRef = referenceNo != null,
            isRefund = isRefundOrReversal
        )

        val parseStatus = when {
            confidence >= 0.85 -> ParseConfidence.HIGH_CONFIDENCE
            confidence >= 0.50 -> ParseConfidence.NEEDS_REVIEW
            else -> ParseConfidence.UNRECOGNIZED
        }

        val candidate = TransactionCandidate(
            id = "cand_${message.id}_${Clock.System.now().toEpochMilliseconds()}",
            amount = transactionAmount,
            direction = direction,
            transactionType = transactionType,
            accountRefMasked = accountRef,
            institutionName = institution,
            merchantName = merchant,
            description = buildDescription(transactionType, merchant, institution, accountRef),
            transactionReference = referenceNo,
            occurredAt = occurredAtIso,
            localTransactionDate = localDate,
            availableBalance = availableBalance,
            confidenceScore = confidence,
            parseStatus = parseStatus,
            rawContentSnippet = rawSnippet,
            sourceFingerprint = fingerprint,
            sender = message.sender
        )

        return TransactionParseResult(
            status = parseStatus,
            candidate = candidate
        )
    }

    private fun isOtpOrSecurityMessage(lower: String): Boolean {
        return lower.contains("otp") ||
                lower.contains("one time password") ||
                lower.contains("verification code") ||
                lower.contains("secret code") ||
                lower.contains("do not share") ||
                lower.contains("valid for") && lower.contains("minutes")
    }

    private fun isPromotionalOrLoan(lower: String): Boolean {
        return lower.contains("apply now") ||
                lower.contains("pre-approved") ||
                lower.contains("click here") ||
                lower.contains("cashback offer") ||
                lower.contains("loan up to") ||
                lower.contains("zero joining fee") ||
                lower.contains("get a loan")
    }

    private fun isFailedTransaction(lower: String): Boolean {
        return lower.contains("failed") ||
                lower.contains("declined") ||
                lower.contains("unsuccessful") ||
                lower.contains("could not be processed") ||
                lower.contains("transaction failed")
    }

    private fun isPendingTransaction(lower: String): Boolean {
        return lower.contains("payment pending") ||
                lower.contains("payment is processing") ||
                lower.contains("being processed") ||
                lower.contains("processing your payment") ||
                lower.contains("transaction pending") ||
                lower.contains("in progress") && lower.contains("payment")
    }

    private fun isPaymentRequest(lower: String): Boolean {
        return lower.contains("requested money") ||
                lower.contains("collect request") ||
                lower.contains("has requested") && lower.contains("pay")
    }

    private fun hasFinancialKeywords(lower: String): Boolean {
        val keywords = listOf(
            "debited", "credited", "spent", "withdrawn", "deposited", "received",
            "paid to", "sent to", "paid", "vpa", "upi", "neft", "rtgs", "imps", "card",
            "refund", "reversed", "transferred", "transfer", "a/c", "acct", "account", "inr", "rs", "₹", "charged"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isRefund(lower: String): Boolean {
        return lower.contains("refund") || lower.contains("reversed") || lower.contains("reversal")
    }

    private fun determineDirection(lower: String): TransactionDirection {
        val creditKeywords = listOf("credited", "received", "deposited", "refund", "reversal", "cr")
        val isCredit = creditKeywords.any { lower.contains(it) }
        return if (isCredit) TransactionDirection.CREDIT else TransactionDirection.DEBIT
    }

    private fun extractAvailableBalance(text: String): Money? {
        val regex = Regex("(?i)(?:avail(?:able)?\\s*(?:bal(?:ance)?|limit)?|bal(?:ance)?)\\s*(?:is|:)?\\s*(?:inr|rs\\.?|₹)?\\s*([\\d,]+(?:\\.\\d{1,2})?)")
        val match = regex.find(text) ?: return null
        val group = match.groupValues.getOrNull(1) ?: return null
        val cleanAmount = group.replace(",", "")
        val major = cleanAmount.toDoubleOrNull() ?: return null
        return Money((major * 100).toLong(), "INR")
    }

    private fun extractTransactionAmount(text: String, availBal: Money?): Money? {
        val regex = Regex("(?i)(?:inr|rs\\.?|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)")
        val matches = regex.findAll(text).toList()

        for (m in matches) {
            val group = m.groupValues.getOrNull(1) ?: continue
            val cleanAmount = group.replace(",", "")
            val major = cleanAmount.toDoubleOrNull() ?: continue
            val cents = (major * 100).toLong()

            if (availBal != null && cents == availBal.amountInCents) {
                continue
            }
            return Money(cents, "INR")
        }

        val secondaryRegex = Regex("(?i)([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:inr|rs\\.?|₹)")
        val secMatch = secondaryRegex.find(text)
        if (secMatch != null) {
            val group = secMatch.groupValues.getOrNull(1) ?: return null
            val cleanAmount = group.replace(",", "")
            val major = cleanAmount.toDoubleOrNull() ?: return null
            val cents = (major * 100).toLong()
            if (availBal == null || cents != availBal.amountInCents) {
                return Money(cents, "INR")
            }
        }

        return null
    }

    private fun extractAccountReference(text: String): String? {
        val regex = Regex("(?i)(?:a/c|acct|account|card)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with)?)?\\s*[*X]*(\\d{3,4})")
        val match = regex.find(text)
        if (match != null) {
            val digits = match.groupValues.getOrNull(1)
            if (!digits.isNullOrBlank()) {
                return "•••• $digits"
            }
        }
        return null
    }

    private fun extractInstitutionName(sender: String, text: String): String {
        val upperSender = sender.uppercase()
        val upperText = text.uppercase()

        return when {
            upperSender.contains("GOOGLE") || upperSender.contains("GNAV") || upperSender.contains("COM.GOOGLE") -> "Google Pay"
            upperSender.contains("PHONEPE") || upperSender.contains("COM.PHONEPE") -> "PhonePe"
            upperSender.contains("PAYTM") || upperSender.contains("NET.ONE97") -> "Paytm"
            upperSender.contains("CRED") || upperSender.contains("DREAMPLUG") -> "CRED"
            upperSender.contains("JUPITER") -> "Jupiter"
            upperSender.contains("SLICE") -> "Slice"
            upperSender.contains("NAVI") -> "Navi"
            upperSender.contains("HDFC") || upperText.contains("HDFC") -> "HDFC Bank"
            upperSender.contains("SBIBNK") || upperSender.contains("SBI") || upperText.contains("STATE BANK") || upperText.contains("SBI") -> "SBI"
            upperSender.contains("ICICI") || upperText.contains("ICICI") -> "ICICI Bank"
            upperSender.contains("AXIS") || upperText.contains("AXIS") -> "Axis Bank"
            upperSender.contains("KOTAK") || upperText.contains("KOTAK") -> "Kotak Mahindra Bank"
            upperSender.contains("PNB") || upperText.contains("PUNJAB NATIONAL") -> "PNB"
            upperSender.contains("BOB") || upperText.contains("BANK OF BARODA") -> "Bank of Baroda"
            upperSender.contains("YESBK") || upperText.contains("YES BANK") -> "Yes Bank"
            upperSender.contains("INDUS") || upperText.contains("INDUSIND") -> "IndusInd Bank"
            upperSender.contains("FEDERAL") || upperText.contains("FEDERAL BANK") -> "Federal Bank"
            upperSender.contains("SCISMS") || upperText.contains("STANDARD CHARTERED") -> "Standard Chartered"
            else -> if (sender.isNotBlank()) sender else "Bank/Institution"
        }
    }

    private fun extractMerchant(text: String): String? {
        val patterns = listOf(
            Regex("(?i)\\bat\\s+([A-Za-z0-9.\\&\\s]{2,20})\\b(?:\\son|\\.|\n|ref|upi|a/c|avail)"),
            Regex("(?i)\\bto\\s+([A-Za-z0-9.\\&\\s]{2,20})\\b(?:\\son|\\.|\n|ref|upi|a/c|via)"),
            Regex("(?i)\\bvpa\\s+([A-Za-z0-9._\\-]+\\@[A-Za-z0-9_\\-]+)"),
            Regex("(?i)paid to\\s+([A-Za-z0-9.\\&\\s]{2,20})")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val candidate = match.groupValues.getOrNull(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length in 2..30) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun extractReferenceNumber(text: String): String? {
        val regex = Regex("(?i)(?:upi\\s*ref|ref\\s*no|utr)\\s*[:\\.]?\\s*([A-Za-z0-9]{6,16})")
        val match = regex.find(text)
        return match?.groupValues?.getOrNull(1)
    }

    private fun extractDateOrFallback(text: String, timestampMillis: Long): String {
        val dateRegex = Regex("(?i)\\b(\\d{1,2})[-/]([A-Za-z]{3}|\\d{1,2})[-/](\\d{2,4})\\b")
        val match = dateRegex.find(text)
        if (match != null) {
            val day = match.groupValues[1].padStart(2, '0')
            val monthRaw = match.groupValues[2]
            val yearRaw = match.groupValues[3]

            val month = parseMonth(monthRaw)
            val year = if (yearRaw.length == 2) "20$yearRaw" else yearRaw
            return "$year-$month-${day}T12:00:00Z"
        }

        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        return instant.toString()
    }

    private fun parseMonth(raw: String): String {
        return when (raw.lowercase()) {
            "jan", "01", "1" -> "01"
            "feb", "02", "2" -> "02"
            "mar", "03", "3" -> "03"
            "apr", "04", "4" -> "04"
            "may", "05", "5" -> "05"
            "jun", "06", "6" -> "06"
            "jul", "07", "7" -> "07"
            "aug", "08", "8" -> "08"
            "sep", "09", "9" -> "09"
            "oct", "10" -> "10"
            "nov", "11" -> "11"
            "dec", "12" -> "12"
            else -> "01"
        }
    }

    private fun generateSourceFingerprint(
        sender: String,
        timestampMillis: Long,
        amountCents: Long,
        direction: TransactionDirection,
        accountRef: String?,
        referenceNo: String?
    ): String {
        val prefix = if (sender.contains(".") || sender.contains("com.") || sender.contains("net.")) "NOTIF" else "SMS"
        val rawKey = "$prefix:$sender:${timestampMillis / 60000}:$amountCents:${direction.name}:${accountRef ?: ""}:${referenceNo ?: ""}"
        return "fp_${rawKey.hashCode()}"
    }

    private fun calculateConfidence(
        hasAccount: Boolean,
        hasMerchant: Boolean,
        hasRef: Boolean,
        isRefund: Boolean
    ): Double {
        var score = 0.40 // Base score for having parsed monetary amount
        if (hasAccount) score += 0.20
        if (hasMerchant) score += 0.20
        if (hasRef) score += 0.10
        if (isRefund) score += 0.10
        return score
    }

    private fun buildDescription(
        type: TransactionType,
        merchant: String?,
        institution: String,
        accountRef: String?
    ): String {
        val refPart = accountRef?.let { " ($it)" } ?: ""
        return when {
            merchant != null -> "${merchant.replaceFirstChar { it.uppercase() }}$refPart"
            type == TransactionType.REFUND -> "Refund Credit $institution$refPart"
            type == TransactionType.INCOME -> "Income Credit $institution$refPart"
            else -> "Payment via $institution$refPart"
        }
    }
}
