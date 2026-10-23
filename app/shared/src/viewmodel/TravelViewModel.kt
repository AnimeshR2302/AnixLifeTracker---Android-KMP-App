package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TravelUiState(
    val trips: List<TripItem> = DemoDataProvider.trips,
    val isAddTripOpen: Boolean = false
)

class TravelViewModel {
    private val _uiState = MutableStateFlow(TravelUiState())
    val uiState: StateFlow<TravelUiState> = _uiState.asStateFlow()

    fun toggleAddTrip(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddTripOpen = isOpen)
    }
}
