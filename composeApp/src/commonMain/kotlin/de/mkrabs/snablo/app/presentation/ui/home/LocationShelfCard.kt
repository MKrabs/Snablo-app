package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.SlotMapping
import de.mkrabs.snablo.app.presentation.ui.ShelfSlotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationShelfCard(
    locations: List<Location>,
    selectedLocation: Location?,
    slots: List<SlotMapping>,
    onLocationSelected: (Location) -> Unit,
    onSlotClick: (slotId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Location", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                LocationDropdown(
                    locations = locations,
                    selectedLocation = selectedLocation,
                    onLocationSelected = onLocationSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                slots.take(6).forEach { slot ->
                    ShelfSlotCard(
                        item = slot.catalogItemId,
                        stockState = slot.stockState,
                        onClick = { onSlotClick(slot.id) }
                    )
                }
            }
        }
    }
}

