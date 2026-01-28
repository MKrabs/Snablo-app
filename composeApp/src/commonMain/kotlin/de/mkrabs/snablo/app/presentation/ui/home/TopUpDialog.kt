package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Top Up") },
        text = { Text("Möchtest du dein Guthaben aufladen?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Weiter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

