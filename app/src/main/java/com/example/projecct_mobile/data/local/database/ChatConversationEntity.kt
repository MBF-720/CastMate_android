package com.example.projecct_mobile.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour stocker une conversation chatbot (associée à un casting)
 */
@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long = 0,
    
    @ColumnInfo(name = "casting_id")
    val castingId: String,
    
    @ColumnInfo(name = "casting_title")
    val castingTitle: String,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "message_count")
    val messageCount: Int = 0
)

