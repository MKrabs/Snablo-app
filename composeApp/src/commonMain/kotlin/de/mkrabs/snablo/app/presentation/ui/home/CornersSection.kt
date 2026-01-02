package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter

// Use the ViewModel types directly so callers don't need to convert between
// packages; HomeViewModel provides CornerUi and CornerShelfSlotUi.
import de.mkrabs.snablo.app.presentation.viewmodel.CornerUi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CornersSection(
    corners: List<CornerUi>,
    onShelfClick: (locationId: String, slotId: String) -> Unit,
    locationsDebugText: String?
) {
    when {
        corners.isEmpty() -> EmptyCornersState()
        corners.size == 1 -> {
            // Show single corner full width with a responsive grid of shelves
            CornerCardFull(corner = corners.first(), onShelfClick = onShelfClick)
        }
        else -> {
            // Manual snapping implementation (works across Compose versions)
            val rowState = rememberLazyListState()

            // When scrolling stops, snap to nearest item with a spring animation
            LaunchedEffect(rowState) {
                snapshotFlow { rowState.isScrollInProgress }
                    .filter { inProgress -> !inProgress }
                    .collect {
                        // compute nearest item
                        val firstVisible = rowState.firstVisibleItemIndex
                        val visibleOffset = rowState.firstVisibleItemScrollOffset
                        // assume item width ~ 320.dp -> convert to px using density if needed; use heuristic
                        // Here we decide: if more than half scrolled to next, move to next
                        val target = if (visibleOffset > 160) firstVisible + 1 else firstVisible
                        // animateScrollToItem doesn't accept animationSpec on this compose version; use default animation
                        rowState.animateScrollToItem(target)
                    }
            }

            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(corners, key = { it.location.id }) { corner ->
                    CornerCard(
                        corner = corner,
                        modifier = Modifier.width(320.dp),
                        onShelfClick = onShelfClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CornerCardFull(
    corner: CornerUi,
    onShelfClick: (locationId: String, slotId: String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(corner.location.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            // Limit the grid height so when this card is placed inside a LazyColumn item
            // it gets finite constraints instead of infinity (which causes Compose to crash).
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                gridItems(items = corner.shelves.sortedBy { it.slotIndex }) { shelf ->
                    SnackCard(name = shelf.itemName, price = shelf.price, imageUrl = shelf.imageUrl, inventoryCount = shelf.inventoryCount, onClick = { onShelfClick(corner.location.id, shelf.slotId) })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CornerCard(
    corner: CornerUi,
    modifier: Modifier,
    onShelfClick: (locationId: String, slotId: String) -> Unit
) {
    Card(modifier = modifier.clip(RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(corner.location.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // show all shelves in a responsive grid inside the card
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(240.dp)
            ) {
                gridItems(items = corner.shelves.sortedBy { it.slotIndex }) { shelf ->
                    SnackCard(name = shelf.itemName, price = shelf.price, imageUrl = shelf.imageUrl, inventoryCount = shelf.inventoryCount, onClick = { onShelfClick(corner.location.id, shelf.slotId) })
                }
            }
        }
    }
}

@Composable
fun SnackCard(name: String, price: Double?, imageUrl: String?, inventoryCount: Int = 0, onClick: () -> Unit) {
    Card(modifier = Modifier
        .width(120.dp)
        .height(120.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable { onClick() }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            // RemoteImage will render a platform-appropriate image or placeholder (same package)
            RemoteImage(imageUrl = imageUrl, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = name, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${inventoryCount} left", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = price?.let { "€${String.format("%.2f", it)}" } ?: "—", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyCornersState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No locations found", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Once locations are configured, they will show up here.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
