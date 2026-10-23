package com.anix.android.anixlifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.android.anixlifetracker.database.DatabaseManager
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.notification.delivery.DisabledNotificationDeliveryGateway
import com.anix.android.anixlifetracker.notification.delivery.NotificationDeliveryGateway
import com.anix.android.anixlifetracker.repository.InAppInboxRepository
import com.anix.android.anixlifetracker.repository.InAppInboxRepositoryImpl
import com.anix.android.anixlifetracker.repository.ReminderRepository
import com.anix.android.anixlifetracker.repository.ReminderRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class RemindersUiState(
    val occurrences: List<ReminderOccurrenceModel> = emptyList(),
    val definitions: List<ReminderDefinitionModel> = emptyList(),
    val inboxItems: List<InAppInboxItemModel> = emptyList(),
    val preferences: ReminderPreferencesModel = ReminderPreferencesModel(),
    val selectedTab: Int = 0,
    val selectedCategory: ReminderCategory? = null,
    val deliveryCapability: DeliveryCapabilityStatus = DeliveryCapabilityStatus(
        isEnabled = false,
        isSupported = false,
        statusExplanation = "Delivery is not available in this version"
    ),
    val isCreateDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

class RemindersViewModel(
    private val reminderRepository: ReminderRepository = ReminderRepositoryImpl(DatabaseManager.getDatabase()),
    private val inboxRepository: InAppInboxRepository = InAppInboxRepositoryImpl(DatabaseManager.getDatabase()),
    private val deliveryGateway: NotificationDeliveryGateway = DisabledNotificationDeliveryGateway()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            launch {
                reminderRepository.observeActiveOccurrences()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { occurrences ->
                        _uiState.value = _uiState.value.copy(occurrences = occurrences)
                    }
            }

            launch {
                reminderRepository.observeDefinitions()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { defs ->
                        _uiState.value = _uiState.value.copy(definitions = defs)
                    }
            }

            launch {
                inboxRepository.observeActiveInboxItems()
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { inbox ->
                        _uiState.value = _uiState.value.copy(inboxItems = inbox)
                    }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun filterByCategory(category: ReminderCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun openCreateDialog(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isCreateDialogOpen = isOpen)
    }

    fun createCustomReminder(
        title: String,
        description: String?,
        dueDate: String,
        category: ReminderCategory,
        priority: ReminderPriority,
        isRecurring: Boolean,
        cadence: CadenceType
    ) {
        viewModelScope.launch {
            val nowIso = Clock.System.now().toString()
            val defId = "rem_${Clock.System.now().toEpochMilliseconds()}"

            val definition = ReminderDefinitionModel(
                id = defId,
                ownerId = "local_user",
                title = title,
                description = description,
                category = category,
                priority = priority,
                isEnabled = true,
                scheduleVersion = 1L,
                sourceType = "MANUAL",
                sourceEntityId = null,
                createdAt = nowIso,
                updatedAt = nowIso,
                serverRevision = 0L,
                syncStatus = SyncStatus.LOCAL_ONLY
            )

            val schedule = ReminderScheduleModel(
                id = "sch_$defId",
                reminderId = defId,
                isRecurring = isRecurring,
                localDueDateOrTime = dueDate,
                ianaTimezone = "Asia/Kolkata",
                dateOnlyPolicy = DateOnlyPolicy.LOCAL_DAY_END,
                cadence = if (isRecurring) cadence else CadenceType.ONE_OFF,
                anchorDay = null,
                endDate = null,
                endCount = null,
                leadTimesDays = 1,
                missedDatePolicy = MissedDatePolicy.GENERATE_LATEST,
                scheduleVersion = 1L,
                updatedAt = nowIso
            )

            reminderRepository.addDefinition(definition, schedule)
            _uiState.value = _uiState.value.copy(isCreateDialogOpen = false)
        }
    }

    fun performOccurrenceAction(occurrenceId: String, actionType: ReminderActionType) {
        viewModelScope.launch {
            reminderRepository.performOccurrenceAction(
                occurrenceId = occurrenceId,
                actionType = actionType
            )
        }
    }

    fun dismissInboxItem(itemId: String) {
        viewModelScope.launch {
            // Dismiss inbox item locally
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
