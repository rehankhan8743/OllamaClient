package com.rehan.ollamaclient.data.repository

import com.rehan.ollamaclient.data.local.dao.ChatMessageDao
import com.rehan.ollamaclient.data.local.dao.ChatSessionDao
import com.rehan.ollamaclient.data.local.entities.ChatMessageEntity
import com.rehan.ollamaclient.data.local.entities.ChatSession
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) {
    fun getAllSessions(): Flow<List<ChatSession>> = sessionDao.getAllSessions()
    suspend fun getSessionById(id: Long): ChatSession? = sessionDao.getSessionById(id)
    fun observeSessionById(id: Long): Flow<ChatSession?> = sessionDao.observeSessionById(id)
    suspend fun insertSession(session: ChatSession): Long = sessionDao.insertSession(session)
    suspend fun updateSession(session: ChatSession) = sessionDao.updateSession(session)
    suspend fun deleteSession(session: ChatSession) {
        messageDao.deleteMessagesForSession(session.id)
        sessionDao.deleteSession(session)
    }
    suspend fun deleteAllData() {
        messageDao.deleteAllMessages()
        sessionDao.deleteAllSessions()
    }

    fun getMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = messageDao.getMessagesForSession(sessionId)
    suspend fun getMessagesOnce(sessionId: Long): List<ChatMessageEntity> = messageDao.getMessagesForSessionOnce(sessionId)
    suspend fun insertMessage(message: ChatMessageEntity): Long = messageDao.insertMessage(message)
    suspend fun updateMessage(message: ChatMessageEntity) = messageDao.updateMessage(message)
    suspend fun deleteMessage(message: ChatMessageEntity) = messageDao.deleteMessage(message)
    suspend fun getLastMessage(sessionId: Long): ChatMessageEntity? = messageDao.getLastMessage(sessionId)
}
