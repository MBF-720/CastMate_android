package com.example.projecct_mobile.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.projecct_mobile.data.model.*
import com.example.projecct_mobile.utils.GeminiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream

/**
 * Service pour analyser les vidéos d'entraînement avec Gemini AI
 */
class GeminiTrainingService(private val context: Context) {
    
    companion object {
        private const val TAG = "GeminiTrainingService"
        private const val MAX_VIDEO_SIZE_MB = 10 // Taille max en MB (limite stricte du backend)
        private const val MAX_VIDEO_DURATION_SECONDS = 30
    }
    
    /**
     * Analyser une vidéo d'entraînement d'acteur avec Gemini
     * @param videoUri URI de la vidéo sélectionnée
     * @return Result contenant le feedback ou une erreur
     */
    suspend fun analyzeActingVideo(videoUri: Uri): Result<TrainingFeedback> {
        return analyzeActingVideo(videoUri, null, null, null)
    }
    
    /**
     * Analyser une vidéo d'entraînement d'acteur avec Gemini (avec contexte de casting)
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
                Log.d(TAG, "🎬 Début de l'analyse de la vidéo: $videoUri")
                
                // Lire le fichier vidéo
                val videoBytes = readVideoFromUri(videoUri)
                val videoSizeMB = videoBytes.size / (1024 * 1024)
                
                Log.d(TAG, "📦 Taille de la vidéo: $videoSizeMB MB")
                
                if (videoSizeMB > MAX_VIDEO_SIZE_MB) {
                    return@withContext Result.failure(
                        Exception("La vidéo est trop volumineuse ($videoSizeMB MB). Maximum: $MAX_VIDEO_SIZE_MB MB")
                    )
                }
                
                // Construire le prompt avec le contexte du casting si disponible
                val prompt = buildAnalysisPrompt(roleDescription, synopsis, castingTitle)
                
                // Encoder en base64
                val videoBase64 = android.util.Base64.encodeToString(
                    videoBytes,
                    android.util.Base64.NO_WRAP
                )
                
                Log.d(TAG, "🔄 Envoi de la vidéo à Gemini...")
                
                // Appeler l'API Gemini
                val response = callGeminiApi(videoBase64, prompt)
                
                Log.d(TAG, "✅ Réponse reçue de Gemini")
                
                // Parser la réponse
                val feedback = parseGeminiFeedback(response)
                
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
        
        return inputStream.use { it.readBytes() }
    }
    
    /**
     * Construire le prompt d'analyse pour Gemini
     * @param roleDescription Description du rôle à jouer (optionnel)
     * @param synopsis Synopsis du projet/film (optionnel)
     * @param castingTitle Titre du casting (optionnel)
     */
    private fun buildAnalysisPrompt(
        roleDescription: String? = null,
        synopsis: String? = null,
        castingTitle: String? = null
    ): String {
        val contextSection = buildString {
            if (castingTitle != null || roleDescription != null || synopsis != null) {
                append("\nCONTEXTE DU CASTING:\n")
                if (castingTitle != null) {
                    append("- Titre du casting: $castingTitle\n")
                }
                if (roleDescription != null && roleDescription.isNotBlank()) {
                    append("- Description du rôle à jouer: $roleDescription\n")
                }
                if (synopsis != null && synopsis.isNotBlank()) {
                    append("- Synopsis du projet: $synopsis\n")
                }
                append("\nIMPORTANT: Évalue la performance de l'acteur en tenant compte de ce contexte. ")
                append("Analyse si l'interprétation correspond aux attentes du rôle décrit. ")
                append("Donne des conseils spécifiques pour mieux incarner ce personnage.\n")
            }
        }
        
        return """
Tu es un coach professionnel en acting et en jeu d'acteur. Analyse cette vidéo d'entraînement d'un acteur (durée max 30 secondes).
$contextSection
INSTRUCTIONS:
1. Analyse les aspects suivants:
   - **Émotions** : Quelles émotions sont exprimées ? Sont-elles cohérentes et intenses ? ${if (roleDescription != null) "Correspondent-elles au personnage décrit ?" else ""}
   - **Posture** : La posture corporelle est-elle appropriée ? Points forts et à améliorer ? ${if (roleDescription != null) "Est-elle adaptée au personnage ?" else ""}
   - **Intonation** : La voix est-elle claire, rythmée et expressive ? ${if (roleDescription != null) "Le ton correspond-il au personnage ?" else ""}
   - **Expressivité** : Les expressions faciales et le langage corporel sont-ils convaincants ? ${if (roleDescription != null) "Sont-ils adaptés au rôle ?" else ""}

2. Pour chaque aspect, donne :
   - Un score de 0 à 100
   - Un commentaire constructif et bienveillant
   - Des conseils d'amélioration spécifiques et actionnables ${if (roleDescription != null) "pour mieux incarner ce personnage" else ""}

3. Fournis également :
   - Un score global (moyenne pondérée des 4 aspects)
   - Une liste de 3-5 points forts à conserver
   - Une liste de 3-5 recommandations prioritaires ${if (roleDescription != null) "pour améliorer l'interprétation du rôle" else ""}
   - Un résumé en 2-3 phrases

IMPORTANT:
- Sois bienveillant mais honnête
- Donne des conseils concrets et actionnables
- Utilise un langage professionnel mais accessible
- Réponds en français
- Les commentaires doivent être CONCIS (max 2 phrases chacun)
- Réponds UNIQUEMENT au format JSON suivant (AUCUN texte avant ou après):
- Le JSON doit être VALIDE et COMPLET (ferme toutes les accolades)
- RESPECTE EXACTEMENT cette structure (tous les champs sont OBLIGATOIRES):

{
  "globalScore": 75,
  "emotions": {
    "detected": ["joie", "surprise"],
    "coherence": 80,
    "intensity": 70,
    "comment": "Les émotions sont bien exprimées mais pourraient être plus intenses."
  },
  "posture": {
    "score": 75,
    "strengths": ["Bonne présence scénique", "Dos droit"],
    "improvements": ["Utiliser plus les mains", "Varier les positions"],
    "comment": "La posture est correcte mais manque de dynamisme."
  },
  "intonation": {
    "score": 70,
    "clarity": 85,
    "rhythm": 65,
    "expressiveness": 70,
    "comment": "La diction est claire mais le rythme pourrait être plus varié."
  },
  "expressivite": {
    "score": 80,
    "facialExpressions": "Expressions faciales convaincantes et naturelles.",
    "bodyLanguage": "Le langage corporel pourrait être plus expressif.",
    "comment": "Bonne expressivité globale, continuez à travailler l'amplification."
  },
  "recommendations": [
    "Varier davantage le ton de voix",
    "Utiliser plus l'espace scénique",
    "Travailler l'intensité émotionnelle"
  ],
  "strengths": [
    "Excellente diction",
    "Bonne connexion avec la caméra",
    "Expressions faciales naturelles"
  ],
  "summary": "Performance solide avec une bonne base technique. L'acteur montre une diction claire et des expressions naturelles. Pour progresser, il faudrait travailler l'intensité émotionnelle et varier davantage le rythme vocal."
}

CHAMPS OBLIGATOIRES:
- emotions.detected (array de strings, même si vide [])
- emotions.coherence (int 0-100)
- emotions.intensity (int 0-100)
- emotions.comment (string)
- posture.score, posture.strengths (array), posture.improvements (array), posture.comment
- intonation.score, intonation.clarity, intonation.rhythm, intonation.expressiveness, intonation.comment
- expressivite.score, expressivite.facialExpressions, expressivite.bodyLanguage, expressivite.comment
- recommendations (array de strings)
- strengths (array de strings)
- summary (string)
  "posture": {
    "score": 75,
    "strengths": ["Bonne présence scénique", "Dos droit"],
    "improvements": ["Utiliser plus les mains", "Varier les positions"],
    "comment": "La posture est correcte mais manque de dynamisme."
  },
  "intonation": {
    "score": 70,
    "clarity": 85,
    "rhythm": 65,
    "expressiveness": 70,
    "comment": "La diction est claire mais le rythme pourrait être plus varié."
  },
  "expressivite": {
    "score": 80,
    "facialExpressions": "Expressions faciales convaincantes et naturelles.",
    "bodyLanguage": "Le langage corporel pourrait être plus expressif.",
    "comment": "Bonne expressivité globale, continuez à travailler l'amplification."
  },
  "recommendations": [
    "Varier davantage le ton de voix",
    "Utiliser plus l'espace scénique",
    "Travailler l'intensité émotionnelle"
  ],
  "strengths": [
    "Excellente diction",
    "Bonne connexion avec la caméra",
    "Expressions faciales naturelles"
  ],
  "summary": "Performance solide avec une bonne base technique. L'acteur montre une diction claire et des expressions naturelles. Pour progresser, il faudrait travailler l'intensité émotionnelle et varier davantage le rythme vocal."
}

ANALYSE LA VIDÉO MAINTENANT:
        """.trimIndent()
    }
    
