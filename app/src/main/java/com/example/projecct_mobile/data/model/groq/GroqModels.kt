package com.example.projecct_mobile.data.model.groq

import com.google.gson.annotations.SerializedName

/**
 * Requête pour générer du contenu avec Groq
 */
data class GroqChatRequest(
    @SerializedName("model")
    val model: String,
    
    @SerializedName("messages")
    val messages: List<GroqMessage>,
    
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    
    @SerializedName("top_p")
    val topP: Double = 1.0,
    
    @SerializedName("stream")
    val stream: Boolean = false
)

/**
 * Message dans une conversation Groq
 */
data class GroqMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("content")
    val content: String
)

/**
 * Réponse de chat Groq
 */
data class GroqChatResponse(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("object")
    val objectType: String? = null,
    
    @SerializedName("created")
    val created: Long? = null,
    
    @SerializedName("model")
    val model: String? = null,
    
    @SerializedName("choices")
    val choices: List<GroqChoice>? = null,
    
    @SerializedName("usage")
    val usage: GroqUsage? = null
)

/**
 * Choix de réponse Groq
 */
data class GroqChoice(
    @SerializedName("index")
    val index: Int? = null,
    
    @SerializedName("message")
    val message: GroqMessage? = null,
    
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

/**
 * Utilisation des tokens
 */
data class GroqUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)

