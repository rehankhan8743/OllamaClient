package com.rehan.ollamaclient.ui.screens.modelmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.remote.StreamEvent
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ModelManagerViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val serverId: Long = savedStateHandle["serverId"] ?: -1L
    private val serverRepo = ServiceLocator.getServerRepository(application)
    private val streamingClient = ServiceLocator.getStreamingClient()

    data class ModelInfo(val name: String, val size: String = "", val details: String = "")

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPulling = MutableStateFlow(false)
    val isPulling: StateFlow<Boolean> = _isPulling

    private val _pullStatus = MutableStateFlow("")
    val pullStatus: StateFlow<String> = _pullStatus

    private val _pullProgress = MutableStateFlow(0f)
    val pullProgress: StateFlow<Float> = _pullProgress

    private val _pullModelName = MutableStateFlow("")
    val pullModelName: StateFlow<String> = _pullModelName

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var server: com.rehan.ollamaclient.data.local.entities.ServerConfig? = null

    fun setPullModelName(name: String) { _pullModelName.value = name }

    fun loadModels() {
        val s = server ?: return
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(s.connectTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(s.readTimeout.toLong(), java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                if (s.type == "ollama") {
                    val request = Request.Builder().url("${s.url.trimEnd('/')}/api/tags").get().build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: throw Exception("Empty response")
                    val json = Json { ignoreUnknownKeys = true }
                    val obj = json.parseToJsonElement(body).jsonObject
                    val modelsList = obj["models"]?.jsonArray?.mapNotNull { element ->
                        val modelObj = element.jsonObject
                        val name = modelObj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        val size = modelObj["size"]?.jsonPrimitive?.longOrNull ?: 0L
                        val details = modelObj["details"]?.jsonObject
                        val paramSize = details?.get("parameter_size")?.jsonPrimitive?.contentOrNull ?: ""
                        val quant = details?.get("quantization_level")?.jsonPrimitive?.contentOrNull ?: ""
                        val sizeStr = if (size > 0) "${String.format("%.1f", size / (1024.0 * 1024.0 * 1024.0))} GB" else ""
                        val detailsStr = listOfNotNull(paramSize.ifBlank { null }, quant.ifBlank { null }).joinToString(", ")
                        ModelInfo(name, sizeStr, detailsStr)
                    } ?: emptyList()
                    _models.value = modelsList
                } else {
                    val url = "${s.url.trimEnd('/')}/models"
                    val requestBuilder = Request.Builder().url(url).get()
                    if (s.apiKey.isNotBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer ${s.apiKey}")
                    }
                    val response = client.newCall(requestBuilder.build()).execute()
                    val body = response.body?.string() ?: throw Exception("Empty response")
                    val json = Json { ignoreUnknownKeys = true }
                    val obj = json.parseToJsonElement(body).jsonObject
                    val modelsList = obj["data"]?.jsonArray?.mapNotNull {
                        val modelObj = it.jsonObject
                        val id = modelObj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                        ModelInfo(id)
                    } ?: emptyList()
                    _models.value = modelsList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load models"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pullModel() {
        val s = server ?: return
        val modelName = _pullModelName.value
        if (modelName.isBlank()) return

        _isPulling.value = true
        _pullProgress.value = 0f
        _pullStatus.value = "Starting..."

        viewModelScope.launch {
            streamingClient.pullModel(s, modelName).collect { event ->
                when (event) {
                    is StreamEvent.Progress -> {
                        _pullStatus.value = event.status
                        _pullProgress.value = if (event.total > 0) event.completed.toFloat() / event.total else 0f
                    }
                    is StreamEvent.Done -> {
                        _isPulling.value = false
                        _pullProgress.value = 1f
                        _pullStatus.value = "Done"
                        loadModels()
                    }
                    is StreamEvent.Error -> {
                        _isPulling.value = false
                        _pullStatus.value = "Error: ${event.message}"
                        _error.value = event.message
                    }
                    else -> {}
                }
            }
        }
    }

    fun deleteModel(modelName: String) {
        val s = server ?: return
        viewModelScope.launch {
            try {
                val client = OkHttpClient.Builder().build()
                val body = buildJsonObject { put("model", modelName) }
                    .toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${s.url.trimEnd('/')}/api/delete")
                    .post(body)
                    .build()
                client.newCall(request).execute()
                loadModels()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete model"
            }
        }
    }

    init {
        viewModelScope.launch {
            server = serverRepo.getServerById(serverId)
            loadModels()
        }
    }
}
