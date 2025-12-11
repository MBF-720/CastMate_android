package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.GroqApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.ChatbotResponse
import com.example.projecct_mobile.data.model.SuggestedActor
import com.example.projecct_mobile.data.model.groq.*
import com.example.projecct_mobile.utils.GroqConfig
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Repository pour le chatbot utilisant Groq (100% gratuit)
 */
class GroqChatbotRepository {
    
    private val groqService: GroqApiService = ApiClient.getGroqService()
    private val gson = Gson()
    
    // Clé API Groq centralisée
    private val GROQ_API_KEY = GroqConfig.GROQ_API_KEY
    
    /**
     * Interroge Groq pour filtrer les acteurs d'un casting
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
            
            android.util.Log.d("GroqChatbot", "🤖 Interrogation de Groq pour casting: ${casting.titre}")
            android.util.Log.d("GroqChatbot", "📝 Question: $query")
            
            // Préparer le prompt système
            val systemPrompt = buildSystemPrompt(casting)
            android.util.Log.d("GroqChatbot", "📝 Longueur du prompt système: ${systemPrompt.length} caractères")
            
            // Créer la requête Groq (format chat completions)
            val groqRequest = GroqChatRequest(
                model = GroqConfig.DEFAULT_MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = query)
                ),
                temperature = 0.7,
                maxTokens = 2048
            )
            
            android.util.Log.d("GroqChatbot", "🔧 Modèle utilisé: ${GroqConfig.DEFAULT_MODEL}")
            android.util.Log.d("GroqChatbot", "🔧 Nombre de messages: ${groqRequest.messages.size}")
            
            // Appeler l'API Groq
            val response = groqService.chatCompletions("Bearer $GROQ_API_KEY", groqRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val groqResponse = response.body()!!
                
                // Extraire le texte de la réponse
                val answerText = extractTextFromResponse(groqResponse)
                
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
                
                android.util.Log.d("GroqChatbot", "✅ Réponse Groq reçue: ${suggestedActors.size} acteurs suggérés")
                Result.success(chatbotResponse)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("GroqChatbot", "❌ Erreur Groq ${errorCode}: ${response.message()}")
                android.util.Log.e("GroqChatbot", "❌ Error body: $errorBody")
                
                val errorMessage = when (errorCode) {
                    400 -> {
                        val errorDetails = errorBody?.let {
                            try {
                                val errorJson = org.json.JSONObject(it)
                                errorJson.optJSONObject("error")?.optString("message") 
                                    ?: errorJson.optString("message") 
                                    ?: it.take(300)
                            } catch (e: Exception) {
                                it.take(300)
                            }
                        } ?: "Erreur de requête"
                        "Erreur de requête Groq (400): $errorDetails"
                    }
                    401 -> {
                        "Clé API Groq invalide. Veuillez vérifier votre clé API."
                    }
                    429 -> {
                        "Trop de requêtes. Veuillez réessayer dans quelques instants."
                    }
                    else -> {
                        val errorDetails = errorBody?.take(300) ?: response.message()
                        "Erreur Groq ${errorCode}: $errorDetails"
                    }
                }
                
                Result.failure(
                    ApiException.UnknownException(errorMessage)
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("GroqChatbot", "❌ Exception Groq: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de l'interrogation de Groq: ${e.message}"))
        }
    }
    
    /**
     * Construit le prompt système pour Groq
     */
    private fun buildSystemPrompt(casting: Casting): String {
        val candidatesInfo = if (casting.candidats != null) {
            casting.candidats.mapIndexed { index, candidat ->
                val acteur = candidat.acteurId
                val hasVideo = candidat.videoFileId != null || candidat.aiFeedback != null
                val aiScore = candidat.aiFeedback?.globalScore
                
                // Résumé du feedback IA si disponible
                val aiFeedbackSummary = if (candidat.aiFeedback != null) {
                    val feedback = candidat.aiFeedback
                    """
                    - ✅ VIDÉO D'AUDITION DISPONIBLE
                    - 🎯 SCORE IA GLOBAL: ${feedback.globalScore}/100
                    - Émotions: ${feedback.emotions.detected.joinToString(", ")} (Cohérence: ${feedback.emotions.coherence}/100, Intensité: ${feedback.emotions.intensity}/100)
                    - Posture: ${feedback.posture.score}/100
                    - Intonation: ${feedback.intonation.score}/100
                    - Expressivité: ${feedback.expressivite.score}/100
                    - Points forts: ${feedback.strengths.joinToString(", ")}
                    - Recommandations IA: ${feedback.recommendations.joinToString(", ")}
                    - Résumé IA: ${feedback.summary}
                    """.trimIndent()
                } else if (hasVideo) {
                    "- ⏳ VIDÉO D'AUDITION EN COURS DE TRAITEMENT"
                } else {
                    "- ⚠️ Aucune vidéo d'audition soumise"
                }
                
                """
                Candidat ${index + 1}:
                - ID: ${acteur?.actualId ?: "N/A"}
                - Nom: ${acteur?.prenom ?: ""} ${acteur?.nom ?: ""}
                - Email: ${acteur?.email ?: "N/A"}
                - Statut: ${candidat.statut ?: "N/A"}
                - Date candidature: ${candidat.dateCandidature ?: "N/A"}
                $aiFeedbackSummary
                """.trimIndent()
            }.joinToString("\n\n")
        } else {
            "Aucun candidat"
        }
        
        return """
        Tu es un assistant IA spécialisé de CastMate, une plateforme de casting en ligne qui connecte les agences avec les acteurs talentueux.
        
        ⚠️ IMPORTANT : Tu es l'assistant IA intégré à l'application CastMate. Tu n'es PAS ChatGPT ni un autre assistant générique. 
        Tu es spécialisé pour aider les agences de casting à trouver les meilleurs acteurs parmi leurs candidats.
        
        CONTEXTE DU CASTING "${casting.titre ?: "N/A"}":
        - Description du rôle: ${casting.descriptionRole ?: "N/A"}
        - Synopsis: ${casting.synopsis ?: "N/A"}
        - Types: ${if (casting.types != null) casting.types.joinToString(", ") else "N/A"}
        - Âge requis: ${casting.age ?: "N/A"}
        - Lieu: ${casting.lieu ?: "N/A"}
        - Conditions: ${casting.conditions ?: "N/A"}
        - Prix: ${casting.prix ?: "N/A"} DT
        
        CANDIDATS DISPONIBLES (${casting.candidats?.size ?: 0} candidats):
        $candidatesInfo
        
        📊 INFORMATIONS IMPORTANTES SUR LES SCORES IA:
        - Chaque candidat peut soumettre une vidéo d'audition (max 30 secondes)
        - Notre IA analyse automatiquement la vidéo et fournit un score sur 100
        - Le score IA évalue: émotions, posture, intonation, expressivité
        - Un score IA élevé (70+) indique une bonne performance pour le rôle
        - Les candidats avec vidéo d'audition ont un avantage car l'agence peut mieux évaluer leurs talents
        
        INSTRUCTIONS:
        
        1. SALUTATIONS (bonjour, bnj, salut, etc.):
           - Réponds de manière amicale et professionnelle
           - Présente-toi brièvement comme l'assistant IA de CastMate
           - Fais un résumé du casting (titre, nombre de candidats, statuts)
           - Mentionne le nombre de candidats avec vidéo d'audition et leur score IA moyen
           - Suggère les meilleurs candidats (priorité aux statuts "ACCEPTE" ou scores IA élevés)
           - Exemple: "Bonjour ! Je suis votre assistant IA CastMate pour le casting '[TITRE]'. 
             J'ai analysé [X] candidat(s), dont [Y] avec vidéo d'audition. Voici les meilleurs candidats :"
        
        2. QUESTIONS HORS APPLICATION (météo, actualités, etc.):
           - Réponds poliment mais rappelle que tu es spécialisé pour CastMate
           - Redirige la conversation vers le casting
           - Exemple: "Je suis désolé, mais je suis l'assistant IA de CastMate spécialisé pour 
             vous aider à trouver les meilleurs acteurs pour vos castings. Je ne peux pas répondre 
             aux questions générales. Comment puis-je vous aider avec le casting '[TITRE]' ?"
        
        3. QUESTIONS SUR LE CASTING (filtrage d'acteurs):
           - Analyse les critères demandés (âge, expérience, localisation, statut, SCORE IA, etc.)
           - Filtre les candidats selon ces critères
           - **UTILISE LE SCORE IA** pour classer les candidats (priorité aux scores élevés)
           - **MENTIONNE TOUJOURS** le score IA quand disponible
           - Suggère les acteurs correspondants avec leurs scores IA
           - Explique pourquoi chaque acteur est suggéré (inclure analyse IA)
           - Si un candidat a un bon score IA, mentionne ses points forts de l'analyse
        
        4. QUESTIONS SPÉCIFIQUES SUR LES SCORES IA:
           - Si on te demande "qui a le meilleur score IA?", classe par score IA décroissant
           - Si on te demande "qui a soumis une vidéo?", liste ceux avec score IA
           - Si on te demande des détails sur le score, mentionne les sous-scores (émotions, posture, etc.)
           - Utilise les recommandations IA pour donner des conseils
        
        5. RÈGLES DE FILTRAGE:
           - Ne suggère JAMAIS les acteurs avec statut "REFUSE"
           - Priorité aux acteurs avec statut "ACCEPTE" ET score IA élevé (70+)
           - Si critères spécifiques, suggère tous les acteurs correspondants (EN_ATTENTE ou ACCEPTE)
           - Privilégie les candidats avec vidéo d'audition (plus d'informations disponibles)
           - Si aucun acteur ne correspond, explique pourquoi
        
        6. FORMAT DES RÉPONSES AVEC SCORE IA:
           - Exemple: "🎯 Score IA: 85/100 - Excellente performance"
           - Exemple: "✅ Points forts: Bonne posture (82/100), Expressions naturelles"
           - Exemple: "📊 Analyse IA: L'acteur montre une grande expressivité..."
        
        RÉPONSE ATTENDUE (format JSON strict, sans texte avant ou après):
        {
          "answer": "Réponse naturelle, contextuelle et professionnelle en français qui répond directement à la question. INCLUS les scores IA quand disponibles.",
          "suggestedActors": [
            {
              "acteurId": "ID exact de l'acteur",
              "nom": "Nom de famille",
              "prenom": "Prénom",
              "matchScore": 0.95,
              "matchReasons": ["Raison précise 1", "Raison précise 2"]
            }
          ]
        }
        
        EXEMPLES:
        - Question: "bnj" → Réponds: "Bonjour ! Je suis votre assistant IA CastMate pour le casting '[TITRE]'. 
          J'ai trouvé [X] candidat(s). Voici les meilleurs candidats :" + suggère les acteurs acceptés/en attente
        
        - Question: "Quelle est la météo ?" → Réponds: "Je suis désolé, mais je suis l'assistant IA de CastMate 
          spécialisé pour vous aider à trouver les meilleurs acteurs. Comment puis-je vous aider avec le casting '[TITRE]' ?"
        
        - Question: "Trouve-moi les acteurs de 25-35 ans" → Réponds: "J'ai trouvé [X] acteur(s) correspondant à 
          votre critère d'âge..." + liste les acteurs avec scores
        
        IMPORTANT:
        - Sois naturel, professionnel mais accessible
        - Réponds TOUJOURS à la question, même pour une salutation
        - Pour salutations/questions générales: suggère les acteurs acceptés/en attente
        - Pour questions hors sujet: rappelle que tu es l'assistant CastMate
        - Retourne UNIQUEMENT du JSON valide, rien d'autre
        """.trimIndent()
    }
    
    /**
     * Extrait le texte de la réponse Groq
     */
    private fun extractTextFromResponse(response: GroqChatResponse): String {
        return response.choices?.firstOrNull()?.message?.content
            ?: "Désolé, je n'ai pas pu générer de réponse."
    }
    
    /**
     * Parse les acteurs suggérés et l'answer depuis la réponse Groq
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
            android.util.Log.e("GroqChatbot", "❌ Erreur parsing JSON: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }
}

