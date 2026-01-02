package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.util.formatPriceEu

@Composable
fun BalanceHeaderCard(
    balance: Double,
    onTopUp: () -> Unit,
    onProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Balance", style = MaterialTheme.typography.labelMedium)
                Text(
                    formatPriceEu(balance),
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onTopUp) {
                    // Placeholder (no icon dependency)
                    Text("+")
                }
                IconButton(onClick = onProfile) {
                    Text("👤")
                }
            }
        }
    }
}
