package com.example.projecct_mobile.data.local.database

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation Room pour charger une conversation avec tous ses messages
 */
data class ConversationWithMessages(
    @Embedded
    val conversation: ChatConversationEntity,
    
    @Relation(
        parentColumn = "conversation_id",
        entityColumn = "conversation_id"
    )
    val messages: List<ChatMessageEntity>
)

