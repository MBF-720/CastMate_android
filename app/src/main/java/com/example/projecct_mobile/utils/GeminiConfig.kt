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
    const val GEMINI_API_KEY = "AIzaSyCAqVuPKwQ-vdWFQxA3NOUEMmQ4HPE1D1A"
    
    /**
     * Modèles Gemini disponibles
     */
    const val MODEL_FLASH = "gemini-1.5-flash"  // Rapide et économique
    const val MODEL_PRO = "gemini-1.5-pro"      // Plus performant mais plus cher
    const val MODEL_PRO_2_5 = "gemini-2.5-pro" // Modèle utilisé par le chatbot
    
    /**
     * URL de base pour l'API Gemini
     */
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}

