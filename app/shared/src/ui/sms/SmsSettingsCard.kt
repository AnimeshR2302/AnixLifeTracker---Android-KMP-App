package com.anix.android.anixlifetracker.ui.sms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.SectionHeader
import com.anix.android.anixlifetracker.model.AccountModel
import com.anix.android.anixlifetracker.viewmodel.SmsViewModel

@Composable
fun SmsSettingsCard(
    viewModel: SmsViewModel = remember { SmsViewModel() },
    availableAccounts: List<AccountModel> = emptyList(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "SMS Financial Ingestion (Plan 5)",
            subtitle = "Automatic bank SMS collection, deterministic parsing & review queue"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Feature Enable Switch
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
                                text = "SMS Financial Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Permission status badge
                            if (uiState.isPermissionGranted) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text("Permission Granted", modifier = Modifier.padding(2.dp))
                                }
                            } else {
                                Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                    Text("No Permission / Sideload", modifier = Modifier.padding(2.dp))
                                }
                            }
                        }
                        Text(
                            text = "Parse bank & UPI messages locally to record income and expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = settings.isSmsFeatureEnabled && settings.isConsentGiven,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.openConsentDialog(true)
                            } else {
                                viewModel.revokeConsentAndDisable()
                            }
                        }
                    )
                }

                if (settings.isSmsFeatureEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Live monitoring toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Incoming SMS Receiver",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically process transaction messages as they arrive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.liveMonitoringEnabled,
                            onCheckedChange = { viewModel.toggleLiveMonitoring(it) }
                        )
                    }

                    // Auto-save toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Confirm High Confidence",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically save high confidence transactions with matching account",
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

                    // Actions: Historical Import & Review Queue
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Imported: ${settings.totalImportedCount} Transactions",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Last Scan: ${settings.lastImportTimestamp?.substringBefore("T") ?: "Never"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.openImportDialog(true) }) {
                                Text("Import Inbox")
                            }

                            Button(onClick = { viewModel.openReviewQueueDialog(true) }) {
                                Text("Review (${uiState.pendingCandidates.size})")
                            }
                        }
                    }
                }
            }
        }
    }

    // Consent Dialog
    if (uiState.isConsentDialogOpen) {
        SmsConsentDialog(
            onConfirmConsent = { viewModel.giveConsentAndEnable() },
            onDismiss = { viewModel.openConsentDialog(false) }
        )
    }

    // Historical Import Dialog
    if (uiState.isImportDialogOpen) {
        HistoricalImportDialog(
            isImporting = uiState.isImporting,
            scannedCount = uiState.importProgressScanned,
            financialCount = uiState.importProgressFinancial,
            importedCount = uiState.importProgressImported,
            skippedCount = uiState.importProgressSkipped,
            lastSummary = uiState.lastImportSummary,
            onStartImport = { days -> viewModel.runHistoricalImport(days) },
            onDismiss = { viewModel.openImportDialog(false) }
        )
    }

    // Review Queue Dialog
    if (uiState.isReviewQueueDialogOpen) {
        SmsReviewQueueDialog(
            pendingCandidates = uiState.pendingCandidates,
            availableAccounts = availableAccounts,
            onConfirmCandidate = { id, corrected -> viewModel.confirmCandidate(id, corrected) },
            onRejectCandidate = { id -> viewModel.rejectCandidate(id) },
            onClearAll = { viewModel.clearReviewQueue() },
            onDismiss = { viewModel.openReviewQueueDialog(false) }
        )
    }
}
