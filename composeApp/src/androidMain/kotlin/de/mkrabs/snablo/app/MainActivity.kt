package de.mkrabs.snablo.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import de.mkrabs.snablo.app.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lightweight startup log to help debugging initialization ordering
        Log.d("Snablo", "MainActivity.onCreate - starting setContent")

        // Minimal startup: delegate to the Compose entry point. Add a small top padding
        // so the UI doesn't sit flush against the notification/status bar.
        setContent {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                App()
            }
        }

        Log.d("Snablo", "MainActivity.onCreate - setContent returned")
    }
}

@Preview
@androidx.compose.runtime.Composable
fun AppAndroidPreview() {
    App()
}