package de.mkrabs.snablo.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.mkrabs.snablo.app.data.model.Transaction
import de.mkrabs.snablo.app.data.model.TransactionType
import de.mkrabs.snablo.app.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object HistoryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val transactionRepository = koinInject<TransactionRepository>()
        val scope = rememberCoroutineScope()

        var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            scope.launch {
                transactionRepository.getMyTransactions()
                    .onSuccess {
                        transactions = it
                        isLoading = false
                    }
                    .onFailure {
                        error = it.message
                        isLoading = false
                    }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transaktionshistorie") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                        }
                    }
                )
            }
        ) { padding ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Fehler: $error")
                    }
                }
                transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Noch keine Transaktionen")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { transaction ->
                            TransactionItem(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (transaction.type) {
                        TransactionType.purchase -> "Kauf"
                        TransactionType.topup -> "Aufladung"
                        TransactionType.correction -> "Korrektur"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.paymentMethod.name.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                transaction.created?.let {
                    Text(
                        text = it.take(10), // Nur Datum
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = transaction.amountFormatted,
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amountCents >= 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
