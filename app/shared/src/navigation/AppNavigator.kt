package com.anix.android.anixlifetracker.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNavigator(initialRoute: AppRoute = AppRoute.Dashboard) {
    private val _backStack = MutableStateFlow(listOf(initialRoute))
    val backStack: StateFlow<List<AppRoute>> = _backStack.asStateFlow()

    val currentRoute: AppRoute
        get() = _backStack.value.lastOrNull() ?: AppRoute.Dashboard

    val canGoBack: Boolean
        get() = _backStack.value.size > 1

    fun navigateTo(route: AppRoute) {
        if (currentRoute == route) return
        val currentList = _backStack.value.toMutableList()
        // If route exists in stack, pop up to it
        val existingIndex = currentList.indexOf(route)
        if (existingIndex != -1) {
            _backStack.value = currentList.subList(0, existingIndex + 1)
        } else {
            currentList.add(route)
            _backStack.value = currentList
        }
    }

    fun popBackStack(): Boolean {
        val currentList = _backStack.value.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _backStack.value = currentList
            return true
        }
        return false
    }

    fun replaceTop(route: AppRoute) {
        val currentList = _backStack.value.toMutableList()
        if (currentList.isNotEmpty()) {
            currentList[currentList.lastIndex] = route
        } else {
            currentList.add(route)
        }
        _backStack.value = currentList
    }
}
