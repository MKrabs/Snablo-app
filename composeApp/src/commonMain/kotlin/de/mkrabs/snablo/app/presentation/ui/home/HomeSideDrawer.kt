package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HomeSideDrawer(
    drawerState: DrawerState,
    onProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    content: @Composable () -> Unit
) {
    val (open, setOpen) = remember { mutableStateOf(false) }

    RightFloatingDrawer(
        isOpen = open,
        onOpenChange = setOpen,
        openFromAnywhere = true,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                DrawerItem(iconText = "👤", label = "Profile") {
                    setOpen(false)
                    onProfile()
                }
                DrawerItem(iconText = "⚙", label = "Settings") {
                    setOpen(false)
                    onOpenSettings()
                }
                DrawerItem(iconText = "✉", label = "Send feedback") {
                    setOpen(false)
                    onSendFeedback()
                }
            }
        },
        content = {
            content()
        }
    )
}

@Composable
private fun DrawerItem(
    iconText: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = iconText, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
