package de.mkrabs.snablo.app.data.session

import de.mkrabs.snablo.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Verwaltet die aktuelle Session (Token + User)
 *
 * TODO: Token persistent speichern (platform-spezifisch)
 */
class SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var _token: String? = null
    val token: String? get() = _token

    val isLoggedIn: Boolean get() = _token != null && _currentUser.value != null

    val isAdmin: Boolean get() = _currentUser.value?.role == de.mkrabs.snablo.app.data.model.UserRole.admin

    fun setSession(token: String, user: User) {
        _token = token
        _currentUser.value = user
    }

    fun clearSession() {
        _token = null
        _currentUser.value = null
    }

    fun updateUser(user: User) {
        _currentUser.value = user
    }
}
