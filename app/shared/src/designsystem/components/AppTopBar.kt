package com.anix.android.anixlifetracker.designsystem.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.navigation.AppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentRoute: AppRoute,
    canGoBack: Boolean,
    themeMode: ThemeMode,
    onBackClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onOpenSidebarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = currentRoute.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onBackClick) {
                    Text("←", style = MaterialTheme.typography.titleMedium)
                }
            } else if (onOpenSidebarClick != null) {
                IconButton(onClick = onOpenSidebarClick) {
                    Text("☰", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Text("🔍", style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onThemeToggle) {
                val symbol = when (themeMode) {
                    ThemeMode.SYSTEM -> "🌗"
                    ThemeMode.LIGHT -> "☀️"
                    ThemeMode.DARK -> "🌙"
                }
                Text(symbol, style = MaterialTheme.typography.titleMedium)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    )
}
