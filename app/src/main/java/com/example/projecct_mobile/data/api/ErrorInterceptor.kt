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
            if (e.message?.contains("Canceled", ignoreCase = true) == true || 
                e.message?.contains("canceled", ignoreCase = true) == true) {
                throw ApiException.CanceledException("Requête annulée")
            }
            throw ApiException.NetworkException("Erreur de connexion réseau: ${e.message}")
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