    /**
     * Appeler l'API Gemini avec la vidéo encodée
     * Utilise un streaming pour éviter OutOfMemoryError avec les grandes vidéos
     */
    private suspend fun callGeminiApi(videoBase64: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            // Construire l'URL - Utiliser la même structure que le chatbot (v1 et gemini-2.5-pro)
            val url = "${GeminiConfig.BASE_URL}v1/models/gemini-2.5-pro:generateContent?key=${GeminiConfig.GEMINI_API_KEY}"
            
            // Faire la requête HTTP
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 60000 // 60 secondes
            connection.readTimeout = 120000 // 120 secondes pour les grandes vidéos
            
            // Écrire le JSON directement dans le stream pour éviter OutOfMemoryError
            // On utilise un BufferedWriter pour améliorer les performances
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("{")
                writer.write("\"contents\":[")
                writer.write("{")
                writer.write("\"parts\":[")
                
                // Partie vidéo
                writer.write("{")
                writer.write("\"inline_data\":{")
                writer.write("\"mime_type\":\"video/mp4\",")
                writer.write("\"data\":\"")
                // Écrire la vidéo Base64 par chunks pour éviter de charger tout en mémoire
                val chunkSize = 8192 // 8KB chunks
                var offset = 0
                while (offset < videoBase64.length) {
                    val end = minOf(offset + chunkSize, videoBase64.length)
                    writer.write(videoBase64, offset, end - offset)
                    offset = end
                }
                writer.write("\"")
                writer.write("}")
                writer.write("},")
                
                // Partie prompt
                writer.write("{")
                writer.write("\"text\":")
                // Échapper le prompt JSON
                writer.write(escapeJsonString(prompt))
                writer.write("}")
                
                writer.write("]")
                writer.write("}")
                writer.write("],")
                
                // Generation config
                writer.write("\"generationConfig\":{")
                writer.write("\"temperature\":0.7,")
                writer.write("\"maxOutputTokens\":4096")
                writer.write("}")
                
                writer.write("}")
                writer.flush()
            }
            
