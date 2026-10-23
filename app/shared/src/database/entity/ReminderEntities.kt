package com.anix.android.anixlifetracker.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anix.android.anixlifetracker.model.*

@Entity(
    tableName = "reminder_schedules",
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["cadence"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderScheduleEntity(
    @PrimaryKey val id: String,
    val reminderId: String,
    val isRecurring: Boolean,
    val localDueDateOrTime: String,
    val ianaTimezone: String,
    val dateOnlyPolicy: DateOnlyPolicy,
    val cadence: CadenceType,
    val anchorDay: Int?,
    val endDate: String?,
    val endCount: Int?,
    val leadTimesDays: Int,
    val missedDatePolicy: MissedDatePolicy,
    val scheduleVersion: Long,
    val updatedAt: String
)

@Entity(
    tableName = "reminder_occurrences",
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["ownerId"]),
        Index(value = ["scheduledLocalDateTime"]),
        Index(value = ["state"])
    ]
)
data class ReminderOccurrenceEntity(
    @PrimaryKey val id: String,
    val reminderId: String,
    val ownerId: String,
    val scheduleVersion: Long,
    val scheduledLocalDateTime: String,
    val resolvedInstant: String,
    val state: OccurrenceState,
    val snoozeUntilISO: String?,
    val amountMinor: Long?,
    val currencyCode: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)

@Entity(
    tableName = "reminder_actions",
    indices = [
        Index(value = ["occurrenceId"]),
        Index(value = ["reminderId"]),
        Index(value = ["ownerId"]),
        Index(value = ["syncStatus"])
    ]
)
data class ReminderActionEntity(
    @PrimaryKey val id: String,
    val occurrenceId: String,
    val reminderId: String,
    val ownerId: String,
    val actionType: ReminderActionType,
    val effectiveTimeISO: String,
    val baseRevision: Long,
    val syncStatus: SyncStatus,
    val conflictState: String?
)

@Entity(
    tableName = "in_app_inbox_items",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["reminderId"]),
        Index(value = ["occurrenceId"]),
        Index(value = ["isRead"]),
        Index(value = ["isDismissed"]),
        Index(value = ["isInvalidated"])
    ]
)
data class InAppInboxItemEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val reminderId: String?,
    val occurrenceId: String?,
    val linkedEntityType: String?,
    val linkedEntityId: String?,
    val title: String,
    val subtitle: String,
    val severity: InboxSeverity,
    val reason: String,
    val sourceRevision: Long,
    val freshnessISO: String,
    val isRead: Boolean,
    val isDismissed: Boolean,
    val isInvalidated: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "reminder_preferences"
)
data class ReminderPreferencesEntity(
    @PrimaryKey val ownerId: String,
    val inAppVisibility: Boolean,
    val quietHoursStart: String?,
    val quietHoursEnd: String?,
    val userTimezone: String,
    val displaySettingsJson: String,
    val enabledChannelsJson: String,
    val updatedAt: String
)
