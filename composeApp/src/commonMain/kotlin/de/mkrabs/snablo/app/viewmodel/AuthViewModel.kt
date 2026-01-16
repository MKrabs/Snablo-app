package de.mkrabs.snablo.app.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.mkrabs.snablo.app.data.model.User
import de.mkrabs.snablo.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Bitte E-Mail und Passwort eingeben")
            return
        }

        _uiState.value = AuthUiState.Loading

        screenModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "Anmeldung fehlgeschlagen"
                    )
                }
        }
    }

    fun register(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Bitte alle Felder ausfüllen")
            return
        }

        if (password.length < 8) {
            _uiState.value = AuthUiState.Error("Passwort muss mindestens 8 Zeichen haben")
            return
        }

        _uiState.value = AuthUiState.Loading

        screenModelScope.launch {
            authRepository.register(email, password, name)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "Registrierung fehlgeschlagen"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
