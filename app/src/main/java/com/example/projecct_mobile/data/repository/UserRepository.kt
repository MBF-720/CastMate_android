package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.UserApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.User

/**
 * Repository pour gérer les utilisateurs
 * Toutes les routes nécessitent un token JWT
 */
class UserRepository {
    
    private val userService: UserApiService = ApiClient.getUserService()
    
    /**
     * Récupère les informations d'un utilisateur par son ID (route protégée)
     * 
     * @param id ID de l'utilisateur
     * @return Result<User> Les informations de l'utilisateur
     */
    suspend fun getUserById(id: String): Result<User> {
        return try {
            val response = userService.getUserById(id)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    ApiException.NotFoundException("Utilisateur non trouvé")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Récupère le profil de l'utilisateur connecté (route protégée)
     * GET /users/me
     * 
     * @return Result<User> Le profil de l'utilisateur connecté
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = userService.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    ApiException.UnauthorizedException("Utilisateur non authentifié")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
}

