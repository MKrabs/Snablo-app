package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A right-side modal drawer with fluid drag motion.
 *
 * Notes:
 * - This is intentionally "native-feeling": continuous drag + spring settle.
 * - Material3 doesn't support a native right-edge ModalNavigationDrawer, so this is a small custom scaffold.
 */
@Composable
fun RightFloatingDrawer(
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    drawerWidthDp: Int = 280,
    scrimAlpha: Float = 0.35f,
    openFromAnywhere: Boolean = false,
    edgeSwipeWidthDp: Int = 24,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val endPaddingDp = 16.dp
    val topPaddingDp = 24.dp
    val bottomPaddingDp = 16.dp

    val drawerWidthPx = with(density) { drawerWidthDp.dp.toPx() }
    val edgeWidthPx = with(density) { edgeSwipeWidthDp.dp.toPx() }
    val endPaddingPx = with(density) { endPaddingDp.toPx() }
    val closedOffsetPx = drawerWidthPx + endPaddingPx

    // offsetX: 0 = open; closedOffsetPx = fully closed (off-screen right)
    val offsetX = remember(closedOffsetPx) { Animatable(closedOffsetPx) }

    // Sync external state -> position
    LaunchedEffect(isOpen, closedOffsetPx) {
        val target = if (isOpen) 0f else closedOffsetPx
        offsetX.animateTo(
            target,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
        )
    }

    val progress = 1f - (offsetX.value / closedOffsetPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Single unified gesture layer. Works whether opened or closed.
            .pointerInput(drawerWidthPx, isOpen, openFromAnywhere, edgeWidthPx, closedOffsetPx) {
                var dragging = false
                var accumulatedDx = 0f
                var startedAllowed = false
                var snapJob: Job? = null
                val velocityTracker = VelocityTracker()

                suspend fun settle(shouldOpen: Boolean) {
                    snapJob?.cancel()
                    val target = if (shouldOpen) 0f else closedOffsetPx
                    offsetX.animateTo(
                        target,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        )
                    )
                    onOpenChange(shouldOpen)
                }

                fun record(change: PointerInputChange) {
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                }

                detectHorizontalDragGestures(
                    onDragStart = { start: Offset ->
                        dragging = false
                        accumulatedDx = 0f
                        velocityTracker.resetTracking()
                        startedAllowed = openFromAnywhere || start.x >= (size.width - edgeWidthPx) || isOpen
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        accumulatedDx += dragAmount
                        record(change)

                        if (!dragging) {
                            if (abs(accumulatedDx) < 10f) return@detectHorizontalDragGestures

                            val wantsOpen = accumulatedDx < 0f
                            val wantsClose = accumulatedDx > 0f

                            dragging = when {
                                // open: swipe left
                                !isOpen && wantsOpen && startedAllowed -> true
                                // close: swipe right
                                isOpen && wantsClose -> true
                                else -> false
                            }
                        }

                        if (!dragging) return@detectHorizontalDragGestures

                        change.consume()
                        snapJob?.cancel()
                        val newVal = (offsetX.value + dragAmount).coerceIn(0f, closedOffsetPx)
                        // We're already in a suspend scope here.
                        snapJob = scope.launch { offsetX.snapTo(newVal) }
                    },
                    onDragEnd = {
                        if (!dragging) return@detectHorizontalDragGestures

                        val vx = velocityTracker.calculateVelocity().x
                        val shouldOpen = when {
                            vx < -800f -> true
                            vx > 800f -> false
                            else -> offsetX.value < (closedOffsetPx * 0.5f)
                        }

                        // Always settle fully.
                        scope.launch { settle(shouldOpen) }

                        dragging = false
                        accumulatedDx = 0f
                        startedAllowed = false
                    },
                    onDragCancel = {
                        // Snap back to the current external state
                        val shouldOpen = isOpen
                        // Best-effort: settle to where state says we are.
                        // If state changes concurrently, LaunchedEffect will correct it.
                        val target = if (shouldOpen) 0f else closedOffsetPx
                        snapJob?.cancel()
                        snapJob = scope.launch { offsetX.snapTo(target) }

                        dragging = false
                        accumulatedDx = 0f
                        startedAllowed = false
                    }
                )
            }
    ) {
        content()

        // Scrim (only visible when partially open)
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha * progress))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOpenChange(false) }
            )
        }

        // Floating drawer panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPaddingDp, end = endPaddingDp, bottom = bottomPaddingDp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .width(drawerWidthDp.dp)
            ) {
                drawerContent()
            }
        }
    }
}
