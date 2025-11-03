package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour les erreurs de l'API
 */
data class ApiError(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("details")
    val details: Map<String, List<String>>? = null
)

/**
 * Exceptions personnalisées pour les erreurs HTTP
 */
sealed class ApiException(
    message: String,
    val statusCode: Int? = null
) : Exception(message) {
    
    class UnauthorizedException(message: String = "Token invalide ou expiré") 
        : ApiException(message, 401)
    
    class ForbiddenException(message: String = "Accès refusé") 
        : ApiException(message, 403)
    
    class NotFoundException(message: String = "Ressource non trouvée") 
        : ApiException(message, 404)
    
    class ConflictException(message: String = "Conflit : la ressource existe déjà") 
        : ApiException(message, 409)
    
    class BadRequestException(message: String = "Requête invalide") 
        : ApiException(message, 400)
    
    class ServerException(message: String = "Erreur serveur") 
        : ApiException(message, 500)
    
    class NetworkException(message: String = "Erreur de connexion réseau") 
        : ApiException(message, null)
    
    class UnknownException(message: String = "Erreur inconnue") 
        : ApiException(message, null)
}

