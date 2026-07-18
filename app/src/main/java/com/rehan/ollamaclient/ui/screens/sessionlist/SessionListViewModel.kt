package com.rehan.ollamaclient.ui.screens.sessionlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rehan.ollamaclient.data.local.entities.ChatSession
import com.rehan.ollamaclient.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepo = ServiceLocator.getChatRepository(application)

    val sessions = chatRepo.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(session: ChatSession) {
        viewModelScope.launch {
            chatRepo.deleteSession(session)
        }
    }
}
