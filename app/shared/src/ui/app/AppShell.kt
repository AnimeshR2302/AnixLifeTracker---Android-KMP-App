package com.anix.android.anixlifetracker.ui.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.*
import com.anix.android.anixlifetracker.navigation.AppRoute
import com.anix.android.anixlifetracker.theme.AnixTheme
import com.anix.android.anixlifetracker.ui.dashboard.DashboardScreen
import com.anix.android.anixlifetracker.ui.events.EventsScreen
import com.anix.android.anixlifetracker.ui.finances.FinancesScreen
import com.anix.android.anixlifetracker.ui.investments.InvestmentsScreen
import com.anix.android.anixlifetracker.ui.reminders.RemindersScreen
import com.anix.android.anixlifetracker.ui.settings.SettingsScreen
import com.anix.android.anixlifetracker.ui.travel.TravelScreen
import com.anix.android.anixlifetracker.viewmodel.AppViewModel

@Composable
fun AppShell(
    viewModel: AppViewModel = remember { AppViewModel() }
) {
    val appState by viewModel.appState.collectAsState()

    val onQuickAction: (String) -> Unit = { actionTitle ->
        viewModel.showQuickActionNotice(actionTitle)
    }

    AnixTheme(themeMode = appState.themeMode) {
        // Quick Action Notice Dialog
        if (appState.quickActionNoticeMessage != null) {
            ConfirmationDialog(
                title = "Demonstration Action",
                message = appState.quickActionNoticeMessage ?: "",
                confirmLabel = "Understand",
                dismissLabel = "Close",
                onConfirm = { viewModel.clearQuickActionNotice() },
                onDismiss = { viewModel.clearQuickActionNotice() }
            )
        }

        AppScaffold(
            currentRoute = appState.currentRoute,
            canGoBack = viewModel.navigator.canGoBack,
            themeMode = appState.themeMode,
            isDemoNoticeVisible = appState.isDemoNoticeVisible,
            onRouteSelected = { route -> viewModel.navigateTo(route) },
            onBackClick = { viewModel.popBackStack() },
            onThemeToggle = { viewModel.toggleThemeMode() },
            onSearchClick = { viewModel.toggleSearchActive() },
            onDismissDemoNotice = { viewModel.toggleDemoNotice() },
            onQuickActionClick = { viewModel.showQuickActionNotice("Global Quick Action") }
        ) { _ ->
            Column(modifier = Modifier.fillMaxSize()) {
                if (appState.isSearchActive) {
                    SearchInput(
                        query = appState.searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Crossfade(targetState = appState.currentRoute) { route ->
                    when (route) {
                        AppRoute.Dashboard -> DashboardScreen(
                            onNavigateToFinances = { viewModel.navigateTo(AppRoute.Finances) },
                            onNavigateToInvestments = { viewModel.navigateTo(AppRoute.Investments) },
                            onNavigateToReminders = { viewModel.navigateTo(AppRoute.Reminders) },
                            onQuickAction = onQuickAction
                        )
                        AppRoute.Finances -> FinancesScreen(
                            onQuickActionNotice = onQuickAction
                        )
                        AppRoute.Investments -> InvestmentsScreen(
                            onQuickActionNotice = onQuickAction
                        )
                        AppRoute.Reminders -> RemindersScreen(
                            onQuickActionNotice = onQuickAction
                        )
                        AppRoute.Travel -> TravelScreen(
                            onQuickActionNotice = onQuickAction
                        )
                        AppRoute.Events -> EventsScreen(
                            onQuickActionNotice = onQuickAction
                        )
                        AppRoute.Settings -> SettingsScreen(
                            currentThemeMode = appState.themeMode,
                            onThemeChange = { mode -> viewModel.setThemeMode(mode) },
                            onQuickActionNotice = onQuickAction
                        )
                    }
                }
            }
        }
    }
}
