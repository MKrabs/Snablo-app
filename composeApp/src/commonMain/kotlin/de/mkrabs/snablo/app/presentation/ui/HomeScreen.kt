package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.presentation.ui.home.BalanceHeaderCard
import de.mkrabs.snablo.app.presentation.ui.home.CornersSection
import de.mkrabs.snablo.app.presentation.ui.home.HomeSideDrawer
import de.mkrabs.snablo.app.presentation.ui.home.RecentTransactionsHeader
import de.mkrabs.snablo.app.presentation.ui.home.PullRefreshLayout
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

    // load initial data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadForUser(userId)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    HomeSideDrawer(
        drawerState = drawerState,
        onProfile = onProfile,
        onOpenSettings = onOpenSettings,
        onSendFeedback = onSendFeedback
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                            onTopUp = onTopUp,
                            onProfile = onProfile
                        )
                    }

                    item {
                        // New: locations/corners section (empty/one/many carousel)
                        CornersSection(
                            corners = uiState.corners,
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

                    // spacer at bottom to allow additional scroll past content (approx. 50% screen)
                    item {
                        LongDummyContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun LongDummyContent() {
    // Produces a lot of text lines to force a scrollable page for debugging
    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(top = 16.dp)) {
        for (i in 1..100) {
            androidx.compose.material3.Text(
                text = "Dummy content line #$i",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
