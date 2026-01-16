package de.mkrabs.snablo.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.mkrabs.snablo.app.data.model.Product
import de.mkrabs.snablo.app.ui.components.BalanceHeader
import de.mkrabs.snablo.app.ui.components.ProductCard
import de.mkrabs.snablo.app.ui.components.PurchaseSheet
import de.mkrabs.snablo.app.ui.components.TopUpSheet
import de.mkrabs.snablo.app.ui.screens.auth.LoginScreen
import de.mkrabs.snablo.app.ui.screens.history.HistoryScreen
import de.mkrabs.snablo.app.ui.screens.profile.ProfileScreen
import de.mkrabs.snablo.app.ui.screens.admin.AdminScreen
import de.mkrabs.snablo.app.viewmodel.HomeViewModel

object HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        var showMenu by remember { mutableStateOf(false) }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var showTopUpSheet by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }

        // Snackbar für Erfolgs-/Fehlermeldungen
        LaunchedEffect(uiState.purchaseSuccess, uiState.topUpSuccess, uiState.error) {
            uiState.purchaseSuccess?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
            uiState.topUpSuccess?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
            uiState.error?.let {
                snackbarHostState.showSnackbar("Fehler: $it")
                viewModel.clearMessages()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Snablo") },
                    actions = {
                        IconButton(onClick = { viewModel.loadData() }) {
                            Icon(Icons.Default.Refresh, "Aktualisieren")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.Menu, "Menü")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Aufladen") },
                                    onClick = {
                                        showMenu = false
                                        showTopUpSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Historie") },
                                    onClick = {
                                        showMenu = false
                                        navigator.push(HistoryScreen)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profil") },
                                    onClick = {
                                        showMenu = false
                                        navigator.push(ProfileScreen)
                                    }
                                )
                                if (viewModel.isAdmin) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Verwaltung") },
                                        onClick = {
                                            showMenu = false
                                            navigator.push(AdminScreen)
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Abmelden") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.logout()
                                        navigator.replaceAll(LoginScreen)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Balance Header
                BalanceHeader(
                    balance = uiState.balanceFormatted,
                    onTopUpClick = { showTopUpSheet = true }
                )

                // Loading Indicator
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Produktliste
                if (uiState.products.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Keine Produkte verfügbar")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { selectedProduct = product }
                            )
                        }
                    }
                }
            }
        }

        // Purchase Bottom Sheet
        selectedProduct?.let { product ->
            PurchaseSheet(
                product = product,
                currentBalance = uiState.balanceFormatted,
                isLoading = uiState.isPurchasing,
                onDismiss = { selectedProduct = null },
                onConfirm = {
                    viewModel.purchase(product)
                    selectedProduct = null
                }
            )
        }

        // TopUp Bottom Sheet
        if (showTopUpSheet) {
            TopUpSheet(
                onDismiss = { showTopUpSheet = false },
                onConfirm = { amount, method ->
                    viewModel.topUp(amount, method)
                    showTopUpSheet = false
                }
            )
        }
    }
}
