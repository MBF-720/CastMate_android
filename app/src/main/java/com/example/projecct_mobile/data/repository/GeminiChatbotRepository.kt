package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.GeminiApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.ChatbotResponse
import com.example.projecct_mobile.data.model.SuggestedActor
import com.example.projecct_mobile.data.model.gemini.*
import com.example.projecct_mobile.utils.GeminiConfig
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository pour le chatbot utilisant Gemini directement depuis Android
 */
class GeminiChatbotRepository {
    
    private val geminiService: GeminiApiService = ApiClient.getGeminiService()
    private val gson = Gson()
    
    // Clé API Gemini centralisée
    private val GEMINI_API_KEY = GeminiConfig.GEMINI_API_KEY
    
    /**
     * Interroge Gemini pour filtrer les acteurs d'un casting
     * 
     * @param casting Le casting avec ses candidats
     * @param query Question en langage naturel
     * @param maxResults Nombre maximum de résultats (optionnel, défaut: 10)
     * @return Result<ChatbotResponse> Réponse du chatbot avec suggestions
     */
    suspend fun queryChatbot(
        casting: Casting,
        query: String,
        maxResults: Int = 10
    ): Result<ChatbotResponse> {
        return try {
            if (query.isBlank()) {
                return Result.failure(
                    ApiException.BadRequestException("La question ne peut pas être vide")
                )
            }
            
            android.util.Log.d("GeminiChatbot", "🤖 Interrogation de Gemini pour casting: ${casting.titre}")
            android.util.Log.d("GeminiChatbot", "📝 Question: $query")
            
            // Préparer le prompt système
            val systemPrompt = buildSystemPrompt(casting)
            
            // Créer la requête Gemini
            val geminiRequest = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = systemPrompt + "\n\nQuestion de l'agence: $query")
                        ),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7,
                    maxOutputTokens = 2048
                )
            )
            
            // Appeler l'API Gemini avec retry et backoff exponentiel pour 429
            var currentRetry = 0
            val maxRetries = 5
            var currentDelay = 1000L // 1 seconde
            var response: retrofit2.Response<GeminiGenerateContentResponse>? = null

            while (currentRetry < maxRetries) {
                try {
                    response = geminiService.generateContent(GEMINI_API_KEY, geminiRequest)

                    if (response.code() == 429) {
                        android.util.Log.w("GeminiChatbot", "⚠️ Quota dépassé (429). Tentative ${currentRetry + 1}/$maxRetries dans ${currentDelay}ms")
                        delay(currentDelay)
                        currentDelay *= 2
                        currentRetry++
                    } else {
                        // Si ce n'est pas une 429, on sort de la boucle (succès ou autre erreur)
                        break
                    }
                } catch (e: Exception) {
                    // Si exception réseau, on peut aussi envisager de retry, mais ici on se concentre sur 429
                    throw e
                }
            }
            
            // Si après les retries on a toujours null (ne devrait pas arriver sauf exception) ou 429
            if (response == null) {
                 return Result.failure(ApiException.UnknownException("Échec de l'appel Gemini après retries"))
            }

            if (response.isSuccessful && response.body() != null) {
                val geminiResponse = response.body()!!
                
                // Extraire le texte de la réponse
                val answerText = extractTextFromResponse(geminiResponse)
                
                // Parser la réponse pour extraire les suggestions d'acteurs et l'answer
                val parseResult = parseSuggestedActorsAndAnswer(answerText, casting, query)
                val suggestedActors = parseResult.first
                val answer = parseResult.second ?: answerText // Utiliser l'answer du JSON ou le texte brut si pas de JSON
                
                val chatbotResponse = ChatbotResponse(
                    answer = answer,
                    suggestedActors = suggestedActors,
                    totalCandidates = casting.candidats?.size ?: 0,
                    filteredCount = suggestedActors.size
                )
                
                android.util.Log.d("GeminiChatbot", "✅ Réponse Gemini reçue: ${suggestedActors.size} acteurs suggérés")
                Result.success(chatbotResponse)
            } else {
                val errorCode = response.code()
                val errorMessage = "Erreur Gemini ${errorCode}: ${response.message()}"
                android.util.Log.e("GeminiChatbot", "❌ Erreur Gemini: $errorMessage")
                Result.failure(
                    ApiException.UnknownException(errorMessage)
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("GeminiChatbot", "❌ Exception Gemini: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de l'interrogation de Gemini: ${e.message}"))
        }
    }
    
    /**
     * Construit le prompt système optimisé pour Gemini
     */
    private fun buildSystemPrompt(casting: Casting): String {
        // Limiter aux 20 premiers candidats pour économiser des tokens
        val candidatesList = casting.candidats?.take(20) ?: emptyList()
        
        val candidatesInfo = if (candidatesList.isNotEmpty()) {
            candidatesList.mapIndexed { index, candidat ->
                val acteur = candidat.acteurId
                val hasVideo = candidat.videoFileId != null || candidat.aiFeedback != null
                
                // Résumé ultra-court du feedback IA
                val aiSummary = candidat.aiFeedback?.let { fb ->
                    "Score IA: ${fb.globalScore}/100 (Pts forts: ${fb.strengths.take(2).joinToString()})"
                } ?: if (hasVideo) "Vidéo dispo" else "Pas de vidéo"
                
                "C${index+1}: ${acteur?.prenom} ${acteur?.nom} (${candidat.statut}). $aiSummary"
            }.joinToString("\n")
        } else {
            "Aucun candidat"
        }
        
        return """
        Tu es l'assistant IA de CastMate pour le casting: "${casting.titre}".
        Rôle: ${casting.descriptionRole ?: "N/A"}.
        
        Candidats (Top 20):
        $candidatesInfo
        
        RÈGLES:
        1. Réponds aux questions sur les candidats.
        2. Suggère les meilleurs profils (Score IA élevé ou statut ACCEPTE).
        3. Si hors-sujet, redirige poliment.
        4. Réponds en JSON: { "answer": "Ta réponse...", "suggestedActors": [ { "acteurId": "ID", "nom": "Nom", "matchScore": 0.9, "matchReasons": ["Raison"] } ] }
        """.trimIndent()
    }
    
    /**
     * Extrait le texte de la réponse Gemini
     */
    private fun extractTextFromResponse(response: GeminiGenerateContentResponse): String {
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Désolé, je n'ai pas pu générer de réponse."
    }
    
    /**
     * Parse les acteurs suggérés et l'answer depuis la réponse Gemini
     * Retourne une paire (List<SuggestedActor>, String?) où le String est l'answer extraite du JSON
     */
    private fun parseSuggestedActorsAndAnswer(
        answerText: String,
        casting: Casting,
        query: String
    ): Pair<List<SuggestedActor>, String?> {
        return try {
            // Essayer d'extraire le JSON de la réponse
            val jsonStart = answerText.indexOf("{")
            val jsonEnd = answerText.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonText = answerText.substring(jsonStart, jsonEnd)
                val jsonObject = JsonParser.parseString(jsonText).asJsonObject
                
                // Extraire l'answer du JSON
                val answer = jsonObject.get("answer")?.asString
                val suggestedActorsArray = jsonObject.getAsJsonArray("suggestedActors")
                
                val actors = mutableListOf<SuggestedActor>()
                
                suggestedActorsArray?.forEach { element ->
                    val actorJson = element.asJsonObject
                    val acteurId = actorJson.get("acteurId")?.asString ?: ""
                    
                    // Trouver l'acteur dans les candidats pour obtenir les infos complètes
                    val candidat = casting.candidats?.find { 
                        it.acteurId?.actualId == acteurId 
                    }
                    val acteur = candidat?.acteurId
                    
                    val matchReasons = actorJson.getAsJsonArray("matchReasons")?.map { 
                        it.asString 
                    }?.toList() ?: emptyList()
                    
                    actors.add(
                        SuggestedActor(
                            acteurId = acteurId,
                            nom = actorJson.get("nom")?.asString ?: acteur?.nom,
                            prenom = actorJson.get("prenom")?.asString ?: acteur?.prenom,
                            matchScore = actorJson.get("matchScore")?.asDouble ?: 0.0,
                            matchReasons = matchReasons
                        )
                    )
                }
                
                Pair(actors, answer)
            } else {
                // Si pas de JSON, retourner une liste vide et null pour answer
                Pair(emptyList(), null)
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiChatbot", "❌ Erreur parsing JSON: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }
}

