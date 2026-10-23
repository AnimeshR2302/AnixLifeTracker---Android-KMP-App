package com.anix.android.anixlifetracker.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.navigation.AppRoute

@Composable
fun AppNavigationRail(
    currentRoute: AppRoute,
    onRouteSelected: (AppRoute) -> Unit,
    onQuickActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = "⚡",
                    style = MaterialTheme.typography.headlineSmall
                )
                FloatingActionButton(
                    onClick = onQuickActionClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        modifier = modifier
    ) {
        AppRoute.ALL.forEach { route ->
            val selected = currentRoute == route
            NavigationRailItem(
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
                }
            )
        }
    }
}
