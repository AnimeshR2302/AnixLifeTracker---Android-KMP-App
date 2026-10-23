package com.anix.android.anixlifetracker.notification.delivery

import com.anix.android.anixlifetracker.model.DeliveryCapabilityStatus
import com.anix.android.anixlifetracker.model.NotificationDeliveryResult

/**
 * Provider-neutral interface for notification delivery
 */
interface NotificationDeliveryGateway {
    fun getCapabilityStatus(): DeliveryCapabilityStatus
    suspend fun deliverNotification(
        title: String,
        body: String,
        targetChannel: String = "default"
    ): NotificationDeliveryResult
}

/**
 * Disabled implementation enforcing the Plan 8 rule:
 * All OS/browser/remote notification delivery is disabled in this release.
 */
class DisabledNotificationDeliveryGateway : NotificationDeliveryGateway {

    override fun getCapabilityStatus(): DeliveryCapabilityStatus {
        return DeliveryCapabilityStatus(
            isEnabled = false,
            isSupported = false,
            statusExplanation = "Delivery is not available in this version"
        )
    }

    override suspend fun deliverNotification(
        title: String,
        body: String,
        targetChannel: String
    ): NotificationDeliveryResult {
        return NotificationDeliveryResult.Disabled(
            reason = "Delivery is not available in this version. Outgoing notifications are disabled."
        )
    }
}
