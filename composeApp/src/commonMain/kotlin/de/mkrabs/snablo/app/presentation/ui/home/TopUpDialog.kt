package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onConfirm: (amountEuro: Int) -> Unit
) {
    val maxAmountEuro = 100

    // Defaultmäßig 10€ und +/- in 1€ Schritten
    var amountEuro by remember { mutableIntStateOf(10) }

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add balance") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberStepper(
                    value = amountEuro,
                    onValueChange = { amountEuro = it },
                    minValue = 0,
                    maxValue = maxAmountEuro,
                    suffix = "€"
                )

                // Unter der Balance: Done Button
                TextButton(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onConfirm(amountEuro)
                    },
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