            // Lire la réponse
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                
                Log.d(TAG, "📥 Réponse brute reçue (${response.length} caractères)")
                
                // Parser la réponse pour extraire le texte
                val responseJson = JSONObject(response)
                
                // Vérifier si c'est une réponse directe JSON (si responseMimeType est utilisé)
                if (responseJson.has("text")) {
                    val text = responseJson.getString("text")
                    Log.d(TAG, "✅ Texte extrait directement: ${text.take(200)}...")
                    return@withContext text
                }
                
                // Sinon, extraire depuis candidates (format standard)
                if (responseJson.has("candidates")) {
                    val candidates = responseJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        
                        // Vérifier s'il y a une erreur de blocage
                        if (firstCandidate.has("finishReason") && 
                            firstCandidate.getString("finishReason") == "SAFETY") {
                            throw Exception("Le contenu a été bloqué pour des raisons de sécurité")
                        }
                        
                        if (firstCandidate.has("content")) {
                            val content = firstCandidate.getJSONObject("content")
                            if (content.has("parts")) {
                                val parts = content.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    val part = parts.getJSONObject(0)
                                    if (part.has("text")) {
                                        val text = part.getString("text")
                                        Log.d(TAG, "✅ Texte extrait depuis candidates: ${text.take(200)}...")
                                        return@withContext text
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Si aucune structure attendue, essayer de retourner la réponse brute
                Log.w(TAG, "⚠️ Structure de réponse inattendue, tentative d'extraction directe")
                throw Exception("Réponse Gemini dans un format inattendu")
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Erreur API Gemini ($responseCode): $errorResponse")
                
                // Gérer spécifiquement l'erreur 429 (quota dépassé)
                if (responseCode == 429) {
                    val retryAfter = parseRetryAfterFromError(errorResponse)
                    val userMessage = if (retryAfter != null) {
                        "Quota API Gemini dépassé. Veuillez réessayer dans ${formatRetryTime(retryAfter)}."
                    } else {
                        "Quota API Gemini dépassé. Le plan gratuit permet 2 requêtes par minute. Veuillez réessayer dans quelques minutes."
                    }
                    Log.e(TAG, "❌ Erreur 429 - Quota dépassé. Retry après: $retryAfter secondes")
                    throw com.example.projecct_mobile.data.model.ApiException.GeminiQuotaException(userMessage, retryAfter)
                } else {
                    throw Exception("Erreur API Gemini: $responseCode - $errorResponse")
                }
            }
        }
    }
    
    /**
     * Parse le délai de retry depuis le message d'erreur Gemini
     */
    private fun parseRetryAfterFromError(errorBody: String?): Long? {
        if (errorBody == null) return null
        
        return try {
            // Chercher "Please retry in X.XXXXXXs" dans le message
            val retryPattern = Regex("Please retry in (\\d+(?:\\.\\d+)?)s", RegexOption.IGNORE_CASE)
            val match = retryPattern.find(errorBody)
            match?.groupValues?.get(1)?.toDoubleOrNull()?.toLong()
        } catch (e: Exception) {
            Log.w(TAG, "Impossible de parser retryAfter: ${e.message}")
            null
        }
    }
    
    /**
     * Formate le temps de retry en message lisible
     */
    private fun formatRetryTime(seconds: Long): String {
        return when {
            seconds < 60 -> "$seconds seconde${if (seconds > 1) "s" else ""}"
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                if (remainingSeconds > 0) {
                    "$minutes minute${if (minutes > 1) "s" else ""} et $remainingSeconds seconde${if (remainingSeconds > 1) "s" else ""}"
                } else {
                    "$minutes minute${if (minutes > 1) "s" else ""}"
                }
            }
            else -> {
                val hours = seconds / 3600
                val remainingMinutes = (seconds % 3600) / 60
                if (remainingMinutes > 0) {
                    "$hours heure${if (hours > 1) "s" else ""} et $remainingMinutes minute${if (remainingMinutes > 1) "s" else ""}"
                } else {
                    "$hours heure${if (hours > 1) "s" else ""}"
                }
            }
        }
    }
    
