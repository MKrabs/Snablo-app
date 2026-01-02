package de.mkrabs.snablo.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import de.mkrabs.snablo.app.data.api.HttpClientFactory
import de.mkrabs.snablo.app.data.api.PocketBaseClient
import de.mkrabs.snablo.app.data.repository.*
import de.mkrabs.snablo.app.data.session.defaultSessionManager
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
    // Create & keep dependencies stable across recomposition (including desktop resize).
    val httpClient = remember { HttpClientFactory.createHttpClient() }
    val sessionManager = remember { defaultSessionManager() }
    val apiClient = remember { PocketBaseClient(httpClient, sessionManager) }

    // Repositories (stable)
    val authRepository = remember { AuthRepositoryImpl(apiClient) }
    val catalogRepository = remember { CatalogRepositoryImpl(apiClient) }
    val shelfRepository = remember { ShelfRepositoryImpl(apiClient) }
    val ledgerRepository = remember { LedgerRepositoryImpl(apiClient) }
    @Suppress("UNUSED_VARIABLE")
    val reconciliationRepository = remember { ReconciliationRepositoryImpl(apiClient) }

    // Services (stable)
    val authService = remember { de.mkrabs.snablo.app.data.auth.AuthService(authRepository, sessionManager, apiClient) }

    // ViewModels (stable for the life of this composition)
    val authViewModel = remember { AuthViewModel(authService) }
    val homeViewModel = remember { HomeViewModel(ledgerRepository, shelfRepository, catalogRepository) }
    val shelfViewModel = remember { ShelfViewModel(catalogRepository, shelfRepository) }

    // Navigation state: saveable so it survives configuration changes / window recreation where supported.
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }

    // Drive navigation from auth state, but don't reset user-driven navigation unnecessarily.
    val authState by authViewModel.uiState.collectAsState()
    LaunchedEffect(authState.isLoading, authState.isAuthenticated) {
        // Only auto-navigate while we're on SPLASH or LOGIN.
        if (!authState.isLoading) {
            if (authState.isAuthenticated) {
                if (currentScreen == AppScreen.SPLASH || currentScreen == AppScreen.LOGIN) {
                    currentScreen = AppScreen.HOME
                }
            } else {
                if (currentScreen == AppScreen.SPLASH) {
                    currentScreen = AppScreen.LOGIN
                }
            }
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
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
                        userId = authState.user?.id ?: "",
                        onTopUp = { /* TODO: navigate to TopUp */ },
                        onProfile = { /* TODO: open profile */ },
                        onSlotClick = { _, _ -> /* TODO: open purchase */ },
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
