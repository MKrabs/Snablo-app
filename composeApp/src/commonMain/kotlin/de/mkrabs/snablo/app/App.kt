package de.mkrabs.snablo.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import de.mkrabs.snablo.app.data.api.HttpClientFactory
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.repository.*
import de.mkrabs.snablo.app.data.session.InMemorySessionManager
import de.mkrabs.snablo.app.presentation.ui.LoginScreen
import de.mkrabs.snablo.app.presentation.ui.SplashScreen
import de.mkrabs.snablo.app.presentation.ui.HomeScreen
import de.mkrabs.snablo.app.presentation.viewmodel.AuthViewModel
import de.mkrabs.snablo.app.presentation.viewmodel.HomeViewModel
import de.mkrabs.snablo.app.presentation.viewmodel.ShelfViewModel

enum class AppScreen {
    SPLASH,
    LOGIN,
    HOME,
    SHELF,
    PURCHASE,
    HISTORY,
    ADMIN
}

/**
 * Main application entry point
 */
@Composable
@Preview
fun App() {
    // Initialize dependencies
    val httpClient = HttpClientFactory.createHttpClient()
    val sessionManager = InMemorySessionManager()
    val apiClient = PocketBaseClient(httpClient, sessionManager)

    // Repositories
    val authRepository = AuthRepositoryImpl(apiClient)
    val catalogRepository = CatalogRepositoryImpl(apiClient)
    val shelfRepository = ShelfRepositoryImpl(apiClient)
    val ledgerRepository = LedgerRepositoryImpl(apiClient)
    val reconciliationRepository = ReconciliationRepositoryImpl(apiClient)

    // Services
    val authService = de.mkrabs.snablo.app.data.auth.AuthService(authRepository, sessionManager, apiClient)

    // ViewModels
    val authViewModel = AuthViewModel(authService)
    val homeViewModel = HomeViewModel(ledgerRepository, shelfRepository)
    val shelfViewModel = ShelfViewModel(catalogRepository, shelfRepository)

    // Navigation state
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.SPLASH -> SplashScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = { currentScreen = AppScreen.HOME },
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN }
                )
                AppScreen.LOGIN -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { currentScreen = AppScreen.HOME }
                )
                AppScreen.HOME -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        shelfViewModel = shelfViewModel,
                        userId = "", // TODO: populate with real user id from authService/session
                        onTopUp = { /* TODO: navigate to TopUp */ },
                        onProfile = { /* TODO: open profile */ },
                        onSlotClick = { locationId, slotId -> /* TODO: open purchase */ },
                        onOpenSettings = { /* TODO: open settings */ },
                        onSendFeedback = { /* TODO: send feedback */ },
                        onOpenProfile = { /* TODO: open profile */ }
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Screen: $currentScreen")
                    }
                }
            }
        }
    }
}
