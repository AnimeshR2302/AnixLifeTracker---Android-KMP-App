package com.anix.android.anixlifetracker.database.dao

import androidx.room.*
import com.anix.android.anixlifetracker.database.entity.NotificationAppConfigEntity
import com.anix.android.anixlifetracker.database.entity.NotificationReviewCandidateEntity
import com.anix.android.anixlifetracker.database.entity.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE id = 'notification_settings_root' LIMIT 1")
    fun observeSettings(): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM notification_settings WHERE id = 'notification_settings_root' LIMIT 1")
    suspend fun getSettings(): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: NotificationSettingsEntity)
}

@Dao
interface NotificationAppConfigDao {
    @Query("SELECT * FROM notification_app_configs")
    fun observeAppConfigs(): Flow<List<NotificationAppConfigEntity>>

    @Query("SELECT * FROM notification_app_configs")
    suspend fun getAllAppConfigs(): List<NotificationAppConfigEntity>

    @Query("SELECT * FROM notification_app_configs WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): NotificationAppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: NotificationAppConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(configs: List<NotificationAppConfigEntity>)
}

@Dao
interface NotificationReviewCandidateDao {
    @Query("SELECT * FROM notification_review_candidates WHERE status = 'PENDING' ORDER BY occurredAt DESC")
    fun observePendingCandidates(): Flow<List<NotificationReviewCandidateEntity>>

    @Query("SELECT * FROM notification_review_candidates WHERE id = :id LIMIT 1")
    suspend fun getCandidateById(id: String): NotificationReviewCandidateEntity?

    @Query("SELECT * FROM notification_review_candidates WHERE sourceFingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): NotificationReviewCandidateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: NotificationReviewCandidateEntity)

    @Update
    suspend fun updateCandidate(candidate: NotificationReviewCandidateEntity)

    @Query("DELETE FROM notification_review_candidates WHERE id = :id")
    suspend fun deleteCandidateById(id: String)

    @Query("DELETE FROM notification_review_candidates WHERE status = 'PENDING'")
    suspend fun clearPendingCandidates()
}
