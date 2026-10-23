package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.navigation.AppNavigator
import com.anix.android.anixlifetracker.navigation.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationTest {

    @Test
    fun testInitialRouteIsDashboard() {
        val navigator = AppNavigator()
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun testNavigationAndBackstack() {
        val navigator = AppNavigator()
        navigator.navigateTo(AppRoute.Finances)
        assertEquals(AppRoute.Finances, navigator.currentRoute)
        assertTrue(navigator.canGoBack)

        navigator.navigateTo(AppRoute.Settings)
        assertEquals(AppRoute.Settings, navigator.currentRoute)

        val popped = navigator.popBackStack()
        assertTrue(popped)
        assertEquals(AppRoute.Finances, navigator.currentRoute)

        navigator.popBackStack()
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun testRouteFromPath() {
        assertEquals(AppRoute.Dashboard, AppRoute.fromPath("dashboard"))
        assertEquals(AppRoute.Finances, AppRoute.fromPath("/finances"))
        assertEquals(AppRoute.Investments, AppRoute.fromPath("#investments"))
        assertEquals(AppRoute.Dashboard, AppRoute.fromPath("unknown_route"))
    }
}
