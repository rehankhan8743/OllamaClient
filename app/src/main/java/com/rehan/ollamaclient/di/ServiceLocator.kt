package com.rehan.ollamaclient.di

import android.content.Context
import com.rehan.ollamaclient.data.local.AppDatabase
import com.rehan.ollamaclient.data.local.PreferencesManager
import com.rehan.ollamaclient.data.remote.OllamaStreamingClient
import com.rehan.ollamaclient.data.repository.ChatRepository
import com.rehan.ollamaclient.data.repository.ServerRepository

object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var preferencesManager: PreferencesManager? = null

    @Volatile
    private var serverRepository: ServerRepository? = null

    @Volatile
    private var chatRepository: ChatRepository? = null

    @Volatile
    private var streamingClient: OllamaStreamingClient? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }
    }

    fun getPreferencesManager(context: Context): PreferencesManager {
        return preferencesManager ?: synchronized(this) {
            preferencesManager ?: PreferencesManager(context).also { preferencesManager = it }
        }
    }

    fun getServerRepository(context: Context): ServerRepository {
        return serverRepository ?: synchronized(this) {
            val dao = getDatabase(context).serverConfigDao()
            ServerRepository(dao).also { serverRepository = it }
        }
    }

    fun getChatRepository(context: Context): ChatRepository {
        return chatRepository ?: synchronized(this) {
            val db = getDatabase(context)
            ChatRepository(db.chatSessionDao(), db.chatMessageDao()).also { chatRepository = it }
        }
    }

    fun getStreamingClient(): OllamaStreamingClient {
        return streamingClient ?: synchronized(this) {
            OllamaStreamingClient().also { streamingClient = it }
        }
    }
}
