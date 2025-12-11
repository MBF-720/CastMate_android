package com.example.projecct_mobile.utils

import android.content.Context

/**
 * Configuration centralisée pour Groq AI (100% gratuit)
 * Utilisée pour le chatbot agence et l'entraînement acteur
 * 
 * La clé API est chargée depuis le fichier .env dans app/src/main/assets/
 */
object GroqConfig {
    private var cachedApiKey: String? = null
    private var context: Context? = null
    
    /**
     * Initialise le chargement du fichier .env depuis les assets
     * Doit être appelé au démarrage de l'application (dans Application.onCreate ou MainActivity)
     */
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        loadApiKey()
    }
    
    /**
     * Charge la clé API depuis le fichier .env dans assets/
     */
    private fun loadApiKey() {
        val ctx = context ?: return
        
        try {
            val inputStream = ctx.assets.open(".env")
            val content = inputStream.bufferedReader().use { it.readText() }
            
            val lines = content.lines()
            for (line in lines) {
                val trimmedLine = line.trim()
                // Ignorer les commentaires et lignes vides
                if (trimmedLine.isBlank() || trimmedLine.startsWith("#")) {
                    continue
                }
                
                // Parser KEY=VALUE
                val parts = trimmedLine.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    
                    if (key == "GROQ_API_KEY") {
                        cachedApiKey = value
                        android.util.Log.d("GroqConfig", "✅ Clé API Groq chargée depuis .env")
                        return
                    }
                }
            }
            
            throw IllegalStateException("GROQ_API_KEY non trouvée dans le fichier .env")
        } catch (e: Exception) {
            android.util.Log.e("GroqConfig", "❌ Erreur lors du chargement de .env: ${e.message}")
            throw IllegalStateException(
                "Impossible de charger GROQ_API_KEY depuis .env. " +
                "Vérifiez que le fichier app/src/main/assets/.env existe et contient GROQ_API_KEY=...",
                e
            )
        }
    }
    
    /**
     * Clé API Groq partagée pour toute l'application
     * Utilisée pour :
     * - Chatbot agence (filtrage d'acteurs)
     * - Entraînement acteur (analyse vidéo)
     * 
     * @throws IllegalStateException si initialize() n'a pas été appelé ou si la clé est absente
     */
    val GROQ_API_KEY: String
        get() {
            if (cachedApiKey == null) {
                // Essayer de charger si pas encore fait
                if (context != null) {
                    loadApiKey()
                } else {
                    throw IllegalStateException(
                        "GROQ_API_KEY non initialisée. " +
                        "Appelez GroqConfig.initialize(context) dans votre Application.onCreate() ou MainActivity.onCreate()"
                    )
                }
            }
            
            val key = cachedApiKey
            if (key.isNullOrBlank()) {
                throw IllegalStateException("GROQ_API_KEY est vide dans le fichier .env")
            }
            
            return key
        }
    
    /**
     * Modèles Groq disponibles
     */
    const val MODEL_LLAMA_3_8B = "llama-3.1-8b-instant"      // Rapide et efficace
    const val MODEL_LLAMA_3_70B = "llama-3.1-70b-versatile"  // Plus performant
    const val MODEL_MIXTRAL = "mixtral-8x7b-32768"           // Grande capacité
    const val MODEL_GEMMA = "gemma-7b-it"                    // Modèle Google
    
    /**
     * Modèle par défaut utilisé (utiliser 8b-instant car plus fiable)
     */
    const val DEFAULT_MODEL = MODEL_LLAMA_3_8B
    
    /**
     * URL de base pour l'API Groq
     */
    const val BASE_URL = "https://api.groq.com/openai/v1/"
}

