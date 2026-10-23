package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.repository.SmsRepository
import com.anix.android.anixlifetracker.repository.SmsRepositoryImpl
import com.anix.android.anixlifetracker.sms.collector.SmsCollector
import com.anix.android.anixlifetracker.usecase.ImportHistoricalSmsUseCase
import com.anix.android.anixlifetracker.usecase.ManageSmsSettingsUseCase
import com.anix.android.anixlifetracker.usecase.ReviewSmsCandidatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SmsUiState(
    val settings: SmsSettingsModel = SmsSettingsModel(),
    val pendingCandidates: List<SmsReviewCandidateModel> = emptyList(),
    val isConsentDialogOpen: Boolean = false,
    val isImportDialogOpen: Boolean = false,
    val isReviewQueueDialogOpen: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val isImporting: Boolean = false,
    val importProgressScanned: Int = 0,
    val importProgressFinancial: Int = 0,
    val importProgressImported: Int = 0,
    val importProgressSkipped: Int = 0,
    val lastImportSummary: HistoricalImportSummary? = null,
    val errorMessage: String? = null
)

class SmsViewModel(
    private val smsRepository: SmsRepository = SmsRepositoryImpl(DatabaseManager.getDatabase()),
    private val smsCollector: SmsCollector = SmsCollector(),
    private val manageSettingsUseCase: ManageSmsSettingsUseCase = ManageSmsSettingsUseCase(smsRepository),
    private val importHistoricalUseCase: ImportHistoricalSmsUseCase = ImportHistoricalSmsUseCase(smsRepository, smsCollector),
    private val reviewCandidatesUseCase: ReviewSmsCandidatesUseCase = ReviewSmsCandidatesUseCase(smsRepository),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(SmsUiState())
    val uiState: StateFlow<SmsUiState> = _uiState.asStateFlow()

    init {
        checkPermissionStatus()

        scope.launch {
            manageSettingsUseCase.observeSettings()
                .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(settings = settings)
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

    fun checkPermissionStatus() {
        val granted = smsCollector.isSmsPermissionGranted()
        _uiState.value = _uiState.value.copy(isPermissionGranted = granted)
    }

    fun openConsentDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isConsentDialogOpen = isOpen)
    }

    fun openImportDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isImportDialogOpen = isOpen)
    }

    fun openReviewQueueDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isReviewQueueDialogOpen = isOpen)
    }

    fun giveConsentAndEnable() {
        scope.launch {
            val updated = _uiState.value.settings.copy(
                isSmsFeatureEnabled = true,
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
                isSmsFeatureEnabled = false,
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

    fun runHistoricalImport(daysRange: Int) {
        _uiState.value = _uiState.value.copy(
            isImporting = true,
            importProgressScanned = 0,
            importProgressFinancial = 0,
            importProgressImported = 0,
            importProgressSkipped = 0,
            lastImportSummary = null
        )

        scope.launch(Dispatchers.Default) {
            try {
                val summary = importHistoricalUseCase(
                    daysRange = daysRange,
                    onProgress = { scanned, financial, imported, skipped ->
                        _uiState.value = _uiState.value.copy(
                            importProgressScanned = scanned,
                            importProgressFinancial = financial,
                            importProgressImported = imported,
                            importProgressSkipped = skipped
                        )
                    }
                )

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    lastImportSummary = summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = e.message ?: "Historical import failed"
                )
            }
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
