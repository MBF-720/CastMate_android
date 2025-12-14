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
                            
                            // Essayer de trouver l'ID dans plusieurs champs possibles
                            val userId = jsonObject.optString("id", null)
                                ?: jsonObject.optString("userId", null)
                                ?: jsonObject.optString("sub", null)
                                ?: jsonObject.optString("_id", null)
                                ?: jsonObject.optString("actorId", null)
                                ?: "N/A"
                            
                            val role = jsonObject.optString("role", "N/A")
                            val type = jsonObject.optString("type", "N/A")
                            val email = jsonObject.optString("email", "N/A")
                            
                            android.util.Log.d("AuthInterceptor", "📋 Token info: id=$userId, role=$role, type=$type, email=$email, exp=${jsonObject.optLong("exp", 0)}")
                            
                            // Log toutes les clés disponibles dans le token pour diagnostic
                            val keys = jsonObject.keys()
                            val allKeys = mutableListOf<String>()
                            while (keys.hasNext()) {
                                allKeys.add(keys.next())
                            }
                            android.util.Log.d("AuthInterceptor", "📋 Toutes les clés du token: ${allKeys.joinToString(", ")}")
                            
                            if (userId == "N/A") {
                                android.util.Log.e("AuthInterceptor", "❌ ERREUR: ID introuvable dans le token JWT !")
                                android.util.Log.e("AuthInterceptor", "❌ Payload décodé: $decodedString")
                                android.util.Log.e("AuthInterceptor", "❌ Le backend ne pourra pas extraire l'ID de l'utilisateur")
                            }
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
            // Nettoyer le token (supprimer les espaces avant/après et les retours à la ligne)
            var cleanToken = token.trim()
            
            // Enlever "Bearer " si présent au début du token
            if (cleanToken.startsWith("Bearer ", ignoreCase = true)) {
                cleanToken = cleanToken.substring(7).trim()
            }
            
            // Vérifier que le token n'est pas vide après nettoyage
            if (cleanToken.isBlank()) {
                android.util.Log.e("AuthInterceptor", "❌ Token JWT vide après nettoyage")
                originalRequest
            } else {
                // Vérifier que le token a le bon format (3 parties séparées par des points)
                val tokenParts = cleanToken.split(".")
                if (tokenParts.size != 3) {
                    android.util.Log.e("AuthInterceptor", "❌ Token JWT invalide: format incorrect (${tokenParts.size} parties au lieu de 3)")
                    android.util.Log.e("AuthInterceptor", "❌ Token: ${cleanToken.take(50)}...")
                    originalRequest
                } else {
                    // Vérifier si c'est une requête multipart
                    val contentType = originalRequest.header("Content-Type")
                    val isMultipart = contentType?.contains("multipart", ignoreCase = true) == true || 
                                      originalRequest.body?.contentType()?.toString()?.contains("multipart", ignoreCase = true) == true
                    
                    // Décoder le token pour afficher le rôle et le type
                    try {
                        val payload = tokenParts[1]
                        val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        val decodedString = String(decodedBytes, Charsets.UTF_8)
                        val jsonObject = org.json.JSONObject(decodedString)
                        val role = jsonObject.optString("role", "N/A")
                        val type = jsonObject.optString("type", "N/A")
                        
                        // Vérifier si le type et le rôle correspondent pour créer un casting
                        if (originalRequest.url.encodedPath == "/castings" && originalRequest.method == "POST") {
                            val canCreate = (role == "RECRUTEUR" || role == "ADMIN") && type == "AGENCE"
                            if (!canCreate) {
                                android.util.Log.e("AuthInterceptor", "❌ Rôle ou type invalide pour créer un casting: role=$role, type=$type (requis: role=RECRUTEUR/ADMIN, type=AGENCE)")
                            }
                        }
                    } catch (e: Exception) {
                        // Erreur silencieuse pour le décodage
                    }
                    
                    // Construire le header Authorization avec exactement un espace entre "Bearer" et le token
                    val authHeaderValue = "Bearer $cleanToken"
                    
                    // Pour les requêtes multipart, s'assurer que le header Authorization est bien ajouté
                    // en utilisant removeHeader puis addHeader pour éviter les doublons
                    val requestBuilder = originalRequest.newBuilder()
                        .removeHeader("Authorization") // Supprimer s'il existe déjà
                        .addHeader("Authorization", authHeaderValue) // Ajouter le header
                    
                    val requestWithAuth = requestBuilder.build()
                    
                    // Vérifier que le header a bien été ajouté
                    val authHeader = requestWithAuth.header("Authorization")
                    if (authHeader != null) {
                        android.util.Log.d("AuthInterceptor", "✅ Header Authorization confirmé: ${authHeader.take(30)}...")
                    } else {
                        android.util.Log.e("AuthInterceptor", "❌ Header Authorization manquant dans la requête finale!")
                    }
                    
                    // Log tous les headers pour diagnostic
                    android.util.Log.d("AuthInterceptor", "📋 Tous les headers de la requête:")
                    requestWithAuth.headers.names().forEach { name ->
                        val value = requestWithAuth.header(name)
                        if (name.equals("Authorization", ignoreCase = true)) {
                            android.util.Log.d("AuthInterceptor", "  - $name: ${value?.take(30)}...")
                        } else {
                            android.util.Log.d("AuthInterceptor", "  - $name: $value")
                        }
                    }
                    
                    requestWithAuth
                }
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
            } else if (response.code == 403) {
                // Détecter l'endpoint pour afficher le message approprié
                val requestUrl = originalRequest.url.toString()
                val requestMethod = originalRequest.method
                
                val errorMessage = when {
                    // Candidature à un casting - nécessite ACTEUR
                    requestUrl.contains("/castings/") && requestUrl.contains("/apply") -> {
                        "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (ACTEUR requis pour postuler à un casting)"
                    }
                    // Création de casting - nécessite RECRUTEUR ou ADMIN
                    requestUrl.contains("/castings") && requestMethod == "POST" && !requestUrl.contains("/apply") -> {
                        "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (RECRUTEUR ou ADMIN requis pour créer un casting)"
                    }
                    // Autres endpoints protégés
                    else -> {
                        "⚠️ Erreur 403 (Forbidden) - Vérifier les permissions de l'utilisateur"
                    }
                }
                
                android.util.Log.e("AuthInterceptor", errorMessage)
            }
            
            // Log du body d'erreur pour diagnostic
            try {
                val errorBody = response.peekBody(1024).string()
                android.util.Log.e("AuthInterceptor", "📄 Body d'erreur (premiers 1024 bytes): $errorBody")
            } catch (e: Exception) {
                android.util.Log.w("AuthInterceptor", "⚠️ Impossible de lire le body d'erreur: ${e.message}")
            }
        } else {
            android.util.Log.d("AuthInterceptor", "✅ Réponse ${response.code} pour: ${originalRequest.method} ${originalRequest.url}")
        }
        
        return response
    }
}

