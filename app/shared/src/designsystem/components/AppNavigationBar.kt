package com.anix.android.anixlifetracker.designsystem.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.anix.android.anixlifetracker.navigation.AppRoute

@Composable
fun AppNavigationBar(
    currentRoute: AppRoute,
    onRouteSelected: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        AppRoute.ALL.forEach { route ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = { onRouteSelected(route) },
                icon = {
                    Text(
                        text = route.iconSymbol,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                label = {
                    Text(
                        text = route.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
