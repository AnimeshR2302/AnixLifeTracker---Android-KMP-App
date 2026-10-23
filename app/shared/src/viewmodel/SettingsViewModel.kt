package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.auth.AuthRepository
import com.anix.android.anixlifetracker.auth.AuthState
import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.error.AppResult
import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.security.EncryptionState
import com.anix.android.anixlifetracker.security.EncryptionStatus
import com.anix.android.anixlifetracker.security.KeyStorage
import com.anix.android.anixlifetracker.security.PrivacyManager
import com.anix.android.anixlifetracker.security.RedactingLogger
import com.anix.android.anixlifetracker.security.createPlatformKeyStorage
import com.anix.android.anixlifetracker.sync.ConflictResolutionStrategy
import com.anix.android.anixlifetracker.sync.RemoteDataSource
import com.anix.android.anixlifetracker.sync.SupabaseRemoteDataSource
import com.anix.android.anixlifetracker.sync.SyncReason
import com.anix.android.anixlifetracker.sync.SyncRepository
import com.anix.android.anixlifetracker.sync.SyncState
import com.anix.android.anixlifetracker.sync.SyncStatusDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String = "INR",
    val isNotificationsEnabled: Boolean = true,
    val isBiometricsEnabled: Boolean = false,
    val isCloudSyncEnabled: Boolean = false,
    val authState: AuthState = AuthState.Unauthenticated,
    val syncState: SyncState = SyncState.Idle,
    val syncDetails: SyncStatusDetails = SyncStatusDetails(),
    val lastSyncTime: String? = null,
    val pendingChangesCount: Int = 0,
    val encryptionState: EncryptionState = EncryptionState(
        status = EncryptionStatus.SUPPORTED,
        platformName = "Room SQLite / Key Vault",
        isHardwareBacked = true,
        userMessage = "Encrypted Local Storage Active"
    ),
    val appVersion: String = "1.0.0-alpha01",
    val buildTarget: String = "Multiplatform (Android, JVM, WasmJS)",
    val noticeMessage: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository? = null,
    private val syncRepository: SyncRepository? = null,
    private val keyStorage: KeyStorage = createPlatformKeyStorage(null),
    private val remoteDataSource: RemoteDataSource = SupabaseRemoteDataSource(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var privacyManager: PrivacyManager? = null

    init {
        val db = DatabaseManager.getDatabase()
        authRepository?.let { repo ->
            privacyManager = PrivacyManager(db, repo, remoteDataSource)
            scope.launch {
                repo.observeAuthState().collect { auth ->
                    _uiState.value = _uiState.value.copy(authState = auth)
                }
            }
        }

        scope.launch {
            keyStorage.getEncryptionState().collect { enc ->
                _uiState.value = _uiState.value.copy(encryptionState = enc)
            }
        }

        syncRepository?.let { repo ->
            scope.launch {
                repo.isCloudSyncEnabled().collect { enabled ->
                    _uiState.value = _uiState.value.copy(isCloudSyncEnabled = enabled)
                }
            }
            scope.launch {
                repo.observeSyncState().collect { sync ->
                    val lastTime = repo.getLastSyncTimestamp()
                    val pendingCount = repo.getPendingChangesCount()
                    _uiState.value = _uiState.value.copy(
                        syncState = sync,
                        lastSyncTime = lastTime,
                        pendingChangesCount = pendingCount
                    )
                }
            }
            scope.launch {
                repo.observeSyncDetails().collect { details ->
                    _uiState.value = _uiState.value.copy(syncDetails = details)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setCurrency(code: String) {
        _uiState.value = _uiState.value.copy(currencyCode = code)
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationsEnabled = enabled)
    }

    fun toggleBiometrics(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isBiometricsEnabled = enabled)
    }

    fun toggleCloudSync(enabled: Boolean) {
        scope.launch {
            syncRepository?.setCloudSyncEnabled(enabled)
            _uiState.value = _uiState.value.copy(isCloudSyncEnabled = enabled)
        }
    }

    fun triggerSyncNow() {
        scope.launch {
            syncRepository?.syncExplicit(SyncReason.USER_SYNC)
            val lastTime = syncRepository?.getLastSyncTimestamp()
            val pendingCount = syncRepository?.getPendingChangesCount() ?: 0
            _uiState.value = _uiState.value.copy(
                lastSyncTime = lastTime,
                pendingChangesCount = pendingCount
            )
        }
    }

    fun signOut() {
        scope.launch {
            authRepository?.signOut()
            syncRepository?.setCloudSyncEnabled(false)
        }
    }

    fun clearLocalCache() {
        privacyManager?.clearLocalCache()
        _uiState.value = _uiState.value.copy(noticeMessage = "Local cache cleared.")
    }

    fun purgeLocalFinancialData() {
        val ownerId = (uiState.value.authState as? AuthState.Authenticated)?.session?.user?.id ?: "local_user"
        scope.launch {
            val res = privacyManager?.clearLocalFinancialData(ownerId)
            if (res is AppResult.Success) {
                _uiState.value = _uiState.value.copy(noticeMessage = "Local financial records purged.")
            }
        }
    }

    fun wipeCloudFinancialData() {
        val ownerId = (uiState.value.authState as? AuthState.Authenticated)?.session?.user?.id ?: "local_user"
        scope.launch {
            val res = privacyManager?.deleteCloudFinancialData(ownerId)
            if (res is AppResult.Success) {
                _uiState.value = _uiState.value.copy(noticeMessage = "Cloud data wipe requested.")
            }
        }
    }

    fun resolveConflict(conflictId: String, strategy: ConflictResolutionStrategy) {
        scope.launch {
            syncRepository?.resolveConflict(conflictId, strategy)
            triggerSyncNow()
        }
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(noticeMessage = null)
    }
}
