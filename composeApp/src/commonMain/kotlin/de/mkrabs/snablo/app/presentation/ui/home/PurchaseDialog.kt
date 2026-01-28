package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.util.formatPriceEu

@Composable
fun PurchaseDialog(
    itemName: String,
    imageUrl: String?,
    price: Double?,
    inventoryCount: Int,
    currentBalance: Double,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int) -> Unit
) {
    val maxQuantity = inventoryCount.coerceAtLeast(0)
    val minQuantity = if (maxQuantity > 0) 1 else 0

    var quantity by remember { mutableIntStateOf(minQuantity) }

    LaunchedEffect(itemName, maxQuantity) {
        quantity = quantity.coerceIn(minQuantity, maxQuantity)
        if (maxQuantity == 0) {
            quantity = 0
        }
    }

    val newBalance = price?.let { currentBalance - (it * quantity) }
    val canConfirm = !isSubmitting && price != null && quantity in 1..maxQuantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buy item") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RemoteImage(
                    imageUrl = imageUrl,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Text(itemName, style = MaterialTheme.typography.titleLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Price")
                    Text(
                        formatPriceEu(price),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Left")
                    Text("$inventoryCount")
                }

                NumberStepper(
                    value = quantity,
                    onValueChange = { quantity = it.coerceIn(minQuantity, maxQuantity) },
                    minValue = minQuantity,
                    maxValue = maxQuantity
                )

                Text(
                    "${formatPriceEu(currentBalance)} - ${formatPriceEu(price)} x $quantity = ${formatPriceEu(newBalance)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = { onConfirm(quantity) },
                    enabled = canConfirm,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (isSubmitting) "Working..." else "Done")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
