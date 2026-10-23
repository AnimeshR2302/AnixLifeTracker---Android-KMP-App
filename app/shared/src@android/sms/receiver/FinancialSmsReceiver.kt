package com.anix.android.anixlifetracker.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.FinancialMessage
import com.anix.android.anixlifetracker.repository.SmsRepositoryImpl
import com.anix.android.anixlifetracker.security.RedactingLogger
import com.anix.android.anixlifetracker.usecase.ProcessIncomingSmsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FinancialSmsReceiver : BroadcastReceiver() {

    private val otpRegex = Regex("(?i)(otp|one-time password|verification code|security code|login code)")

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (!smsMessages.isNullOrEmpty()) {
                    val sender = smsMessages[0].originatingAddress ?: "SMS_SENDER"
                    val timestamp = smsMessages[0].timestampMillis
                    val body = smsMessages.joinToString("") { it.messageBody ?: "" }

                    // Minimization: Discard OTP/security verification messages immediately
                    if (body.isNotBlank() && !otpRegex.containsMatchIn(body)) {
                        RedactingLogger.i("FinancialSmsReceiver", "Received SMS from $sender. Processing financial candidate.")
                        val message = FinancialMessage(
                            id = "rec_$timestamp",
                            sender = sender,
                            body = body,
                            timestampMillis = timestamp
                        )

                        val db = DatabaseManager.getDatabase()
                        val repo = SmsRepositoryImpl(db)
                        val processUseCase = ProcessIncomingSmsUseCase(repo)

                        processUseCase(message)
                    } else {
                        RedactingLogger.d("FinancialSmsReceiver", "Discarding non-financial or OTP SMS message.")
                    }
                }
            } catch (e: Exception) {
                RedactingLogger.e("FinancialSmsReceiver", "Error processing SMS receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
