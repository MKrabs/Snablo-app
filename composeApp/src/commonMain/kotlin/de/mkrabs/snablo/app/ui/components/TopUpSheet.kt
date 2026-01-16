package de.mkrabs.snablo.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.data.model.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpSheet(
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Int, method: PaymentMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.cash) }

    val amountCents = amountText.toDoubleOrNull()?.let { (it * 100).toInt() } ?: 0

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Guthaben aufladen",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Betrag eingeben
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.') },
                label = { Text("Betrag in €") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Schnellauswahl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("5", "10", "20").forEach { amount ->
                    FilterChip(
                        selected = amountText == amount,
                        onClick = { amountText = amount },
                        label = { Text("$amount €") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Zahlungsmethode
            Text(
                text = "Zahlungsmethode",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                PaymentMethodOption(
                    method = PaymentMethod.cash,
                    label = "Bargeld",
                    description = "In die Kasse eingeworfen",
                    selected = selectedMethod == PaymentMethod.cash,
                    onClick = { selectedMethod = PaymentMethod.cash }
                )
                PaymentMethodOption(
                    method = PaymentMethod.paypal,
                    label = "PayPal",
                    description = "Per PayPal überwiesen",
                    selected = selectedMethod == PaymentMethod.paypal,
                    onClick = { selectedMethod = PaymentMethod.paypal }
                )
                PaymentMethodOption(
                    method = PaymentMethod.wero,
                    label = "Wero",
                    description = "Per Wero überwiesen",
                    selected = selectedMethod == PaymentMethod.wero,
                    onClick = { selectedMethod = PaymentMethod.wero }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Button(
                    onClick = { onConfirm(amountCents, selectedMethod) },
                    enabled = amountCents > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aufladen")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaymentMethodOption(
    method: PaymentMethod,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
