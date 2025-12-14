package com.example.projecct_mobile.utils

/**
 * Configuration centralisée pour Gemini AI
 * Utilisée pour le chatbot agence et l'entraînement acteur
 */
object GeminiConfig {
    /**
     * Clé API Gemini partagée pour toute l'application
     * Utilisée pour :
     * - Chatbot agence (filtrage d'acteurs)
     * - Entraînement acteur (analyse vidéo)
     */
    const val GEMINI_API_KEY = "AIzaSyBmebtmZxhr44J8CTpdbknXVGxxJkcX5xg"
    
    /**
     * Modèles Gemini disponibles
     */
    const val MODEL_FLASH = "gemini-1.5-flash"    // Rapide et économique
    const val MODEL_PRO = "gemini-1.5-pro"        // Plus performant mais plus cher
    const val MODEL_PRO_2_5 = "gemini-2.5-pro"    // Ancien modèle
    const val MODEL_FLASH_2_5 = "gemini-2.5-flash" // Modèle utilisé (meilleur équilibre)
    
    /**
     * URL de base pour l'API Gemini
     */
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}