    /**
     * Échapper une chaîne pour l'inclure dans un JSON
     */
    private fun escapeJsonString(str: String): String {
        val escaped = str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
    
    /**
     * Parser la réponse JSON de Gemini
     */
    private fun parseGeminiFeedback(responseText: String): TrainingFeedback {
        try {
            Log.d(TAG, "📝 Réponse brute de Gemini (${responseText.length} caractères): ${responseText.take(500)}...")
            
            // Extraire le JSON (enlever le texte markdown si présent)
            var jsonText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            // Chercher le début du JSON (première accolade ouvrante)
            val jsonStartIndex = jsonText.indexOf('{')
            if (jsonStartIndex > 0) {
                jsonText = jsonText.substring(jsonStartIndex)
            }
            
            // Chercher la fin du JSON (dernière accolade fermante)
            val jsonEndIndex = jsonText.lastIndexOf('}')
            if (jsonEndIndex > 0 && jsonEndIndex < jsonText.length - 1) {
                jsonText = jsonText.substring(0, jsonEndIndex + 1)
            }
            
            // Nettoyer les caractères problématiques dans les chaînes JSON
            jsonText = cleanJsonString(jsonText)
            
            Log.d(TAG, "📝 JSON nettoyé (${jsonText.length} caractères): ${jsonText.take(500)}...")
            
            val json = JSONObject(jsonText)
            
            // Parser les émotions (gérer les deux formats possibles)
            val emotionsJson = json.optJSONObject("emotions") ?: JSONObject()
            val emotions = EmotionAnalysis(
                detected = emotionsJson.optJSONArray("detected")?.let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                } ?: emptyList(),
                coherence = emotionsJson.optInt("coherence", emotionsJson.optInt("score", 0)),
                intensity = emotionsJson.optInt("intensity", emotionsJson.optInt("score", 0)),
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
            
            return TrainingFeedback(
                globalScore = json.optInt("globalScore", 0),
                emotions = emotions,
                posture = posture,
                intonation = intonation,
                expressivite = expressivite,
                recommendations = recommendations,
                strengths = strengths,
                summary = json.optString("summary", "N/A")
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de parsing: ${e.message}", e)
            Log.e(TAG, "Réponse complète: $responseText")
            
            // Si le JSON est tronqué, essayer de parser ce qui est disponible
            if (e.message?.contains("Unterminated string") == true || 
                e.message?.contains("End of input") == true) {
                return tryParsePartialJson(responseText)
            }
            
            throw Exception("Erreur lors du traitement de la réponse Gemini: ${e.message}")
        }
    }
    
    /**
     * Nettoyer les chaînes JSON pour éviter les erreurs de parsing
     */
    private fun cleanJsonString(json: String): String {
        // Remplacer les sauts de ligne dans les chaînes par des espaces
        // (sauf ceux qui sont déjà échappés)
        val cleaned = StringBuilder()
        var inString = false
        var escapeNext = false
        
        for (i in json.indices) {
            val char = json[i]
            
            if (escapeNext) {
                cleaned.append(char)
                escapeNext = false
                continue
            }
            
            when (char) {
                '\\' -> {
                    cleaned.append(char)
                    escapeNext = true
                }
                '"' -> {
                    cleaned.append(char)
                    inString = !inString
                }
                '\n', '\r' -> {
                    if (inString) {
                        // Remplacer les sauts de ligne dans les chaînes par des espaces
                        cleaned.append(' ')
                    } else {
                        cleaned.append(char)
                    }
                }
                else -> cleaned.append(char)
            }
        }
        
        return cleaned.toString()
    }
    
    /**
     * Essayer de parser un JSON partiel/tronqué
     */
    private fun tryParsePartialJson(responseText: String): TrainingFeedback {
        Log.w(TAG, "⚠️ Tentative de parsing partiel du JSON...")
        
        try {
            // Extraire les valeurs disponibles même si le JSON est incomplet
            val jsonText = responseText.substringAfter("{").substringBeforeLast("}")
            
            // Valeurs par défaut
            var globalScore = 50
            var emotionsDetected = listOf("neutre")
            var emotionsCoherence = 50
            var emotionsIntensity = 50
            var emotionsComment = "Analyse incomplète - la réponse a été tronquée."
            
            var postureScore = 50
            var postureStrengths = listOf("Analyse incomplète")
            var postureImprovements = listOf("Réessayez avec une vidéo plus courte")
            var postureComment = "Analyse incomplète - la réponse a été tronquée."
            
            var intonationScore = 50
            var intonationClarity = 50
            var intonationRhythm = 50
            var intonationExpressiveness = 50
            var intonationComment = "Analyse incomplète - la réponse a été tronquée."
            
            var expressiviteScore = 50
            var expressiviteFacial = "Analyse incomplète"
            var expressiviteBody = "Analyse incomplète"
            var expressiviteComment = "Analyse incomplète - la réponse a été tronquée."
            
            var recommendations = listOf("Réessayez avec une vidéo plus courte ou une connexion plus stable")
            var strengths = listOf("Analyse incomplète")
            var summary = "La réponse de l'IA a été tronquée. Veuillez réessayer avec une vidéo plus courte ou vérifier votre connexion internet."
            
            // Essayer d'extraire ce qui est disponible
            try {
                val globalScoreMatch = Regex("\"globalScore\"\\s*:\\s*(\\d+)").find(responseText)
                globalScore = globalScoreMatch?.groupValues?.get(1)?.toIntOrNull() ?: 50
            } catch (e: Exception) {
                Log.w(TAG, "Impossible d'extraire globalScore")
            }
            
            try {
                val coherenceMatch = Regex("\"coherence\"\\s*:\\s*(\\d+)").find(responseText)
                emotionsCoherence = coherenceMatch?.groupValues?.get(1)?.toIntOrNull() ?: 50
            } catch (e: Exception) {
                Log.w(TAG, "Impossible d'extraire coherence")
            }
            
            try {
                val intensityMatch = Regex("\"intensity\"\\s*:\\s*(\\d+)").find(responseText)
                emotionsIntensity = intensityMatch?.groupValues?.get(1)?.toIntOrNull() ?: 50
            } catch (e: Exception) {
                Log.w(TAG, "Impossible d'extraire intensity")
            }
            
            // Créer un feedback partiel avec les valeurs extraites
            return TrainingFeedback(
                globalScore = globalScore,
                emotions = EmotionAnalysis(
                    detected = emotionsDetected,
                    coherence = emotionsCoherence,
                    intensity = emotionsIntensity,
                    comment = emotionsComment
                ),
                posture = PostureAnalysis(
                    score = postureScore,
                    strengths = postureStrengths,
                    improvements = postureImprovements,
                    comment = postureComment
                ),
                intonation = IntonationAnalysis(
                    score = intonationScore,
                    clarity = intonationClarity,
                    rhythm = intonationRhythm,
                    expressiveness = intonationExpressiveness,
                    comment = intonationComment
                ),
                expressivite = ExpressivityAnalysis(
                    score = expressiviteScore,
                    facialExpressions = expressiviteFacial,
                    bodyLanguage = expressiviteBody,
                    comment = expressiviteComment
                ),
                recommendations = recommendations,
                strengths = strengths,
                summary = summary
            )
        } catch (e: Exception) {
            Log.e(TAG, "Impossible de parser même partiellement: ${e.message}")
            throw Exception("La réponse de Gemini est incomplète ou mal formée. Veuillez réessayer avec une vidéo plus courte.")
        }
    }
}

