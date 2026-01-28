package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.R

@Composable
actual fun RemoteImage(imageUrl: String?, modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.default_item),
        contentDescription = null,
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        contentScale = ContentScale.Crop
    )
}
