package com.anix.android.anixlifetracker.ui.reminders

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
import com.anix.android.anixlifetracker.viewmodel.RemindersViewModel

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = remember { RemindersViewModel() },
    onQuickActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    val upcomingList = uiState.occurrences.filter { it.state == OccurrenceState.UPCOMING }
    val dueOverdueList = uiState.occurrences.filter { it.state == OccurrenceState.DUE || it.state == OccurrenceState.OVERDUE }
    val completedList = uiState.occurrences.filter { it.state == OccurrenceState.COMPLETED || it.state == OccurrenceState.SKIPPED }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Intelligent Reminders & Inbox",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Due dues, financial obligations, in-app insights & preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PrimaryActionButton(
                text = "New Reminder",
                iconSymbol = "⏰",
                onClick = { showCreateDialog = true }
            )
        }

        // Navigation Tabs
        TabRow(
            selectedTabIndex = uiState.selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Due & Overdue (${dueOverdueList.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Upcoming (${upcomingList.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 2,
                onClick = { viewModel.selectTab(2) },
                text = { Text("Inbox Insights (${uiState.inboxItems.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 3,
                onClick = { viewModel.selectTab(3) },
                text = { Text("Completed (${completedList.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 4,
                onClick = { viewModel.selectTab(4) },
                text = { Text("Settings") }
            )
        }

        // Category Filter Bar (for Tab 0, 1, 3)
        if (uiState.selectedTab in listOf(0, 1, 3)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.filterByCategory(null) },
                    label = { Text("All") }
                )
                ReminderCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.filterByCategory(cat) },
                        label = { Text(cat.name) }
                    )
                }
            }
        }

        // Main Tab Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (uiState.selectedTab) {
                0 -> DueAndOverdueTabContent(
                    occurrences = dueOverdueList,
                    definitions = uiState.definitions,
                    onAction = { occId, act -> viewModel.performOccurrenceAction(occId, act) }
                )
                1 -> UpcomingTabContent(
                    occurrences = upcomingList,
                    definitions = uiState.definitions,
                    onAction = { occId, act -> viewModel.performOccurrenceAction(occId, act) }
                )
                2 -> InboxInsightsTabContent(
                    inboxItems = uiState.inboxItems,
                    onDismiss = { id -> viewModel.dismissInboxItem(id) }
                )
                3 -> CompletedTabContent(
                    occurrences = completedList,
                    definitions = uiState.definitions,
                    onAction = { occId, act -> viewModel.performOccurrenceAction(occId, act) }
                )
                4 -> ReminderPreferencesTabContent(
                    capability = uiState.deliveryCapability,
                    preferences = uiState.preferences
                )
            }
        }
    }

    // Create Reminder Dialog
    if (showCreateDialog) {
        CreateReminderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, date, cat, prio, rec, cad ->
                viewModel.createCustomReminder(title, desc, date, cat, prio, rec, cad)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun DueAndOverdueTabContent(
    occurrences: List<ReminderOccurrenceModel>,
    definitions: List<ReminderDefinitionModel>,
    onAction: (String, ReminderActionType) -> Unit
) {
    if (occurrences.isEmpty()) {
        EmptyState(
            title = "No Dues or Overdue Items",
            message = "All scheduled obligations and dues are up to date! 🎉",
            iconSymbol = "✅"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(occurrences, key = { it.id }) { item ->
                val def = definitions.find { it.id == item.reminderId }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.state == OccurrenceState.OVERDUE)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = def?.title ?: "Reminder ${item.reminderId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Scheduled: ${item.scheduledLocalDateTime.take(10)} • Status: ${item.state.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.amount != null) {
                                FinancialAmountText(
                                    amount = item.amount,
                                    type = TransactionType.EXPENSE,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onAction(item.id, ReminderActionType.COMPLETE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Paid / Complete")
                            }
                            OutlinedButton(
                                onClick = { onAction(item.id, ReminderActionType.SNOOZE) }
                            ) {
                                Text("Snooze")
                            }
                            OutlinedButton(
                                onClick = { onAction(item.id, ReminderActionType.SKIP) }
                            ) {
                                Text("Skip")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingTabContent(
    occurrences: List<ReminderOccurrenceModel>,
    definitions: List<ReminderDefinitionModel>,
    onAction: (String, ReminderActionType) -> Unit
) {
    if (occurrences.isEmpty()) {
        EmptyState(
            title = "No Upcoming Reminders",
            message = "No upcoming dues scheduled for the next 30 days.",
            iconSymbol = "📅"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(occurrences, key = { it.id }) { item ->
                val def = definitions.find { it.id == item.reminderId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = def?.title ?: "Reminder ${item.reminderId}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Due Date: ${item.scheduledLocalDateTime.take(10)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.amount != null) {
                                FinancialAmountText(
                                    amount = item.amount,
                                    type = TransactionType.EXPENSE,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxInsightsTabContent(
    inboxItems: List<InAppInboxItemModel>,
    onDismiss: (String) -> Unit
) {
    if (inboxItems.isEmpty()) {
        EmptyState(
            title = "In-App Inbox Clean",
            message = "No budget threshold alerts or spending anomalies detected.",
            iconSymbol = "🔔"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inboxItems, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.severity == InboxSeverity.CRITICAL)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onDismiss(item.id) }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedTabContent(
    occurrences: List<ReminderOccurrenceModel>,
    definitions: List<ReminderDefinitionModel>,
    onAction: (String, ReminderActionType) -> Unit
) {
    if (occurrences.isEmpty()) {
        EmptyState(
            title = "No Completed Items",
            message = "Cleared and settled reminders will be listed here.",
            iconSymbol = "📋"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(occurrences, key = { it.id }) { item ->
                val def = definitions.find { it.id == item.reminderId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = def?.title ?: "Reminder ${item.reminderId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Settled on: ${item.updatedAt.take(10)} • ${item.state.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = { onAction(item.id, ReminderActionType.UNDO) }) {
                            Text("Reopen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderPreferencesTabContent(
    capability: DeliveryCapabilityStatus,
    preferences: ReminderPreferencesModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Notification Engine Settings",
            subtitle = "Delivery status, in-app visibility, and quiet hours"
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    Column {
                        Text(
                            text = "Outgoing Notification Delivery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = capability.statusExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Text("Disabled", modifier = Modifier.padding(2.dp))
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Delivery is strictly disabled in this release across all platforms. In-app due lists and inbox insights are populated when the application is open.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("In-App Dues & Inbox Display", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = preferences.inAppVisibility, onCheckedChange = {})
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configured Quiet Hours", style = MaterialTheme.typography.bodyMedium)
                    Text("${preferences.quietHoursStart} - ${preferences.quietHoursEnd}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("User Timezone", style = MaterialTheme.typography.bodyMedium)
                    Text(preferences.userTimezone, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, ReminderCategory, ReminderPriority, Boolean, CadenceType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-03-01") }
    var category by remember { mutableStateOf(ReminderCategory.PAYMENT) }
    var priority by remember { mutableStateOf(ReminderPriority.MEDIUM) }
    var isRecurring by remember { mutableStateOf(false) }
    var cadence by remember { mutableStateOf(CadenceType.MONTHLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Universal Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Recurring")
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description.ifBlank { null }, dueDate, category, priority, isRecurring, cadence)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
