package com.rehan.ollamaclient.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ChatMessageEntity
import com.rehan.ollamaclient.data.local.entities.ChatSession
import com.rehan.ollamaclient.data.local.entities.MessageRole
import com.rehan.ollamaclient.data.remote.StreamEvent
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val sessionId: Long = savedStateHandle["sessionId"] ?: -1L
    private val chatRepo = ServiceLocator.getChatRepository(application)
    private val serverRepo = ServiceLocator.getServerRepository(application)
    private val streamingClient = ServiceLocator.getStreamingClient()

    private val _session = MutableStateFlow<ChatSession?>(null)
    val session: StateFlow<ChatSession?> = _session

    val messages: StateFlow<List<ChatMessageEntity>> = chatRepo.getMessages(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent

    private var streamingJob: Job? = null

    init {
        viewModelScope.launch {
            _session.value = chatRepo.getSessionById(sessionId)
        }
    }

    fun sendMessage(content: String) {
        if (_isStreaming.value) return
        if (content.isBlank()) return

        viewModelScope.launch {
            val session = chatRepo.getSessionById(sessionId) ?: return@launch
            val server = serverRepo.getServerById(session.serverId) ?: return@launch

            val userMessage = ChatMessageEntity(
                sessionId = sessionId,
                role = MessageRole.USER.name,
                content = content
            )
            chatRepo.insertMessage(userMessage)

            if (messages.value.isEmpty() && messages.value.size <= 1) {
                val title = content.take(50) + if (content.length > 50) "..." else ""
                chatRepo.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
            }

            _isStreaming.value = true
            _streamingContent.value = ""

            val assistantMessage = ChatMessageEntity(
                sessionId = sessionId,
                role = MessageRole.ASSISTANT.name,
                content = "",
                isStreaming = true
            )
            val assistantId = chatRepo.insertMessage(assistantMessage)

            val allMessages = chatRepo.getMessagesOnce(sessionId)
            val chatHistory = allMessages.dropLast(1).map { msg ->
                msg.role.lowercase() to msg.content
            }

            streamingJob = viewModelScope.launch {
                val flow = if (server.type == "ollama") {
                    streamingClient.streamChat(
                        server = server,
                        model = session.modelName,
                        messages = chatHistory,
                        temperature = session.temperature,
                        topP = session.topP,
                        topK = session.topK,
                        numCtx = session.numCtx
                    )
                } else {
                    streamingClient.streamChatCloud(
                        server = server,
                        model = session.modelName,
                        messages = chatHistory,
                        temperature = session.temperature,
                        topP = session.topP
                    )
                }

                val fullContent = StringBuilder()

                flow.collect { event ->
                    when (event) {
                        is StreamEvent.Token -> {
                            fullContent.append(event.text)
                            _streamingContent.value = fullContent.toString()
                        }
                        is StreamEvent.Done -> {
                            chatRepo.updateMessage(
                                assistantMessage.copy(
                                    content = fullContent.toString(),
                                    isStreaming = false
                                )
                            )
                            chatRepo.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
                            _isStreaming.value = false
                            _streamingContent.value = ""
                        }
                        is StreamEvent.Error -> {
                            chatRepo.updateMessage(
                                assistantMessage.copy(
                                    content = fullContent.toString().ifEmpty { "Error: ${event.message}" },
                                    isStreaming = false,
                                    isError = true
                                )
                            )
                            _isStreaming.value = false
                            _streamingContent.value = ""
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        streamingJob?.cancel()
        viewModelScope.launch {
            val session = chatRepo.getSessionById(sessionId) ?: return@launch
            val lastMsg = chatRepo.getLastMessage(sessionId)
            if (lastMsg != null && lastMsg.isStreaming) {
                chatRepo.updateMessage(lastMsg.copy(isStreaming = false))
            }
            _isStreaming.value = false
            _streamingContent.value = ""
        }
    }

    fun refreshSession() {
        viewModelScope.launch {
            _session.value = chatRepo.getSessionById(sessionId)
        }
    }
}
