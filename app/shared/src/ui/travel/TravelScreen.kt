package com.anix.android.anixlifetracker.ui.travel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anix.android.anixlifetracker.designsystem.components.*
import com.anix.android.anixlifetracker.viewmodel.TravelViewModel

@Composable
fun TravelScreen(
    viewModel: TravelViewModel = remember { TravelViewModel() },
    onQuickActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Travel & Trips",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upcoming itineraries, travel budgets & flight check-in tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PrimaryActionButton(
                    text = "Add Trip",
                    iconSymbol = "✈️",
                    onClick = { onQuickActionNotice("Add Travel Itinerary Modal") }
                )
            }
        }

        if (uiState.trips.isEmpty()) {
            item {
                EmptyState(
                    title = "No Upcoming Trips",
                    message = "Add your next travel itinerary to track flight dates, hotel bookings, and travel budgets.",
                    iconSymbol = "🏖️",
                    actionLabel = "Plan Trip",
                    onActionClick = { onQuickActionNotice("Add Travel Itinerary Modal") }
                )
            }
        } else {
            items(uiState.trips) { trip ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = trip.destination,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge {
                                Text(trip.status, modifier = Modifier.padding(4.dp))
                            }
                        }
                        Text(
                            text = "Dates: ${trip.dates} • Budget: ${trip.budget.formattedString()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Checklist Tasks: ${trip.completedChecklistCount}/${trip.checklistCount} Completed",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { onQuickActionNotice("View Trip Details") }) {
                                Text("View Itinerary")
                            }
                        }
                    }
                }
            }
        }
    }
}
