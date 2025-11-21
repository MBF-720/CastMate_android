package com.example.projecct_mobile.data.model.gemini

import com.google.gson.annotations.SerializedName

/**
 * Requête pour générer du contenu avec Gemini
 */
data class GeminiGenerateContentRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>,
    
    @SerializedName("generationConfig")
    val generationConfig: GeminiGenerationConfig? = null
)

/**
 * Contenu d'une requête Gemini
 */
data class GeminiContent(
    @SerializedName("parts")
    val parts: List<GeminiPart>,
    
    @SerializedName("role")
    val role: String? = null // "user" ou "model"
)

/**
 * Partie d'un contenu (texte)
 */
data class GeminiPart(
    @SerializedName("text")
    val text: String
)

/**
 * Configuration de génération
 */
data class GeminiGenerationConfig(
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    
    @SerializedName("topK")
    val topK: Int = 40,
    
    @SerializedName("topP")
    val topP: Double = 0.95,
    
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 2048
)

/**
 * Réponse de génération de contenu Gemini
 */
data class GeminiGenerateContentResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>? = null,
    
    @SerializedName("promptFeedback")
    val promptFeedback: GeminiPromptFeedback? = null
)

/**
 * Candidat de réponse
 */
data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContent? = null,
    
    @SerializedName("finishReason")
    val finishReason: String? = null,
    
    @SerializedName("safetyRatings")
    val safetyRatings: List<GeminiSafetyRating>? = null
)

/**
 * Feedback du prompt
 */
data class GeminiPromptFeedback(
    @SerializedName("blockReason")
    val blockReason: String? = null,
    
    @SerializedName("safetyRatings")
    val safetyRatings: List<GeminiSafetyRating>? = null
)

/**
 * Évaluation de sécurité
 */
data class GeminiSafetyRating(
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("probability")
    val probability: String? = null
)

