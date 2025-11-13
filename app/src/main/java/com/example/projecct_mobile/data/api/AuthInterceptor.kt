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
        
        // Liste des routes publiques qui ne nécessitent pas de token
        val publicRoutes = listOf(
            "/auth/login",
            "/acteur/signup",
            "/agence/signup"
        )
        
        // Routes publiques avec méthodes spécifiques
        val publicRoutesWithMethods = mapOf(
            "/castings" to listOf("GET") // Seul GET /castings est public
        )
        
        val requestPath = originalRequest.url.encodedPath
        val requestMethod = originalRequest.method
        
        // Vérifie si la route est publique (sans restriction de méthode)
        val isPublicRoute = publicRoutes.any { route ->
            requestPath.startsWith(route)
        }
        
        // Vérifie si la route est publique avec restriction de méthode
        val isPublicRouteWithMethod = publicRoutesWithMethods.any { (route, methods) ->
            requestPath.startsWith(route) && requestMethod in methods
        }
        
        // Si la route est publique, on ne modifie pas la requête
        if (isPublicRoute || isPublicRouteWithMethod) {
            return chain.proceed(originalRequest)
        }
        
        // Pour les routes protégées, on ajoute le token
        val token = runBlocking {
            tokenManager.getTokenSync()
        }
        
        val newRequest: Request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

