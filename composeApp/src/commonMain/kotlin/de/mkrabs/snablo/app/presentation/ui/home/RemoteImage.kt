package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun RemoteImage(imageUrl: String?, modifier: Modifier)
