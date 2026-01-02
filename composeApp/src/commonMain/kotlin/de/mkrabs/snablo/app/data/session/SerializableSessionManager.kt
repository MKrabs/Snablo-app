package de.mkrabs.snablo.app.data.session

import de.mkrabs.snablo.app.domain.model.Session
import kotlinx.serialization.json.Json

internal object SessionJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

internal fun encodeSession(session: Session): String =
    SessionJson.json.encodeToString(Session.serializer(), session)

internal fun decodeSession(raw: String): Session? =
    runCatching { SessionJson.json.decodeFromString(Session.serializer(), raw) }.getOrNull()
