package de.mkrabs.snablo.app

import android.app.Application

class SnabloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        // Make instance the concrete type to avoid accidental casts elsewhere
        lateinit var instance: SnabloApplication
            private set
    }
}
