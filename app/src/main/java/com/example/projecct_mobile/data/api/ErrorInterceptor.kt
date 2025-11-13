package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ApiError
import com.example.projecct_mobile.data.model.ApiException
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ErrorInterceptor(
    private val tokenManager: TokenManager,
    private val gson: Gson
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            // Les IOException (y compris les requêtes annulées) sont normales
            // Ne pas les intercepter ici - laisser Retrofit les gérer
            // Les repositories captureront ces exceptions et les convertiront en Result.failure
            android.util.Log.d("ErrorInterceptor", "⚠️ IOException (sera gérée par Retrofit): ${e.message}")
            // Relancer l'IOException originale - Retrofit la gère correctement
            throw e
        }
        
        if (response.isSuccessful) {
            return response
        }
        
        when (response.code) {
            401 -> {
                val requestUrl = request.url.toString()
                
                if (requestUrl.contains("/auth/login") || 
                    requestUrl.contains("/auth/register") ||
                    requestUrl.contains("/acteur/login") ||
                    requestUrl.contains("/agence/login")) {
                    return response
                } else {
                    runBlocking {
                        tokenManager.clearToken()
                    }
                    val error = parseError(response)
                    throw ApiException.UnauthorizedException(error?.message ?: "Token invalide ou expiré")
                }
            }
            
            403 -> {
                // IMPORTANT: Ne pas lancer l'exception ici pour éviter le crash
                // Laisser la réponse être retournée avec le code 403
                // L'erreur sera gérée dans le repository (updateActeur)
                val error = parseError(response)
                val errorMessage = error?.message ?: "Accès refusé"
                android.util.Log.e("ErrorInterceptor", "❌❌❌ ERREUR 403: $errorMessage ❌❌❌")
                android.util.Log.e("ErrorInterceptor", "❌ URL de la requête: ${request.url}")
                android.util.Log.e("ErrorInterceptor", "❌ Méthode: ${request.method}")
                // Ne pas lancer l'exception - laisser la réponse être retournée
                // L'erreur sera gérée dans updateActeur
                return response
            }
            
            404 -> {
                val error = parseError(response)
                throw ApiException.NotFoundException(error?.message ?: "Ressource non trouvée")
            }
            
            409 -> {
                // IMPORTANT: Ne pas lancer l'exception ici pour éviter le crash
                // Laisser la réponse être retournée avec le code 409
                // L'erreur sera gérée dans le repository (applyToCasting)
                val error = parseError(response)
                val errorMessage = error?.message ?: "Conflit : la ressource existe déjà"
                android.util.Log.e("ErrorInterceptor", "❌ Erreur 409 (Conflict): $errorMessage - URL: ${request.url}")
                // Ne pas lancer l'exception - laisser la réponse être retournée
                // L'erreur sera gérée dans le repository
                return response
            }
            
            400 -> {
                val error = parseError(response)
                val errorMessage = buildErrorMessage(error, response)
                // Ne pas lancer l'exception pour éviter le crash - laisser le repository gérer
                android.util.Log.e("ErrorInterceptor", "❌ Erreur 400: $errorMessage - URL: ${request.url}")
                // Retourner la réponse pour que le repository puisse la gérer
                return response
            }
            
            in 500..599 -> {
                val error = parseError(response)
                val errorMessage = error?.message ?: "Erreur serveur"
                // Ne pas lancer l'exception pour éviter le crash - laisser le repository gérer
                android.util.Log.e("ErrorInterceptor", "❌ Erreur serveur (${response.code}): $errorMessage - URL: ${request.url}")
                // Retourner la réponse pour que le repository puisse la gérer
                return response
            }
            
            else -> {
                val error = parseError(response)
                throw ApiException.UnknownException(error?.message ?: "Erreur inconnue: ${response.code}")
            }
        }
    }
    
    private fun parseError(response: Response): ApiError? {
        return try {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            if (errorBody.isNotBlank()) {
                try {
                    val jsonObject = JsonParser.parseString(errorBody).asJsonObject
                    val statusCode = jsonObject.get("statusCode")?.asInt ?: response.code
                    val error = jsonObject.get("error")?.asString
                    
                    val messageValue = jsonObject.get("message")
                    val messageText = when {
                        messageValue?.isJsonArray == true -> {
                            messageValue.asJsonArray.firstOrNull()?.asString ?: "Erreur de validation"
                        }
                        messageValue?.isJsonPrimitive == true -> {
                            messageValue.asString
                        }
                        else -> "Requête invalide"
                    }
                    
                    val details = jsonObject.get("message")?.takeIf { it.isJsonObject }
                        ?.asJsonObject?.entrySet()?.associate { entry ->
                            val value = entry.value
                            val messages = if (value.isJsonArray) {
                                value.asJsonArray.map { it.asString }
                            } else {
                                listOf(value.asString)
                            }
                            entry.key to messages
                        }
                    
                    ApiError(
                        statusCode = statusCode,
                        message = messageText,
                        error = error,
                        details = details
                    )
                } catch (e: Exception) {
                    ApiError(
                        statusCode = response.code,
                        message = errorBody.take(200)
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
    
    private fun buildErrorMessage(error: ApiError?, response: Response): String {
        val baseMessage = error?.message ?: "Requête invalide"
        
        error?.details?.let { details ->
            val detailsText = details.entries.joinToString("\n") { (field, messages) ->
                "$field: ${messages.joinToString(", ")}"
            }
            return "$baseMessage\n\nDétails:\n$detailsText"
        }
        
        return baseMessage
    }
}

