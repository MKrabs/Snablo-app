package de.mkrabs.snablo.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mkrabs.snablo.app.data.auth.AuthService
import de.mkrabs.snablo.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for authentication
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSsoLoading: Boolean = false,
    val user: User? = null,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for authentication flow
 */
class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        Log.d("Snablo", "AuthViewModel.init - checking existing session")
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                Log.d("Snablo", "AuthViewModel.checkExistingSession - calling authService.hasValidSession()")
                if (authService.hasValidSession()) {
                    Log.d("Snablo", "AuthViewModel.checkExistingSession - has valid session, fetching user")
                    val user = authService.getCurrentUser()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        isAuthenticated = user != null,
                        error = null
                    )
                } else {
                    Log.d("Snablo", "AuthViewModel.checkExistingSession - no valid session")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.w("Snablo", "AuthViewModel.checkExistingSession - error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to check session"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result: Result<User> = authService.login(email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        isAuthenticated = user != null,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login error"
                )
            }
        }
    }

    fun loginWithMicrosoft() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSsoLoading = true, error = null)
            try {
                val result: Result<User> = authService.loginWithMicrosoft()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isSsoLoading = false,
                        user = user,
                        isAuthenticated = user != null,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSsoLoading = false,
                        error = result.exceptionOrNull()?.message ?: "SSO login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSsoLoading = false,
                    error = e.message ?: "SSO error"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authService.logout()
                _uiState.value = AuthUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Logout failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
