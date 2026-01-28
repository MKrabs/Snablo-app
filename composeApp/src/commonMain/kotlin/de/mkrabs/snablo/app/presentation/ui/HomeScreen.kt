package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.presentation.ui.home.BalanceHeaderCard
import de.mkrabs.snablo.app.presentation.ui.home.CornersSection
import de.mkrabs.snablo.app.presentation.ui.home.HomeSideDrawer
import de.mkrabs.snablo.app.presentation.ui.home.RecentTransactionsHeader
import de.mkrabs.snablo.app.presentation.ui.home.PullRefreshLayout
import de.mkrabs.snablo.app.presentation.viewmodel.HomeViewModel
import de.mkrabs.snablo.app.presentation.viewmodel.ShelfViewModel
import de.mkrabs.snablo.app.presentation.ui.home.TopUpDialog
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    var showTopUpDialog by rememberSaveable { mutableStateOf(false) }

    // load initial data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadForUser(userId)
        }
    }

    if (showTopUpDialog) {
        TopUpDialog(
            onDismiss = { showTopUpDialog = false },
            onConfirm = { amountEuro ->
                showTopUpDialog = false
                scope.launch {
                    viewModel.topUpBalance(userId = userId, amountEuro = amountEuro)
                    viewModel.loadForUser(userId)
                }
                onTopUp()
            }
        )
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    HomeSideDrawer(
        drawerState = drawerState,
        onProfile = onProfile,
        onOpenSettings = onOpenSettings,
        onSendFeedback = onSendFeedback,
        locations = uiState.corners.map { it.location.id to it.location.name },
        currentLocationId = uiState.selectedLocationId,
        onLocationSelected = { id -> viewModel.selectLocation(id) }
    ) {
        PullRefreshLayout(isRefreshing = uiState.isRefreshing, onRefresh = { viewModel.loadForUser(userId, isRefresh = true) }) { listState ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                state = listState
            ) {
                item {
                    BalanceHeaderCard(
                        balance = uiState.balance,
                        onTopUp = { showTopUpDialog = true },
                        onProfile = onProfile
                    )
                }

                item {
                    // Show corner carousel scoped to current location (if selected). This
                    // removes the previous locations carousel — CornersSection now receives
                    // only corners for the selected location.
                    val cornersToShow = uiState.selectedLocationId?.let { selectedId ->
                        uiState.corners.filter { it.location.id == selectedId }
                    } ?: uiState.corners

                    CornersSection(
                        corners = cornersToShow,
                        onShelfClick = onSlotClick,
                        locationsDebugText = uiState.locationsDebugText
                    )
                }

                item {
                    RecentTransactionsHeader()
                }

                items(uiState.recentTransactions) { entry ->
                    TransactionCard(entry)
                }
            }
        }
    }
}
