package com.rehan.ollamaclient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rehan.ollamaclient.data.local.dao.ChatMessageDao
import com.rehan.ollamaclient.data.local.dao.ChatSessionDao
import com.rehan.ollamaclient.data.local.dao.ServerConfigDao
import com.rehan.ollamaclient.data.local.entities.ChatMessageEntity
import com.rehan.ollamaclient.data.local.entities.ChatSession
import com.rehan.ollamaclient.data.local.entities.ServerConfig

@Database(
    entities = [ServerConfig::class, ChatSession::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverConfigDao(): ServerConfigDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ollama_client.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
