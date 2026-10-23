package com.anix.android.anixlifetracker.sms.collector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.anix.android.anixlifetracker.model.FinancialMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class SmsCollector(private val context: Context?) {

    actual constructor() : this(null)

    actual fun isSmsPermissionGranted(): Boolean {
        val ctx = context ?: return false
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun readHistoricalMessages(
        startDateMillis: Long,
        endDateMillis: Long
    ): List<FinancialMessage> = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext emptyList()
        if (!isSmsPermissionGranted()) return@withContext emptyList()

        val messages = mutableListOf<FinancialMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "date >= ? AND date <= ?"
        val selectionArgs = arrayOf(startDateMillis.toString(), endDateMillis.toString())
        val sortOrder = "date DESC"

        try {
            ctx.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIdx = cursor.getColumnIndex("_id")
                val addressIdx = cursor.getColumnIndex("address")
                val bodyIdx = cursor.getColumnIndex("body")
                val dateIdx = cursor.getColumnIndex("date")

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) else ""
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L

                    if (body.isNotBlank()) {
                        messages.add(
                            FinancialMessage(
                                id = id.ifBlank { "sms_$date" },
                                sender = address,
                                body = body,
                                timestampMillis = date,
                                rawMessageId = id
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Gracefully catch security or content provider exception
        }

        messages
    }
}
