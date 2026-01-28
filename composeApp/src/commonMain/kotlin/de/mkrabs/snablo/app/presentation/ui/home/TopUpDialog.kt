package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Defaultmäßig 10€ und +/- in 1€ Schritten
    var amountEuro by remember { mutableIntStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add balance") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { if (amountEuro > 0) amountEuro -= 1 }) {
                        Text("−", style = MaterialTheme.typography.headlineMedium)
                    }

                    Text(
                        text = "${amountEuro}€",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    IconButton(onClick = { amountEuro += 1 }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                // Unter der Balance: Done Button
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Done")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
