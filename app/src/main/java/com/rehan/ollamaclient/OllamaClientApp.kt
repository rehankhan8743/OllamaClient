package com.rehan.ollamaclient

import android.app.Application
import com.rehan.ollamaclient.di.ServiceLocator

class OllamaClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.getDatabase(this)
        ServiceLocator.getPreferencesManager(this)
        ServiceLocator.getServerRepository(this)
        ServiceLocator.getChatRepository(this)
        ServiceLocator.getStreamingClient()
    }
}
