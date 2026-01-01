package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.CashCount
import de.mkrabs.snablo.app.domain.model.DriftClassification
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.presentation.viewmodel.ReconciliationViewModel

@Composable
fun CashCountScreen(
    viewModel: ReconciliationViewModel,
    location: Location,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var countedCash by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Cash Count - ${location.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Instructions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Count the physical cash in the till and enter the amount.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Cash input
        Text(
            "Counted Cash (€)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = countedCash,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("\\d+\\.?\\d{0,2}"))) {
                    countedCash = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("0.00") },
            enabled = !uiState.isLoading
        )

        // Notes
        Text(
            "Notes (optional)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .padding(bottom = 16.dp),
            placeholder = { Text("e.g., cash count performed on time") },
            enabled = !uiState.isLoading
        )

        // Last cash count info
        if (uiState.lastCashCount != null) {
            val last = uiState.lastCashCount!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Last Count",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expected: €${String.format("%.2f", last.expectedCash)}")
                        Text("Drift: ${String.format("%.1f", last.driftPercentage)}%")
                    }
                }
            }
        }

        // Error message
        if (uiState.error != null) {
            Text(
                uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Buttons
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val counted = countedCash.toDoubleOrNull()
                    if (counted != null) {
                        viewModel.recordCashCount(
                            locationId = location.id,
                            countedCash = counted,
                            notes = notes.ifEmpty { null }
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && countedCash.isNotEmpty()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Record Count")
                }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onComplete()
        }
    }
}

@Composable
fun CashCountHistoryScreen(
    viewModel: ReconciliationViewModel,
    location: Location
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Cash Count History - ${location.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Text(
                "Error: ${uiState.error}",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn {
                items(uiState.cashCounts) { cashCount ->
                    CashCountCard(cashCount)
                }
            }
        }
    }
}

@Composable
fun CashCountCard(cashCount: CashCount) {
    val driftColor = when (cashCount.classification) {
        DriftClassification.GOOD -> MaterialTheme.colorScheme.primaryContainer
        DriftClassification.WARN -> MaterialTheme.colorScheme.tertiaryContainer
        DriftClassification.BAD -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Date and classification
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cashCount.timestamp.take(19),  // ISO date without milliseconds
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    cashCount.classification.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(
                            color = driftColor,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Details grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Counted", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "€${String.format("%.2f", cashCount.countedCash)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column {
                    Text("Expected", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "€${String.format("%.2f", cashCount.expectedCash)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column {
                    Text("Drift", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${if (cashCount.drift > 0) "+" else ""}€${String.format("%.2f", cashCount.drift)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (cashCount.drift == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Notes if present
            if (cashCount.notes != null && cashCount.notes.isNotEmpty()) {
                Text(
                    cashCount.notes ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

