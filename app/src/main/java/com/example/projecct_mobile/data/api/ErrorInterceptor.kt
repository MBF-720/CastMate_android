package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ApiError
import com.example.projecct_mobile.data.model.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Intercepteur pour gérer les erreurs HTTP et les convertir en exceptions
 */
class ErrorInterceptor(
    private val tokenManager: TokenManager,
    private val gson: Gson
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            throw ApiException.NetworkException("Erreur de connexion réseau: ${e.message}")
        }
        
        // Si la réponse est réussie, on la retourne telle quelle
        if (response.isSuccessful) {
            return response
        }
        
        // Gestion des erreurs HTTP
        when (response.code) {
            401 -> {
                // Token expiré ou invalide - on supprime le token stocké
                runBlocking {
                    tokenManager.clearToken()
                }
                val error = parseError(response)
                throw ApiException.UnauthorizedException(error?.message ?: "Token invalide ou expiré")
            }
            
            403 -> {
                val error = parseError(response)
                throw ApiException.ForbiddenException(error?.message ?: "Accès refusé")
            }
            
            404 -> {
                val error = parseError(response)
                throw ApiException.NotFoundException(error?.message ?: "Ressource non trouvée")
            }
            
            409 -> {
                val error = parseError(response)
                throw ApiException.ConflictException(error?.message ?: "Conflit : la ressource existe déjà")
            }
            
            400 -> {
                val error = parseError(response)
                val errorMessage = buildErrorMessage(error, response)
                throw ApiException.BadRequestException(errorMessage)
            }
            
            in 500..599 -> {
                val error = parseError(response)
                throw ApiException.ServerException(error?.message ?: "Erreur serveur")
            }
            
            else -> {
                val error = parseError(response)
                throw ApiException.UnknownException(error?.message ?: "Erreur inconnue: ${response.code}")
            }
        }
    }
    
    /**
     * Parse le corps de la réponse d'erreur en ApiError
     * Utilise peekBody pour ne pas consommer le body
     */
    private fun parseError(response: Response): ApiError? {
        return try {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            if (errorBody.isNotBlank()) {
                try {
                    gson.fromJson(errorBody, ApiError::class.java)
                } catch (e: Exception) {
                    // Si le parsing échoue, créer un ApiError avec le body brut
                    ApiError(
                        statusCode = response.code,
                        message = errorBody.take(200) // Limiter à 200 caractères
                    )
                }
            } else {
                ApiError(
                    statusCode = response.code,
                    message = "Erreur ${response.code}"
                )
            }
        } catch (e: Exception) {
            ApiError(
                statusCode = response.code,
                message = "Erreur ${response.code}: ${e.message}"
            )
        }
    }
    
    /**
     * Construit un message d'erreur détaillé
     */
    private fun buildErrorMessage(error: ApiError?, response: Response): String {
        val baseMessage = error?.message ?: "Requête invalide"
        
        // Ajouter les détails de validation s'ils existent
        error?.details?.let { details ->
            val detailsText = details.entries.joinToString("\n") { (field, messages) ->
                "$field: ${messages.joinToString(", ")}"
            }
            return "$baseMessage\n\nDétails:\n$detailsText"
        }
        
        return baseMessage
    }
}

