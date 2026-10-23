package com.anix.android.anixlifetracker.notification.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.FinancialNotification
import com.anix.android.anixlifetracker.repository.NotificationRepositoryImpl
import com.anix.android.anixlifetracker.security.RedactingLogger
import com.anix.android.anixlifetracker.usecase.ProcessIncomingNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinancialNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val otpRegex = Regex("(?i)(otp|one-time password|verification code|security code|login code)")

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        RedactingLogger.i("NotificationListener", "Financial Notification Listener Service connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
        RedactingLogger.w("NotificationListener", "Financial Notification Listener Service disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        val fullText = when {
            !bigText.isNullOrBlank() -> bigText
            !text.isNullOrBlank() -> text
            else -> ""
        }

        if (title.isNullOrBlank() && fullText.isBlank()) return

        // Data Minimization: drop security OTPs immediately
        if (otpRegex.containsMatchIn(fullText) || (title != null && otpRegex.containsMatchIn(title))) {
            return
        }

        val financialNotif = FinancialNotification(
            sourcePackage = pkgName,
            title = title,
            content = fullText,
            postedAt = sbn.postTime,
            notificationIdentifier = "${pkgName}_${sbn.id}_${sbn.tag ?: ""}"
        )

        serviceScope.launch {
            try {
                val db = DatabaseManager.getDatabase()
                val repository = NotificationRepositoryImpl(db)

                // Package pre-filtering check against enabled apps
                val settings = repository.getSettings()
                if (!settings.isNotificationFeatureEnabled || !settings.isConsentGiven || !settings.liveMonitoringEnabled) {
                    return@launch
                }

                val enabledApps = repository.getSupportedApps().filter { it.isEnabled }
                if (enabledApps.none { it.packageName.equals(pkgName, ignoreCase = true) }) {
                    return@launch
                }

                val processUseCase = ProcessIncomingNotificationUseCase(repository)
                processUseCase(financialNotif)
            } catch (e: Exception) {
                RedactingLogger.e("NotificationListener", "Error processing notification", e)
            }
        }
    }

    companion object {
        @Volatile
        var isServiceConnected: Boolean = false
    }
}
