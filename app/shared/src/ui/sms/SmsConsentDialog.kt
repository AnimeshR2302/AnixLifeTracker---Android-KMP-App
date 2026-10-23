package com.anix.android.anixlifetracker.ui.sms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SmsConsentDialog(
    onConfirmConsent: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📱")
                Text(
                    text = "SMS Financial Ingestion Consent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Automate transaction tracking by reading financial bank SMS messages.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

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
                        Text(
                            text = "🔒 Privacy Invariants & Rules:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Only bank/financial sender messages are parsed. OTPs, personal messages, and promotional SMS are strictly ignored.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Extracted fields include amount, direction (debit/credit), merchant, account last 4 digits, and transaction reference.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Raw SMS texts are parsed locally and NEVER uploaded to any cloud server.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• You can disable live monitoring or revoke permissions at any time in Settings.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "By granting consent, you authorize AnixLifeTracker to analyze financial SMS messages locally on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmConsent) {
                Text("I Consent & Enable SMS Tracking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
