package com.anix.android.anixlifetracker.sms.collector

import com.anix.android.anixlifetracker.model.FinancialMessage

/**
 * Multiplatform interface for reading SMS inbox and checking runtime SMS permissions
 */
expect class SmsCollector() {
    suspend fun readHistoricalMessages(startDateMillis: Long, endDateMillis: Long): List<FinancialMessage>
    fun isSmsPermissionGranted(): Boolean
}
