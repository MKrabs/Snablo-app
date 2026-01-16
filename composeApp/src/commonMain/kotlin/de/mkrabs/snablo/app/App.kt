package de.mkrabs.snablo.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import de.mkrabs.snablo.app.di.appModule
import de.mkrabs.snablo.app.ui.screens.auth.LoginScreen
import de.mkrabs.snablo.app.ui.theme.SnabloTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        SnabloTheme {
            Navigator(LoginScreen)
        }
    }
}
