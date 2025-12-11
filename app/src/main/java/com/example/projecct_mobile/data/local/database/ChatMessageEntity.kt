package com.example.projecct_mobile.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité Room pour stocker un message de conversation chatbot
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatConversationEntity::class,
            parentColumns = ["conversation_id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversation_id"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    val messageId: Long = 0,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    
    @ColumnInfo(name = "text")
    val text: String,
    
    @ColumnInfo(name = "is_bot")
    val isBot: Boolean,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    // Données optionnelles pour les suggestions d'acteurs (si message bot avec suggestions)
    @ColumnInfo(name = "suggested_actors_json")
    val suggestedActorsJson: String? = null
)

