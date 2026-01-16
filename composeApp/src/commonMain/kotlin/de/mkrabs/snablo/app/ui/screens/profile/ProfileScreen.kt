package de.mkrabs.snablo.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.mkrabs.snablo.app.data.repository.AuthRepository
import de.mkrabs.snablo.app.ui.screens.auth.LoginScreen
import org.koin.compose.koinInject

object ProfileScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val currentUser by authRepository.currentUser.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profil") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Placeholder
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentUser?.name?.take(2)?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name
                Text(
                    text = currentUser?.name ?: "Unbekannt",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rolle
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (currentUser?.role?.name == "admin")
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = currentUser?.role?.name?.uppercase() ?: "USER",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                OutlinedButton(
                    onClick = {
                        authRepository.logout()
                        navigator.replaceAll(LoginScreen)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abmelden")
                }
            }
        }
    }
}
