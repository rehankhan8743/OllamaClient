package com.rehan.ollamaclient.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = ServiceLocator.getPreferencesManager(application)
    private val serverRepo = ServiceLocator.getServerRepository(application)
    private val chatRepo = ServiceLocator.getChatRepository(application)

    val themeMode = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val dynamicColor = prefs.dynamicColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val defaultServerId = prefs.defaultServerId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1L)
    val defaultModel = prefs.defaultModel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val servers = serverRepo.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    fun setDefaultServer(id: Long) {
        viewModelScope.launch { prefs.setDefaultServerId(id) }
    }

    fun setDefaultModel(model: String) {
        viewModelScope.launch { prefs.setDefaultModel(model) }
    }

    fun requestClearHistory() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepo.deleteAllData()
            _showClearDialog.value = false
        }
    }
}
