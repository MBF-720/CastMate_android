package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.gemini.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Service API pour Google Gemini
 * Documentation: https://ai.google.dev/api/generate-content
 */
interface GeminiApiService {
    
    /**
     * Génère du contenu avec Gemini
     * 
     * @param key Clé API Gemini
     * @param request Requête de génération de contenu
     * @return Réponse avec le contenu généré
     */
    @POST("v1/models/gemini-2.5-pro:generateContent")
    suspend fun generateContent(
        @Query("key") key: String,
        @Body request: GeminiGenerateContentRequest
    ): Response<GeminiGenerateContentResponse>
}

