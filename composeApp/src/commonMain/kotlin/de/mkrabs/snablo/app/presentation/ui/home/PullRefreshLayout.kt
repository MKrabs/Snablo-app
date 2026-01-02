package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt
import kotlin.math.max
import kotlin.math.min

/**
 * Lightweight pull-to-refresh container for a single LazyColumn.
 * - Creates and exposes a [LazyListState] to the content lambda.
 * - When the list is scrolled to the top and the user drags down past [refreshThresholdPx], triggers [onRefresh].
 * - Shows [CircularProgressIndicator] at the top center while [isRefreshing] is true.
 */
@Composable
fun PullRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable (state: LazyListState) -> Unit
) {
    val listState = rememberLazyListState()
    val overscroll = remember { mutableStateOf(0f) }

    val touchSlop = with(LocalDensity.current) { 6.dp.toPx() }
    val refreshThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    // Nested scroll connection intercepts pre-scroll when children attempt to scroll.
    val nestedConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // available.y > 0 means the user is dragging down
                val dy = available.y
                if (dy > 0f && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    val prev = overscroll.value
                    val new = prev + dy
                    overscroll.value = new

                    // Only start consuming after touchSlop is exceeded
                    if (new > touchSlop) {
                        val consumedY = min(dy, new - touchSlop)
                        return Offset(0f, consumedY)
                    }
                } else if (dy < 0f && overscroll.value > 0f) {
                    // If pulling up while we have overscroll, reduce overscroll and let the child handle remaining
                    val new = max(0f, overscroll.value + dy)
                    val consumedY = overscroll.value - new
                    overscroll.value = new
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // When fling starts, decide whether to trigger refresh
                if (overscroll.value >= refreshThresholdPx && !isRefreshing) {
                    onRefresh()
                }
                overscroll.value = 0f
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Also handle release (no/fling) — if overscroll exceeded threshold, trigger refresh
                if (overscroll.value >= refreshThresholdPx && !isRefreshing) {
                    onRefresh()
                }
                overscroll.value = 0f
                return Velocity.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // If child scrolled up (consumed.y > 0), reduce overscroll accordingly
                if (consumed.y > 0f && overscroll.value > 0f) {
                    overscroll.value = max(0f, overscroll.value - consumed.y)
                }
                return Offset.Zero
            }
        }
    }

    Box(modifier = Modifier.nestedScroll(nestedConnection)) {
        content(listState)

        val indicatorOffset = if (isRefreshing) 36 else (overscroll.value / 3f).roundToInt()
        if (isRefreshing || overscroll.value > touchSlop) {
            Box(modifier = Modifier.offset { IntOffset(0, indicatorOffset) }.align(Alignment.TopCenter)) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
