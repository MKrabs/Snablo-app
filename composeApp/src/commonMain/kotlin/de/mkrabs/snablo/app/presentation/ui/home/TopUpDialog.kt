package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onConfirm: (amountEuro: Int) -> Unit
) {
    val maxAmountEuro = 100

    // Defaultmäßig 10€ und +/- in 1€ Schritten
    var amountEuro by remember { mutableIntStateOf(10) }

    // Flag muss existieren (wurde in onValueChange referenziert)
    var selectAllOnNextKey by remember { mutableStateOf(false) }

    // Textfield-State (nur Zahl). Beim Öffnen NICHT markiert (Cursor am Ende).
    var amountText by remember {
        val initial = amountEuro.toString()
        mutableStateOf(TextFieldValue(text = initial, selection = TextRange(initial.length)))
    }

    // Wenn amountEuro z.B. über +/- geändert wird, Text aktualisieren.
    LaunchedEffect(amountEuro) {
        val newText = amountEuro.toString()
        if (amountText.text != newText) {
            amountText = amountText.copy(text = newText, selection = TextRange(newText.length))
        }
    }

    val focusManager = LocalFocusManager.current

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
                    IconButton(onClick = { if (amountEuro > 1) amountEuro -= 1 }) {
                        Text("−", style = MaterialTheme.typography.headlineMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { newValue ->
                                // Sobald der User tippt, soll NICHT erneut auto-selektiert werden
                                selectAllOnNextKey = false

                                val digitsOnly = newValue.text.filter { it.isDigit() }
                                val normalized = if (digitsOnly.isEmpty()) "0" else digitsOnly.trimStart('0').ifEmpty { "0" }

                                val parsed = normalized.toIntOrNull() ?: 0
                                val clamped = parsed.coerceIn(0, maxAmountEuro)
                                val clampedText = clamped.toString()

                                amountText = newValue.copy(
                                    text = clampedText,
                                    selection = TextRange(clampedText.length)
                                )

                                amountEuro = clamped
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .onFocusChanged { state ->
                                    // Beim Fokussieren noch NICHT selektieren.
                                    // Erst beim nächsten tatsächlichen Tippen (dazu wird das Flag gesetzt).
                                    selectAllOnNextKey = state.isFocused
                                    if (state.isFocused) {
                                        amountText = amountText.copy(selection = TextRange(amountText.text.length))
                                    }
                                },
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Text("€", style = MaterialTheme.typography.headlineMedium)
                    }

                    IconButton(onClick = { if (amountEuro < maxAmountEuro) amountEuro += 1 }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }

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