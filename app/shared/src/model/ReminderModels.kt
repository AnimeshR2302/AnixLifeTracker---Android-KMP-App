package com.anix.android.anixlifetracker.model

import kotlinx.serialization.Serializable

enum class DateOnlyPolicy {
    EXACT_TIME,
    LOCAL_DAY_END,
    LOCAL_DAY_START
}

enum class CadenceType {
    ONE_OFF,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class MissedDatePolicy {
    SKIP_MISSED,
    GENERATE_LATEST,
    GENERATE_ALL
}

enum class OccurrenceState {
    UPCOMING,
    DUE,
    OVERDUE,
    COMPLETED,
    SKIPPED,
    CANCELLED
}

enum class ReminderActionType {
    COMPLETE,
    SKIP,
    SNOOZE,
    DISMISS,
    UNDO
}

enum class InboxSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Core Reminder Definition Model (Plan 8)
 */
@Serializable
data class ReminderDefinitionModel(
    val id: String,
    val ownerId: String = "local_user",
    val title: String,
    val description: String? = null,
    val category: ReminderCategory = ReminderCategory.PAYMENT,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isEnabled: Boolean = true,
    val scheduleVersion: Long = 1L,
    val sourceType: String = "MANUAL", // MANUAL, OBLIGATION, RECURRING_PAYMENT, BUDGET, ANOMALY, TRAVEL, EVENT
    val sourceEntityId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val serverRevision: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

/**
 * Schedule rule specification associated with a Reminder Definition
 */
@Serializable
data class ReminderScheduleModel(
    val id: String,
    val reminderId: String,
    val isRecurring: Boolean = false,
    val localDueDateOrTime: String, // YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss
    val ianaTimezone: String = "Asia/Kolkata",
    val dateOnlyPolicy: DateOnlyPolicy = DateOnlyPolicy.LOCAL_DAY_END,
    val cadence: CadenceType = CadenceType.ONE_OFF,
    val anchorDay: Int? = null, // e.g. Day 31 of month
    val endDate: String? = null,
    val endCount: Int? = null,
    val leadTimesDays: Int = 1,
    val missedDatePolicy: MissedDatePolicy = MissedDatePolicy.GENERATE_LATEST,
    val scheduleVersion: Long = 1L,
    val updatedAt: String = ""
)

/**
 * Specific instance or occurrence derived from a Reminder Schedule
 */
@Serializable
data class ReminderOccurrenceModel(
    val id: String, // Stable deterministic ID: ${reminderId}_${scheduledLocalDate}
    val reminderId: String,
    val ownerId: String = "local_user",
    val scheduleVersion: Long = 1L,
    val scheduledLocalDateTime: String, // ISO String
    val resolvedInstant: String = "",
    val state: OccurrenceState = OccurrenceState.UPCOMING,
    val snoozeUntilISO: String? = null,
    val amount: Money? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

/**
 * User mutation action targeting a specific occurrence
 */
@Serializable
data class ReminderActionModel(
    val id: String, // Stable operation ID
    val occurrenceId: String,
    val reminderId: String,
    val ownerId: String = "local_user",
    val actionType: ReminderActionType,
    val effectiveTimeISO: String,
    val baseRevision: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val conflictState: String? = null
)

/**
 * In-App Inbox item for due alerts, budget insights, and spending anomalies
 */
@Serializable
data class InAppInboxItemModel(
    val id: String,
    val ownerId: String = "local_user",
    val reminderId: String? = null,
    val occurrenceId: String? = null,
    val linkedEntityType: String? = null,
    val linkedEntityId: String? = null,
    val title: String,
    val subtitle: String,
    val severity: InboxSeverity = InboxSeverity.INFO,
    val reason: String = "",
    val sourceRevision: Long = 1L,
    val freshnessISO: String = "",
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
    val isInvalidated: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Reminder and Notification Preferences
 */
@Serializable
data class ReminderPreferencesModel(
    val ownerId: String = "local_user",
    val inAppVisibility: Boolean = true,
    val quietHoursStart: String? = "22:00",
    val quietHoursEnd: String? = "07:00",
    val userTimezone: String = "Asia/Kolkata",
    val displaySettingsJson: String = "{}",
    val enabledChannelsJson: String = "{\"in_app\":true,\"system\":false,\"email\":false}",
    val updatedAt: String = ""
)

/**
 * Delivery Status interface model for Notification Delivery Gateway
 */
data class DeliveryCapabilityStatus(
    val isEnabled: Boolean = false,
    val isSupported: Boolean = false,
    val statusExplanation: String = "Delivery is not available in this version"
)

sealed interface NotificationDeliveryResult {
    data class Success(val deliveryId: String, val deliveredAtISO: String) : NotificationDeliveryResult
    data class Disabled(val reason: String = "Delivery is not available in this version") : NotificationDeliveryResult
    data class Failed(val error: String) : NotificationDeliveryResult
}
