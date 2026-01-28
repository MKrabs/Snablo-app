package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.LedgerEntry
import de.mkrabs.snablo.app.domain.model.TransactionKind
import de.mkrabs.snablo.app.presentation.viewmodel.BalanceViewModel
import de.mkrabs.snablo.app.presentation.viewmodel.ConfirmPurchaseViewModel
import de.mkrabs.snablo.app.util.formatPriceEu

@Composable
fun ConfirmPurchaseScreen(
    viewModel: ConfirmPurchaseViewModel,
    onPurchaseComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.purchaseData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else if (data != null) {
            // Item details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Confirm Purchase",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        data.catalogItem.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        data.catalogItem.category,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Price and balance
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Price:")
                        Text(formatPriceEu(data.effectivePrice))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Balance:")
                        Text(formatPriceEu(data.userBalance))
                    }
                }
            }

            // Error message
            if (uiState.error != null && !uiState.isConfirmed) {
                Text(
                    uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Confirm or Undo buttons
            if (!uiState.isConfirmed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { /* Will be called from activity */ },
                        modifier = Modifier.weight(1f),
                        enabled = data.canAfford
                    ) {
                        Text("Confirm")
                    }
                }
            } else if (uiState.showUndoWindow) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Purchase Confirmed!",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Undo available in ${uiState.undoTimeRemaining}s",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Button(
                    onClick = { /* Will be called from activity */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Undo")
                }
            }
        }
    }

    LaunchedEffect(uiState.isConfirmed && !uiState.showUndoWindow) {
        if (uiState.isConfirmed && !uiState.showUndoWindow) {
            onPurchaseComplete()
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: BalanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Balance & History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Balance Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Current Balance",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    formatPriceEu(uiState.balance),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedFilterKind == null,
                onClick = { viewModel.setFilter(null) },
                label = { Text("All") }
            )
            FilterChip(
                selected = uiState.selectedFilterKind == TransactionKind.PURCHASE_DIGITAL,
                onClick = { viewModel.setFilter(TransactionKind.PURCHASE_DIGITAL) },
                label = { Text("Purchases") }
            )
            FilterChip(
                selected = uiState.selectedFilterKind?.name?.startsWith("TOPUP") ?: false,
                onClick = { /* Would need multi-select */ },
                label = { Text("Top-ups") }
            )
        }

        // History List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(viewModel.getFilteredHistory()) { entry ->
                    TransactionCard(entry)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(entry: LedgerEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    entry.kind.name.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    entry.createdAt.take(19),  // ISO datetime without milliseconds
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                "${if (entry.amountCents > 0) "+" else ""}${formatPriceEu(entry.amountEuros)}",
                style = MaterialTheme.typography.titleSmall,
                color = if (entry.amountCents > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
