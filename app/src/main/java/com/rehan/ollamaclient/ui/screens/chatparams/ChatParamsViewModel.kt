package com.rehan.ollamaclient.ui.screens.chatparams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ChatSession
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatParamsViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val sessionId: Long = savedStateHandle["sessionId"] ?: -1L
    private val chatRepo = ServiceLocator.getChatRepository(application)

    private val _session = MutableStateFlow<ChatSession?>(null)
    val session: StateFlow<ChatSession?> = _session

    init {
        viewModelScope.launch {
            _session.value = chatRepo.getSessionById(sessionId)
        }
    }

    fun updateSession(updatedSession: ChatSession) {
        viewModelScope.launch {
            chatRepo.updateSession(updatedSession)
            _session.value = updatedSession
        }
    }

    fun updateSystemPrompt(prompt: String) {
        _session.value?.let { updateSession(it.copy(systemPrompt = prompt)) }
    }

    fun updateTemperature(temp: Float) {
        _session.value?.let { updateSession(it.copy(temperature = temp)) }
    }

    fun updateTopP(topP: Float) {
        _session.value?.let { updateSession(it.copy(topP = topP)) }
    }

    fun updateTopK(topK: Int) {
        _session.value?.let { updateSession(it.copy(topK = topK)) }
    }

    fun updateNumCtx(numCtx: Int) {
        _session.value?.let { updateSession(it.copy(numCtx = numCtx)) }
    }
}
