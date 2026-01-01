package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.presentation.viewmodel.HomeViewModel
import de.mkrabs.snablo.app.presentation.viewmodel.ShelfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    shelfViewModel: ShelfViewModel,
    userId: String,
    onTopUp: () -> Unit,
    onProfile: () -> Unit,
    onSlotClick: (locationId: String, slotId: String) -> Unit,
    onOpenSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val shelfState by shelfViewModel.uiState.collectAsState()

    // load initial data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadForUser(userId)
        }
    }

    // Drawer state for right-to-left drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Location dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedLocationName by remember { mutableStateOf(shelfState.selectedLocation?.name ?: "Select location") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // We'll place the drawer on the end (right side) using ModalNavigationDrawer defaults
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // bottom items as requested (icons not required now)
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = { onProfile() }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { onOpenSettings() }
                )
                NavigationDrawerItem(
                    label = { Text("Send feedback") },
                    selected = false,
                    onClick = { onSendFeedback() }
                )
            }
        }
    ) {
        // Main scrollable body
        Box(modifier = Modifier.fillMaxSize()) {
            // Using LazyColumn for scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    // Balance card with top-up action and profile shortcut
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Balance", style = MaterialTheme.typography.labelMedium)
                                Text("€${String.format("%.2f", uiState.balance)}", style = MaterialTheme.typography.displaySmall)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = onTopUp) {
                                    // Use text placeholder for icon to avoid icon dependency
                                    Text("+")
                                }
                                IconButton(onClick = onProfile) {
                                    Text("👤")
                                }
                            }
                        }
                    }
                }

                item {
                    // Location + shelf overview combined
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Location header with dropdown placeholder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Location", style = MaterialTheme.typography.titleMedium)

                                // Exposed dropdown for locations
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        value = selectedLocationName,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Location") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                        modifier = Modifier.width(200.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        shelfState.locations.forEach { loc ->
                                            DropdownMenuItem(
                                                text = { Text(loc.name) },
                                                onClick = {
                                                    selectedLocationName = loc.name
                                                    expanded = false
                                                    shelfViewModel.selectLocation(loc)
                                                    viewModel.selectLocation(loc.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Shelf grid simplified as column of slot cards (clickable)
                            Column {
                                uiState.slots.take(6).forEach { slot ->
                                    ShelfSlotCard(
                                        item = slot.catalogItemId,
                                        stockState = slot.stockState,
                                        onClick = { onSlotClick(shelfState.selectedLocation?.id ?: "", slot.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Recent transactions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                }

                items(uiState.recentTransactions) { entry ->
                    TransactionCard(entry)
                }

                // spacer at bottom to allow additional scroll past content (overscroll to 50% screen)
                item {
                    Spacer(modifier = Modifier.height(300.dp))
                }
            }

            // Overlay gesture or small affordance to open drawer (swipe from right)
            // For simplicity, add a small draggable area on the right edge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.01f))
            ) {
                // TODO: Hook swipe gestures to open drawer; platform specifics may be required
            }
        }
    }
}
