package com.rehan.ollamaclient.ui.screens.newchat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ChatSession
import com.rehan.ollamaclient.data.local.entities.ServerConfig
import com.rehan.ollamaclient.data.remote.OllamaStreamingClient
import com.rehan.ollamaclient.data.remote.StreamEvent
import com.rehan.ollamaclient.data.remote.model.OllamaModel
import com.rehan.ollamaclient.data.remote.model.OpenAIModel
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class NewChatViewModel(application: Application) : AndroidViewModel(application) {
    private val serverRepo = ServiceLocator.getServerRepository(application)
    private val chatRepo = ServiceLocator.getChatRepository(application)
    private val prefs = ServiceLocator.getPreferencesManager(application)
    private val streamingClient = ServiceLocator.getStreamingClient()

    val servers = serverRepo.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ollamaModels = MutableStateFlow<List<String>>(emptyList())
    val ollamaModels: StateFlow<List<String>> = _ollamaModels

    private val _cloudModels = MutableStateFlow<List<String>>(emptyList())
    val cloudModels: StateFlow<List<String>> = _cloudModels

    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val defaultServerId = prefs.defaultServerId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1L)

    val defaultModel = prefs.defaultModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadModels(server: ServerConfig) {
        _isLoadingModels.value = true
        _error.value = null
        _ollamaModels.value = emptyList()
        _cloudModels.value = emptyList()

        viewModelScope.launch {
            try {
                if (server.type == "ollama") {
                    loadOllamaModels(server)
                } else {
                    loadCloudModels(server)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load models"
            } finally {
                _isLoadingModels.value = false
            }
        }
    }

    private suspend fun loadOllamaModels(server: ServerConfig) {
        try {
            val client = OkHttpClient.Builder().connectTimeout(server.connectTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS).readTimeout(server.readTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS).build()
            val request = Request.Builder().url("${server.url.trimEnd('/')}/api/tags").get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response")
            val json = Json { ignoreUnknownKeys = true }
            val obj = json.parseToJsonElement(body).jsonObject
            val models = obj["models"]?.jsonArray?.mapNotNull {
                it.jsonObject["name"]?.jsonPrimitive?.contentOrNull
            } ?: emptyList()
            _ollamaModels.value = models
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load models"
        }
    }

    private suspend fun loadCloudModels(server: ServerConfig) {
        try {
            val client = OkHttpClient.Builder().connectTimeout(server.connectTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS).readTimeout(server.readTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS).build()
            val url = "${server.url.trimEnd('/')}/models"
            val requestBuilder = Request.Builder().url(url).get()
            if (server.apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${server.apiKey}")
            }
            val response = client.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: throw Exception("Empty response")
            val json = Json { ignoreUnknownKeys = true }
            val obj = json.parseToJsonElement(body).jsonObject
            val models = obj["data"]?.jsonArray?.mapNotNull {
                it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
            } ?: emptyList()
            _cloudModels.value = models
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load models"
        }
    }

    fun createSession(
        server: ServerConfig,
        modelName: String,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val session = ChatSession(
                serverId = server.id,
                modelName = modelName,
                title = "New Chat"
            )
            val id = chatRepo.insertSession(session)
            onCreated(id)
        }
    }

    fun createSessionWithDefaults(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val serverId = prefs.defaultServerId.first()
            val model = prefs.defaultModel.first()
            if (serverId > 0 && model.isNotBlank()) {
                val server = serverRepo.getServerById(serverId)
                if (server != null) {
                    val session = ChatSession(
                        serverId = server.id,
                        modelName = model,
                        title = "New Chat"
                    )
                    val id = chatRepo.insertSession(session)
                    onCreated(id)
                    return@launch
                }
            }
            onCreated(-1L)
        }
    }
}
