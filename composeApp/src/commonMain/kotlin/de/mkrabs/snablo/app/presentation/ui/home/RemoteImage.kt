package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Simple common placeholder image renderer. Android-specific remote loading can be added later.
 */
@Composable
fun RemoteImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier
        .size(56.dp)
        .clip(RoundedCornerShape(28.dp))) {
        // placeholder; content (initial letter) is rendered by SnackCard's Text if needed
    }
}
