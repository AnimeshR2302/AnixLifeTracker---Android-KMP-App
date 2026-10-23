package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EventsUiState(
    val events: List<EventItem> = DemoDataProvider.events,
    val isAddEventOpen: Boolean = false
)

class EventsViewModel {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun toggleAddEvent(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddEventOpen = isOpen)
    }
}
