package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.presentation.viewmodel.CornerShelfSlotUi
import de.mkrabs.snablo.app.presentation.viewmodel.CornerUi

@Composable
fun CornersSection(
    corners: List<CornerUi>,
    onShelfClick: (locationId: String, slotId: String) -> Unit
) {
    when {
        corners.isEmpty() -> {
            EmptyCornersState()
        }
        corners.size == 1 -> {
            CornerCard(
                corner = corners.first(),
                modifier = Modifier.fillMaxWidth(),
                onShelfClick = onShelfClick
            )
        }
        else -> {
            LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(corners) { corner ->
                    CornerCard(
                        corner = corner,
                        modifier = Modifier.width(280.dp),
                        onShelfClick = onShelfClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCornersState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No locations found", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Once locations are configured, they will show up here.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CornerCard(
    corner: CornerUi,
    modifier: Modifier,
    onShelfClick: (locationId: String, slotId: String) -> Unit
) {
    // "minimal ratio of square" => keep it near-square, but allow it to grow vertically.
    // Using aspectRatio(1f) gives a square baseline; content can still extend if needed.
    Card(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(corner.location.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (corner.shelves.isEmpty()) {
                Text(
                    "Empty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    corner.shelves.forEach { shelf ->
                        CornerShelfRow(
                            locationId = corner.location.id,
                            shelf = shelf,
                            onShelfClick = onShelfClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerShelfRow(
    locationId: String,
    shelf: CornerShelfSlotUi,
    onShelfClick: (locationId: String, slotId: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShelfClick(locationId, shelf.slotId) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                shelf.itemName,
                style = MaterialTheme.typography.bodyMedium
            )
            val stockLabel = when {
                shelf.inventoryCount <= 0 -> "EMPTY"
                shelf.inventoryCount <= 2 -> "LOW"
                else -> "OK"
            }
            Text(
                stockLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val priceText = shelf.price?.let { "€${String.format("%.2f", it)}" } ?: "—"
        Text(priceText, style = MaterialTheme.typography.bodyMedium)
    }
}

