package com.rehan.ollamaclient.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class OllamaTagsResponse(
    val models: List<OllamaModel> = emptyList()
)

@Serializable
data class OllamaModel(
    val name: String = "",
    val model: String = "",
    val modified_at: String = "",
    val size: Long = 0,
    val digest: String = "",
    val details: OllamaModelDetails = OllamaModelDetails()
)

@Serializable
data class OllamaModelDetails(
    val format: String = "",
    val family: String = "",
    val families: List<String> = emptyList(),
    val parameter_size: String = "",
    val quantization_level: String = ""
)

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = true,
    val options: JsonElement = buildJsonObject {
        put("temperature", 0.7)
    }
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String
)

@Serializable
data class OllamaChatResponse(
    val model: String = "",
    val created_at: String = "",
    val message: OllamaMessage? = null,
    val done: Boolean = false,
    val total_duration: Long = 0,
    val load_duration: Long = 0,
    val prompt_eval_count: Int = 0,
    val eval_count: Int = 0
)

@Serializable
data class OllamaPullRequest(
    val model: String,
    val stream: Boolean = true
)

@Serializable
data class OllamaPullResponse(
    val status: String = "",
    val digest: String = "",
    val total: Long = 0,
    val completed: Long = 0
)

@Serializable
data class OllamaDeleteRequest(
    val model: String
)
