package de.mkrabs.snablo.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mkrabs.snablo.app.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Use BoxWithConstraints to react to available width
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth > 700.dp
            // Use separate scroll states for each scrollable area and ensure they have
            // bounded height constraints so Compose doesn't see infinite maxHeight.
            val leftScrollState = rememberScrollState()
            val rightScrollState = rememberScrollState()
            val narrowScrollState = rememberScrollState()

            if (isWide) {
                // Two-column layout with vertical divider
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: login form constrained to a reasonable width
                    Column(
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .fillMaxHeight()
                            .verticalScroll(leftScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Huge SNABLO logo placeholder
                        Text(
                            text = "SNABLO",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Email: singleLine, email keyboard to help autofill
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            enabled = !uiState.isLoading && !uiState.isSsoLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                            // removed keyboardOptions (platform-specific) to keep commonMain compilable
                        )

                        // Password: singleLine, password keyboard
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            enabled = !uiState.isLoading && !uiState.isSsoLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            visualTransformation = PasswordVisualTransformation()
                            // removed keyboardOptions (platform-specific) to keep commonMain compilable
                        )

                        if (uiState.error != null) {
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isLoading && !uiState.isSsoLoading && email.isNotBlank() && password.isNotBlank()
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else Text("Login")
                        }

                        // Divider with a centered dot (kept for vertical layout it will be hidden)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface)
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { viewModel.loginWithMicrosoft() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isLoading && !uiState.isSsoLoading
                        ) {
                            if (uiState.isSsoLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else Text("Sign in with Microsoft")
                        }
                    }

                    // Vertical divider with a centered dot
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Divider(modifier = Modifier
                            .width(1.dp)
                            .weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface)
                        )
                        Divider(modifier = Modifier
                            .width(1.dp)
                            .weight(1f)
                        )
                    }

                    // Right: secondary content (illustration / marketing / helper)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            // Make sure the right column gets a bounded height from the
                            // parent Row / container so verticalScroll sees finite constraints.
                            .fillMaxHeight()
                            .verticalScroll(rightScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Sign in to continue to your account.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Narrow: single-column, centered and constrained width, scrollable so keyboard won't overlap
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 400.dp)
                        // Limit the height available to this centered column to the
                        // available maxHeight so verticalScroll does not receive
                        // unbounded (infinite) constraints. `heightIn` isn't available
                        // in this Compose version in commonMain, so use `height(max)`.
                        .height(maxHeight)
                        .verticalScroll(narrowScrollState),
                     horizontalAlignment = Alignment.CenterHorizontally
                 ) {
                     // Huge SNABLO logo placeholder
                     Text(
                         text = "SNABLO",
                         style = MaterialTheme.typography.displayLarge,
                         modifier = Modifier.padding(bottom = 24.dp)
                     )

                     // Email: singleLine, email keyboard to help autofill
                     OutlinedTextField(
                         value = email,
                         onValueChange = { email = it },
                         label = { Text("Email") },
                         singleLine = true,
                         enabled = !uiState.isLoading && !uiState.isSsoLoading,
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(bottom = 12.dp)
                     )

                     // Password: singleLine, password keyboard
                     OutlinedTextField(
                         value = password,
                         onValueChange = { password = it },
                         label = { Text("Password") },
                         singleLine = true,
                         enabled = !uiState.isLoading && !uiState.isSsoLoading,
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(bottom = 16.dp),
                         visualTransformation = PasswordVisualTransformation()
                     )

                     if (uiState.error != null) {
                         Text(
                             text = uiState.error ?: "",
                             color = MaterialTheme.colorScheme.error,
                             modifier = Modifier.padding(bottom = 12.dp)
                         )
                     }

                     Button(
                         onClick = { viewModel.login(email, password) },
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(48.dp),
                         enabled = !uiState.isLoading && !uiState.isSsoLoading && email.isNotBlank() && password.isNotBlank()
                     ) {
                         if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                         else Text("Login")
                     }

                     // Divider with a centered dot
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(vertical = 18.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Divider(modifier = Modifier.weight(1f))
                         Box(
                             modifier = Modifier
                                 .padding(horizontal = 12.dp)
                                 .size(10.dp)
                                 .clip(CircleShape)
                                 .background(MaterialTheme.colorScheme.onSurface)
                         )
                         Divider(modifier = Modifier.weight(1f))
                     }

                     Spacer(modifier = Modifier.height(4.dp))

                     Button(
                         onClick = { viewModel.loginWithMicrosoft() },
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(48.dp),
                         enabled = !uiState.isLoading && !uiState.isSsoLoading
                     ) {
                         if (uiState.isSsoLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                         else Text("Sign in with Microsoft")
                     }
                 }
            }
        }

        // Footer: show heart icon instead of <3 and use imePadding so it moves above the keyboard properly
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "made with ",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
            Text(
                text = "♥",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateToHome() else if (!uiState.isLoading) onNavigateToLogin()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
