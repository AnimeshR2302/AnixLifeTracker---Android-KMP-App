package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.database.dao.SyncDao
import com.anix.android.anixlifetracker.database.entity.SyncConflictEntity
import kotlin.time.Clock

interface ConflictResolver {
    suspend fun inspectAndResolve(
        entityType: String,
        entityId: String,
        ownerId: String,
        localJson: String,
        remoteJson: String
    ): Boolean
}

class FinancialConflictResolver(
    private val syncDao: SyncDao
) : ConflictResolver {

    override suspend fun inspectAndResolve(
        entityType: String,
        entityId: String,
        ownerId: String,
        localJson: String,
        remoteJson: String
    ): Boolean {
        if (localJson == remoteJson) return true

        val nowIso = Clock.System.now().toString()
        val conflictId = "cnf_${entityType}_${entityId}_${Clock.System.now().toEpochMilliseconds()}"

        syncDao.insertConflict(
            SyncConflictEntity(
                id = conflictId,
                entityType = entityType,
                entityId = entityId,
                ownerId = ownerId,
                localDataJson = localJson,
                remoteDataJson = remoteJson,
                conflictReason = "CONCURRENT_MODIFICATION",
                isResolved = false,
                createdAt = nowIso
            )
        )
        return false
    }
}
