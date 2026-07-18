package com.rehan.ollamaclient.data.local.dao

import androidx.room.*
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_configs ORDER BY createdAt DESC")
    fun getAllServers(): Flow<List<ServerConfig>>

    @Query("SELECT * FROM server_configs WHERE id = :id")
    suspend fun getServerById(id: Long): ServerConfig?

    @Query("SELECT * FROM server_configs WHERE isDefault = 1 LIMIT 1")
    fun getDefaultServer(): Flow<ServerConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerConfig): Long

    @Update
    suspend fun updateServer(server: ServerConfig)

    @Delete
    suspend fun deleteServer(server: ServerConfig)

    @Query("UPDATE server_configs SET isDefault = 0")
    suspend fun clearDefaultServer()

    @Query("UPDATE server_configs SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultServer(id: Long)
}
