package com.example.projecct_mobile.ai

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.GroqApiService
import com.example.projecct_mobile.data.model.*
import com.example.projecct_mobile.data.model.groq.*
import com.example.projecct_mobile.utils.GroqConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
// Pose detection désactivé temporairement - nécessite une dépendance spécifique
// import com.google.mlkit.vision.pose.Pose
// import com.google.mlkit.vision.pose.PoseDetection
// import com.google.mlkit.vision.pose.PoseLandmark
// import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Service d'analyse vidéo utilisant des modèles IA gratuits (ML Kit, MediaPipe)
 * Remplace GeminiTrainingService pour une solution 100% gratuite et locale
 */
class FreeVideoAnalysisService(private val context: Context) {
    
    companion object {
        private const val TAG = "FreeVideoAnalysis"
        private const val MAX_VIDEO_SIZE_MB = 50
        private const val MAX_VIDEO_DURATION_SECONDS = 30
        private const val FRAMES_TO_ANALYZE = 60 // Nombre de frames à analyser (augmenté pour une meilleure précision)
    }
    
    // Service Groq pour l'analyse détaillée
    private val groqService: GroqApiService = ApiClient.getGroqService()
    private val GROQ_API_KEY = GroqConfig.GROQ_API_KEY
    
    // Détecteurs ML Kit
    // Utilise LANDMARK_MODE_NONE pour éviter les warnings "Unknown landmark type"
    // On utilise seulement les classifications (sourire, yeux) qui sont suffisantes
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE) // Pas besoin de tous les landmarks
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Sourire, yeux ouverts/fermés
            .enableTracking()
            .build()
    )
    
    // Pose detection désactivé - utilise uniquement la détection de visage pour l'instant
    // private val poseDetector = null
    
    /**
     * Analyser une vidéo d'entraînement d'acteur avec des modèles IA gratuits
     */
    suspend fun analyzeActingVideo(videoUri: Uri): Result<TrainingFeedback> {
        return analyzeActingVideo(videoUri, null, null, null)
    }
    
    /**
     * Analyser une vidéo d'entraînement d'acteur (avec contexte de casting optionnel)
     */
    suspend fun analyzeActingVideo(
        videoUri: Uri,
        roleDescription: String? = null,
        synopsis: String? = null,
        castingTitle: String? = null
    ): Result<TrainingFeedback> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🎬 Début de l'analyse vidéo gratuite: $videoUri")
                
                // Extraire les frames de la vidéo
                val frames = extractFramesFromVideo(videoUri)
                if (frames.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("Impossible d'extraire des frames de la vidéo")
                    )
                }
                
                Log.d(TAG, "📸 ${frames.size} frames extraites")
                
                // Analyser chaque frame avec ML Kit
                val analysisResults = mutableListOf<FrameAnalysis>()
                for ((index, frame) in frames.withIndex()) {
                    val frameAnalysis = analyzeFrame(frame)
                    analysisResults.add(frameAnalysis)
                    Log.d(TAG, "✅ Frame ${index + 1}/${frames.size} analysée")
                }
                
                // Vérifier la qualité de la vidéo AVANT d'envoyer à Groq
                val videoQuality = assessVideoQuality(analysisResults)
                Log.d(TAG, "📊 Qualité vidéo: visage=${(videoQuality.faceDetectionRate * 100).toInt()}%, qualité=${(videoQuality.avgQuality * 100).toInt()}%, expressions=${videoQuality.avgExpressions.toInt()}, niveau=${videoQuality.qualityLevel}")
                
                // Si la vidéo est un écran noir ou ne contient pas d'acteur → score 0-1
                if (videoQuality.isBlackScreen || videoQuality.noActorDetected) {
                    Log.w(TAG, "⚠️ Écran noir ou aucun acteur détecté - score 0-1")
                    return@withContext Result.success(generateZeroScoreFeedback(videoQuality))
                }
                
                // Si la vidéo est de très mauvaise qualité → score bas (20-40)
                if (videoQuality.isVeryPoor) {
                    Log.w(TAG, "⚠️ Vidéo de très mauvaise qualité détectée - score bas (20-40)")
                    return@withContext Result.success(generatePoorQualityFeedback(videoQuality))
                }
                
                Log.d(TAG, "🤖 Envoi des résultats ML Kit à Groq pour analyse détaillée...")
                
                // Générer un résumé des résultats ML Kit pour Groq
                val mlKitSummary = generateMLKitSummary(analysisResults, videoQuality)
                
                // Utiliser Groq pour analyser les résultats ML Kit et générer un feedback détaillé
                val feedback = analyzeWithGroq(
                    mlKitSummary,
                    analysisResults,
                    roleDescription,
                    synopsis,
                    castingTitle,
                    videoQuality
                )
                
                Log.d(TAG, "🎯 Analyse terminée - Score global: ${feedback.globalScore}/100")
                
                Result.success(feedback)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'analyse: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Extraire des frames de la vidéo pour analyse
     */
    private suspend fun extractFramesFromVideo(videoUri: Uri): List<Bitmap> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val frames = mutableListOf<Bitmap>()
        
        try {
            retriever.setDataSource(context, videoUri)
            
            // Obtenir la durée de la vidéo
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            val durationSeconds = duration / 1000
            
            if (durationSeconds > MAX_VIDEO_DURATION_SECONDS) {
                throw Exception("La vidéo est trop longue ($durationSeconds s). Maximum: $MAX_VIDEO_DURATION_SECONDS s")
            }
            
            // Extraire des frames uniformément réparties
            val interval = max(1, duration / FRAMES_TO_ANALYZE)
            
            for (i in 0 until FRAMES_TO_ANALYZE) {
                val timeUs = i * interval * 1000L // Convertir en microsecondes
                val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    frames.add(frame)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'extraction des frames: ${e.message}", e)
        } finally {
            retriever.release()
        }
        
        frames
    }
    
    /**
     * Analyser une frame individuelle
     */
    private suspend fun analyzeFrame(bitmap: Bitmap): FrameAnalysis = withContext(Dispatchers.IO) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        // Analyser le visage
        val faceResult = analyzeFace(inputImage)
        
        // Analyser la pose (désactivé pour l'instant)
        val poseResult: PoseAnalysisResult? = null
        
        // Analyser les couleurs et la composition (pour détecter les émotions basiques)
        val emotionEstimate = estimateEmotionFromImage(bitmap, faceResult)
        
        FrameAnalysis(
            faceDetected = faceResult != null,
            faceConfidence = faceResult?.confidence ?: 0f,
            faceExpressions = faceResult?.expressions ?: emptyList(),
            poseDetected = false, // Désactivé pour l'instant
            poseConfidence = 0f,
            postureScore = 70, // Score par défaut
            estimatedEmotion = emotionEstimate,
            frameQuality = calculateFrameQuality(bitmap)
        )
    }
    
    /**
     * Analyser le visage dans une image
     */
    private suspend fun analyzeFace(inputImage: InputImage): FaceAnalysisResult? = withContext(Dispatchers.IO) {
        try {
            val faces = faceDetector.process(inputImage).await()
            
            if (faces.isEmpty()) {
                return@withContext null
            }
            
            val face = faces.first()
            val expressions = mutableListOf<String>()
            
            // Détecter les expressions basiques
            face.smilingProbability?.let {
                if (it > 0.5f) expressions.add("sourire")
            }
            
            face.leftEyeOpenProbability?.let { left ->
                face.rightEyeOpenProbability?.let { right ->
                    val avg = (left + right) / 2f
                    if (avg < 0.3f) expressions.add("yeux_fermés")
                    else if (avg > 0.7f) expressions.add("yeux_ouverts")
                }
            }
            
            FaceAnalysisResult(
                confidence = 0.8f, // ML Kit ne donne pas directement la confiance
                expressions = expressions,
                landmarksCount = 0 // Pas utilisé avec LANDMARK_MODE_NONE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'analyse du visage: ${e.message}", e)
            null
        }
    }
    
    /**
     * Analyser la pose dans une image (désactivé - utilise uniquement la détection de visage)
     */
    private suspend fun analyzePose(inputImage: InputImage): PoseAnalysisResult? = withContext(Dispatchers.IO) {
        // Pose detection désactivé pour l'instant
        null
    }
    
    /**
     * Estimer l'émotion à partir de l'image et de l'analyse du visage
     */
    private fun estimateEmotionFromImage(bitmap: Bitmap, faceResult: FaceAnalysisResult?): String {
        // Analyse basique basée sur les expressions détectées
        return when {
            faceResult?.expressions?.contains("sourire") == true -> "joie"
            faceResult?.expressions?.contains("yeux_fermés") == true -> "concentration"
            else -> "neutre"
        }
    }
    
    /**
     * Calculer la qualité d'une frame
     */
    private fun calculateFrameQuality(bitmap: Bitmap): Float {
        // Mesure basique de la qualité (luminosité, contraste)
        val width = bitmap.width
        val height = bitmap.height
        var totalBrightness = 0L
        var pixelCount = 0
        var darkPixels = 0
        var brightPixels = 0
        
        // Échantillonner quelques pixels pour la performance
        val sampleSize = 10
        for (x in 0 until width step sampleSize) {
            for (y in 0 until height step sampleSize) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val brightness = (r + g + b) / 3
                totalBrightness += brightness
                pixelCount++
                
                // Détecter les pixels très sombres (écran noir) ou très clairs
                if (brightness < 20) darkPixels++
                if (brightness > 240) brightPixels++
            }
        }
        
        val avgBrightness = totalBrightness.toFloat() / pixelCount
        val darkRatio = darkPixels.toFloat() / pixelCount
        
        // Si plus de 80% des pixels sont très sombres, c'est probablement un écran noir
        if (darkRatio > 0.8f || avgBrightness < 15f) {
            return 0.1f // Qualité très mauvaise (écran noir)
        }
        
        // Si plus de 80% des pixels sont très clairs, c'est probablement un écran blanc
        val brightRatio = brightPixels.toFloat() / pixelCount
        if (brightRatio > 0.8f || avgBrightness > 245f) {
            return 0.2f // Qualité mauvaise (écran blanc)
        }
        
        // Normaliser entre 0 et 1 (luminosité idéale autour de 128)
        return 1f - abs(avgBrightness - 128f) / 128f
    }
    
    /**
     * Évaluer la qualité globale de la vidéo et déterminer le niveau
     */
    private fun assessVideoQuality(analysisResults: List<FrameAnalysis>): VideoQualityAssessment {
        val faceDetectedCount = analysisResults.count { it.faceDetected }
        val faceDetectionRate = faceDetectedCount.toFloat() / analysisResults.size
        val avgQuality = analysisResults.map { it.frameQuality }.average().toFloat()
        val avgExpressions = analysisResults.sumOf { it.faceExpressions.size }.toFloat() / analysisResults.size
        
        // Détecter si c'est un écran noir (qualité très faible)
        val isBlackScreen = avgQuality < 0.15f
        
        // Détecter si aucun visage n'est détecté (aucun acteur)
        val noFaceDetected = faceDetectionRate < 0.05f // Moins de 5% de frames avec visage
        val noActorDetected = faceDetectionRate < 0.05f && avgExpressions < 0.1f
        
        // Détecter si très peu d'expressions
        val noExpressions = avgExpressions < 0.1f
        
        // Déterminer le niveau de qualité
        val qualityLevel = when {
            isBlackScreen || noActorDetected -> QualityLevel.NO_ACTOR_OR_BLACK_SCREEN // Score 0-1
            faceDetectionRate < 0.2f || avgQuality < 0.3f -> QualityLevel.VERY_POOR // Score 20-40
            faceDetectionRate < 0.5f || avgQuality < 0.5f -> QualityLevel.POOR // Score 30-50
            faceDetectionRate < 0.7f || avgQuality < 0.7f -> QualityLevel.AVERAGE // Score 50-75
            faceDetectionRate >= 0.8f && avgQuality >= 0.7f && avgExpressions >= 1.0f -> QualityLevel.GOOD // Score 85-95
            else -> QualityLevel.AVERAGE
        }
        
        // Vidéo de très mauvaise qualité si : écran noir OU (pas de visage ET qualité faible) OU (pas de visage ET pas d'expressions)
        val isVeryPoor = qualityLevel == QualityLevel.VERY_POOR || qualityLevel == QualityLevel.POOR
        
        return VideoQualityAssessment(
            faceDetectionRate = faceDetectionRate,
            avgQuality = avgQuality,
            avgExpressions = avgExpressions,
            isVeryPoor = isVeryPoor,
            isBlackScreen = isBlackScreen,
            noFaceDetected = noFaceDetected,
            noActorDetected = noActorDetected,
            qualityLevel = qualityLevel
        )
    }
    
    /**
     * Niveaux de qualité vidéo
     */
    private enum class QualityLevel {
        NO_ACTOR_OR_BLACK_SCREEN, // Score 0-1
        VERY_POOR,                 // Score 20-40
        POOR,                      // Score 30-50
        AVERAGE,                   // Score 50-75
        GOOD                       // Score 85-95
    }
    
    /**
     * Générer un feedback pour une vidéo sans acteur ou écran noir (score 0-1)
     */
    private fun generateZeroScoreFeedback(quality: VideoQualityAssessment): TrainingFeedback {
        val reason = when {
            quality.isBlackScreen -> "écran noir"
            quality.noActorDetected -> "aucun acteur détecté"
            else -> "contenu vidéo invalide"
        }
        
        val globalScore = if (quality.isBlackScreen) 0 else 1
        
        return TrainingFeedback(
            globalScore = globalScore,
            emotions = EmotionAnalysis(
                detected = emptyList(),
                coherence = globalScore,
                intensity = globalScore,
                comment = "Impossible d'analyser les émotions car la vidéo présente un $reason. Cette vidéo ne contient pas de contenu analysable. Veuillez enregistrer une nouvelle vidéo avec un bon éclairage et assurez-vous que votre visage est bien visible."
            ),
            posture = PostureAnalysis(
                score = globalScore,
                strengths = emptyList(),
                improvements = listOf(
                    "Enregistrer une vidéo avec un bon éclairage",
                    "S'assurer que votre visage est bien visible",
                    "Vérifier que la caméra fonctionne correctement",
                    "Éviter les écrans noirs ou les vidéos sans contenu"
                ),
                comment = "Impossible d'analyser la posture car la vidéo présente un $reason. Cette vidéo ne contient pas de contenu analysable. Pour une analyse correcte, veuillez enregistrer une nouvelle vidéo avec un bon éclairage et une bonne visibilité."
            ),
            intonation = IntonationAnalysis(
                score = globalScore,
                clarity = globalScore,
                rhythm = globalScore,
                expressiveness = globalScore,
                comment = "Impossible d'analyser l'intonation car la vidéo présente un $reason. Cette vidéo ne contient pas de contenu analysable. Veuillez enregistrer une nouvelle vidéo avec un bon éclairage et une bonne qualité audio."
            ),
            expressivite = ExpressivityAnalysis(
                score = globalScore,
                facialExpressions = "Aucune expression détectée - la vidéo présente un $reason.",
                bodyLanguage = "Aucun langage corporel détecté - la vidéo présente un $reason.",
                comment = "Impossible d'analyser l'expressivité car la vidéo présente un $reason. Cette vidéo ne contient pas de contenu analysable. Pour une analyse correcte, veuillez enregistrer une nouvelle vidéo avec un bon éclairage et assurez-vous que votre visage et votre corps sont bien visibles."
            ),
            recommendations = listOf(
                "Enregistrer une nouvelle vidéo avec un bon éclairage naturel ou artificiel",
                "S'assurer que votre visage est bien visible et centré dans le cadre",
                "Vérifier que la caméra fonctionne correctement et n'est pas bloquée",
                "Éviter les écrans noirs ou les vidéos sans contenu visible",
                "Tester la vidéo avant de l'envoyer pour vérifier sa qualité"
            ),
            strengths = emptyList(),
            summary = "La vidéo analysée présente un $reason, ce qui rend impossible une analyse de votre performance. Cette vidéo ne contient pas de contenu analysable (score: $globalScore/100). Pour recevoir un feedback détaillé, veuillez enregistrer une nouvelle vidéo avec un bon éclairage, assurez-vous que votre visage est bien visible, et vérifiez que la caméra fonctionne correctement. Une vidéo de qualité est essentielle pour une analyse précise de votre jeu d'acteur."
        )
    }
    
    /**
     * Générer un feedback pour une vidéo de très mauvaise qualité (score 20-40)
     */
    private fun generatePoorQualityFeedback(quality: VideoQualityAssessment): TrainingFeedback {
        val reason = when {
            quality.isBlackScreen -> "écran noir"
            quality.noFaceDetected -> "aucun visage détecté"
            else -> "qualité vidéo insuffisante"
        }
        
        // Score bas selon le niveau de qualité
        val globalScore = when (quality.qualityLevel) {
            QualityLevel.VERY_POOR -> (20..35).random() // Score 20-35
            QualityLevel.POOR -> (30..45).random() // Score 30-45
            else -> 40
        }
        
        return TrainingFeedback(
            globalScore = globalScore,
            emotions = EmotionAnalysis(
                detected = emptyList(),
                coherence = globalScore,
                intensity = globalScore,
                comment = "Impossible d'analyser les émotions car la vidéo présente un $reason. Veuillez enregistrer une nouvelle vidéo avec un bon éclairage et assurez-vous que votre visage est bien visible."
            ),
            posture = PostureAnalysis(
                score = globalScore,
                strengths = emptyList(),
                improvements = listOf(
                    "Enregistrer une vidéo avec un bon éclairage",
                    "S'assurer que votre visage est bien visible",
                    "Vérifier que la caméra fonctionne correctement"
                ),
                comment = "Impossible d'analyser la posture car la vidéo présente un $reason. Pour une analyse correcte, veuillez enregistrer une nouvelle vidéo avec un bon éclairage et une bonne visibilité."
            ),
            intonation = IntonationAnalysis(
                score = globalScore,
                clarity = globalScore,
                rhythm = globalScore,
                expressiveness = globalScore,
                comment = "Impossible d'analyser l'intonation car la vidéo présente un $reason. Veuillez enregistrer une nouvelle vidéo avec un bon éclairage et une bonne qualité audio."
            ),
            expressivite = ExpressivityAnalysis(
                score = globalScore,
                facialExpressions = "Aucune expression détectée - la vidéo présente un $reason.",
                bodyLanguage = "Aucun langage corporel détecté - la vidéo présente un $reason.",
                comment = "Impossible d'analyser l'expressivité car la vidéo présente un $reason. Pour une analyse correcte, veuillez enregistrer une nouvelle vidéo avec un bon éclairage et assurez-vous que votre visage et votre corps sont bien visibles."
            ),
            recommendations = listOf(
                "Enregistrer une nouvelle vidéo avec un bon éclairage naturel ou artificiel",
                "S'assurer que votre visage est bien visible et centré dans le cadre",
                "Vérifier que la caméra fonctionne correctement et n'est pas bloquée",
                "Éviter les écrans noirs ou les vidéos sans contenu visible",
                "Tester la vidéo avant de l'envoyer pour vérifier sa qualité"
            ),
            strengths = emptyList(),
            summary = "La vidéo analysée présente un $reason, ce qui rend impossible une analyse précise de votre performance. Pour recevoir un feedback détaillé, veuillez enregistrer une nouvelle vidéo avec un bon éclairage, assurez-vous que votre visage est bien visible, et vérifiez que la caméra fonctionne correctement. Une vidéo de qualité est essentielle pour une analyse précise de votre jeu d'acteur."
        )
    }
    
    /**
     * Évaluation de la qualité vidéo
     */
    private data class VideoQualityAssessment(
        val faceDetectionRate: Float, // 0.0 à 1.0
        val avgQuality: Float, // 0.0 à 1.0
        val avgExpressions: Float,
        val isVeryPoor: Boolean,
        val isBlackScreen: Boolean,
        val noFaceDetected: Boolean,
        val noActorDetected: Boolean,
        val qualityLevel: QualityLevel
    )
    
    /**
     * Générer le feedback final à partir des analyses de frames
     */
    private fun generateFeedback(
        frameAnalyses: List<FrameAnalysis>,
        roleDescription: String?,
        synopsis: String?,
        castingTitle: String?
    ): TrainingFeedback {
        
        // Agréger les résultats de toutes les frames
        val avgFaceDetected = frameAnalyses.count { it.faceDetected }.toFloat() / frameAnalyses.size
        val avgPoseDetected = frameAnalyses.count { it.poseDetected }.toFloat() / frameAnalyses.size
        val avgPostureScore = frameAnalyses.map { it.postureScore }.average().toInt()
        val avgQuality = frameAnalyses.map { it.frameQuality }.average().toFloat()
        
        // Détecter les émotions les plus fréquentes
        val emotionCounts = frameAnalyses.groupingBy { it.estimatedEmotion }.eachCount()
        val detectedEmotions = emotionCounts.keys.toList()
        val dominantEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "neutre"
        
        // Générer les scores
        val emotionsScore = calculateEmotionsScore(frameAnalyses)
        val postureScore = avgPostureScore
        val intonationScore = 70 // Pas d'analyse audio avec cette version, score par défaut
        val expressiviteScore = calculateExpressivityScore(frameAnalyses)
        
        val globalScore = (emotionsScore + postureScore + intonationScore + expressiviteScore) / 4
        
        // Générer les commentaires
        val emotionsComment = generateEmotionsComment(emotionsScore, detectedEmotions, dominantEmotion)
        val postureComment = generatePostureComment(postureScore, avgPoseDetected)
        val intonationComment = "L'analyse audio n'est pas disponible dans cette version. Score basé sur la qualité générale."
        val expressiviteComment = generateExpressivityComment(expressiviteScore, avgFaceDetected)
        
        // Générer les recommandations
        val recommendations = generateRecommendations(
            emotionsScore,
            postureScore,
            expressiviteScore,
            roleDescription
        )
        
        // Générer les points forts
        val strengths = generateStrengths(
            emotionsScore,
            postureScore,
            expressiviteScore,
            avgQuality
        )
        
        // Générer le résumé
        val summary = generateSummary(
            globalScore,
            emotionsScore,
            postureScore,
            expressiviteScore,
            roleDescription
        )
        
        return TrainingFeedback(
            globalScore = globalScore,
            emotions = EmotionAnalysis(
                detected = detectedEmotions,
                coherence = emotionsScore,
                intensity = emotionsScore,
                comment = emotionsComment
            ),
            posture = PostureAnalysis(
                score = postureScore,
                strengths = if (postureScore >= 70) listOf("Bonne présence", "Posture stable") else emptyList(),
                improvements = if (postureScore < 70) listOf("Améliorer l'alignement", "Travailler la symétrie") else emptyList(),
                comment = postureComment
            ),
            intonation = IntonationAnalysis(
                score = intonationScore,
                clarity = 70,
                rhythm = 70,
                expressiveness = 70,
                comment = intonationComment
            ),
            expressivite = ExpressivityAnalysis(
                score = expressiviteScore,
                facialExpressions = if (avgFaceDetected > 0.7f) "Expressions faciales bien détectées" else "Expressions faciales peu visibles",
                bodyLanguage = if (avgPoseDetected > 0.7f) "Langage corporel présent" else "Langage corporel à améliorer",
                comment = expressiviteComment
            ),
            recommendations = recommendations,
            strengths = strengths,
            summary = summary
        )
    }
    
    private fun calculateEmotionsScore(analyses: List<FrameAnalysis>): Int {
        val faceDetectedRatio = analyses.count { it.faceDetected }.toFloat() / analyses.size
        val expressionsCount = analyses.sumOf { it.faceExpressions.size }
        val avgExpressions = expressionsCount.toFloat() / analyses.size
        
        // Score basé sur la détection du visage et la variété des expressions
        return ((faceDetectedRatio * 0.6f + (avgExpressions / 3f) * 0.4f) * 100).toInt().coerceIn(0, 100)
    }
    
    private fun calculateExpressivityScore(analyses: List<FrameAnalysis>): Int {
        val faceDetectedRatio = analyses.count { it.faceDetected }.toFloat() / analyses.size
        val poseDetectedRatio = analyses.count { it.poseDetected }.toFloat() / analyses.size
        val avgQuality = analyses.map { it.frameQuality }.average().toFloat()
        
        return ((faceDetectedRatio * 0.4f + poseDetectedRatio * 0.4f + avgQuality * 0.2f) * 100).toInt().coerceIn(0, 100)
    }
    
    private fun generateEmotionsComment(score: Int, emotions: List<String>, dominant: String): String {
        return when {
            score >= 80 -> "Excellente expression émotionnelle. Les émotions sont bien visibles et variées."
            score >= 60 -> "Bonne expression émotionnelle. Continuez à travailler la variété et l'intensité."
            else -> "L'expression émotionnelle peut être améliorée. Essayez d'être plus expressif."
        }
    }
    
    private fun generatePostureComment(score: Int, poseDetectedRatio: Float): String {
        return when {
            score >= 80 -> "Excellente posture. Le corps est bien aligné et présent."
            score >= 60 -> "Bonne posture de base. Quelques ajustements pourraient améliorer la présence."
            else -> "La posture peut être améliorée. Travaillez sur l'alignement et la présence scénique."
        }
    }
    
    private fun generateExpressivityComment(score: Int, faceDetectedRatio: Float): String {
        return when {
            score >= 80 -> "Très bonne expressivité globale. Les expressions faciales et le langage corporel sont bien coordonnés."
            score >= 60 -> "Bonne expressivité. Continuez à travailler la coordination entre le visage et le corps."
            else -> "L'expressivité peut être améliorée. Travaillez sur la coordination des expressions."
        }
    }
    
    private fun generateRecommendations(
        emotionsScore: Int,
        postureScore: Int,
        expressiviteScore: Int,
        roleDescription: String?
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (emotionsScore < 70) {
            recommendations.add("Travailler l'intensité et la variété des émotions")
        }
        if (postureScore < 70) {
            recommendations.add("Améliorer l'alignement et la présence corporelle")
        }
        if (expressiviteScore < 70) {
            recommendations.add("Coordonner davantage les expressions faciales et le langage corporel")
        }
        
        if (roleDescription != null && roleDescription.isNotBlank()) {
            recommendations.add("Adapter votre interprétation au personnage décrit")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Continuez à pratiquer pour maintenir votre niveau")
        }
        
        return recommendations.take(5)
    }
    
    private fun generateStrengths(
        emotionsScore: Int,
        postureScore: Int,
        expressiviteScore: Int,
        avgQuality: Float
    ): List<String> {
        val strengths = mutableListOf<String>()
        
        if (emotionsScore >= 70) {
            strengths.add("Bonne expression émotionnelle")
        }
        if (postureScore >= 70) {
            strengths.add("Posture stable et présente")
        }
        if (expressiviteScore >= 70) {
            strengths.add("Bonne coordination des expressions")
        }
        if (avgQuality > 0.7f) {
            strengths.add("Qualité vidéo correcte")
        }
        
        if (strengths.isEmpty()) {
            strengths.add("Base solide à développer")
        }
        
        return strengths.take(5)
    }
    
    private fun generateSummary(
        globalScore: Int,
        emotionsScore: Int,
        postureScore: Int,
        expressiviteScore: Int,
        roleDescription: String?
    ): String {
        val baseSummary = when {
            globalScore >= 80 -> "Performance solide avec une bonne base technique."
            globalScore >= 60 -> "Performance correcte avec des points à améliorer."
            else -> "Performance de base avec un potentiel d'amélioration."
        }
        
        val details = buildString {
            if (emotionsScore >= 70) append(" Les émotions sont bien exprimées.")
            if (postureScore >= 70) append(" La posture est stable.")
            if (expressiviteScore < 70) append(" L'expressivité globale peut être améliorée.")
        }
        
        val roleNote = if (roleDescription != null && roleDescription.isNotBlank()) {
            " Continuez à travailler pour mieux incarner le personnage."
        } else {
            ""
        }
        
        return baseSummary + details + roleNote
    }
    
    /**
     * Résultat d'analyse d'une frame
     */
    private data class FrameAnalysis(
        val faceDetected: Boolean,
        val faceConfidence: Float,
        val faceExpressions: List<String>,
        val poseDetected: Boolean,
        val poseConfidence: Float,
        val postureScore: Int,
        val estimatedEmotion: String,
        val frameQuality: Float
    )
    
    /**
     * Résultat d'analyse du visage
     */
    private data class FaceAnalysisResult(
        val confidence: Float,
        val expressions: List<String>,
        val landmarksCount: Int
    )
    
    /**
     * Résultat d'analyse de la pose
     */
    private data class PoseAnalysisResult(
        val confidence: Float,
        val postureScore: Int,
        val landmarksCount: Int
    )
    
    /**
     * Générer un résumé des résultats ML Kit pour Groq
     */
    private fun generateMLKitSummary(frameAnalyses: List<FrameAnalysis>, quality: VideoQualityAssessment? = null): String {
        val faceDetectedCount = frameAnalyses.count { it.faceDetected }
        val faceDetectedRatio = faceDetectedCount.toFloat() / frameAnalyses.size
        val avgPostureScore = frameAnalyses.map { it.postureScore }.average()
        val avgQuality = frameAnalyses.map { it.frameQuality }.average()
        
        val emotionCounts = frameAnalyses.groupingBy { it.estimatedEmotion }.eachCount()
        val detectedEmotions = emotionCounts.keys.joinToString(", ")
        val dominantEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "neutre"
        
        val expressionsCount = frameAnalyses.sumOf { it.faceExpressions.size }
        val avgExpressions = expressionsCount.toFloat() / frameAnalyses.size
        
        val qualityInfo = quality?.let {
            """
⚠️ ALERTE QUALITÉ VIDÉO:
   - Taux de détection de visage: ${(it.faceDetectionRate * 100).toInt()}%
   - Qualité moyenne: ${(it.avgQuality * 100).toInt()}%
   - Expressions moyennes: ${it.avgExpressions.toInt()}
   - Écran noir détecté: ${if (it.isBlackScreen) "OUI" else "NON"}
   - Aucun visage: ${if (it.noFaceDetected) "OUI" else "NON"}
   - Qualité très mauvaise: ${if (it.isVeryPoor) "OUI - NE PAS DONNER DE BON SCORE" else "NON"}
"""
        } ?: ""
        
        return """
RÉSULTATS DE L'ANALYSE ML KIT (${frameAnalyses.size} frames analysées):
$qualityInfo

1. DÉTECTION DE VISAGE:
   - Visages détectés: $faceDetectedCount/${frameAnalyses.size} frames (${(faceDetectedRatio * 100).toInt()}%)
   - Expressions faciales détectées: ${avgExpressions.toInt()} en moyenne par frame
   - Émotions estimées: $detectedEmotions
   - Émotion dominante: $dominantEmotion

2. POSTURE:
   - Score moyen de posture: ${avgPostureScore.toInt()}/100
   - Posture détectée: ${frameAnalyses.count { it.poseDetected }}/${frameAnalyses.size} frames

3. QUALITÉ VIDÉO:
   - Qualité moyenne des frames: ${(avgQuality * 100).toInt()}%
   ${if (avgQuality < 0.3f) "⚠️ ATTENTION: Qualité très faible - possible écran noir ou vidéo corrompue" else ""}

4. EXPRESSIONS DÉTECTÉES:
${frameAnalyses.flatMap { it.faceExpressions }.groupingBy { it }.eachCount().entries.joinToString("\n") { "   - ${it.key}: ${it.value} fois" }}

IMPORTANT: UTILISE CES DONNÉES RÉELLES pour calculer les scores. Si le visage est détecté < 10% ou qualité < 30%, donne des scores TRÈS BAS (20-40). Si visage détecté > 80% et qualité > 70%, donne des scores ÉLEVÉS (85-95).
        """.trimIndent()
    }
    
    /**
     * Analyser les résultats ML Kit avec Groq pour obtenir un feedback détaillé
     */
    private suspend fun analyzeWithGroq(
        mlKitSummary: String,
        frameAnalyses: List<FrameAnalysis>,
        roleDescription: String?,
        synopsis: String?,
        castingTitle: String?,
        videoQuality: VideoQualityAssessment? = null
    ): TrainingFeedback = withContext(Dispatchers.IO) {
        try {
            val prompt = buildGroqPrompt(mlKitSummary, frameAnalyses, roleDescription, synopsis, castingTitle, videoQuality)
            
            Log.d(TAG, "🤖 Envoi à Groq pour analyse détaillée...")
            val response = callGroqApi(prompt)
            
            Log.d(TAG, "✅ Réponse Groq reçue, parsing...")
            parseGroqFeedback(response)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'analyse Groq: ${e.message}", e)
            // Fallback: utiliser l'analyse ML Kit basique
            Log.w(TAG, "⚠️ Utilisation du feedback ML Kit basique en fallback")
            generateFeedback(frameAnalyses, roleDescription, synopsis, castingTitle)
        }
    }
    
    /**
     * Construire le prompt pour Groq
     */
    private fun buildGroqPrompt(
        mlKitSummary: String,
        frameAnalyses: List<FrameAnalysis>,
        roleDescription: String?,
        synopsis: String?,
        castingTitle: String?,
        videoQuality: VideoQualityAssessment? = null
    ): String {
        val contextSection = buildString {
            if (castingTitle != null || roleDescription != null || synopsis != null) {
                append("\nCONTEXTE DU CASTING:\n")
                if (castingTitle != null) {
                    append("- Titre du casting: $castingTitle\n")
                }
                if (roleDescription != null && roleDescription.isNotBlank()) {
                    append("- Description du rôle: $roleDescription\n")
                }
                if (synopsis != null && synopsis.isNotBlank()) {
                    append("- Synopsis: $synopsis\n")
                }
            }
        }
        
        val qualityWarning = videoQuality?.let {
            if (it.faceDetectionRate > 0.8f && it.avgQuality > 0.7f && it.avgExpressions >= 1.0f) {
                """
✅✅✅ VIDÉO DE BONNE QUALITÉ ✅✅✅
Cette vidéo présente d'excellentes données ML Kit:
- Taux de détection de visage: ${(it.faceDetectionRate * 100).toInt()}% (EXCELLENT)
- Qualité vidéo: ${(it.avgQuality * 100).toInt()}% (EXCELLENT)
- Expressions détectées: ${it.avgExpressions.toInt()} (BON)

TU DOIS ABSOLUMENT:
- Donner des scores ÉLEVÉS (85-95) pour tous les aspects
- Le score global DOIT être entre 85 et 95
- NE JAMAIS donner de score moyen (70-84) pour cette vidéo de bonne qualité
- Sois généreux et encourageant car les données sont excellentes
"""
            } else if (it.isVeryPoor) {
                """
⚠️⚠️⚠️ ATTENTION CRITIQUE ⚠️⚠️⚠️
Cette vidéo présente des problèmes majeurs de qualité:
- Taux de détection de visage: ${(it.faceDetectionRate * 100).toInt()}% (TRÈS FAIBLE)
- Qualité vidéo: ${(it.avgQuality * 100).toInt()}% (TRÈS FAIBLE)
- Écran noir détecté: ${if (it.isBlackScreen) "OUI" else "NON"}
- Aucun visage détecté: ${if (it.noFaceDetected) "OUI" else "NON"}

TU DOIS ABSOLUMENT:
- Donner des scores TRÈS BAS (20-40) pour tous les aspects
- Expliquer clairement que la vidéo est de mauvaise qualité
- Recommander d'enregistrer une nouvelle vidéo
- NE PAS donner de scores moyens (70-80) pour une vidéo de mauvaise qualité
"""
            } else if (it.faceDetectionRate < 0.3f || it.avgQuality < 0.4f) {
                """
⚠️ ATTENTION: Qualité vidéo faible
- Taux de détection de visage: ${(it.faceDetectionRate * 100).toInt()}% (FAIBLE)
- Qualité vidéo: ${(it.avgQuality * 100).toInt()}% (FAIBLE)

Donne des scores MODÉRÉS (50-70) et explique que la qualité peut être améliorée.
"""
            } else {
                ""
            }
        } ?: ""
        
        return """
Tu es un coach professionnel en acting et en jeu d'acteur. Analyse ces résultats d'analyse vidéo obtenus avec ML Kit (détection de visage, expressions, posture).
$contextSection

$qualityWarning

DONNÉES ML KIT (UTILISE CES DONNÉES RÉELLES - NE PAS INVENTER):
$mlKitSummary

⚠️ RÈGLE ABSOLUE: Les scores DOIVENT correspondre aux données ML Kit ci-dessus. Si le visage est détecté < 10% ou qualité < 30%, les scores DOIVENT être TRÈS BAS (20-40). Si visage détecté > 80% et qualité > 70%, les scores peuvent être ÉLEVÉS (85-95).

INSTRUCTIONS:
1. Analyse les données ML Kit et génère un feedback détaillé et professionnel
2. Donne des scores GÉNÉREUX et ENCOURAGEANTS (0-100) pour chaque aspect:
   - Émotions: cohérence, intensité, variété
     * Si visage détecté > 80% ET expressions variées ET qualité > 70% → score OBLIGATOIREMENT 85-95 (EXCELLENT - NE PAS DONNER 70-84)
     * Si visage détecté 60-80% ET qualité > 60% → score 75-88 (TRÈS BIEN)
     * Si visage détecté 40-60% OU qualité 40-60% → score 50-75 (MOYEN)
     * Si visage détecté < 40% OU qualité < 40% → score 30-50 (FAIBLE)
     * COMMENTAIRE: Écris 3-4 phrases détaillées expliquant les émotions détectées, leur cohérence, leur intensité, et des exemples concrets
   - Posture: alignement, présence, stabilité
     * Si visage > 80% ET qualité > 70% → score OBLIGATOIREMENT 80-95 (TRÈS BIEN - NE PAS DONNER 70-79)
     * Si posture détectée > 70% ET visage > 80% ET qualité > 70% → score OBLIGATOIREMENT 85-95 (EXCELLENT)
     * Si posture détectée 50-70% ET visage > 60% → score 75-88 (TRÈS BIEN)
     * Si posture détectée 30-50% OU visage 40-60% → score 50-75 (MOYEN)
     * Sinon → score 30-50 (FAIBLE)
     * COMMENTAIRE: Écris 3-4 phrases détaillées sur l'alignement corporel, la présence scénique, la stabilité, et des conseils spécifiques
   - Intonation: clarté, rythme, expressivité (estime basé sur les données visuelles)
     * Si qualité vidéo > 70% ET visage bien détecté > 80% → score OBLIGATOIREMENT 80-92 (TRÈS BIEN - NE PAS DONNER 70-79)
     * Si qualité vidéo > 70% ET visage > 80% ET expressions > 1.0 → score OBLIGATOIREMENT 85-92 (EXCELLENT)
     * Si qualité vidéo 50-70% ET visage > 60% → score 75-88 (TRÈS BIEN)
     * Si qualité vidéo 40-60% OU visage 40-60% → score 50-75 (MOYEN)
     * Sinon → score 30-50 (FAIBLE)
     * COMMENTAIRE: Écris 3-4 phrases détaillées sur la clarté vocale estimée, le rythme, l'expressivité, et des suggestions d'amélioration
   - Expressivité: coordination visage/corps, amplitude
     * Si visage > 80% ET qualité > 70% → score OBLIGATOIREMENT 75-95 (TRÈS BIEN - NE PAS DONNER < 75)
     * Si visage > 80% ET posture > 70% ET qualité > 70% → score OBLIGATOIREMENT 85-95 (EXCELLENT)
     * Si visage > 60% ET qualité > 60% → score 75-88 (TRÈS BIEN)
     * Si visage 40-60% OU qualité 40-60% → score 50-75 (MOYEN)
     * Sinon → score 30-50 (FAIBLE)
     * COMMENTAIRE: Écris 3-4 phrases détaillées sur la coordination entre expressions faciales et langage corporel, l'amplitude, et des conseils pour améliorer

3. CALCULE le score global comme MOYENNE des 4 scores (émotions + posture + intonation + expressivité) / 4
   * VIDÉO BIEN (visage > 80% ET qualité > 70% ET expressions > 1.0) → score global OBLIGATOIREMENT 85-95 (NE PAS DONNER 70-84)
   * VIDÉO TRÈS BIEN (visage 60-80% ET qualité > 60%) → score global 75-88
   * VIDÉO MOYENNE (visage 40-60% OU qualité 40-60%) → score global 50-75
   * VIDÉO MAUVAISE (visage 20-40% OU qualité 20-40%) → score global 30-50
   * VIDÉO TRÈS MAUVAISE (visage < 20% OU qualité < 20%) → score global 20-40
   * RÈGLE D'OR ABSOLUE: Une vidéo avec visage bien détecté (> 80%) ET bonne qualité (> 70%) DOIT avoir un score global ENTRE 85 ET 95. NE JAMAIS donner 70-84 pour une bonne vidéo.
   * RÈGLE IMPORTANTE: Si au moins un aspect (émotions, posture, intonation, expressivité) a un score > 85, alors TOUS les autres aspects doivent avoir au minimum 75-80, et le score global doit être au minimum 80-85.
   * RÈGLE CRITIQUE: Une vidéo avec visage < 20% OU qualité < 20% DOIT avoir un score global < 50
   * RÈGLE ABSOLUE: Utilise les données ML Kit réelles - ne donne pas de score moyen (70-80) si les données sont bonnes (> 80% visage, > 70% qualité)

4. Liste 5-7 points forts spécifiques et détaillés (sois TRÈS généreux et positif)
   - Chaque point fort doit être détaillé (2-3 phrases expliquant pourquoi c'est un point fort)
5. Liste 5-7 recommandations concrètes et actionnables (sois encourageant, pas critique)
   - Chaque recommandation doit être détaillée avec des conseils pratiques (2-3 phrases)
6. Écris un résumé DÉTAILLÉ en 5-7 phrases (TRÈS positif et motivant)
   - Le résumé doit couvrir tous les aspects analysés
   - Mentionne les points forts principaux
   - Donne une vision d'ensemble de la performance
   - Sois encourageant et constructif

IMPORTANT:
- Sois TRÈS BIENVEILLANT et ENCOURAGEANT - les acteurs ont besoin de motivation positive
- Si les données ML Kit montrent une bonne performance, donne des scores TRÈS ÉLEVÉS (85-95)
- Ne sois JAMAIS trop strict - une vidéo avec visage bien détecté et expressions variées mérite au minimum 80/100, idéalement 85-90
- Privilégie les scores élevés pour encourager l'acteur
- Donne des conseils constructifs et positifs
- Utilise un langage professionnel mais accessible et encourageant
- Réponds UNIQUEMENT en JSON valide (AUCUN texte avant ou après)
- Structure JSON exacte:

{
  "globalScore": 87,
  "emotions": {
    "detected": ["joie", "concentration", "expressivité", "détermination"],
    "coherence": 88,
    "intensity": 85,
    "comment": "Excellente expression émotionnelle ! Les émotions détectées (joie, concentration, expressivité, détermination) sont bien visibles et cohérentes tout au long de la performance. L'intensité émotionnelle est remarquable, avec des transitions naturelles entre les différentes émotions. La variété des expressions montre une bonne capacité à incarner différents états émotionnels. Continuez à travailler sur l'amplification des moments clés pour rendre les émotions encore plus percutantes."
  },
  "posture": {
    "score": 86,
    "strengths": ["Excellente présence scénique avec un alignement corporel solide", "Posture stable et équilibrée qui renforce la crédibilité", "Bonne utilisation de l'espace scénique avec des déplacements naturels", "Dos droit et épaules dégagées qui projettent confiance"],
    "improvements": ["Varier davantage les positions pour créer plus de dynamisme", "Amplifier les gestes pour les moments d'intensité émotionnelle", "Explorer différentes hauteurs (debout, assis, accroupi) pour plus de variété"],
    "comment": "Très bonne posture ! Vous démontrez une présence scénique solide avec un excellent alignement corporel. Votre dos est droit, vos épaules sont dégagées, et vous utilisez l'espace de manière naturelle et efficace. Cette stabilité posturale renforce la crédibilité de votre jeu et projette une image de confiance. Pour aller plus loin, vous pourriez explorer davantage de variations de positions et amplifier vos gestes lors des moments d'intensité émotionnelle."
  },
  "intonation": {
    "score": 85,
    "clarity": 90,
    "rhythm": 82,
    "expressiveness": 83,
    "comment": "Excellente diction et très bonne expressivité vocale ! La clarté de votre élocution est remarquable, chaque mot est bien articulé et facilement compréhensible. Le rythme de votre débit est varié et engageant, avec des pauses bien placées qui créent de la tension dramatique. L'expressivité vocale est présente, avec des variations de ton qui accompagnent bien les émotions exprimées. Pour perfectionner encore, vous pourriez explorer davantage les nuances de volume et d'intensité pour créer plus de contrastes."
  },
  "expressivite": {
    "score": 88,
    "facialExpressions": "Expressions faciales très convaincantes, naturelles et expressives. Les micro-expressions sont bien présentes et les transitions entre les différentes émotions sont fluides. Les yeux sont particulièrement expressifs et créent une vraie connexion avec le spectateur.",
    "bodyLanguage": "Langage corporel bien coordonné avec les expressions faciales. Les gestes accompagnent naturellement le discours et renforcent le message émotionnel. La synchronisation entre le visage et le corps crée une performance cohérente et crédible.",
    "comment": "Excellente expressivité globale ! Vos expressions faciales et votre langage corporel sont remarquablement bien coordonnés. Les micro-expressions sont présentes et naturelles, créant une vraie connexion émotionnelle. Les gestes accompagnent harmonieusement le discours et renforcent le message. Cette coordination entre le visage et le corps démontre une bonne maîtrise de l'expressivité scénique. Pour aller encore plus loin, vous pourriez amplifier certains gestes lors des moments d'intensité maximale."
  },
  "recommendations": [
    "Continuez à varier les intensités émotionnelles en explorant les nuances entre les différents niveaux d'expressivité. Travaillez sur les transitions subtiles entre les émotions pour créer plus de profondeur.",
    "Explorer davantage l'espace scénique en utilisant des déplacements variés (avancer, reculer, tourner) pour créer plus de dynamisme visuel et renforcer l'impact de votre performance.",
    "Amplifier encore plus les gestes pour les grandes scènes en utilisant des mouvements plus larges et plus expressifs qui projettent mieux l'émotion vers le public.",
    "Travailler sur les variations de volume vocal pour créer plus de contrastes et d'impact dramatique, en particulier lors des moments clés de la performance.",
    "Explorer différentes hauteurs de jeu (debout, assis, accroupi) pour ajouter de la variété visuelle et créer plus d'intérêt scénique.",
    "Développer encore plus la connexion avec la caméra en variant les directions du regard et en créant des moments de contact visuel plus intenses."
  ],
  "strengths": [
    "Excellente diction et clarté vocale avec une articulation précise qui rend chaque mot parfaitement compréhensible, démontrant une solide maîtrise technique de la voix.",
    "Très bonne connexion avec la caméra grâce à un regard expressif et varié qui crée une vraie intimité avec le spectateur et renforce l'impact émotionnel.",
    "Expressions faciales naturelles et variées qui montrent une bonne capacité à incarner différents états émotionnels avec authenticité et crédibilité.",
    "Posture stable et présence scénique solide avec un excellent alignement corporel qui projette confiance et professionnalisme.",
    "Coordination remarquable entre expressions faciales et langage corporel qui crée une performance cohérente et engageante.",
    "Rythme vocal varié et engageant avec des pauses bien placées qui créent de la tension dramatique et maintiennent l'attention du spectateur.",
    "Bonne utilisation de l'espace scénique avec des déplacements naturels qui ajoutent du dynamisme à la performance."
  ],
  "summary": "Performance excellente ! Vous démontrez une très bonne maîtrise technique avec une diction claire et précise, des expressions faciales naturelles et variées, et une présence scénique solide. Votre coordination entre le visage et le corps est remarquable, créant une performance cohérente et crédible. La variété émotionnelle et la clarté vocale sont des points forts majeurs de votre jeu. Pour continuer à progresser, concentrez-vous sur l'amplification des gestes pour les grandes scènes, l'exploration de l'espace scénique, et les variations de volume vocal pour créer plus de contrastes dramatiques. Vous avez toutes les bases pour exceller dans le jeu d'acteur !"
}

ANALYSE LES DONNÉES ML KIT MAINTENANT:
        """.trimIndent()
    }
    
    /**
     * Appeler l'API Groq
     */
    private suspend fun callGroqApi(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val groqRequest = GroqChatRequest(
                model = GroqConfig.DEFAULT_MODEL,
                messages = listOf(
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.7,
                maxTokens = 6144 // Augmenté pour permettre des réponses plus longues et détaillées
            )
            
            val response = groqService.chatCompletions("Bearer $GROQ_API_KEY", groqRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val groqResponse = response.body()!!
                val answerText = groqResponse.choices?.firstOrNull()?.message?.content
                    ?: throw Exception("Réponse Groq vide")
                
                Log.d(TAG, "📥 Réponse Groq reçue (${answerText.length} caractères)")
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
    
    /**
     * Parser la réponse JSON de Groq
     */
    private fun parseGroqFeedback(responseText: String): TrainingFeedback {
        return try {
            Log.d(TAG, "📝 Parsing réponse Groq (${responseText.length} caractères)")
            
            // Extraire le JSON
            var jsonText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val jsonStart = jsonText.indexOf("{")
            val jsonEnd = jsonText.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonText = jsonText.substring(jsonStart, jsonEnd)
            }
            
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
            
            // Parser recommandations et points forts
            val recommendations = json.optJSONArray("recommendations")?.let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            } ?: emptyList()
            
            val strengths = json.optJSONArray("strengths")?.let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            } ?: emptyList()
            
            val globalScoreFromJson = json.optInt("globalScore", 0)
            
            // Calculer le score moyen des 4 aspects
            val avgScore = (emotions.coherence + posture.score + intonation.score + expressivite.score) / 4
            
            // Détecter si au moins un aspect est excellent (>85) - signe d'une bonne vidéo
            val hasExcellentAspect = emotions.coherence > 85 || posture.score > 85 || intonation.score > 85 || expressivite.score > 85
            val maxAspectScore = maxOf(emotions.coherence, posture.score, intonation.score, expressivite.score)
            
            // Si Groq n'a pas fourni de score global, utiliser la moyenne
            // Sinon, utiliser le score Groq mais s'assurer qu'il n'est pas trop bas
            val finalGlobalScore = if (globalScoreFromJson == 0) {
                // Si au moins un aspect est excellent, ajuster le score global vers le haut
                val adjustedAvg = if (hasExcellentAspect && avgScore < 85) {
                    Log.d(TAG, "📊 Score moyen ($avgScore) ajusté à ${maxOf(avgScore, maxAspectScore - 5)} car aspect excellent détecté")
                    maxOf(avgScore, maxAspectScore - 5) // Au moins 5 points en dessous du meilleur aspect
                } else {
                    avgScore
                }
                Log.d(TAG, "📊 Score global calculé: $adjustedAvg (Groq n'a pas fourni de score)")
                adjustedAvg
            } else {
                // Si le score Groq est trop bas par rapport à la moyenne, ajuster
                val adjustedScore = when {
                    // Si au moins un aspect est excellent (>85) mais le score global est < 80, ajuster
                    hasExcellentAspect && globalScoreFromJson < 80 -> {
                        val targetScore = maxOf(globalScoreFromJson, maxAspectScore - 5, 85)
                        Log.w(TAG, "⚠️ Score Groq ($globalScoreFromJson) trop bas pour vidéo avec aspect excellent (max: $maxAspectScore), ajustement à $targetScore")
                        targetScore
                    }
                    // Si la moyenne est bonne (>80) mais Groq a donné un score trop bas (<75), utiliser au minimum 85
                    avgScore > 80 && globalScoreFromJson < 75 -> {
                        Log.w(TAG, "⚠️ Score Groq ($globalScoreFromJson) trop bas pour bonnes données (moyenne: $avgScore), ajustement à ${maxOf(avgScore, 85)}")
                        maxOf(avgScore, 85) // Au minimum 85 pour de bonnes données
                    }
                    // Si la moyenne est bonne (>70) mais Groq a donné un score moyen (70-84), augmenter à 85 minimum
                    avgScore > 70 && globalScoreFromJson in 70..84 -> {
                        val targetScore = if (hasExcellentAspect) {
                            maxOf(globalScoreFromJson, maxAspectScore - 5, 85)
                        } else {
                            maxOf(avgScore, 85)
                        }
                        Log.w(TAG, "⚠️ Score Groq ($globalScoreFromJson) trop conservateur pour bonnes données (moyenne: $avgScore), ajustement à $targetScore")
                        targetScore
                    }
                    // Si la moyenne est bonne (>70) mais Groq a donné un score trop bas, utiliser la moyenne
                    avgScore > 70 && globalScoreFromJson < avgScore - 5 -> {
                        Log.w(TAG, "⚠️ Score Groq ($globalScoreFromJson) trop bas, utilisation de la moyenne ($avgScore)")
                        avgScore
                    }
                    else -> {
                        Log.d(TAG, "📊 Score global fourni par Groq: $globalScoreFromJson (moyenne: $avgScore)")
                        globalScoreFromJson
                    }
                }
                adjustedScore
            }
            
            TrainingFeedback(
                globalScore = finalGlobalScore,
                emotions = emotions,
                posture = posture,
                intonation = intonation,
                expressivite = expressivite,
                recommendations = recommendations,
                strengths = strengths,
                summary = json.optString("summary", "N/A")
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de parsing Groq: ${e.message}", e)
            throw Exception("Erreur lors du traitement de la réponse Groq: ${e.message}")
        }
    }
}

