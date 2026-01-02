package de.mkrabs.snablo.app

import android.app.Application

class SnabloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}

