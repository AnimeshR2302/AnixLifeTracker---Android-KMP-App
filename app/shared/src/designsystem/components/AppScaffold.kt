package com.anix.android.anixlifetracker.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.navigation.AppRoute

enum class LayoutType {
    COMPACT, MEDIUM, EXPANDED
}

@Composable
fun AppScaffold(
    currentRoute: AppRoute,
    canGoBack: Boolean,
    themeMode: ThemeMode,
    isDemoNoticeVisible: Boolean,
    onRouteSelected: (AppRoute) -> Unit,
    onBackClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onDismissDemoNotice: () -> Unit,
    onQuickActionClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowWidth = maxWidth
        val layoutType = when {
            windowWidth < 600.dp -> LayoutType.COMPACT
            windowWidth < 840.dp -> LayoutType.MEDIUM
            else -> LayoutType.EXPANDED
        }

        var isDrawerOpen by remember { mutableStateOf(false) }

        when (layoutType) {
            LayoutType.COMPACT -> {
                Scaffold(
                    topBar = {
                        AppTopBar(
                            currentRoute = currentRoute,
                            canGoBack = canGoBack,
                            themeMode = themeMode,
                            onBackClick = onBackClick,
                            onThemeToggle = onThemeToggle,
                            onSearchClick = onSearchClick
                        )
                    },
                    bottomBar = {
                        AppNavigationBar(
                            currentRoute = currentRoute,
                            onRouteSelected = onRouteSelected
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = onQuickActionClick,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        AnimatedVisibility(visible = isDemoNoticeVisible) {
                            DemoDataNotice(
                                onDismiss = onDismissDemoNotice,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        content(PaddingValues(0.dp))
                    }
                }
            }

            LayoutType.MEDIUM -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    AppNavigationRail(
                        currentRoute = currentRoute,
                        onRouteSelected = onRouteSelected,
                        onQuickActionClick = onQuickActionClick
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Scaffold(
                        topBar = {
                            AppTopBar(
                                currentRoute = currentRoute,
                                canGoBack = canGoBack,
                                themeMode = themeMode,
                                onBackClick = onBackClick,
                                onThemeToggle = onThemeToggle,
                                onSearchClick = onSearchClick
                            )
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            AnimatedVisibility(visible = isDemoNoticeVisible) {
                                DemoDataNotice(
                                    onDismiss = onDismissDemoNotice,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            content(PaddingValues(0.dp))
                        }
                    }
                }
            }

            LayoutType.EXPANDED -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    AppNavigationSidebar(
                        currentRoute = currentRoute,
                        onRouteSelected = onRouteSelected,
                        onQuickActionClick = onQuickActionClick
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Scaffold(
                            topBar = {
                                AppTopBar(
                                    currentRoute = currentRoute,
                                    canGoBack = canGoBack,
                                    themeMode = themeMode,
                                    onBackClick = onBackClick,
                                    onThemeToggle = onThemeToggle,
                                    onSearchClick = onSearchClick
                                )
                            },
                            modifier = Modifier.widthIn(max = 1400.dp)
                        ) { innerPadding ->
                            Column(modifier = Modifier.padding(innerPadding)) {
                                AnimatedVisibility(visible = isDemoNoticeVisible) {
                                    DemoDataNotice(
                                        onDismiss = onDismissDemoNotice,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }
                                content(PaddingValues(0.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
