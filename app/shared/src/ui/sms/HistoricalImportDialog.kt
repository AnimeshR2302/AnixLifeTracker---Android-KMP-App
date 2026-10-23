package com.anix.android.anixlifetracker.ui.sms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.model.HistoricalImportSummary

@Composable
fun HistoricalImportDialog(
    isImporting: Boolean,
    scannedCount: Int,
    financialCount: Int,
    importedCount: Int,
    skippedCount: Int,
    lastSummary: HistoricalImportSummary?,
    onStartImport: (daysRange: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Historical SMS Import",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImporting) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Scanning SMS inbox...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scanned: $scannedCount", style = MaterialTheme.typography.bodySmall)
                            Text("Financial: $financialCount", style = MaterialTheme.typography.bodySmall)
                            Text("Imported: $importedCount", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else if (lastSummary != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✅ Import Summary Complete",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Total SMS Scanned: ${lastSummary.scannedCount}", style = MaterialTheme.typography.bodySmall)
                            Text("Financial Messages Identified: ${lastSummary.financialFoundCount}", style = MaterialTheme.typography.bodySmall)
                            Text("Transactions Imported: ${lastSummary.importedCount}", style = MaterialTheme.typography.bodySmall)
                            Text("Duplicates Skipped: ${lastSummary.duplicatesSkippedCount}", style = MaterialTheme.typography.bodySmall)
                            Text("Queued for Review: ${lastSummary.needsReviewCount}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Text(
                        text = "Select date range to scan historical SMS inbox for banking transactions:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            30 to "Last 30 Days",
                            90 to "Last 90 Days",
                            180 to "Last 6 Months",
                            365 to "Last 1 Year"
                        ).forEach { (days, label) ->
                            FilterChip(
                                selected = selectedDays == days,
                                onClick = { selectedDays = days },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isImporting) {
                // Disabled while importing
            } else if (lastSummary != null) {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            } else {
                Button(onClick = { onStartImport(selectedDays) }) {
                    Text("Start Scan & Import")
                }
            }
        },
        dismissButton = {
            if (!isImporting && lastSummary == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
