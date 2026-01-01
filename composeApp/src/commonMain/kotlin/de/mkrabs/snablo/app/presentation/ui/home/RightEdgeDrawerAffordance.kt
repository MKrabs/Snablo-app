package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RightEdgeDrawerAffordance() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(20.dp)
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.01f))
    ) {
        // TODO: hook swipe gestures to open drawer
    }
}

