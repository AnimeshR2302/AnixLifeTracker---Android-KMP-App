package com.anix.android.anixlifetracker.notification.collector

import com.anix.android.anixlifetracker.model.FinancialAppProfile

actual class NotificationCollector actual constructor() {
    actual fun isNotificationAccessGranted(): Boolean = false
    actual fun openNotificationListenerSettings() {}
    actual fun isListenerServiceConnected(): Boolean = false
    actual fun getInstalledSupportedApps(): List<FinancialAppProfile> = emptyList()
}
