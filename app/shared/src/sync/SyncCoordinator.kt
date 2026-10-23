package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.auth.AuthRepository
import com.anix.android.anixlifetracker.database.dao.SyncDao
import com.anix.android.anixlifetracker.database.entity.SyncCheckpointEntity
import com.anix.android.anixlifetracker.error.AppError
import com.anix.android.anixlifetracker.security.RedactingLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

interface SyncCoordinator {
    fun observeSyncState(): StateFlow<SyncState>
    fun observeSyncDetails(): StateFlow<SyncStatusDetails>
    suspend fun pullOnly(reason: SyncReason = SyncReason.ANDROID_FOREGROUND): SyncResult
    suspend fun syncExplicit(reason: SyncReason = SyncReason.USER_SYNC): SyncResult
    suspend fun synchronize(): SyncResult
}

class DefaultSyncCoordinator(
    private val authRepository: AuthRepository,
    private val localChangeTracker: LocalChangeTracker,
    private val remoteDataSource: RemoteDataSource,
    private val conflictResolver: ConflictResolver,
    private val syncDao: SyncDao
) : SyncCoordinator {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    private val _syncDetails = MutableStateFlow(SyncStatusDetails())

    override fun observeSyncState(): StateFlow<SyncState> = _syncState.asStateFlow()
    override fun observeSyncDetails(): StateFlow<SyncStatusDetails> = _syncDetails.asStateFlow()

    override suspend fun synchronize(): SyncResult {
        return syncExplicit(SyncReason.USER_SYNC)
    }

    override suspend fun pullOnly(reason: SyncReason): SyncResult {
        val session = authRepository.getCurrentSession()
            ?: run {
                val state = SyncState.Error(AppError.Auth.SessionExpired.userMessage, isOffline = false)
                _syncState.value = state
                return SyncResult.Failure(AppError.Auth.SessionExpired.userMessage)
            }

        RedactingLogger.i("SyncCoordinator", "Executing pull-only sync operation (reason: $reason) for user: ${session.user.id}")
        _syncState.value = SyncState.Syncing("Pulling backend updates ($reason)...")

        return try {
            val ownerId = session.user.id
            val existingCheckpoints = syncDao.getAllCheckpoints().associate { it.entityType to it.lastSyncTimestamp }

            _syncState.value = SyncState.Syncing("Fetching remote updates...")
            val remotePayload = remoteDataSource.pullChanges(session.accessToken, existingCheckpoints)

            val nowIso = Clock.System.now().toString()
            syncDao.saveCheckpoint(
                SyncCheckpointEntity(
                    entityType = "global",
                    lastSyncTimestamp = remotePayload.serverCheckpointTimestamp.ifBlank { nowIso },
                    lastSyncRevision = 1L,
                    updatedAt = nowIso
                )
            )

            val pendingCount = localChangeTracker.collectPendingChanges(ownerId).totalCount
            val unresolvedConflicts = syncDao.getUnresolvedConflicts().size

            _syncDetails.value = _syncDetails.value.copy(
                localPendingCount = pendingCount,
                unresolvedConflictCount = unresolvedConflicts,
                lastSuccessfulPullISO = nowIso
            )

            _syncState.value = SyncState.Success(
                lastSyncTimeISO = nowIso,
                recordsSyncedCount = remotePayload.accounts.size + remotePayload.transactions.size
            )

            SyncResult.Success(
                pushedCount = 0,
                pulledCount = remotePayload.accounts.size + remotePayload.transactions.size,
                conflictsCount = unresolvedConflicts
            )
        } catch (e: Exception) {
            val appErr = AppError.Network.ConnectionFailed(e.message ?: "Pull synchronization failed")
            RedactingLogger.e("SyncCoordinator", "Pull synchronization error", e)
            _syncState.value = SyncState.Error(appErr.userMessage, isOffline = true)
            SyncResult.Failure(appErr.userMessage, isNetworkError = true)
        }
    }

    override suspend fun syncExplicit(reason: SyncReason): SyncResult {
        val session = authRepository.getCurrentSession()
            ?: run {
                val state = SyncState.Error(AppError.Auth.SessionExpired.userMessage, isOffline = false)
                _syncState.value = state
                return SyncResult.Failure(AppError.Auth.SessionExpired.userMessage)
            }

        RedactingLogger.i("SyncCoordinator", "Executing explicit full sync operation (reason: $reason) for user: ${session.user.id}")
        _syncState.value = SyncState.Syncing("Preparing explicit sync ($reason)...")

        return try {
            val ownerId = session.user.id

            // Push pending local edits
            val pendingPayload = localChangeTracker.collectPendingChanges(ownerId)
            _syncState.value = SyncState.Syncing("Uploading pending offline changes...")

            var pushedCount = 0
            if (pendingPayload.totalCount > 0) {
                val pushResult = remoteDataSource.pushChanges(session.accessToken, pendingPayload)
                if (pushResult is SyncResult.Failure) {
                    _syncState.value = SyncState.Error(pushResult.errorMessage, pushResult.isNetworkError)
                    localChangeTracker.markPushedChangesFailed(ownerId, pendingPayload, pushResult.errorMessage)
                    _syncDetails.value = _syncDetails.value.copy(
                        failedCount = _syncDetails.value.failedCount + 1
                    )
                    return pushResult
                }
                localChangeTracker.markPushedChangesSynced(ownerId, pendingPayload)
                pushedCount = pendingPayload.totalCount
            }

            val lastUploadIso = Clock.System.now().toString()

            // Pull remote updates
            _syncState.value = SyncState.Syncing("Fetching remote updates...")
            val existingCheckpoints = syncDao.getAllCheckpoints().associate { it.entityType to it.lastSyncTimestamp }
            val remotePayload = remoteDataSource.pullChanges(session.accessToken, existingCheckpoints)

            var conflictsCount = 0
            val unresolvedConflicts = syncDao.getUnresolvedConflicts()
            val nowIso = Clock.System.now().toString()

            if (unresolvedConflicts.isNotEmpty()) {
                conflictsCount = unresolvedConflicts.size
                val mappedConflicts = unresolvedConflicts.map {
                    SyncConflict(
                        id = it.id,
                        entityType = it.entityType,
                        entityId = it.entityId,
                        ownerId = it.ownerId,
                        localDataJson = it.localDataJson,
                        remoteDataJson = it.remoteDataJson,
                        conflictReason = it.conflictReason,
                        detectedAtISO = it.createdAt
                    )
                }
                _syncState.value = SyncState.ConflictDetected(mappedConflicts)
            } else {
                syncDao.saveCheckpoint(
                    SyncCheckpointEntity(
                        entityType = "global",
                        lastSyncTimestamp = remotePayload.serverCheckpointTimestamp.ifBlank { nowIso },
                        lastSyncRevision = 1L,
                        updatedAt = nowIso
                    )
                )

                _syncState.value = SyncState.Success(
                    lastSyncTimeISO = nowIso,
                    recordsSyncedCount = pushedCount + remotePayload.accounts.size + remotePayload.transactions.size
                )
            }

            val remainingPending = localChangeTracker.collectPendingChanges(ownerId).totalCount

            _syncDetails.value = SyncStatusDetails(
                localPendingCount = remainingPending,
                failedCount = 0,
                unresolvedConflictCount = conflictsCount,
                lastSuccessfulPullISO = nowIso,
                lastSuccessfulExchangeISO = nowIso,
                lastAndroidUploadISO = lastUploadIso
            )

            SyncResult.Success(
                pushedCount = pushedCount,
                pulledCount = remotePayload.accounts.size + remotePayload.transactions.size,
                conflictsCount = conflictsCount
            )
        } catch (e: Exception) {
            val appErr = AppError.Network.ConnectionFailed(e.message ?: "Sync operation interrupted")
            RedactingLogger.e("SyncCoordinator", "Explicit sync error", e)
            _syncState.value = SyncState.Error(appErr.userMessage, isOffline = true)
            SyncResult.Failure(appErr.userMessage, isNetworkError = true)
        }
    }
}
