package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import de.mkrabs.snablo.app.R

@Composable
actual fun RemoteImage(imageUrl: String?, modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.default_item),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
