package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.sms.parser.DeterministicFinancialMessageParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SmsParserTest {

    private val parser = DeterministicFinancialMessageParser()

    @Test
    fun testHdfcDeductionSms() {
        val body = "INR 1,250.00 debited from A/C XX1234 on 23-Oct-2026 at SWIGGY. UPI Ref 987654321. Avail Bal INR 15,000.00."
        val message = FinancialMessage("m1", "AD-HDFCBK", body, 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(125000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.DEBIT, candidate.direction)
        assertEquals(TransactionType.EXPENSE, candidate.transactionType)
        assertEquals("•••• 1234", candidate.accountRefMasked)
        assertEquals("HDFC Bank", candidate.institutionName)
        assertEquals("SWIGGY", candidate.merchantName)
        assertEquals("987654321", candidate.transactionReference)
        assertEquals(1500000L, candidate.availableBalance?.amountInCents)
    }

    @Test
    fun testSbiCreditSms() {
        val body = "Your account XX5678 has been credited with INR 45,000.00 via NEFT on 15-Nov-2026. Ref UTR112233."
        val message = FinancialMessage("m2", "VM-SBIBNK", body, 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(4500000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.CREDIT, candidate.direction)
        assertEquals(TransactionType.INCOME, candidate.transactionType)
        assertEquals("•••• 5678", candidate.accountRefMasked)
        assertEquals("SBI", candidate.institutionName)
        assertEquals("UTR112233", candidate.transactionReference)
    }

    @Test
    fun testRefundSms() {
        val body = "Refund of INR 750.00 credited to account XX5678 for order 12345."
        val message = FinancialMessage("m3", "ICICIB", body, 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(75000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.CREDIT, candidate.direction)
        assertEquals(TransactionType.REFUND, candidate.transactionType)
    }

    @Test
    fun testOtpMessageRejection() {
        val body = "Your OTP for HDFC Bank transaction is 482190. Do not share this code with anyone."
        val message = FinancialMessage("m4", "AD-HDFCBK", body, 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.REJECTED, result.status)
        assertTrue(result.rejectionReason?.contains("OTP") == true)
    }

    @Test
    fun testFailedPaymentRejection() {
        val body = "Your UPI payment of INR 500 to Swiggy failed due to technical issues."
        val message = FinancialMessage("m5", "AXISBK", body, 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.REJECTED, result.status)
        assertTrue(result.rejectionReason?.contains("Failed") == true)
    }
}
