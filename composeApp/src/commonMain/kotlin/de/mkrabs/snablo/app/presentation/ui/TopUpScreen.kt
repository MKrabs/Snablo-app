package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.domain.model.Location
import de.mkrabs.snablo.app.domain.model.TransactionKind
import de.mkrabs.snablo.app.presentation.viewmodel.TopUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    viewModel: TopUpViewModel,
    locations: List<Location>,
    onTopUpComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedKind by remember { mutableStateOf(TransactionKind.TOPUP_CASH) }
    var amount by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(locations.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Top Up Balance",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Kind selection
        Text("Payment Method", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedKind == TransactionKind.TOPUP_CASH,
                onClick = { selectedKind = TransactionKind.TOPUP_CASH },
                label = { Text("Cash") }
            )
            FilterChip(
                selected = selectedKind == TransactionKind.TOPUP_DIGITAL,
                onClick = { selectedKind = TransactionKind.TOPUP_DIGITAL },
                label = { Text("Digital") }
            )
        }

        // Location selection (only for cash)
        if (selectedKind == TransactionKind.TOPUP_CASH) {
            Text(
                "Location",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectedLocation?.name ?: "Select location",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location.name) },
                            onClick = {
                                selectedLocation = location
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Amount input
        Text(
            "Amount (€)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = {
                // Only allow numbers and decimal point
                if (it.isEmpty() || it.matches(Regex("\\d+\\.?\\d{0,2}"))) {
                    amount = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = { Text("0.00") },
            enabled = !uiState.isLoading
        )

        // Error message
        if (uiState.error != null) {
            Text(
                uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Buttons
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && amountDouble > 0) {
                        viewModel.topUp(
                            kind = selectedKind,
                            amount = amountDouble,
                            locationId = selectedLocation?.id
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && amount.isNotEmpty() && (selectedKind == TransactionKind.TOPUP_DIGITAL || selectedLocation != null)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Top Up")
                }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onTopUpComplete()
        }
    }
}
