package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.groq.GroqChatRequest
import com.example.projecct_mobile.data.model.groq.GroqChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Service API pour Groq
 * Documentation: https://console.groq.com/docs
 */
interface GroqApiService {
    
    /**
     * Génère du contenu avec Groq (chat completions)
     * 
     * @param authorization Clé API Groq (format: "Bearer {key}")
     * @param request Requête de chat
     * @return Réponse avec le contenu généré
     */
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>
}

