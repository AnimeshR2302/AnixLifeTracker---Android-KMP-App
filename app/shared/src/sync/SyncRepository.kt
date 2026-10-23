package com.anix.android.anixlifetracker.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    fun observeSyncState(): StateFlow<SyncState>
    fun observeSyncDetails(): StateFlow<SyncStatusDetails>
    fun isCloudSyncEnabled(): StateFlow<Boolean>
    suspend fun setCloudSyncEnabled(enabled: Boolean): Boolean
    suspend fun pullOnly(reason: SyncReason = SyncReason.ANDROID_FOREGROUND): SyncResult
    suspend fun syncExplicit(reason: SyncReason = SyncReason.USER_SYNC): SyncResult
    suspend fun syncNow(): SyncResult
    suspend fun getLastSyncTimestamp(): String?
    suspend fun getPendingChangesCount(): Int
    suspend fun resolveConflict(conflictId: String, strategy: ConflictResolutionStrategy): Boolean
    suspend fun migrateLocalDataToAccount(newOwnerId: String): Int
}
