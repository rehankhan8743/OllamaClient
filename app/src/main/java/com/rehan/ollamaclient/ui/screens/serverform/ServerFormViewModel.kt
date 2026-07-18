package com.rehan.ollamaclient.ui.screens.serverform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServerFormViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val serverId: Long = savedStateHandle["serverId"] ?: -1L
    private val serverRepo = ServiceLocator.getServerRepository(application)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _type = MutableStateFlow("ollama")
    val type: StateFlow<String> = _type

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _customHeaders = MutableStateFlow("")
    val customHeaders: StateFlow<String> = _customHeaders

    private val _connectTimeout = MutableStateFlow(30)
    val connectTimeout: StateFlow<Int> = _connectTimeout

    private val _readTimeout = MutableStateFlow(120)
    val readTimeout: StateFlow<Int> = _readTimeout

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    val isEditing: Boolean = serverId > 0

    init {
        if (isEditing) {
            viewModelScope.launch {
                serverRepo.getServerById(serverId)?.let { server ->
                    _name.value = server.name
                    _type.value = server.type
                    _url.value = server.url
                    _apiKey.value = server.apiKey
                    _customHeaders.value = server.customHeaders
                    _connectTimeout.value = server.connectTimeout
                    _readTimeout.value = server.readTimeout
                }
            }
        }
    }

    fun setName(name: String) { _name.value = name }
    fun setType(type: String) { _type.value = type }
    fun setUrl(url: String) { _url.value = url }
    fun setApiKey(key: String) { _apiKey.value = key }
    fun setCustomHeaders(headers: String) { _customHeaders.value = headers }
    fun setConnectTimeout(timeout: Int) { _connectTimeout.value = timeout }
    fun setReadTimeout(timeout: Int) { _readTimeout.value = timeout }

    fun save() {
        viewModelScope.launch {
            val server = ServerConfig(
                id = if (isEditing) serverId else 0,
                name = _name.value.ifBlank { "My Server" },
                type = _type.value,
                url = _url.value,
                apiKey = _apiKey.value,
                customHeaders = _customHeaders.value,
                connectTimeout = _connectTimeout.value,
                readTimeout = _readTimeout.value
            )
            serverRepo.insertServer(server)
            _saved.value = true
        }
    }
}
