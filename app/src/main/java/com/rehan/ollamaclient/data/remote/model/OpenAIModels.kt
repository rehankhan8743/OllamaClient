package com.rehan.ollamaclient.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val stream: Boolean = true,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val max_tokens: Int? = null
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIChatResponse(
    val id: String = "",
    val object_: String = "",
    val choices: List<OpenAIChoice> = emptyList(),
    val usage: OpenAIUsage? = null
)

@Serializable
data class OpenAIChoice(
    val index: Int = 0,
    val delta: OpenAIDelta? = null,
    val message: OpenAIMessage? = null,
    val finish_reason: String? = null
)

@Serializable
data class OpenAIDelta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class OpenAIUsage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)

@Serializable
data class OpenAIModelResponse(
    val data: List<OpenAIModel> = emptyList()
)

@Serializable
data class OpenAIModel(
    val id: String = "",
    val object_: String = "",
    val created: Long = 0,
    val owned_by: String = ""
)
