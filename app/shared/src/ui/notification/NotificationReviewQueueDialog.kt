package com.anix.android.anixlifetracker.ui.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.FinancialAmountText
import com.anix.android.anixlifetracker.model.*

@Composable
fun NotificationReviewQueueDialog(
    pendingCandidates: List<NotificationReviewCandidateModel>,
    availableAccounts: List<AccountModel>,
    onConfirmCandidate: (candidateId: String, corrected: TransactionCandidate) -> Unit,
    onRejectCandidate: (candidateId: String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notification Review Queue (${pendingCandidates.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (pendingCandidates.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Clear All")
                    }
                }
            }
        },
        text = {
            if (pendingCandidates.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "🎉 No pending notification candidates for review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(pendingCandidates) { reviewItem ->
                        NotificationReviewCandidateCard(
                            item = reviewItem,
                            availableAccounts = availableAccounts,
                            onConfirm = { corrected ->
                                onConfirmCandidate(reviewItem.id, corrected)
                            },
                            onReject = {
                                onRejectCandidate(reviewItem.id)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun NotificationReviewCandidateCard(
    item: NotificationReviewCandidateModel,
    availableAccounts: List<AccountModel>,
    onConfirm: (TransactionCandidate) -> Unit,
    onReject: () -> Unit
) {
    val candidate = item.candidate
    var editedMerchant by remember { mutableStateOf(candidate.merchantName ?: "") }
    var selectedAccountId by remember { mutableStateOf(candidate.suggestedAccountId ?: availableAccounts.firstOrNull()?.id ?: "") }
    var editedType by remember { mutableStateOf(candidate.transactionType) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = candidate.institutionName ?: item.sourcePackage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.sourcePackage} • ${candidate.localTransactionDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FinancialAmountText(
                    amount = candidate.amount,
                    type = candidate.transactionType,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!candidate.rawContentSnippet.isNullOrBlank()) {
                Text(
                    text = "Snippet: \"${candidate.rawContentSnippet}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = editedMerchant,
                onValueChange = { editedMerchant = it },
                label = { Text("Merchant / Payee") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Account selection chips
            if (availableAccounts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select Account:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableAccounts.take(3).forEach { acc ->
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.accountName.take(15)) }
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val corrected = candidate.copy(
                            merchantName = editedMerchant.ifBlank { null },
                            suggestedAccountId = selectedAccountId,
                            transactionType = editedType
                        )
                        onConfirm(corrected)
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}
