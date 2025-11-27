package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Intercepteur pour ajouter automatiquement le token JWT dans les requêtes
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        
        android.util.Log.d("AuthInterceptor", "🌐 Intercepteur appelé pour: ${originalRequest.method} ${originalRequest.url}")
        
        // Liste des routes publiques qui ne nécessitent pas de token
        val publicRoutes = listOf(
            "/auth/login",
            "/acteur/signup",
            "/agence/signup"
        )
        
        // Routes publiques avec méthodes spécifiques
        val publicRoutesWithMethods = mapOf(
            "/castings" to listOf("GET") // Seul GET /castings (liste) est public
        )
        
        val requestPath = originalRequest.url.encodedPath
        val requestMethod = originalRequest.method
        
        android.util.Log.d("AuthInterceptor", "📍 Path: $requestPath, Method: $requestMethod")
        
        // Vérifie si la route est publique (sans restriction de méthode)
        val isPublicRoute = publicRoutes.any { route ->
            requestPath.startsWith(route)
        }
        
        // Vérifie si la route est publique avec restriction de méthode
        // IMPORTANT: /castings (exact) est public, mais /castings/{id}/... est protégé
        val isPublicRouteWithMethod = publicRoutesWithMethods.any { (route, methods) ->
            if (route == "/castings" && requestMethod in methods) {
                // Vérifier que ce n'est PAS une sous-route (doit être exactement /castings ou /castings/)
                requestPath == "/castings" || requestPath == "/castings/"
            } else {
                requestPath.startsWith(route) && requestMethod in methods
            }
        }
        
        android.util.Log.d("AuthInterceptor", "🔓 Route publique? isPublicRoute=$isPublicRoute, isPublicRouteWithMethod=$isPublicRouteWithMethod")
        
        // Si la route est publique, on ne modifie pas la requête
        if (isPublicRoute || isPublicRouteWithMethod) {
            android.util.Log.d("AuthInterceptor", "✅ Route publique détectée - pas de token nécessaire")
            return chain.proceed(originalRequest)
        }
        
        // Pour les routes protégées, on ajoute le token
        val token = runBlocking {
            android.util.Log.d("AuthInterceptor", "🔍 Vérification du token pour: ${originalRequest.method} ${originalRequest.url}")
            
            // Vérifier si le token est expiré avant de l'utiliser
            val isExpired = tokenManager.isTokenExpired()
            android.util.Log.d("AuthInterceptor", "🔍 Token expiré? $isExpired")
            
            if (isExpired) {
                android.util.Log.w("AuthInterceptor", "⚠️ Token JWT expiré - suppression automatique")
                android.util.Log.w("AuthInterceptor", "⚠️ Requête bloquée: ${originalRequest.method} ${originalRequest.url}")
                null
            } else {
                val tokenValue = tokenManager.getTokenSync()
                if (tokenValue != null) {
                    android.util.Log.d("AuthInterceptor", "✅ Token JWT valide et présent (${tokenValue.length} caractères)")
                    // Log les premières informations du token pour diagnostic
                    try {
                        val parts = tokenValue.split(".")
                        if (parts.size == 3) {
                            val payload = parts[1]
                            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                            val decodedString = String(decodedBytes, Charsets.UTF_8)
                            val jsonObject = org.json.JSONObject(decodedString)
                            android.util.Log.d("AuthInterceptor", "📋 Token info: id=${jsonObject.optString("id", "N/A")}, role=${jsonObject.optString("role", "N/A")}, exp=${jsonObject.optLong("exp", 0)}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("AuthInterceptor", "⚠️ Impossible de décoder le token pour les logs: ${e.message}")
                    }
                } else {
                    android.util.Log.w("AuthInterceptor", "⚠️ Token JWT null après vérification")
                }
                tokenValue
            }
        }
        
        val newRequest: Request = if (token != null && token.isNotBlank()) {
            // Nettoyer le token (supprimer les espaces avant/après)
            val cleanToken = token.trim()
            
            // Vérifier que le token a le bon format (3 parties séparées par des points)
            val tokenParts = cleanToken.split(".")
            if (tokenParts.size != 3) {
                android.util.Log.e("AuthInterceptor", "❌ Token JWT invalide: format incorrect (${tokenParts.size} parties au lieu de 3)")
                android.util.Log.e("AuthInterceptor", "❌ Token: ${cleanToken.take(50)}...")
                originalRequest
            } else {
                // Logs pour diagnostic
                android.util.Log.d("AuthInterceptor", "✅ Token JWT présent et valide: ${cleanToken.take(20)}...")
                android.util.Log.d("AuthInterceptor", "📤 Envoi requête: ${originalRequest.method} ${originalRequest.url}")
                android.util.Log.d("AuthInterceptor", "📤 Header Authorization: Bearer ${cleanToken.take(20)}...")
                
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $cleanToken")
                    .build()
            }
        } else {
            android.util.Log.e("AuthInterceptor", "❌ Token JWT manquant ou expiré pour: ${originalRequest.method} ${originalRequest.url}")
            originalRequest
        }
        
        val response = chain.proceed(newRequest)
        
        // Log de la réponse pour diagnostic
        if (!response.isSuccessful) {
            android.util.Log.e("AuthInterceptor", "❌ Erreur ${response.code} pour: ${originalRequest.method} ${originalRequest.url}")
            if (response.code == 401) {
                android.util.Log.e("AuthInterceptor", "⚠️ Erreur 401 (Unauthorized) - Vérifier le token JWT")
            }
        } else {
            android.util.Log.d("AuthInterceptor", "✅ Réponse ${response.code} pour: ${originalRequest.method} ${originalRequest.url}")
        }
        
        return response
    }
}

