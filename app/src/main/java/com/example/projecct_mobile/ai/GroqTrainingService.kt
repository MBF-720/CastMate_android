package com.example.projecct_mobile.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.GroqApiService
import com.example.projecct_mobile.data.model.*
import com.example.projecct_mobile.data.model.groq.*
import com.example.projecct_mobile.utils.GroqConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream

/**
 * Service pour analyser les vidéos d'entraînement avec Groq AI (100% gratuit)
 * NOTE: Groq ne supporte pas directement les vidéos, donc on utilise une description textuelle
 * ou on envoie au backend qui gère l'analyse avec Groq
 */
class GroqTrainingService(private val context: Context) {
    
    companion object {
        private const val TAG = "GroqTrainingService"
        private const val MAX_VIDEO_SIZE_MB = 10
        private const val MAX_VIDEO_DURATION_SECONDS = 30
    }
    
    private val groqService: GroqApiService = ApiClient.getGroqService()
    private val GROQ_API_KEY = GroqConfig.GROQ_API_KEY
    
    /**
     * Analyser une vidéo d'entraînement d'acteur avec Groq
     * @param videoUri URI de la vidéo sélectionnée
     * @return Result contenant le feedback ou une erreur
     */
    suspend fun analyzeActingVideo(videoUri: Uri): Result<TrainingFeedback> {
        return analyzeActingVideo(videoUri, null, null, null)
    }
    
    /**
     * Analyser une vidéo d'entraînement d'acteur avec Groq (avec contexte de casting)
     * Pour l'analyse vidéo complète, la vidéo est envoyée au backend qui utilise Groq
     * Ici, on simule l'analyse avec Groq en utilisant une description textuelle
     * 
     * @param videoUri URI de la vidéo sélectionnée
     * @param roleDescription Description du rôle à jouer (optionnel)
     * @param synopsis Synopsis du projet/film (optionnel)
     * @param castingTitle Titre du casting (optionnel)
     * @return Result contenant le feedback ou une erreur
     */
    suspend fun analyzeActingVideo(
        videoUri: Uri,
        roleDescription: String? = null,
        synopsis: String? = null,
        castingTitle: String? = null
    ): Result<TrainingFeedback> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🎬 Début de l'analyse de la vidéo avec Groq: $videoUri")
                
                // Lire le fichier vidéo pour vérifier la taille
                val videoBytes = readVideoFromUri(videoUri)
                val videoSizeMB = videoBytes.size / (1024.0 * 1024.0)
                
                Log.d(TAG, "📦 Taille de la vidéo: $videoSizeMB MB")
                
                if (videoSizeMB > MAX_VIDEO_SIZE_MB) {
                    return@withContext Result.failure(
                        Exception("La vidéo est trop volumineuse (${String.format("%.1f", videoSizeMB)} MB). Maximum: $MAX_VIDEO_SIZE_MB MB")
                    )
                }
                
                // Construire le prompt avec le contexte du casting
                val prompt = buildAnalysisPrompt(roleDescription, synopsis, castingTitle, videoSizeMB)
                
                Log.d(TAG, "🔄 Envoi de la requête à Groq...")
                
                // Appeler l'API Groq avec le prompt
                val response = callGroqApi(prompt)
                
                Log.d(TAG, "✅ Réponse reçue de Groq")
                
                // Parser la réponse
                val feedback = parseGroqFeedback(response)
                
                Log.d(TAG, "🎯 Analyse terminée - Score global: ${feedback.globalScore}/100")
                
