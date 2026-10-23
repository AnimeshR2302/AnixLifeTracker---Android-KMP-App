package com.anix.android.anixlifetracker.database.dao

import androidx.room.*
import com.anix.android.anixlifetracker.database.entity.*
import com.anix.android.anixlifetracker.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    // Checkpoint operations
    @Query("SELECT * FROM sync_checkpoints WHERE entityType = :entityType LIMIT 1")
    suspend fun getCheckpoint(entityType: String): SyncCheckpointEntity?

    @Query("SELECT * FROM sync_checkpoints")
    suspend fun getAllCheckpoints(): List<SyncCheckpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCheckpoint(checkpoint: SyncCheckpointEntity)

    // Tombstone operations
    @Query("SELECT * FROM sync_tombstones WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_DELETE', 'PENDING_UPLOAD')")
    suspend fun getPendingTombstones(): List<SyncTombstoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTombstone(tombstone: SyncTombstoneEntity)

    @Query("DELETE FROM sync_tombstones WHERE id = :id")
    suspend fun deleteTombstone(id: String)

    // Conflict operations
    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun observeUnresolvedConflicts(): Flow<List<SyncConflictEntity>>

    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0")
    suspend fun getUnresolvedConflicts(): List<SyncConflictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity)

    @Query("UPDATE sync_conflicts SET isResolved = 1 WHERE id = :id")
    suspend fun markConflictResolved(id: String)

    // Pending entity state queries
    @Query("SELECT * FROM accounts WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_UPLOAD')")
    suspend fun getPendingAccounts(): List<AccountEntity>

    @Query("SELECT * FROM transactions WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_UPLOAD', 'PENDING_DELETE')")
    suspend fun getPendingTransactions(): List<TransactionEntity>

    @Query("UPDATE accounts SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateAccountSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE transactions SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateTransactionSyncStatus(ids: List<String>, status: SyncStatus)

    // Re-associate local entities when user logs in and enables sync (Local Data Ownership Migration)
    @Query("UPDATE accounts SET ownerId = :newOwnerId, syncStatus = 'PENDING_UPLOAD' WHERE ownerId = 'local_user'")
    suspend fun migrateAccountsOwner(newOwnerId: String): Int

    @Query("UPDATE transactions SET ownerId = :newOwnerId, syncStatus = 'PENDING_UPLOAD' WHERE ownerId = 'local_user'")
    suspend fun migrateTransactionsOwner(newOwnerId: String): Int

    @Query("UPDATE categories SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateCategoriesOwner(newOwnerId: String): Int

    @Query("UPDATE budgets SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateBudgetsOwner(newOwnerId: String): Int

    @Query("UPDATE recurring_payments SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateRecurringPaymentsOwner(newOwnerId: String): Int

    @Query("UPDATE upcoming_obligations SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateUpcomingObligationsOwner(newOwnerId: String): Int

    @Query("UPDATE investment_accounts SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateInvestmentAccountsOwner(newOwnerId: String): Int

    @Query("UPDATE investment_transactions SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateInvestmentTransactionsOwner(newOwnerId: String): Int

    @Query("UPDATE sip_schedules SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateSipSchedulesOwner(newOwnerId: String): Int

    @Query("UPDATE financial_goals SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateFinancialGoalsOwner(newOwnerId: String): Int

    @Query("UPDATE reminders SET ownerId = :newOwnerId WHERE ownerId = 'local_user'")
    suspend fun migrateRemindersOwner(newOwnerId: String): Int
}
