package com.anix.android.anixlifetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.auth.AuthState
import com.anix.android.anixlifetracker.designsystem.components.SectionHeader
import com.anix.android.anixlifetracker.model.ThemeMode
import com.anix.android.anixlifetracker.sync.SyncState
import com.anix.android.anixlifetracker.ui.auth.CloudSyncDisclosureDialog
import com.anix.android.anixlifetracker.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = remember { SettingsViewModel() },
    currentThemeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onQuickActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDisclosureModal by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Column {
                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preferences, themes, cloud sync, security & privacy controls",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Account & Auth Card
        item {
            SectionHeader(
                title = "User Account & Authentication",
                subtitle = "Optional cloud sync account management"
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.authState is AuthState.Authenticated)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        when (val auth = uiState.authState) {
                            is AuthState.Authenticated -> {
                                Text(
                                    text = auth.session.user.email,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cloud Account Active • Verified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            else -> {
                                Text(
                                    text = "Local-Only Anonymous User",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sign in or register to enable optional cross-device synchronization",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (uiState.authState is AuthState.Authenticated) {
                        OutlinedButton(onClick = { viewModel.signOut() }) {
                            Text("Sign Out")
                        }
                    } else {
                        Button(onClick = { onQuickActionNotice("Sign In / Account Registration Dialog Triggered") }) {
                            Text("Sign In / Register")
                        }
                    }
                }
            }
        }

        // Section 1: Appearance & Theme
        item {
            SectionHeader(
                title = "Appearance & Theme",
                subtitle = "Active for current application session"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Theme Selection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = currentThemeMode == mode,
                                onClick = { onThemeChange(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Regional & Monetary Format
        item {
            SectionHeader(
                title = "Monetary & Currency Preferences",
                subtitle = "Primary accounting display currency"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Primary Currency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("INR", "USD", "EUR", "GBP", "JPY").forEach { code ->
                            FilterChip(
                                selected = uiState.currencyCode == code,
                                onClick = { viewModel.setCurrency(code) },
                                label = { Text(code) }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: System Notifications & Alerts
        item {
            SectionHeader(
                title = "Notifications & Reminders",
                subtitle = "Local alerts for upcoming payments and SIPs"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local Reminder Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trigger native platform alerts for payment due dates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            }
        }

        // Section 4: SMS Financial Ingestion (Plan 5)
        item {
            com.anix.android.anixlifetracker.ui.sms.SmsSettingsCard()
        }

        // Section 5: Notification Financial Ingestion (Plan 6)
        item {
            com.anix.android.anixlifetracker.ui.notification.NotificationSettingsCard()
        }

        // Section 6: Security, Encryption & Privacy Controls (Plan 9)
        item {
            SectionHeader(
                title = "Security, Encryption & Privacy Controls",
                subtitle = "Hardware key vault, local database encryption & data deletion workflows"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Encryption Engine: ${uiState.encryptionState.platformName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Status: ${uiState.encryptionState.status.name} • ${uiState.encryptionState.userMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Data Retention & Destruction Workflows",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearLocalCache() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Local Cache", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = { viewModel.purgeLocalFinancialData() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Purge Local Data", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (uiState.authState is AuthState.Authenticated) {
                        OutlinedButton(
                            onClick = { viewModel.wipeCloudFinancialData() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Wipe Cloud Data & Reset Dataset Generation")
                        }
                    }
                }
            }
        }

        // Section 7: Cloud Synchronization & Platform Sync Contract
        item {
            SectionHeader(
                title = "Cloud Synchronization & Cross-Device Sync",
                subtitle = "Encrypted cross-platform synchronization between Android, Desktop, and Web"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cross-Device Data Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                when (val sync = uiState.syncState) {
                                    is SyncState.Syncing -> {
                                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                            Text(sync.progressMessage, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                    is SyncState.Success -> {
                                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                            Text("Synced", modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                    is SyncState.Error -> {
                                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                            Text(if (sync.isOffline) "Offline" else "Sync Error", modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                    is SyncState.ConflictDetected -> {
                                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                            Text("Conflict Detected", modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                    is SyncState.Idle -> {
                                        if (uiState.pendingChangesCount > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                                Text("${uiState.pendingChangesCount} Local Edits Unsent", modifier = Modifier.padding(2.dp))
                                            }
                                        } else {
                                            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                                Text("Idle", modifier = Modifier.padding(2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Ktor/Supabase backed encrypted sync across Android, Desktop, and Web",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isCloudSyncEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && uiState.authState !is AuthState.Authenticated) {
                                    showDisclosureModal = true
                                } else {
                                    viewModel.toggleCloudSync(enabled)
                                }
                            }
                        )
                    }

                    if (uiState.isCloudSyncEnabled) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Last Android upload: ${uiState.syncDetails.lastAndroidUploadISO ?: uiState.lastSyncTime ?: "None"}; newer phone records require Sync on Android.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Last Pull: ${uiState.syncDetails.lastSuccessfulPullISO ?: uiState.lastSyncTime ?: "Not synced yet"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Pending Unsent Records: ${uiState.syncDetails.localPendingCount} • Unresolved Conflicts: ${uiState.syncDetails.unresolvedConflictCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = { viewModel.triggerSyncNow() }) {
                                    Text("Explicit Sync Now")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 8: About Application
        item {
            SectionHeader(
                title = "About Application",
                subtitle = "Build info & multiplatform diagnostics"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AnixLifeTracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version: ${uiState.appVersion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Target Platforms: ${uiState.buildTarget}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Architecture: Clean Architecture + MVVM + UDF + Offline-First",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDisclosureModal) {
        CloudSyncDisclosureDialog(
            onConfirmConsent = {
                showDisclosureModal = false
                viewModel.toggleCloudSync(true)
            },
            onDismiss = {
                showDisclosureModal = false
            }
        )
    }
}
