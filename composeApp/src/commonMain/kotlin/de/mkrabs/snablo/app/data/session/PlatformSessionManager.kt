package de.mkrabs.snablo.app.data.session

/**
 * Platform-specific session manager factory.
 *
 * Each platform should provide an actual implementation that persists the session across app restarts.
 */
expect fun platformSessionManager(): SessionManager

