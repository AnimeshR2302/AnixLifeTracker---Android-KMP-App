package com.anix.android.anixlifetracker.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface PlatformSyncManager {
    fun schedulePeriodicSync(intervalMinutes: Long = 15)
    fun cancelPeriodicSync()
    fun triggerForegroundPull()
    fun triggerImmediateSync()
}

class DefaultPlatformSyncManager(
    private val syncRepository: SyncRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : PlatformSyncManager {

    private var syncJob: Job? = null

    override fun schedulePeriodicSync(intervalMinutes: Long) {
        // Disabled per Plan 7 Sync Contract: Background periodic uploading or polling is strictly disabled.
        cancelPeriodicSync()
    }

    override fun cancelPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }

    override fun triggerForegroundPull() {
        scope.launch {
            if (syncRepository.isCloudSyncEnabled().value) {
                syncRepository.pullOnly(SyncReason.ANDROID_FOREGROUND)
            }
        }
    }

    override fun triggerImmediateSync() {
        scope.launch {
            if (syncRepository.isCloudSyncEnabled().value) {
                syncRepository.syncExplicit(SyncReason.USER_SYNC)
            }
        }
    }
}
