package com.anix.android.anixlifetracker.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.viewmodel.AuthTab
import com.anix.android.anixlifetracker.viewmodel.AuthViewModel

@Composable
fun AuthDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (uiState.activeTab) {
                    AuthTab.SIGN_IN -> "Sign In to AnixCloud"
                    AuthTab.SIGN_UP -> "Create AnixCloud Account"
                    AuthTab.FORGOT_PASSWORD -> "Account Recovery"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tab Selection Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.activeTab == AuthTab.SIGN_IN,
                        onClick = { viewModel.setTab(AuthTab.SIGN_IN) },
                        label = { Text("Sign In") }
                    )
                    FilterChip(
                        selected = uiState.activeTab == AuthTab.SIGN_UP,
                        onClick = { viewModel.setTab(AuthTab.SIGN_UP) },
                        label = { Text("Sign Up") }
                    )
                    FilterChip(
                        selected = uiState.activeTab == AuthTab.FORGOT_PASSWORD,
                        onClick = { viewModel.setTab(AuthTab.FORGOT_PASSWORD) },
                        label = { Text("Reset") }
                    )
                }

                // Error Banner
                uiState.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Info Banner
                uiState.infoMessage?.let { info ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = info,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.activeTab == AuthTab.SIGN_UP) {
                    OutlinedTextField(
                        value = uiState.displayNameInput,
                        onValueChange = { viewModel.updateDisplayName(it) },
                        label = { Text("Display Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.activeTab != AuthTab.FORGOT_PASSWORD) {
                    OutlinedTextField(
                        value = uiState.passwordInput,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submit() },
                enabled = !uiState.isLoading && uiState.emailInput.isNotBlank()
            ) {
                Text(
                    when (uiState.activeTab) {
                        AuthTab.SIGN_IN -> "Sign In"
                        AuthTab.SIGN_UP -> "Create Account"
                        AuthTab.FORGOT_PASSWORD -> "Send Recovery Email"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
fun CloudSyncDisclosureDialog(
    onConfirmConsent: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cloud Synchronization Data Disclosure",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Before enabling cloud synchronization across your devices, please review what information is uploaded to secure storage:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "✔️ Uploaded for Sync:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("• Accounts, Balances, and Currency Preferences", style = MaterialTheme.typography.bodySmall)
                        Text("• Confirmed Financial Transactions and Categories", style = MaterialTheme.typography.bodySmall)
                        Text("• Budgets, Recurring Payments, and Reminders", style = MaterialTheme.typography.bodySmall)
                        Text("• Investments, Holdings, and Financial Goals", style = MaterialTheme.typography.bodySmall)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "❌ Strictly Kept Local (NEVER Uploaded):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("• Raw SMS message bodies and notification texts", style = MaterialTheme.typography.bodySmall)
                        Text("• Bank credentials, passwords, or PINs", style = MaterialTheme.typography.bodySmall)
                        Text("• Unrelated device contacts or photos", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Your existing offline records will be safely linked to your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmConsent) {
                Text("Enable Cloud Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Local-Only")
            }
        },
        modifier = modifier
    )
}
