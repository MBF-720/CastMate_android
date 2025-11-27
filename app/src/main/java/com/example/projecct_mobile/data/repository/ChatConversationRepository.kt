package com.example.projecct_mobile.data.repository

import android.content.Context
import com.example.projecct_mobile.data.local.database.CastMateDatabase
import com.example.projecct_mobile.data.local.database.ChatConversationEntity
import com.example.projecct_mobile.data.local.database.ChatMessageEntity
import com.example.projecct_mobile.data.local.database.ConversationWithMessages
import com.example.projecct_mobile.data.model.ChatMessage
import com.example.projecct_mobile.data.model.SuggestedActor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer la persistance des conversations du chatbot
 */
class ChatConversationRepository(context: Context) {
    
    private val chatDao = CastMateDatabase.getInstance(context).chatDao()
    private val gson = Gson()
    
    /**
     * Charge ou crée une conversation pour un casting donné
     */
    suspend fun getOrCreateConversation(
        castingId: String,
        castingTitle: String
    ): ChatConversationEntity {
        return withContext(Dispatchers.IO) {
            var conversation = chatDao.getConversationByCastingId(castingId)
            
            if (conversation == null) {
                val newConversation = ChatConversationEntity(
                    castingId = castingId,
                    castingTitle = castingTitle,
                    lastUpdated = System.currentTimeMillis(),
                    messageCount = 0
                )
                val id = chatDao.insertConversation(newConversation)
                conversation = newConversation.copy(conversationId = id)
                
                android.util.Log.d("ChatConversationRepo", "✅ Nouvelle conversation créée: casting=$castingTitle, id=$id")
            } else {
                android.util.Log.d("ChatConversationRepo", "📂 Conversation existante chargée: casting=$castingTitle, id=${conversation.conversationId}")
            }
            
            conversation
        }
    }
    
    /**
     * Sauvegarde un message dans la conversation
     */
    suspend fun saveMessage(
        conversationId: Long,
        text: String,
        isBot: Boolean,
        suggestedActors: List<SuggestedActor>? = null
    ): ChatMessageEntity {
        return withContext(Dispatchers.IO) {
            val suggestedActorsJson = if (!suggestedActors.isNullOrEmpty()) {
                gson.toJson(suggestedActors)
            } else null
            
            val message = ChatMessageEntity(
                conversationId = conversationId,
                text = text,
                isBot = isBot,
                timestamp = System.currentTimeMillis(),
                suggestedActorsJson = suggestedActorsJson
            )
            
            chatDao.addMessageAndUpdateConversation(message, conversationId)
            
            android.util.Log.d("ChatConversationRepo", "💬 Message sauvegardé: conversation=$conversationId, isBot=$isBot, length=${text.length}")
            
            message
        }
    }
    
    /**
     * Charge tous les messages d'une conversation
     */
    suspend fun getMessages(castingId: String): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            val conversation = chatDao.getConversationByCastingId(castingId)
            
            if (conversation == null) {
                android.util.Log.d("ChatConversationRepo", "⚠️ Aucune conversation trouvée pour: $castingId")
                return@withContext emptyList()
            }
            
            val messages = chatDao.getMessagesByConversationId(conversation.conversationId)
            
            android.util.Log.d("ChatConversationRepo", "📥 Messages chargés: ${messages.size} messages pour casting=$castingId")
            
            // Convertir les entités Room en modèles UI
            messages.map { entity ->
                ChatMessage(
                    text = entity.text,
                    isBot = entity.isBot,
                    timestamp = entity.timestamp,
                    suggestedActors = parseSuggestedActors(entity.suggestedActorsJson)
                )
            }
        }
    }
    
    /**
     * Charge une conversation complète avec tous ses messages
     */
    suspend fun getConversationWithMessages(castingId: String): ConversationWithMessages? {
        return withContext(Dispatchers.IO) {
            chatDao.getConversationWithMessages(castingId)
        }
    }
    
    /**
     * Observe les changements de toutes les conversations (Flow)
     */
    fun getAllConversationsFlow(): Flow<List<ConversationWithMessages>> {
        return chatDao.getAllConversationsFlow()
    }
    
    /**
     * Supprime une conversation et tous ses messages
     */
    suspend fun deleteConversation(castingId: String) {
        return withContext(Dispatchers.IO) {
            chatDao.deleteConversationByCastingId(castingId)
            android.util.Log.d("ChatConversationRepo", "🗑️ Conversation supprimée: $castingId")
        }
    }
    
    /**
     * Supprime toutes les conversations
     */
    suspend fun deleteAllConversations() {
        return withContext(Dispatchers.IO) {
            chatDao.deleteAllConversations()
            android.util.Log.d("ChatConversationRepo", "🗑️ Toutes les conversations supprimées")
        }
    }
    
    /**
     * Compte le nombre de messages d'une conversation
     */
    suspend fun getMessageCount(castingId: String): Int {
        return withContext(Dispatchers.IO) {
            val conversation = chatDao.getConversationByCastingId(castingId)
            conversation?.messageCount ?: 0
        }
    }
    
    /**
     * Vérifie si une conversation existe pour un casting
     */
    suspend fun hasConversation(castingId: String): Boolean {
        return withContext(Dispatchers.IO) {
            chatDao.getConversationByCastingId(castingId) != null
        }
    }
    
    /**
     * Parse le JSON des acteurs suggérés
     */
    private fun parseSuggestedActors(json: String?): List<SuggestedActor>? {
        if (json.isNullOrBlank()) return null
        
        return try {
            val type = object : TypeToken<List<SuggestedActor>>() {}.type
            gson.fromJson<List<SuggestedActor>>(json, type)
        } catch (e: Exception) {
            android.util.Log.e("ChatConversationRepo", "Erreur parsing suggestedActors: ${e.message}")
            null
        }
    }
    
    /**
     * Obtient des statistiques sur les conversations
     */
    suspend fun getStatistics(): ConversationStatistics {
        return withContext(Dispatchers.IO) {
            val conversations = chatDao.getAllConversations()
            val totalMessages = conversations.sumOf { it.messageCount }
            
            ConversationStatistics(
                totalConversations = conversations.size,
                totalMessages = totalMessages,
                averageMessagesPerConversation = if (conversations.isNotEmpty()) {
                    totalMessages.toFloat() / conversations.size
                } else 0f
            )
        }
    }
    
    data class ConversationStatistics(
        val totalConversations: Int,
        val totalMessages: Int,
        val averageMessagesPerConversation: Float
    )
}