                Result.success(feedback)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'analyse: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Lire les bytes d'une vidéo depuis son URI
     */
    private fun readVideoFromUri(uri: Uri): ByteArray {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Impossible de lire la vidéo")
        
        return try {
            inputStream.use { stream ->
                val bytes = mutableListOf<Byte>()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    bytes.addAll(buffer.take(bytesRead))
                }
                
                bytes.toByteArray()
            }
        } catch (e: Exception) {
            throw Exception("Erreur lors de la lecture de la vidéo: ${e.message}")
        }
    }
    
    /**
     * Construire le prompt d'analyse pour Groq
     * NOTE: Groq ne peut pas analyser directement les vidéos, donc on crée un prompt descriptif
     */
    private fun buildAnalysisPrompt(
        roleDescription: String?,
        synopsis: String?,
        castingTitle: String?,
        videoSizeMB: Double
    ): String {
        val contextInfo = buildString {
            if (castingTitle != null) {
                append("TITRE DU CASTING: $castingTitle\n\n")
            }
            
            if (roleDescription != null) {
                append("DESCRIPTION DU RÔLE:\n$roleDescription\n\n")
            }
            
            if (synopsis != null) {
                append("SYNOPSIS DU PROJET:\n$synopsis\n\n")
            }
            
            append("INFORMATIONS SUR LA VIDÉO:\n")
            append("- Taille: ${String.format("%.2f", videoSizeMB)} MB\n")
            append("- Durée estimée: Max 30 secondes\n")
        }
        
        return """
        Tu es un expert en analyse de performance d'acteur pour CastMate, une plateforme de casting.

        $contextInfo

        Tu dois analyser une vidéo d'audition d'acteur et fournir un feedback détaillé.
        
        IMPORTANT: Analyse la performance de l'acteur dans cette vidéo en te concentrant sur:
        1. **ÉMOTIONS** (Cohérence et Intensité) - L'acteur exprime-t-il correctement les émotions requises?
        2. **POSTURE** - La posture et la présence physique sont-elles adaptées au rôle?
        3. **INTONATION** - L'intonation et la diction sont-elles claires et expressives?
        4. **EXPRESSIVITÉ** - Le jeu d'acteur est-il naturel et convaincant?
        
        Fournis une analyse complète avec:
        - Un score global sur 100
        - Des scores détaillés pour chaque critère (émotions, posture, intonation, expressivité)
        - Les émotions détectées dans la performance
        - Une liste de points forts
        - Une liste de recommandations pour améliorer
        - Un résumé global de la performance
        
        Réponds UNIQUEMENT au format JSON suivant (sans texte avant ou après):
        {
          "globalScore": 85,
          "emotions": {
            "detected": ["joie", "tristesse", "colère"],
            "coherence": 82,
            "intensity": 80,
            "comment": "Bonne variété émotionnelle"
          },
          "posture": {
            "score": 85,
            "strengths": ["Bonne présence scénique", "Posture confiante"],
            "improvements": ["Améliorer la gestuelle"],
            "comment": "Bonne présence scénique"
          },
          "intonation": {
            "score": 80,
            "clarity": 85,
            "rhythm": 75,
            "expressiveness": 80,
            "comment": "Diction claire"
          },
          "expressivite": {
            "score": 88,
            "facialExpressions": "Expressions naturelles et expressives",
            "bodyLanguage": "Langage corporel adapté au rôle",
            "comment": "Jeu naturel et convaincant"
          },
          "strengths": ["Bonne expressivité", "Émotions authentiques"],
          "recommendations": ["Travailler la projection de la voix", "Améliorer la gestuelle"],
          "summary": "Performance solide avec une bonne expressivité émotionnelle."
        }
        
        ANALYSE LA VIDÉO MAINTENANT:
        """.trimIndent()
    }
    
    /**
     * Appeler l'API Groq avec le prompt
     */
    private suspend fun callGroqApi(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Créer la requête Groq
                val groqRequest = GroqChatRequest(
                    model = GroqConfig.DEFAULT_MODEL,
                    messages = listOf(
                        GroqMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7,
                    maxTokens = 4096
                )
                
                // Appeler l'API
                val response = groqService.chatCompletions("Bearer $GROQ_API_KEY", groqRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val groqResponse = response.body()!!
                    val answerText = groqResponse.choices?.firstOrNull()?.message?.content
                        ?: throw Exception("Réponse Groq vide")
                    
                    Log.d(TAG, "📥 Réponse brute reçue (${answerText.length} caractères)")
                    return@withContext answerText
                } else {
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    Log.e(TAG, "Erreur API Groq ($errorCode): $errorMessage")
                    throw Exception("Erreur API Groq: $errorCode - $errorMessage")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception lors de l'appel Groq: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Parser la réponse JSON de Groq
     */
    private fun parseGroqFeedback(responseText: String): TrainingFeedback {
        return try {
            Log.d(TAG, "📝 Réponse brute de Groq (${responseText.length} caractères): ${responseText.take(500)}...")
            
            // Essayer d'extraire le JSON de la réponse
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart < 0 || jsonEnd <= jsonStart) {
                throw Exception("Format de réponse invalide: pas de JSON trouvé")
            }
            
            val jsonText = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonText)
            
            // Parser les émotions
            val emotionsJson = json.optJSONObject("emotions") ?: JSONObject()
            val emotions = EmotionAnalysis(
                detected = emotionsJson.optJSONArray("detected")?.let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                } ?: emptyList(),
                coherence = emotionsJson.optInt("coherence", 0),
                intensity = emotionsJson.optInt("intensity", 0),
                comment = emotionsJson.optString("comment", "N/A")
            )
            
            // Parser la posture
            val postureJson = json.optJSONObject("posture") ?: JSONObject()
            val posture = PostureAnalysis(
                score = postureJson.optInt("score", 0),
                strengths = postureJson.optJSONArray("strengths")?.let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                } ?: emptyList(),
                improvements = postureJson.optJSONArray("improvements")?.let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                } ?: emptyList(),
                comment = postureJson.optString("comment", "N/A")
            )
            
            // Parser l'intonation
            val intonationJson = json.optJSONObject("intonation") ?: JSONObject()
            val intonation = IntonationAnalysis(
                score = intonationJson.optInt("score", 0),
                clarity = intonationJson.optInt("clarity", intonationJson.optInt("score", 0)),
                rhythm = intonationJson.optInt("rhythm", intonationJson.optInt("score", 0)),
                expressiveness = intonationJson.optInt("expressiveness", intonationJson.optInt("score", 0)),
                comment = intonationJson.optString("comment", "N/A")
            )
            
            // Parser l'expressivité
            val expressiviteJson = json.optJSONObject("expressivite") ?: JSONObject()
            val expressivite = ExpressivityAnalysis(
                score = expressiviteJson.optInt("score", 0),
                facialExpressions = expressiviteJson.optString("facialExpressions", "N/A"),
                bodyLanguage = expressiviteJson.optString("bodyLanguage", "N/A"),
                comment = expressiviteJson.optString("comment", "N/A")
            )
            
            // Parser les recommandations et points forts
            val recommendations = json.optJSONArray("recommendations")?.let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            } ?: emptyList()
            
            val strengths = json.optJSONArray("strengths")?.let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            } ?: emptyList()
            
            // Parser le feedback
            TrainingFeedback(
                globalScore = json.getInt("globalScore"),
                emotions = emotions,
                posture = posture,
                intonation = intonation,
                expressivite = expressivite,
                strengths = strengths,
                recommendations = recommendations,
                summary = json.getString("summary")
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du parsing de la réponse Groq: ${e.message}", e)
            throw Exception("Erreur lors du traitement de la réponse Groq: ${e.message}")
        }
    }
}

