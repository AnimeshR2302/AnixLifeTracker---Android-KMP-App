package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.navigation.AppRoute
import com.anix.android.anixlifetracker.viewmodel.AppViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppViewModelTest {

    @Test
    fun testThemeModeToggle() {
        val viewModel = AppViewModel()
        assertEquals(ThemeMode.SYSTEM, viewModel.appState.value.themeMode)

        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.LIGHT, viewModel.appState.value.themeMode)

        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.DARK, viewModel.appState.value.themeMode)

        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.SYSTEM, viewModel.appState.value.themeMode)
    }

    @Test
    fun testNavigationInViewModel() {
        val viewModel = AppViewModel()
        assertEquals(AppRoute.Dashboard, viewModel.appState.value.currentRoute)

        viewModel.navigateTo(AppRoute.Investments)
        assertEquals(AppRoute.Investments, viewModel.appState.value.currentRoute)

        viewModel.popBackStack()
        assertEquals(AppRoute.Dashboard, viewModel.appState.value.currentRoute)
    }

    @Test
    fun testDemoNoticeToggle() {
        val viewModel = AppViewModel()
        assertTrue(viewModel.appState.value.isDemoNoticeVisible)

        viewModel.toggleDemoNotice()
        assertFalse(viewModel.appState.value.isDemoNoticeVisible)
    }
}
