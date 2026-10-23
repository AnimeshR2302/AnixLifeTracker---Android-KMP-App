package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.notification.collector.NotificationCollector
import com.anix.android.anixlifetracker.repository.NotificationRepository
import com.anix.android.anixlifetracker.repository.NotificationRepositoryImpl
import com.anix.android.anixlifetracker.usecase.ManageNotificationSettingsUseCase
import com.anix.android.anixlifetracker.usecase.ManageSupportedAppsUseCase
import com.anix.android.anixlifetracker.usecase.ReviewNotificationCandidatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class NotificationUiState(
    val settings: NotificationSettingsModel = NotificationSettingsModel(),
    val supportedApps: List<FinancialAppProfile> = emptyList(),
    val pendingCandidates: List<NotificationReviewCandidateModel> = emptyList(),
    val isAccessGranted: Boolean = false,
    val isServiceConnected: Boolean = false,
    val isConsentDialogOpen: Boolean = false,
    val isAppSelectionDialogOpen: Boolean = false,
    val isReviewQueueDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepositoryImpl(DatabaseManager.getDatabase()),
    private val collector: NotificationCollector = NotificationCollector(),
    private val manageSettingsUseCase: ManageNotificationSettingsUseCase = ManageNotificationSettingsUseCase(repository),
    private val manageSupportedAppsUseCase: ManageSupportedAppsUseCase = ManageSupportedAppsUseCase(repository),
    private val reviewCandidatesUseCase: ReviewNotificationCandidatesUseCase = ReviewNotificationCandidatesUseCase(repository),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        checkAccessStatus()

        scope.launch {
            manageSettingsUseCase.observeSettings()
                .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(settings = settings)
                }
        }

        scope.launch {
            manageSupportedAppsUseCase.observeSupportedApps()
                .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                .collect { apps ->
                    _uiState.value = _uiState.value.copy(supportedApps = apps)
                }
        }

        scope.launch {
            reviewCandidatesUseCase.observePendingCandidates()
                .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                .collect { candidates ->
                    _uiState.value = _uiState.value.copy(pendingCandidates = candidates)
                }
        }
    }

    fun checkAccessStatus() {
        val granted = collector.isNotificationAccessGranted()
        val connected = collector.isListenerServiceConnected()
        _uiState.value = _uiState.value.copy(
            isAccessGranted = granted,
            isServiceConnected = connected
        )
    }

    fun openNotificationListenerSettings() {
        collector.openNotificationListenerSettings()
    }

    fun openConsentDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isConsentDialogOpen = isOpen)
    }

    fun openAppSelectionDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAppSelectionDialogOpen = isOpen)
    }

    fun openReviewQueueDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isReviewQueueDialogOpen = isOpen)
    }

    fun giveConsentAndEnable() {
        scope.launch {
            val updated = _uiState.value.settings.copy(
                isNotificationFeatureEnabled = true,
                isConsentGiven = true,
                liveMonitoringEnabled = true
            )
            manageSettingsUseCase.updateSettings(updated)
            _uiState.value = _uiState.value.copy(isConsentDialogOpen = false)
        }
    }

    fun revokeConsentAndDisable() {
        scope.launch {
            val updated = _uiState.value.settings.copy(
                isNotificationFeatureEnabled = false,
                isConsentGiven = false,
                liveMonitoringEnabled = false
            )
            manageSettingsUseCase.updateSettings(updated)
        }
    }

    fun toggleLiveMonitoring(enabled: Boolean) {
        scope.launch {
            val updated = _uiState.value.settings.copy(liveMonitoringEnabled = enabled)
            manageSettingsUseCase.updateSettings(updated)
        }
    }

    fun toggleAutoSaveHighConfidence(enabled: Boolean) {
        scope.launch {
            val updated = _uiState.value.settings.copy(autoSaveHighConfidence = enabled)
            manageSettingsUseCase.updateSettings(updated)
        }
    }

    fun toggleAppEnabled(packageName: String, isEnabled: Boolean) {
        scope.launch {
            manageSupportedAppsUseCase.setAppEnabled(packageName, isEnabled)
        }
    }

    fun confirmCandidate(candidateId: String, correctedCandidate: TransactionCandidate) {
        scope.launch {
            try {
                val result = reviewCandidatesUseCase.confirmCandidate(candidateId, correctedCandidate)
                if (result is IngestionResult.Failed) {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun rejectCandidate(candidateId: String) {
        scope.launch {
            try {
                reviewCandidatesUseCase.rejectCandidate(candidateId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearReviewQueue() {
        scope.launch {
            try {
                reviewCandidatesUseCase.clearAll()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
