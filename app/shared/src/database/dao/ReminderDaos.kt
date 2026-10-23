package com.anix.android.anixlifetracker.database.dao

import androidx.room.*
import com.anix.android.anixlifetracker.database.entity.*
import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderScheduleDao {
    @Query("SELECT * FROM reminder_schedules WHERE reminderId = :reminderId LIMIT 1")
    suspend fun getScheduleForReminder(reminderId: String): ReminderScheduleEntity?

    @Query("SELECT * FROM reminder_schedules")
    fun observeAllSchedules(): Flow<List<ReminderScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ReminderScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ReminderScheduleEntity>)

    @Query("DELETE FROM reminder_schedules WHERE reminderId = :reminderId")
    suspend fun deleteScheduleForReminder(reminderId: String)
}

@Dao
interface ReminderOccurrenceDao {
    @Query("SELECT * FROM reminder_occurrences ORDER BY scheduledLocalDateTime ASC")
    fun observeAllOccurrences(): Flow<List<ReminderOccurrenceEntity>>

    @Query("SELECT * FROM reminder_occurrences WHERE state IN ('DUE', 'OVERDUE', 'UPCOMING') ORDER BY scheduledLocalDateTime ASC")
    fun observeActiveOccurrences(): Flow<List<ReminderOccurrenceEntity>>

    @Query("SELECT * FROM reminder_occurrences WHERE reminderId = :reminderId ORDER BY scheduledLocalDateTime ASC")
    fun observeOccurrencesByReminder(reminderId: String): Flow<List<ReminderOccurrenceEntity>>

    @Query("SELECT * FROM reminder_occurrences WHERE id = :id LIMIT 1")
    suspend fun getOccurrenceById(id: String): ReminderOccurrenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: ReminderOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrences(occurrences: List<ReminderOccurrenceEntity>)

    @Update
    suspend fun updateOccurrence(occurrence: ReminderOccurrenceEntity)

    @Query("UPDATE reminder_occurrences SET state = :state, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOccurrenceState(id: String, state: OccurrenceState, updatedAt: String)

    @Query("DELETE FROM reminder_occurrences WHERE id = :id")
    suspend fun deleteOccurrence(id: String)
}

@Dao
interface ReminderActionDao {
    @Query("SELECT * FROM reminder_actions WHERE syncStatus = 'PENDING_UPLOAD' OR syncStatus = 'LOCAL_ONLY'")
    suspend fun getPendingActions(): List<ReminderActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ReminderActionEntity)

    @Query("UPDATE reminder_actions SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateActionSyncStatus(ids: List<String>, syncStatus: SyncStatus)

    @Query("DELETE FROM reminder_actions WHERE id = :id")
    suspend fun deleteAction(id: String)
}

@Dao
interface InAppInboxDao {
    @Query("SELECT * FROM in_app_inbox_items WHERE isDismissed = 0 AND isInvalidated = 0 ORDER BY freshnessISO DESC")
    fun observeActiveInboxItems(): Flow<List<InAppInboxItemEntity>>

    @Query("SELECT * FROM in_app_inbox_items WHERE isRead = 0 AND isDismissed = 0 AND isInvalidated = 0")
    fun observeUnreadInboxItems(): Flow<List<InAppInboxItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxItem(item: InAppInboxItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxItems(items: List<InAppInboxItemEntity>)

    @Query("UPDATE in_app_inbox_items SET isRead = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsRead(id: String, updatedAt: String)

    @Query("UPDATE in_app_inbox_items SET isDismissed = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun dismissItem(id: String, updatedAt: String)

    @Query("UPDATE in_app_inbox_items SET isInvalidated = 1, updatedAt = :updatedAt WHERE linkedEntityType = :entityType AND linkedEntityId = :entityId")
    suspend fun invalidateItemsForEntity(entityType: String, entityId: String, updatedAt: String)
}

@Dao
interface ReminderPreferencesDao {
    @Query("SELECT * FROM reminder_preferences WHERE ownerId = :ownerId LIMIT 1")
    suspend fun getPreferences(ownerId: String): ReminderPreferencesEntity?

    @Query("SELECT * FROM reminder_preferences WHERE ownerId = :ownerId LIMIT 1")
    fun observePreferences(ownerId: String): Flow<ReminderPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: ReminderPreferencesEntity)
}
