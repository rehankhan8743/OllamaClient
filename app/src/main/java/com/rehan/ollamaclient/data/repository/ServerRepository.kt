package com.rehan.ollamaclient.data.repository

import com.rehan.ollamaclient.data.local.dao.ServerConfigDao
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val dao: ServerConfigDao) {
    fun getAllServers(): Flow<List<ServerConfig>> = dao.getAllServers()
    fun getDefaultServer(): Flow<ServerConfig?> = dao.getDefaultServer()
    suspend fun getServerById(id: Long): ServerConfig? = dao.getServerById(id)
    suspend fun insertServer(server: ServerConfig): Long = dao.insertServer(server)
    suspend fun updateServer(server: ServerConfig) = dao.updateServer(server)
    suspend fun deleteServer(server: ServerConfig) = dao.deleteServer(server)
    suspend fun setDefaultServer(id: Long) {
        dao.clearDefaultServer()
        dao.setDefaultServer(id)
    }
}
