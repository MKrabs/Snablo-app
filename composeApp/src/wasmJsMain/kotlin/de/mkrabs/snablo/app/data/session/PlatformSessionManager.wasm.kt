package de.mkrabs.snablo.app.data.session

// wasmJs has no stable localStorage API in all targets here; keep in-memory for now.
actual fun platformSessionManager(): SessionManager = InMemorySessionManager()

