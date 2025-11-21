package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Requête pour le chatbot de filtrage d'acteurs
 */
data class ChatbotQueryRequest(
    @SerializedName("query")
    val query: String,
    
    @SerializedName("context")
    val context: ChatbotContext? = null
)

/**
 * Contexte optionnel pour le chatbot
 */
data class ChatbotContext(
    @SerializedName("preferLocalization")
    val preferLocalization: Boolean = false,
    
    @SerializedName("maxResults")
    val maxResults: Int = 10
)

/**
 * Réponse du chatbot
 */
data class ChatbotResponse(
    @SerializedName("answer")
    val answer: String,
    
    @SerializedName("suggestedActors")
    val suggestedActors: List<SuggestedActor>,
    
    @SerializedName("totalCandidates")
    val totalCandidates: Int,
    
    @SerializedName("filteredCount")
    val filteredCount: Int
)

/**
 * Acteur suggéré par le chatbot
 */
data class SuggestedActor(
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("experience")
    val experience: Int? = null,
    
    @SerializedName("gouvernorat")
    val gouvernorat: String? = null,
    
    @SerializedName("matchScore")
    val matchScore: Double = 0.0,
    
    @SerializedName("matchReasons")
    val matchReasons: List<String> = emptyList()
)

