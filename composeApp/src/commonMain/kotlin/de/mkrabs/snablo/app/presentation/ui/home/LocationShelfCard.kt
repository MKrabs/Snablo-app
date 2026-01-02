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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.SlotMapping
import de.mkrabs.snablo.app.presentation.ui.ShelfSlotCard
import de.mkrabs.snablo.app.util.formatItemLabel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationShelfCard(
    locations: List<Location>,
    selectedLocation: Location?,
    slots: List<SlotMapping>,
    onLocationSelected: (Location) -> Unit,
    onSlotClick: (slotId: String) -> Unit,
    // optional mapping from catalogItemId -> display name
    catalogById: Map<String, String>? = null
) {
    // Debug output state: JSON of the last clicked slot mapping
    var debugJson by remember { mutableStateOf<String?>(null) }

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
                    val rawDisplay = catalogById?.get(slot.catalogItemId) ?: slot.catalogItemId
                    val displayName = formatItemLabel(rawDisplay)
                    ShelfSlotCard(
                        item = displayName,
                        stockState = slot.stockState,
                        price = null,
                        inventoryCount = slot.inventoryCount,
                        onClick = {
                            // set debug JSON to full slot mapping for inspection
                            debugJson = Json.encodeToString(slot)
                            // forward the original click as well
                            onSlotClick(slot.id)
                        }
                    )
                }

                // show debug JSON when available
                if (!debugJson.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Debug: $debugJson", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
