package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.model.SyncStatus
import kotlinx.serialization.Serializable

/**
 * Explicit trigger origin for sync operations to enforce platform sync contract
 */
enum class SyncReason {
    ANDROID_FOREGROUND,
    USER_SYNC,
    REMOTE_USER_SAVE,
    CONNECTIVITY_RESTORE
}

/**
 * Detailed sync status metrics for platform status UI and diagnostics
 */
data class SyncStatusDetails(
    val localPendingCount: Int = 0,
    val failedCount: Int = 0,
    val unresolvedConflictCount: Int = 0,
    val lastSuccessfulPullISO: String? = null,
    val lastSuccessfulExchangeISO: String? = null,
    val lastAndroidUploadISO: String? = null
)

sealed interface SyncState {
    data object Idle : SyncState
    data class Syncing(val progressMessage: String = "Synchronizing data...") : SyncState
    data class Success(val lastSyncTimeISO: String, val recordsSyncedCount: Int) : SyncState
    data class Error(val message: String, val isOffline: Boolean = false) : SyncState
    data class ConflictDetected(val conflicts: List<SyncConflict>) : SyncState
}

sealed interface SyncResult {
    data class Success(
        val pushedCount: Int,
        val pulledCount: Int,
        val conflictsCount: Int
    ) : SyncResult

    data class Failure(
        val errorMessage: String,
        val isNetworkError: Boolean = false
    ) : SyncResult
}

@Serializable
data class SyncConflict(
    val id: String,
    val entityType: String,
    val entityId: String,
    val ownerId: String,
    val localDataJson: String,
    val remoteDataJson: String,
    val conflictReason: String,
    val detectedAtISO: String
)

enum class ConflictResolutionStrategy {
    KEEP_LOCAL,
    OVERWRITE_WITH_REMOTE,
    AUTO_MERGE
}

@Serializable
data class PendingChangesPayload(
    val ownerId: String,
    val accounts: List<String> = emptyList(), // JSON formatted account entities
    val transactions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val budgets: List<String> = emptyList(),
    val recurringPayments: List<String> = emptyList(),
    val upcomingObligations: List<String> = emptyList(),
    val investmentAccounts: List<String> = emptyList(),
    val investmentTransactions: List<String> = emptyList(),
    val sipSchedules: List<String> = emptyList(),
    val financialGoals: List<String> = emptyList(),
    val reminders: List<String> = emptyList(),
    val reminderSchedules: List<String> = emptyList(),
    val reminderOccurrences: List<String> = emptyList(),
    val reminderActions: List<String> = emptyList(),
    val inboxItems: List<String> = emptyList(),
    val reminderPreferences: List<String> = emptyList(),
    val deletedTombstones: List<TombstonePayload> = emptyList()
) {
    val totalCount: Int
        get() = accounts.size + transactions.size + categories.size + budgets.size +
                recurringPayments.size + upcomingObligations.size + investmentAccounts.size +
                investmentTransactions.size + sipSchedules.size + financialGoals.size +
                reminders.size + reminderSchedules.size + reminderOccurrences.size +
                reminderActions.size + inboxItems.size + reminderPreferences.size +
                deletedTombstones.size
}

@Serializable
data class TombstonePayload(
    val entityType: String,
    val entityId: String,
    val deletedAtISO: String
)

@Serializable
data class RemoteChangesPayload(
    val ownerId: String,
    val accounts: List<String> = emptyList(),
    val transactions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val budgets: List<String> = emptyList(),
    val recurringPayments: List<String> = emptyList(),
    val upcomingObligations: List<String> = emptyList(),
    val investmentAccounts: List<String> = emptyList(),
    val investmentTransactions: List<String> = emptyList(),
    val sipSchedules: List<String> = emptyList(),
    val financialGoals: List<String> = emptyList(),
    val reminders: List<String> = emptyList(),
    val reminderSchedules: List<String> = emptyList(),
    val reminderOccurrences: List<String> = emptyList(),
    val reminderActions: List<String> = emptyList(),
    val inboxItems: List<String> = emptyList(),
    val reminderPreferences: List<String> = emptyList(),
    val deletedTombstones: List<TombstonePayload> = emptyList(),
    val serverCheckpointTimestamp: String
)
