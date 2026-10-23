package com.anix.android.anixlifetracker.ui.notification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.SectionHeader
import com.anix.android.anixlifetracker.viewmodel.FinancesViewModel
import com.anix.android.anixlifetracker.viewmodel.NotificationViewModel

@Composable
fun NotificationSettingsCard(
    viewModel: NotificationViewModel = remember { NotificationViewModel() },
    financesViewModel: FinancesViewModel = remember { FinancesViewModel() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val financesUiState by financesViewModel.uiState.collectAsState()

    val settings = uiState.settings
    val enabledAppCount = uiState.supportedApps.count { it.isEnabled }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = "Financial Notification Tracking",
            subtitle = "Automatic transaction reading from authorized Android banking and payment app alerts"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Status Row
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
                                text = "Notification Listener",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (settings.isConsentGiven && uiState.isAccessGranted) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text("Active & Authorized", modifier = Modifier.padding(2.dp))
                                }
                            } else if (settings.isConsentGiven) {
                                Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                    Text("Access Required", modifier = Modifier.padding(2.dp))
                                }
                            } else {
                                Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text("Not Configured", modifier = Modifier.padding(2.dp))
                                }
                            }
                        }

                        Text(
                            text = if (settings.isConsentGiven)
                                "$enabledAppCount financial app(s) selected for monitoring"
                            else "Read payment alerts from Google Pay, PhonePe, Paytm, and banking apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!settings.isConsentGiven) {
                        Button(onClick = { viewModel.openConsentDialog(true) }) {
                            Text("Enable")
                        }
                    } else if (!uiState.isAccessGranted) {
                        Button(onClick = { viewModel.openNotificationListenerSettings() }) {
                            Text("Grant Access")
                        }
                    } else {
                        OutlinedButton(onClick = { viewModel.revokeConsentAndDisable() }) {
                            Text("Disable")
                        }
                    }
                }

                if (settings.isConsentGiven) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // App Selection Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Payment & Banking Apps",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Only notifications from explicitly enabled apps will be processed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(onClick = { viewModel.openAppSelectionDialog(true) }) {
                            Text("Select Apps ($enabledAppCount)")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Live Monitoring Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Background Monitoring",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Listen for new transaction notifications in real time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.liveMonitoringEnabled,
                            onCheckedChange = { viewModel.toggleLiveMonitoring(it) }
                        )
                    }

                    // Auto Save Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Confirm High Confidence",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatically add matched high confidence records to Room database",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.autoSaveHighConfidence,
                            onCheckedChange = { viewModel.toggleAutoSaveHighConfidence(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Review Queue & Stats Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Total Notifications Ingested: ${settings.totalImportedCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.pendingCandidates.isNotEmpty()) {
                                Text(
                                    text = "⚠️ ${uiState.pendingCandidates.size} candidate(s) awaiting review",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (uiState.pendingCandidates.isNotEmpty()) {
                            Button(onClick = { viewModel.openReviewQueueDialog(true) }) {
                                Text("Review Queue (${uiState.pendingCandidates.size})")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.isConsentDialogOpen) {
        NotificationConsentDialog(
            onConfirmConsent = {
                viewModel.giveConsentAndEnable()
                viewModel.openNotificationListenerSettings()
            },
            onDismiss = { viewModel.openConsentDialog(false) }
        )
    }

    if (uiState.isAppSelectionDialogOpen) {
        NotificationAppSelectionDialog(
            supportedApps = uiState.supportedApps,
            onToggleAppEnabled = { pkg, isEnabled ->
                viewModel.toggleAppEnabled(pkg, isEnabled)
            },
            onDismiss = { viewModel.openAppSelectionDialog(false) }
        )
    }

    if (uiState.isReviewQueueDialogOpen) {
        NotificationReviewQueueDialog(
            pendingCandidates = uiState.pendingCandidates,
            availableAccounts = financesUiState.accounts,
            onConfirmCandidate = { candidateId, corrected ->
                viewModel.confirmCandidate(candidateId, corrected)
            },
            onRejectCandidate = { candidateId ->
                viewModel.rejectCandidate(candidateId)
            },
            onClearAll = { viewModel.clearReviewQueue() },
            onDismiss = { viewModel.openReviewQueueDialog(false) }
        )
    }
}
