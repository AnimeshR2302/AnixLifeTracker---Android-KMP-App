package com.anix.android.anixlifetracker.viewmodel

import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.navigation.AppNavigator
import com.anix.android.anixlifetracker.navigation.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentRoute: AppRoute = AppRoute.Dashboard,
    val isDemoNoticeVisible: Boolean = true,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val quickActionNoticeMessage: String? = null
)

class AppViewModel(val navigator: AppNavigator = AppNavigator()) {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        // Synchronize route changes from navigator
        _appState.value = _appState.value.copy(currentRoute = navigator.currentRoute)
    }

    fun navigateTo(route: AppRoute) {
        navigator.navigateTo(route)
        _appState.value = _appState.value.copy(currentRoute = navigator.currentRoute)
    }

    fun popBackStack(): Boolean {
        val popped = navigator.popBackStack()
        if (popped) {
            _appState.value = _appState.value.copy(currentRoute = navigator.currentRoute)
        }
        return popped
    }

    fun toggleThemeMode() {
        val nextMode = when (_appState.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        setThemeMode(nextMode)
    }

    fun setThemeMode(mode: ThemeMode) {
        _appState.value = _appState.value.copy(themeMode = mode)
    }

    fun toggleDemoNotice() {
        _appState.value = _appState.value.copy(isDemoNoticeVisible = !_appState.value.isDemoNoticeVisible)
    }

    fun updateSearchQuery(query: String) {
        _appState.value = _appState.value.copy(searchQuery = query)
    }

    fun toggleSearchActive() {
        _appState.value = _appState.value.copy(isSearchActive = !_appState.value.isSearchActive)
    }

    fun showQuickActionNotice(actionTitle: String) {
        _appState.value = _appState.value.copy(
            quickActionNoticeMessage = "$actionTitle — Demonstration Action. Database persistence will be integrated in Plan 3."
        )
    }

    fun clearQuickActionNotice() {
        _appState.value = _appState.value.copy(quickActionNoticeMessage = null)
    }
}
