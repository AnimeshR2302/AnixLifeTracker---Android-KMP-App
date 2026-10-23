package com.anix.android.anixlifetracker.sms.collector

import com.anix.android.anixlifetracker.model.FinancialMessage

actual class SmsCollector {
    actual fun isSmsPermissionGranted(): Boolean = false

    actual suspend fun readHistoricalMessages(
        startDateMillis: Long,
        endDateMillis: Long
    ): List<FinancialMessage> = emptyList()
}
