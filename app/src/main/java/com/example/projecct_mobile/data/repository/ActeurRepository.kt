package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.ActeurApiService
import com.example.projecct_mobile.data.model.ActeurSignupRequest
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.UpdateActeurRequest
import com.example.projecct_mobile.data.local.TokenManager

/**
 * Repository pour gérer les acteurs
 */
class ActeurRepository {
    
    private val acteurService: ActeurApiService = ApiClient.getActeurService()
    private val tokenManager: TokenManager = ApiClient.getTokenManager()
    
    /**
     * Récupère le profil de l'acteur connecté
     * Utilise l'ID depuis le token JWT pour appeler /acteur/:id
     */
    suspend fun getCurrentActeur(): Result<ActeurSignupRequest> {
        return try {
            val userId = getStoredActeurId()
            
            if (userId != null) {
                // Essayer d'obtenir le profil avec l'ID
                getActeurById(userId)
            } else {
                // Si on ne peut pas obtenir l'ID, retourner une erreur gracieuse
                Result.failure(
                    ApiException.UnauthorizedException("Impossible de récupérer l'ID de l'acteur")
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Erreur lors de la récupération du profil: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de la récupération du profil: ${e.message}"))
        }
    }
    
    /**
     * Extrait l'ID de l'acteur depuis le token JWT
     */
    private suspend fun getStoredActeurId(): String? {
        val storedId = tokenManager.getUserIdSync()
        if (!storedId.isNullOrBlank()) {
            return storedId
        }
        return null
    }
    
    /**
     * Récupère un acteur par son ID
     */
    suspend fun getActeurById(id: String): Result<ActeurSignupRequest> {
        return try {
            val response = acteurService.getActeurById(id)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Si l'ID est invalide, retourner une erreur gracieuse
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody?.contains("ID invalide") == true || response.code() == 400) {
                    "L'ID fourni n'est pas valide pour récupérer le profil acteur"
                } else {
                    "Acteur non trouvé"
                }
                Result.failure(
                    ApiException.NotFoundException(errorMessage)
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            // Si c'est une erreur BadRequest avec "ID invalide", retourner une erreur gracieuse
            if (e is ApiException.BadRequestException && e.message?.contains("ID invalide") == true) {
                Result.failure(
                    ApiException.NotFoundException("L'ID fourni n'est pas valide pour récupérer le profil acteur")
                )
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Erreur getActeurById: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil de l'acteur connecté
     * Utilise l'ID depuis le token JWT pour appeler /acteur/:id
     */
    suspend fun updateCurrentActeur(request: UpdateActeurRequest): Result<ActeurSignupRequest> {
        return try {
            // Ne pas appeler /acteur/me car il n'existe pas
            // Utiliser directement l'ID depuis le token
            val userId = getStoredActeurId()
            
            if (userId != null) {
                // Mettre à jour le profil avec l'ID
                updateActeur(userId, request)
            } else {
                // Si on ne peut pas obtenir l'ID, retourner une erreur gracieuse
                Result.failure(
                    ApiException.UnauthorizedException("Impossible de récupérer l'ID de l'acteur pour la mise à jour")
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Erreur lors de la mise à jour: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de la mise à jour: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil d'un acteur par ID
     */
    suspend fun updateActeur(id: String, request: UpdateActeurRequest): Result<ActeurSignupRequest> {
        return try {
            val response = acteurService.updateActeur(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    ApiException.BadRequestException(
                        errorBody ?: "Erreur lors de la mise à jour"
                    )
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
}

