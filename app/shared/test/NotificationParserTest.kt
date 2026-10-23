package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.sms.parser.DeterministicFinancialMessageParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NotificationParserTest {

    private val parser = DeterministicFinancialMessageParser()

    @Test
    fun testUpiPaymentNotification() {
        val title = "Payment Successful"
        val body = "₹350 paid to Cafe Coffee Day using HDFC Bank •••• 4821. UPI Ref: 123456789012"
        val message = FinancialMessage("n1", "com.google.android.apps.nfc.phone", "$title $body", 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(35000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.DEBIT, candidate.direction)
        assertEquals(TransactionType.EXPENSE, candidate.transactionType)
        assertEquals("•••• 4821", candidate.accountRefMasked)
        assertEquals("Google Pay", candidate.institutionName)
        assertEquals("Cafe Coffee Day", candidate.merchantName)
        assertEquals("123456789012", candidate.transactionReference)
    }

    @Test
    fun testMoneyReceivedNotification() {
        val title = "Money Received"
        val body = "You received ₹2,500 from Rahul. Credited to SBI Account •••• 9102. Ref: UTR998877."
        val message = FinancialMessage("n2", "com.phonepe.app", "$title $body", 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(250000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.CREDIT, candidate.direction)
        assertEquals(TransactionType.INCOME, candidate.transactionType)
        assertEquals("•••• 9102", candidate.accountRefMasked)
        assertEquals("PhonePe", candidate.institutionName)
        assertEquals("UTR998877", candidate.transactionReference)
    }

    @Test
    fun testPendingPaymentNotificationRejection() {
        val title = "Payment Pending"
        val body = "Your payment of ₹799 is being processed by your bank."
        val message = FinancialMessage("n3", "net.one97.paytm", "$title $body", 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.REJECTED, result.status)
        assertTrue(result.rejectionReason?.lowercase()?.contains("pending") == true)
    }

    @Test
    fun testFailedPaymentNotificationRejection() {
        val title = "Payment Failed"
        val body = "₹1,000 could not be transferred due to insufficient funds."
        val message = FinancialMessage("n4", "com.phonepe.app", "$title $body", 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.REJECTED, result.status)
        assertTrue(result.rejectionReason?.lowercase()?.contains("failed") == true)
    }

    @Test
    fun testRefundNotification() {
        val title = "Refund Completed"
        val body = "Refund of ₹1,200.00 processed to ICICI Bank •••• 9102."
        val message = FinancialMessage("n5", "com.csam.icici.bank.imobile", "$title $body", 1792752000000L)

        val result = parser.parse(message)

        assertEquals(ParseConfidence.HIGH_CONFIDENCE, result.status)
        val candidate = result.candidate
        assertNotNull(candidate)
        assertEquals(120000L, candidate.amount.amountInCents)
        assertEquals(TransactionDirection.CREDIT, candidate.direction)
        assertEquals(TransactionType.REFUND, candidate.transactionType)
        assertEquals("ICICI Bank", candidate.institutionName)
    }
}
