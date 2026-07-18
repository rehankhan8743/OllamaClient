package com.rehan.ollamaclient.ui.screens.serverlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServerListViewModel(application: Application) : AndroidViewModel(application) {
    private val serverRepo = ServiceLocator.getServerRepository(application)

    val servers = serverRepo.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDefault(serverId: Long) {
        viewModelScope.launch {
            serverRepo.setDefaultServer(serverId)
        }
    }

    fun deleteServer(server: ServerConfig) {
        viewModelScope.launch {
            serverRepo.deleteServer(server)
        }
    }
}
