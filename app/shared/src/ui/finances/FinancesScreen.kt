package com.anix.android.anixlifetracker.ui.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.*
import com.anix.android.anixlifetracker.model.*
import com.anix.android.anixlifetracker.viewmodel.FinancesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    viewModel: FinancesViewModel = remember { FinancesViewModel() },
    onQuickActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var amountInput by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var descriptionInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2026-02-10") }

    var accountNameInput by remember { mutableStateOf("") }
    var accountTypeInput by remember { mutableStateOf(AccountCategoryType.SAVINGS) }
    var institutionInput by remember { mutableStateOf("") }
    var maskedRefInput by remember { mutableStateOf("•••• 0000") }
    var openingBalanceInput by remember { mutableStateOf("0") }

    var budgetNameInput by remember { mutableStateOf("") }
    var budgetLimitInput by remember { mutableStateOf("5000") }

    // Synchronize default selections when state updates
    LaunchedEffect(uiState.accounts, uiState.categories) {
        if (selectedAccountId.isEmpty() && uiState.accounts.isNotEmpty()) {
            selectedAccountId = uiState.accounts.first().id
        }
        if (selectedCategoryId.isEmpty() && uiState.categories.isNotEmpty()) {
            selectedCategoryId = uiState.categories.first().id
        }
    }

    // Delete Confirmation Dialog
    uiState.transactionToDelete?.let { txToDelete ->
        ConfirmationDialog(
            title = "Delete Transaction",
            message = "Are you sure you want to delete '${txToDelete.description ?: txToDelete.transactionType.name}'? This action cannot be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = { viewModel.deleteTransaction(txToDelete.id) },
            onDismiss = { viewModel.confirmDeleteTransaction(null) }
        )
    }

    // Add Account Dialog
    if (uiState.isAddAccountDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddAccountDialog(false) },
            title = { Text("Add Financial Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StandardFormField(
                        label = "Account Name",
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        placeholder = "e.g. HDFC Salary Account"
                    )
                    StandardFormField(
                        label = "Institution Name",
                        value = institutionInput,
                        onValueChange = { institutionInput = it },
                        placeholder = "e.g. HDFC Bank"
                    )
                    StandardFormField(
                        label = "Account Number / Reference Masked",
                        value = maskedRefInput,
                        onValueChange = { maskedRefInput = it },
                        placeholder = "•••• 4821"
                    )
                    StandardFormField(
                        label = "Opening Balance (Units)",
                        value = openingBalanceInput,
                        onValueChange = { openingBalanceInput = it },
                        placeholder = "0"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val units = openingBalanceInput.toLongOrNull() ?: 0L
                        viewModel.addAccount(
                            name = accountNameInput.ifBlank { "New Account" },
                            type = accountTypeInput,
                            institutionName = institutionInput,
                            accountReferenceMasked = maskedRefInput,
                            openingBalanceInCents = units * 100
                        )
                        accountNameInput = ""
                        openingBalanceInput = "0"
                    }
                ) {
                    Text("Save Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddAccountDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Budget Dialog
    if (uiState.isAddBudgetDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddBudgetDialog(false) },
            title = { Text("Create Category Budget") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StandardFormField(
                        label = "Budget Name",
                        value = budgetNameInput,
                        onValueChange = { budgetNameInput = it },
                        placeholder = "e.g. Monthly Dining Budget"
                    )
                    StandardFormField(
                        label = "Monthly Limit (₹)",
                        value = budgetLimitInput,
                        onValueChange = { budgetLimitInput = it },
                        placeholder = "5000"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitUnits = budgetLimitInput.toLongOrNull() ?: 5000L
                        viewModel.addBudget(
                            name = budgetNameInput.ifBlank { "Monthly Budget" },
                            limitInCents = limitUnits * 100,
                            categoryId = selectedCategoryId.ifBlank { null }
                        )
                        budgetNameInput = ""
                    }
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddBudgetDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Transaction Form Dialog
    if (uiState.isAddTransactionDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddTransactionDialog(false) },
            title = { Text("Record Manual Transaction") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = transactionType == TransactionType.EXPENSE,
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.INCOME,
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text("Income") }
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.TRANSFER,
                            onClick = { transactionType = TransactionType.TRANSFER },
                            label = { Text("Transfer") }
                        )
                    }

                    StandardFormField(
                        label = "Amount (₹)",
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        placeholder = "0.00"
                    )

                    StandardFormField(
                        label = "Description / Note",
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        placeholder = "e.g. Weekly Grocery Shopping"
                    )

                    StandardFormField(
                        label = "Date (YYYY-MM-DD)",
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        placeholder = "2026-02-10"
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = amountInput.toDoubleOrNull() != null && selectedAccountId.isNotBlank(),
                    onClick = {
                        val majorUnits = amountInput.toDoubleOrNull() ?: 0.0
                        val cents = (majorUnits * 100).toLong()
                        val direction = if (transactionType == TransactionType.INCOME) TransactionDirection.CREDIT else TransactionDirection.DEBIT

                        viewModel.addTransaction(
                            accountId = selectedAccountId,
                            amountInCents = cents,
                            type = transactionType,
                            direction = direction,
                            categoryId = selectedCategoryId.ifBlank { null },
                            description = descriptionInput.ifBlank { "Manual Transaction" },
                            localDate = dateInput
                        )
                        amountInput = ""
                        descriptionInput = ""
                    }
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddTransactionDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Accounts & Finances",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared processing, analytics, budgets & forecasts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.toggleAddAccountDialog(true) }) {
                        Text("+ Account")
                    }
                    PrimaryActionButton(
                        text = "Add Txn",
                        iconSymbol = "➕",
                        onClick = { viewModel.toggleAddTransactionDialog(true) }
                    )
                }
            }
        }

        // Section 1: Financial Position & Analytics
        item {
            SectionHeader(
                title = "Financial Analytics Position",
                subtitle = "Income, gross/net expenses, refunds, and savings rate"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FinancialAmountText(amount = uiState.summary.totalIncome, type = TransactionType.INCOME, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Gross Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FinancialAmountText(amount = uiState.summary.grossExpenses, type = TransactionType.EXPENSE, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Refunds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FinancialAmountText(amount = uiState.summary.totalRefunds, type = TransactionType.INCOME, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Net Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FinancialAmountText(amount = uiState.summary.netExpenses, type = TransactionType.EXPENSE, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Net Cash Flow", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FinancialAmountText(amount = uiState.summary.netCashFlow, type = if (uiState.summary.netCashFlow.isNegative) TransactionType.EXPENSE else TransactionType.INCOME, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Savings Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = uiState.savingsRate.savingsRatePercentage?.let { "${it.toString().take(4)}%" } ?: "N/A",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Month-End Expense & Cash-Flow Forecast
        uiState.expenseForecast?.let { forecast ->
            item {
                SectionHeader(
                    title = "Month-End Expense & Cash Flow Forecast",
                    subtitle = "Explainable deterministic calculations (excluding recurring from variable baseline)"
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Projected Month-End Expenses", style = MaterialTheme.typography.bodySmall)
                                FinancialAmountText(amount = forecast.projectedMonthEndExpenses, type = TransactionType.EXPENSE, style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Text("Unpaid Bills", style = MaterialTheme.typography.bodySmall)
                                FinancialAmountText(amount = forecast.unpaidKnownObligations, type = TransactionType.EXPENSE, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        Text(
                            text = "Scenarios: Low ${forecast.scenarios.lowScenario.formattedString()} • Base ${forecast.scenarios.baseScenario.formattedString()} • High ${forecast.scenarios.highScenario.formattedString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = forecast.methodologyNotes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section 3: Spending Anomalies
        if (uiState.spendingAnomalies.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Unusual Spending Alerts",
                    subtitle = "Statistical threshold alerts relative to category baseline"
                )
            }
            items(uiState.spendingAnomalies) { anomaly ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = anomaly.merchantName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(text = anomaly.anomalyDescription, style = MaterialTheme.typography.bodySmall)
                        }
                        FinancialAmountText(amount = anomaly.actualAmount, type = TransactionType.EXPENSE, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        // Section 4: Accounts Ledger
        item {
            SectionHeader(
                title = "Connected Accounts & Balances",
                subtitle = "Observed vs derived balance snapshots"
            )
        }

        if (uiState.accounts.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    title = "No Accounts Added",
                    message = "Add an account to manage transactions and track balances."
                )
            }
        } else {
            items(uiState.accountSummaries) { accSummary ->
                val acc = accSummary.account
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = acc.accountName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Badge(
                                    containerColor = if (accSummary.isObserved) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(if (accSummary.isObserved) "Observed" else "Derived", modifier = Modifier.padding(2.dp))
                                }
                            }
                            Text(
                                text = "${acc.accountType.name} • ${acc.accountReferenceMasked.ifBlank { acc.institutionName }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FinancialAmountText(
                            amount = acc.balanceSnapshot,
                            type = if (acc.balanceSnapshot.isNegative) TransactionType.EXPENSE else TransactionType.INCOME,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Section 5: Budgets Overview
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionHeader(
                    title = "Budgets & Spending Limits",
                    subtitle = "Monthly category limits & forecast overspend"
                )
                OutlinedButton(onClick = { viewModel.toggleAddBudgetDialog(true) }) {
                    Text("+ Budget")
                }
            }
        }

        if (uiState.budgetStatuses.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("No active budgets created. Click '+ Budget' to add a category spending limit.")
                    }
                }
            }
        } else {
            items(uiState.budgetStatuses) { bStatus ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(bStatus.budget.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${bStatus.spentAmount.formattedString()} / ${bStatus.budget.limitAmount.formattedString()}", style = MaterialTheme.typography.bodySmall)
                        }
                        LinearProgressIndicator(
                            progress = { (bStatus.percentageUsed / 100.0).coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (bStatus.isOverbudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section 6: Transaction Ledger
        item {
            SectionHeader(
                title = "Transaction History Ledger",
                subtitle = "Persistent income, expense, and transfer records"
            )
        }

        if (uiState.transactions.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    title = "No Transactions Recorded",
                    message = "Your financial database is empty. Click 'Add Txn' to log your first manual transaction."
                )
            }
        } else {
            items(uiState.transactions) { txn ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                val symbol = when (txn.transactionType) {
                                    TransactionType.INCOME -> "📥"
                                    TransactionType.EXPENSE -> "📤"
                                    TransactionType.TRANSFER -> "🔄"
                                    else -> "💳"
                                }
                                Text(symbol)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = txn.description ?: txn.transactionType.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (txn.sourceType == TransactionSourceType.SMS) {
                                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                            Text("📱 SMS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(2.dp))
                                        }
                                    } else if (txn.sourceType == TransactionSourceType.NOTIFICATION) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text("🔔 Notification", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = "${txn.categoryName ?: "General"} • ${txn.localTransactionDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialAmountText(
                                amount = txn.amount,
                                type = txn.transactionType,
                                showBadge = true,
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(onClick = { viewModel.confirmDeleteTransaction(txn) }) {
                                Text("🗑️")
                            }
                        }
                    }
                }
            }
        }
    }
}
