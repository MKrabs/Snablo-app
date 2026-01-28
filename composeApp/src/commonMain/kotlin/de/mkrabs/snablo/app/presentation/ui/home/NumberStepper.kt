package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    textFieldWidthFraction: Float = 0.35f,
    suffix: String? = null
) {
    val safeMin = minValue.coerceAtLeast(0)
    val safeMax = maxValue.coerceAtLeast(safeMin)
    val clampedValue = value.coerceIn(safeMin, safeMax)

    var fieldValue by remember {
        val initial = clampedValue.toString()
        mutableStateOf(TextFieldValue(text = initial, selection = TextRange(initial.length)))
    }

    LaunchedEffect(clampedValue) {
        val newText = clampedValue.toString()
        if (fieldValue.text != newText) {
            fieldValue = fieldValue.copy(text = newText, selection = TextRange(newText.length))
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { onValueChange((clampedValue - 1).coerceAtLeast(safeMin)) },
            enabled = clampedValue > safeMin
        ) {
            Text("-", style = MaterialTheme.typography.headlineMedium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.text.filter { it.isDigit() }
                    val normalized = if (digitsOnly.isEmpty()) "0" else digitsOnly.trimStart('0').ifEmpty { "0" }
                    val parsed = normalized.toIntOrNull() ?: 0
                    val clamped = parsed.coerceIn(safeMin, safeMax)
                    val clampedText = clamped.toString()

                    fieldValue = newValue.copy(
                        text = clampedText,
                        selection = TextRange(clampedText.length)
                    )

                    if (clamped != clampedValue) {
                        onValueChange(clamped)
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(textFieldWidthFraction),
                colors = OutlinedTextFieldDefaults.colors()
            )

            if (!suffix.isNullOrBlank()) {
                Text(suffix, style = MaterialTheme.typography.headlineMedium)
            }
        }

        IconButton(
            onClick = { onValueChange((clampedValue + 1).coerceAtMost(safeMax)) },
            enabled = clampedValue < safeMax
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
