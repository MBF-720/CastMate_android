package com.example.projecct_mobile.data.model

/**
 * Modèles de données pour l'entraînement acteur avec Gemini AI
 */

/**
 * Feedback complet d'analyse d'une vidéo d'entraînement
 */
data class TrainingFeedback(
    val globalScore: Int,
    val emotions: EmotionAnalysis,
    val posture: PostureAnalysis,
    val intonation: IntonationAnalysis,
    val expressivite: ExpressivityAnalysis,
    val recommendations: List<String>,
    val strengths: List<String>,
    val summary: String
)

/**
 * Analyse des émotions exprimées
 */
data class EmotionAnalysis(
    val detected: List<String>,      // Émotions détectées (joie, tristesse, colère, etc.)
    val coherence: Int,               // Score de cohérence (0-100)
    val intensity: Int,               // Intensité émotionnelle (0-100)
    val comment: String               // Commentaire du coach
)

/**
 * Analyse de la posture corporelle
 */
data class PostureAnalysis(
    val score: Int,                   // Score global de posture (0-100)
    val strengths: List<String>,      // Points forts
    val improvements: List<String>,   // Points à améliorer
    val comment: String               // Commentaire du coach
)

/**
 * Analyse de l'intonation vocale
 */
data class IntonationAnalysis(
    val score: Int,                   // Score global d'intonation (0-100)
    val clarity: Int,                 // Clarté de la diction (0-100)
    val rhythm: Int,                  // Rythme vocal (0-100)
    val expressiveness: Int,          // Expressivité vocale (0-100)
    val comment: String               // Commentaire du coach
)

/**
 * Analyse de l'expressivité globale
 */
data class ExpressivityAnalysis(
    val score: Int,                   // Score global d'expressivité (0-100)
    val facialExpressions: String,    // Description des expressions faciales
    val bodyLanguage: String,         // Description du langage corporel
    val comment: String               // Commentaire du coach
)

