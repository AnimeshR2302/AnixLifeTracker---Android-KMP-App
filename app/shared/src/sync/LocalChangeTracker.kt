package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.database.dao.SyncDao
import com.anix.android.anixlifetracker.model.SyncStatus

interface LocalChangeTracker {
    suspend fun collectPendingChanges(ownerId: String): PendingChangesPayload
    suspend fun markPushedChangesSynced(ownerId: String, payload: PendingChangesPayload)
    suspend fun markPushedChangesFailed(ownerId: String, payload: PendingChangesPayload, error: String)
    suspend fun migrateLocalDataOwner(newOwnerId: String): Int
}

class RoomLocalChangeTracker(
    private val syncDao: SyncDao
) : LocalChangeTracker {

    override suspend fun collectPendingChanges(ownerId: String): PendingChangesPayload {
        val pendingAccounts = syncDao.getPendingAccounts().filter { it.ownerId == ownerId }
        val pendingTransactions = syncDao.getPendingTransactions().filter { it.ownerId == ownerId }
        val pendingTombstones = syncDao.getPendingTombstones().filter { it.ownerId == ownerId }

        return PendingChangesPayload(
            ownerId = ownerId,
            accounts = pendingAccounts.map { it.id },
            transactions = pendingTransactions.map { it.id },
            deletedTombstones = pendingTombstones.map {
                TombstonePayload(
                    entityType = it.entityType,
                    entityId = it.entityId,
                    deletedAtISO = it.deletedAt
                )
            }
        )
    }

    override suspend fun markPushedChangesSynced(ownerId: String, payload: PendingChangesPayload) {
        if (payload.accounts.isNotEmpty()) {
            syncDao.updateAccountSyncStatus(payload.accounts, SyncStatus.SYNCED)
        }
        if (payload.transactions.isNotEmpty()) {
            syncDao.updateTransactionSyncStatus(payload.transactions, SyncStatus.SYNCED)
        }
        payload.deletedTombstones.forEach {
            syncDao.deleteTombstone(it.entityId)
        }
    }

    override suspend fun markPushedChangesFailed(ownerId: String, payload: PendingChangesPayload, error: String) {
        if (payload.accounts.isNotEmpty()) {
            syncDao.updateAccountSyncStatus(payload.accounts, SyncStatus.SYNC_ERROR)
        }
        if (payload.transactions.isNotEmpty()) {
            syncDao.updateTransactionSyncStatus(payload.transactions, SyncStatus.SYNC_ERROR)
        }
    }

    override suspend fun migrateLocalDataOwner(newOwnerId: String): Int {
        var total = 0
        total += syncDao.migrateAccountsOwner(newOwnerId)
        total += syncDao.migrateTransactionsOwner(newOwnerId)
        total += syncDao.migrateCategoriesOwner(newOwnerId)
        total += syncDao.migrateBudgetsOwner(newOwnerId)
        total += syncDao.migrateRecurringPaymentsOwner(newOwnerId)
        total += syncDao.migrateUpcomingObligationsOwner(newOwnerId)
        total += syncDao.migrateInvestmentAccountsOwner(newOwnerId)
        total += syncDao.migrateInvestmentTransactionsOwner(newOwnerId)
        total += syncDao.migrateSipSchedulesOwner(newOwnerId)
        total += syncDao.migrateFinancialGoalsOwner(newOwnerId)
        total += syncDao.migrateRemindersOwner(newOwnerId)
        return total
    }
}
