package com.example.projecct_mobile.utils

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.projecct_mobile.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Configuration Coil avec authentification JWT pour le chargement d'images.
 */
object CoilConfig {
    private var imageLoader: ImageLoader? = null
    
    /**
     * Intercepteur qui ajoute le token JWT aux requêtes d'images
     * Utilise la même logique que AuthInterceptor pour garantir la cohérence
     */
    private class AuthImageInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Créer un TokenManager avec le context.applicationContext pour garantir la persistance
            val tokenManager = TokenManager(context.applicationContext)
            
            // Utiliser la même logique que AuthInterceptor : vérifier si le token est expiré
            val token = kotlinx.coroutines.runBlocking {
                val isExpired = tokenManager.isTokenExpired()
                if (isExpired) {
                    android.util.Log.w("CoilConfig", "⚠️ Token JWT expiré - ne peut pas charger l'image: ${originalRequest.url}")
                    null
                } else {
                    val tokenValue = tokenManager.getTokenSync()
                    if (tokenValue != null) {
                        android.util.Log.d("CoilConfig", "✅ Token JWT valide pour image: ${originalRequest.url}")
                    }
                    tokenValue
                }
            }
            
            android.util.Log.d("CoilConfig", "🖼️ Requête image: ${originalRequest.url}")
            android.util.Log.d("CoilConfig", "🔑 Token présent: ${token != null}, token length: ${token?.length ?: 0}")
            
            val newRequest = if (token != null && token.isNotBlank()) {
                // Nettoyer le token (comme dans AuthInterceptor)
                val cleanToken = token.trim()
                
                // Vérifier que le token a le bon format (3 parties séparées par des points)
                val tokenParts = cleanToken.split(".")
                if (tokenParts.size != 3) {
                    android.util.Log.e("CoilConfig", "❌ Token JWT invalide: format incorrect (${tokenParts.size} parties au lieu de 3)")
                    originalRequest
                } else {
                    // Décoder le token pour afficher toutes les informations (pour debug)
                    try {
                        val payload = tokenParts[1]
                        val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        val decodedString = String(decodedBytes, Charsets.UTF_8)
                        val jsonObject = org.json.JSONObject(decodedString)
                        
                        // Extraire toutes les informations importantes
                        val id = jsonObject.optString("id", null)
                            ?: jsonObject.optString("userId", null)
                            ?: jsonObject.optString("sub", null)
                            ?: jsonObject.optString("_id", null)
                        val role = jsonObject.optString("role", "N/A")
                        val type = jsonObject.optString("type", "N/A")
                        val email = jsonObject.optString("email", "N/A")
                        
                        android.util.Log.d("CoilConfig", "📋 Token JWT décodé pour requête média:")
                        android.util.Log.d("CoilConfig", "  - id: $id")
                        android.util.Log.d("CoilConfig", "  - role: $role")
                        android.util.Log.d("CoilConfig", "  - type: $type")
                        android.util.Log.d("CoilConfig", "  - email: $email")
                        android.util.Log.d("CoilConfig", "  - Payload complet: $decodedString")
                        
                        // Log toutes les clés disponibles dans le token
                        val keys = jsonObject.keys()
                        val allKeys = mutableListOf<String>()
                        while (keys.hasNext()) {
                            allKeys.add(keys.next())
                        }
                        android.util.Log.d("CoilConfig", "  - Toutes les clés du token: ${allKeys.joinToString(", ")}")
                        
                        if (id.isNullOrBlank()) {
                            android.util.Log.e("CoilConfig", "❌ ERREUR: ID introuvable dans le token JWT !")
                            android.util.Log.e("CoilConfig", "❌ Le backend ne pourra pas extraire requesterId")
                        } else {
                            android.util.Log.d("CoilConfig", "✅ ID trouvé dans le token: $id (le backend devrait pouvoir extraire requesterId)")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("CoilConfig", "⚠️ Impossible de décoder le token: ${e.message}")
                    }
                    
                    // Utiliser removeHeader puis addHeader pour éviter les doublons (comme AuthInterceptor)
                    val requestWithAuth = originalRequest.newBuilder()
                        .removeHeader("Authorization") // Supprimer s'il existe déjà
                        .addHeader("Authorization", "Bearer $cleanToken")
                        .build()
                    
                    android.util.Log.d("CoilConfig", "✅ Header Authorization ajouté pour: ${originalRequest.url}")
                    requestWithAuth
                }
            } else {
                android.util.Log.w("CoilConfig", "⚠️ Pas de token valide pour charger l'image: ${originalRequest.url}")
                originalRequest
            }
            
            // Log des headers de la requête finale
            android.util.Log.d("CoilConfig", "📤 Headers de la requête: ${newRequest.headers.names()}")
            val authHeader = newRequest.header("Authorization")
            if (authHeader != null) {
                android.util.Log.d("CoilConfig", "✅ Authorization header présent: ${authHeader.take(30)}...")
            } else {
                android.util.Log.e("CoilConfig", "❌ Authorization header ABSENT dans la requête!")
            }
            
            val response = chain.proceed(newRequest)
            
            // Log de la réponse pour diagnostic
            if (!response.isSuccessful) {
                android.util.Log.e("CoilConfig", "❌ Erreur ${response.code} pour image: ${originalRequest.url}")
                if (response.code == 403) {
                    android.util.Log.e("CoilConfig", "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle/type dans le token JWT")
                    // Log du body d'erreur
                    try {
                        val errorBody = response.peekBody(1024).string()
                        android.util.Log.e("CoilConfig", "📄 Body d'erreur: $errorBody")
                    } catch (e: Exception) {
                        android.util.Log.w("CoilConfig", "⚠️ Impossible de lire le body d'erreur: ${e.message}")
                    }
                }
            }
            
            return response
        }
    }
    
    /**
     * Crée ou récupère un ImageLoader Coil configuré avec l'authentification JWT (singleton)
     */
    fun createImageLoader(context: Context): ImageLoader {
        if (imageLoader == null) {
            // Utiliser applicationContext pour garantir la persistance
            val appContext = context.applicationContext
            
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthImageInterceptor(appContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            imageLoader = ImageLoader.Builder(appContext)
                .okHttpClient(okHttpClient)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .respectCacheHeaders(false) // Important: ignore les headers de cache pour les images authentifiées
                .logger(DebugLogger()) // Active les logs pour le debug
                .build()
        }
        
        return imageLoader!!
    }
}

