package de.mkrabs.snablo.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.mkrabs.snablo.app.ui.screens.home.HomeScreen
import de.mkrabs.snablo.app.viewmodel.AuthUiState
import de.mkrabs.snablo.app.viewmodel.AuthViewModel

object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AuthViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        // Bei Erfolg zur Home-Seite navigieren
        LaunchedEffect(uiState) {
            if (uiState is AuthUiState.Success) {
                navigator.replace(HomeScreen)
            }
        }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo / Titel
                Text(
                    text = "Snablo",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Snack-Ecke",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                // E-Mail Feld
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Passwort Feld
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message
                if (uiState is AuthUiState.Error) {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Login Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    enabled = uiState !is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Anmelden")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Registrieren Link
                TextButton(
                    onClick = { navigator.push(RegisterScreen) }
                ) {
                    Text("Noch kein Konto? Registrieren")
                }
            }
        }
    }
}
