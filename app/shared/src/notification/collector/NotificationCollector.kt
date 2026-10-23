package com.anix.android.anixlifetracker.notification.collector

import com.anix.android.anixlifetracker.model.FinancialAppProfile

/**
 * Multiplatform expect class for checking Notification Listener status, opening settings,
 * and inspecting installed supported financial applications.
 */
expect class NotificationCollector() {
    fun isNotificationAccessGranted(): Boolean
    fun openNotificationListenerSettings()
    fun isListenerServiceConnected(): Boolean
    fun getInstalledSupportedApps(): List<FinancialAppProfile>
}
