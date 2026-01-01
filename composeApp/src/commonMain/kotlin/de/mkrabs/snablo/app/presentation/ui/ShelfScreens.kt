package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.StockState
import de.mkrabs.snablo.app.presentation.viewmodel.ShelfViewModel

@Composable
fun LocationListScreen(
    viewModel: ShelfViewModel,
    onLocationSelected: (Location) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Select Location",
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
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(uiState.locations) { location ->
                    LocationCard(
                        location = location,
                        onClick = {
                            viewModel.selectLocation(location)
                            onLocationSelected(location)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Location,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                location.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (location.address != null) {
                Text(
                    location.address ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DigitalShelfScreen(
    viewModel: ShelfViewModel,
    onSlotSelected: (String, String) -> Unit  // locationId, slotId
) {
    val uiState by viewModel.uiState.collectAsState()
    val location = uiState.selectedLocation ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Digital Shelf - ${location.name}",
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
                items(uiState.slots) { slot ->
                    val item = uiState.catalogItems.find { it.id == slot.catalogItemId }
                    if (item != null) {
                        ShelfSlotCard(
                            item = item.name,
                            stockState = slot.stockState,
                            onClick = { onSlotSelected(location.id, slot.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShelfSlotCard(
    item: String,
    stockState: StockState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item, style = MaterialTheme.typography.titleMedium)

            val stockLabel = when (stockState) {
                StockState.EMPTY -> "EMPTY"
                StockState.ONE -> "LOW"
                StockState.MANY -> "AVAILABLE"
            }
            val stockColor = when (stockState) {
                StockState.EMPTY -> MaterialTheme.colorScheme.errorContainer
                StockState.ONE -> MaterialTheme.colorScheme.tertiaryContainer
                StockState.MANY -> MaterialTheme.colorScheme.primaryContainer
            }

            Text(
                stockLabel,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(
                        color = stockColor,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


