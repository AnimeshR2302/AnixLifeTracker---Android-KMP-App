package com.anix.android.anixlifetracker.ui.dashboard

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
import com.anix.android.anixlifetracker.model.TransactionType
import com.anix.android.anixlifetracker.viewmodel.DashboardUiState
import com.anix.android.anixlifetracker.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = remember { DashboardViewModel() },
    onNavigateToFinances: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Quick Action Bar
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrimaryActionButton(
                            text = "Add Txn",
                            iconSymbol = "💳",
                            onClick = { onQuickAction("Add Transaction") },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryActionButton(
                            text = "Add SIP",
                            iconSymbol = "📈",
                            onClick = { onQuickAction("Add Investment") },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryActionButton(
                            text = "Reminder",
                            iconSymbol = "⏰",
                            onClick = { onQuickAction("Add Reminder") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 1: Financial Summary Cards
        item {
            SectionHeader(
                title = "Financial Summary",
                subtitle = "Combined position across accounts",
                actionLabel = "All Accounts",
                onActionClick = onNavigateToFinances
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    title = "Total Balance",
                    amount = uiState.totalBalance,
                    type = TransactionType.INCOME,
                    subtitle = "Across 4 active accounts",
                    iconSymbol = "💰",
                    onClick = onNavigateToFinances,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Monthly Income",
                    amount = uiState.monthlyIncome,
                    type = TransactionType.INCOME,
                    subtitle = "Feb 2026 total",
                    iconSymbol = "💵",
                    onClick = onNavigateToFinances,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    title = "Monthly Expenses",
                    amount = uiState.monthlyExpenses,
                    type = TransactionType.EXPENSE,
                    subtitle = "37% of monthly budget",
                    iconSymbol = "🛍️",
                    onClick = onNavigateToFinances,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Net Savings",
                    amount = uiState.monthlySavings,
                    type = TransactionType.TRANSFER,
                    subtitle = "Surplus allocated",
                    iconSymbol = "🏦",
                    onClick = onNavigateToFinances,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 2: Spending Overview
        item {
            SectionHeader(
                title = "Spending Breakdown",
                subtitle = "Category limits & month progress",
                actionLabel = "Details",
                onActionClick = onNavigateToFinances
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.spendingCategories.forEach { cat ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${cat.spent.formattedString()} / ${cat.budget.formattedString()} (${cat.percentage}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { cat.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (cat.percentage > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Investment Overview
        item {
            SectionHeader(
                title = "Investment Portfolio",
                subtitle = "Wealth accumulation & return rate",
                actionLabel = "Portfolio",
                onActionClick = onNavigateToInvestments
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                onClick = onNavigateToInvestments,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Current Portfolio Value",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FinancialAmountText(
                                amount = uiState.portfolioValue,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+${uiState.portfolioReturnPercentage}% Total Gain",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Market Insights",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.marketInsights.forEach { insight ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${insight.title}: ${insight.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Upcoming Items
        item {
            SectionHeader(
                title = "Upcoming Obligations & Reminders",
                subtitle = "Due in the next 14 days",
                actionLabel = "All Reminders",
                onActionClick = onNavigateToReminders
            )
        }

        items(uiState.upcomingReminders) { item ->
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.description} • Due: ${item.dueDate}",
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
