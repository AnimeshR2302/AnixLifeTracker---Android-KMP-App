package com.anix.android.anixlifetracker.database.dao

import androidx.room.*
import com.anix.android.anixlifetracker.database.entity.SmsReviewCandidateEntity
import com.anix.android.anixlifetracker.database.entity.SmsSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsReviewCandidateDao {
    @Query("SELECT * FROM sms_review_candidates WHERE status = 'PENDING' ORDER BY occurredAt DESC")
    fun observePendingCandidates(): Flow<List<SmsReviewCandidateEntity>>

    @Query("SELECT * FROM sms_review_candidates WHERE id = :id LIMIT 1")
    suspend fun getCandidateById(id: String): SmsReviewCandidateEntity?

    @Query("SELECT * FROM sms_review_candidates WHERE sourceFingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): SmsReviewCandidateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: SmsReviewCandidateEntity)

    @Update
    suspend fun updateCandidate(candidate: SmsReviewCandidateEntity)

    @Query("DELETE FROM sms_review_candidates WHERE id = :id")
    suspend fun deleteCandidateById(id: String)

    @Query("DELETE FROM sms_review_candidates WHERE status = 'PENDING'")
    suspend fun clearPendingCandidates()
}

@Dao
interface SmsSettingsDao {
    @Query("SELECT * FROM sms_settings WHERE id = 'sms_settings_root' LIMIT 1")
    fun observeSettings(): Flow<SmsSettingsEntity?>

    @Query("SELECT * FROM sms_settings WHERE id = 'sms_settings_root' LIMIT 1")
    suspend fun getSettings(): SmsSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SmsSettingsEntity)
}
