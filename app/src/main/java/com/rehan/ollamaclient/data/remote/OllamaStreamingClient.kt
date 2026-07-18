package com.rehan.ollamaclient.data.remote

import com.rehan.ollamaclient.data.local.entities.ServerConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

sealed class StreamEvent {
    data class Token(val text: String) : StreamEvent()
    object Done : StreamEvent()
    data class Error(val message: String) : StreamEvent()
    data class Progress(val status: String, val total: Long, val completed: Long) : StreamEvent()
}

class OllamaStreamingClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun streamChat(
        server: ServerConfig,
        model: String,
        messages: List<Pair<String, String>>,
        temperature: Float,
        topP: Float,
        topK: Int,
        numCtx: Int
    ): Flow<StreamEvent> = callbackFlow {
        val options = buildJsonObject {
            put("temperature", temperature.toDouble())
            put("top_p", topP.toDouble())
            put("top_k", topK)
            put("num_ctx", numCtx)
        }

        val messagesArray = buildJsonArray {
            for ((role, content) in messages) {
                addJsonObject {
                    put("role", role)
                    put("content", content)
                }
            }
        }

        val body = buildJsonObject {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
            put("options", options)
        }

        val requestBody = body.toString().toRequestBody("application/json".toMediaType())
        val url = "${server.url.trimEnd('/')}/api/chat"

        val requestBuilder = Request.Builder().url(url).post(requestBody)
        val customHeaders = parseCustomHeaders(server.customHeaders)
        for ((key, value) in customHeaders) {
            requestBuilder.addHeader(key, value)
        }

        val callback = object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                trySend(StreamEvent.Error(e.message ?: "Connection failed"))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    trySend(StreamEvent.Error("HTTP ${response.code}: ${response.message}"))
                    close()
                    return
                }

                val source = response.body?.source() ?: run {
                    trySend(StreamEvent.Error("Empty response body"))
                    close()
                    return
                }

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (line.isBlank()) continue

                        try {
                            val element = json.parseToJsonElement(line)
                            val obj = element.jsonObject

                            if (obj["done"]?.jsonPrimitive?.booleanOrNull == true) {
                                trySend(StreamEvent.Done)
                                break
                            }

                            val message = obj["message"]?.jsonObject
                            val content = message?.get("content")?.jsonPrimitive?.contentOrNull
                            if (!content.isNullOrEmpty()) {
                                trySend(StreamEvent.Token(content))
                            }
                        } catch (_: Exception) {
                            // Skip malformed JSON lines
                        }
                    }
                } catch (_: Exception) {
                    // Stream ended
                }

                trySend(StreamEvent.Done)
                close()
            }
        }

        val call = client.newCall(requestBuilder.build())
        call.enqueue(callback)

        awaitClose { call.cancel() }
    }

    fun streamChatCloud(
        server: ServerConfig,
        model: String,
        messages: List<Pair<String, String>>,
        temperature: Float,
        topP: Float
    ): Flow<StreamEvent> = callbackFlow {
        val messagesArray = buildJsonArray {
            for ((role, content) in messages) {
                addJsonObject {
                    put("role", role)
                    put("content", content)
                }
            }
        }

        val body = buildJsonObject {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
            put("temperature", temperature.toDouble())
            put("top_p", topP.toDouble())
        }

        val requestBody = body.toString().toRequestBody("application/json".toMediaType())
        val url = "${server.url.trimEnd('/')}/chat/completions"

        val requestBuilder = Request.Builder().url(url).post(requestBody)
        if (server.apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${server.apiKey}")
        }
        val customHeaders = parseCustomHeaders(server.customHeaders)
        for ((key, value) in customHeaders) {
            requestBuilder.addHeader(key, value)
        }

        val eventSourceFactory = EventSources.createFactory(client)
        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data.trim() == "[DONE]") {
                    trySend(StreamEvent.Done)
                    eventSource.cancel()
                    close()
                    return
                }

                try {
                    val element = json.parseToJsonElement(data)
                    val obj = element.jsonObject
                    val choices = obj["choices"]?.jsonArray
                    val delta = choices?.firstOrNull()?.jsonObject?.get("delta")?.jsonObject
                    val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
                    if (!content.isNullOrEmpty()) {
                        trySend(StreamEvent.Token(content))
                    }
                } catch (_: Exception) {
                    // Skip malformed lines
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val errorMsg = t?.message ?: response?.let { "HTTP ${it.code}" } ?: "Stream error"
                trySend(StreamEvent.Error(errorMsg))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
                trySend(StreamEvent.Done)
                close()
            }
        }

        val request = requestBuilder.build()
        val eventSource = eventSourceFactory.newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }

    fun pullModel(
        server: ServerConfig,
        model: String
    ): Flow<StreamEvent> = callbackFlow {
        val body = buildJsonObject {
            put("model", model)
            put("stream", true)
        }

        val requestBody = body.toString().toRequestBody("application/json".toMediaType())
        val url = "${server.url.trimEnd('/')}/api/pull"

        val request = Request.Builder().url(url).post(requestBody).build()

        val callback = object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                trySend(StreamEvent.Error(e.message ?: "Connection failed"))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    trySend(StreamEvent.Error("HTTP ${response.code}: ${response.message}"))
                    close()
                    return
                }

                val source = response.body?.source() ?: run {
                    trySend(StreamEvent.Error("Empty response body"))
                    close()
                    return
                }

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (line.isBlank()) continue

                        try {
                            val element = json.parseToJsonElement(line)
                            val obj = element.jsonObject
                            val status = obj["status"]?.jsonPrimitive?.contentOrNull ?: ""
                            val total = obj["total"]?.jsonPrimitive?.longOrNull ?: 0L
                            val completed = obj["completed"]?.jsonPrimitive?.longOrNull ?: 0L

                            trySend(StreamEvent.Progress(status, total, completed))

                            if (status == "success") {
                                trySend(StreamEvent.Done)
                                break
                            }
                        } catch (_: Exception) {
                            // Skip malformed JSON
                        }
                    }
                } catch (_: Exception) {
                    // Stream ended
                }

                trySend(StreamEvent.Done)
                close()
            }
        }

        val call = client.newCall(request)
        call.enqueue(callback)

        awaitClose { call.cancel() }
    }

    private fun parseCustomHeaders(headersJson: String): Map<String, String> {
        if (headersJson.isBlank()) return emptyMap()
        return try {
            val obj = json.parseToJsonElement(headersJson).jsonObject
            obj.mapValues { it.value.jsonPrimitive.content }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
