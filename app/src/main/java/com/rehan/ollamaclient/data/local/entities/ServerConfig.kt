package com.rehan.ollamaclient.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "server_configs")
@Serializable
data class ServerConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val url: String,
    val apiKey: String = "",
    val customHeaders: String = "",
    val connectTimeout: Int = 30,
    val readTimeout: Int = 120,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
