package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.mkrabs.snablo.app.domain.model.Location

@Composable
fun LocationDropdown(
    locations: List<Location>,
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = selectedLocation?.name ?: "Select location"

    TextButton(onClick = { expanded = true }) {
        Text(selectedName)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        locations.forEach { loc ->
            DropdownMenuItem(
                text = { Text(loc.name) },
                onClick = {
                    expanded = false
                    onLocationSelected(loc)
                }
            )
        }
    }
}
