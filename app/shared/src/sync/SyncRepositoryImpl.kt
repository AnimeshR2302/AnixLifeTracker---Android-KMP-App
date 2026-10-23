package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.auth.AuthRepository
import com.anix.android.anixlifetracker.database.dao.SyncDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncRepositoryImpl(
    private val syncCoordinator: SyncCoordinator,
    private val localChangeTracker: LocalChangeTracker,
    private val syncDao: SyncDao,
    private val authRepository: AuthRepository
) : SyncRepository {

    private val _isCloudSyncEnabled = MutableStateFlow(false)

    override fun observeSyncState(): StateFlow<SyncState> = syncCoordinator.observeSyncState()

    override fun observeSyncDetails(): StateFlow<SyncStatusDetails> = syncCoordinator.observeSyncDetails()

    override fun isCloudSyncEnabled(): StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    override suspend fun setCloudSyncEnabled(enabled: Boolean): Boolean {
        _isCloudSyncEnabled.value = enabled
        if (enabled) {
            val session = authRepository.getCurrentSession()
            if (session != null) {
                // Perform local data ownership migration if needed
                localChangeTracker.migrateLocalDataOwner(session.user.id)
                // Perform initial foreground pull
                syncCoordinator.pullOnly(SyncReason.ANDROID_FOREGROUND)
            }
        }
        return _isCloudSyncEnabled.value
    }

    override suspend fun pullOnly(reason: SyncReason): SyncResult {
        if (!_isCloudSyncEnabled.value) {
            return SyncResult.Failure("Cloud synchronization is disabled in settings")
        }
        return syncCoordinator.pullOnly(reason)
    }

    override suspend fun syncExplicit(reason: SyncReason): SyncResult {
        if (!_isCloudSyncEnabled.value) {
            return SyncResult.Failure("Cloud synchronization is disabled in settings")
        }
        return syncCoordinator.syncExplicit(reason)
    }

    override suspend fun syncNow(): SyncResult {
        return syncExplicit(SyncReason.USER_SYNC)
    }

    override suspend fun getLastSyncTimestamp(): String? {
        return syncDao.getCheckpoint("global")?.lastSyncTimestamp
    }

    override suspend fun getPendingChangesCount(): Int {
        val session = authRepository.getCurrentSession() ?: return 0
        val pending = localChangeTracker.collectPendingChanges(session.user.id)
        return pending.totalCount
    }

    override suspend fun resolveConflict(
        conflictId: String,
        strategy: ConflictResolutionStrategy
    ): Boolean {
        syncDao.markConflictResolved(conflictId)
        return true
    }

    override suspend fun migrateLocalDataToAccount(newOwnerId: String): Int {
        return localChangeTracker.migrateLocalDataOwner(newOwnerId)
    }
}
