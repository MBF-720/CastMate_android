package com.example.projecct_mobile.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour gérer les conversations et messages du chatbot
 */
@Dao
interface ChatDao {
    
    // ===== CONVERSATIONS =====
    
    /**
     * Crée ou met à jour une conversation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatConversationEntity): Long
    
    /**
     * Récupère une conversation par castingId
     */
    @Query("SELECT * FROM chat_conversations WHERE casting_id = :castingId LIMIT 1")
    suspend fun getConversationByCastingId(castingId: String): ChatConversationEntity?
    
    /**
     * Récupère une conversation avec tous ses messages
     */
    @Transaction
    @Query("SELECT * FROM chat_conversations WHERE casting_id = :castingId LIMIT 1")
    suspend fun getConversationWithMessages(castingId: String): ConversationWithMessages?
    
    /**
     * Récupère toutes les conversations avec leurs messages (Flow pour observer les changements)
     */
    @Transaction
    @Query("SELECT * FROM chat_conversations ORDER BY last_updated DESC")
    fun getAllConversationsFlow(): Flow<List<ConversationWithMessages>>
    
    /**
     * Récupère toutes les conversations
     */
    @Query("SELECT * FROM chat_conversations ORDER BY last_updated DESC")
    suspend fun getAllConversations(): List<ChatConversationEntity>
    
    /**
     * Met à jour le timestamp de dernière modification
     */
    @Query("UPDATE chat_conversations SET last_updated = :timestamp WHERE conversation_id = :conversationId")
    suspend fun updateConversationTimestamp(conversationId: Long, timestamp: Long)
    
    /**
     * Met à jour le nombre de messages d'une conversation
     */
    @Query("UPDATE chat_conversations SET message_count = :count WHERE conversation_id = :conversationId")
    suspend fun updateMessageCount(conversationId: Long, count: Int)
    
    /**
     * Supprime une conversation (les messages seront supprimés en cascade)
     */
    @Delete
    suspend fun deleteConversation(conversation: ChatConversationEntity)
    
    /**
     * Supprime une conversation par castingId
     */
    @Query("DELETE FROM chat_conversations WHERE casting_id = :castingId")
    suspend fun deleteConversationByCastingId(castingId: String)
    
    /**
     * Supprime toutes les conversations
     */
    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAllConversations()
    
    // ===== MESSAGES =====
    
    /**
     * Insère un nouveau message
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
    
    /**
     * Insère plusieurs messages en une seule transaction
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    /**
     * Récupère tous les messages d'une conversation
     */
    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesByConversationId(conversationId: Long): List<ChatMessageEntity>
    
    /**
     * Récupère tous les messages d'une conversation (Flow)
     */
    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversationIdFlow(conversationId: Long): Flow<List<ChatMessageEntity>>
    
    /**
     * Compte le nombre de messages d'une conversation
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversation_id = :conversationId")
    suspend fun getMessageCount(conversationId: Long): Int
    
    /**
     * Supprime un message
     */
    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)
    
    /**
     * Supprime tous les messages d'une conversation
     */
    @Query("DELETE FROM chat_messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: Long)
    
    /**
     * Supprime tous les messages
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
    
    // ===== OPÉRATIONS COMBINÉES =====
    
    /**
     * Ajoute un message et met à jour la conversation (transaction atomique)
     */
    @Transaction
    suspend fun addMessageAndUpdateConversation(message: ChatMessageEntity, conversationId: Long) {
        insertMessage(message)
        val count = getMessageCount(conversationId)
        updateMessageCount(conversationId, count)
        updateConversationTimestamp(conversationId, System.currentTimeMillis())
    }
}

