package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.UserApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.model.UserRole

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
     * Récupère le profil de l'utilisateur connecté sans dépendre d'une route /users/me absente.
     */
    suspend fun getCurrentUser(): Result<User> {
        val tokenManager = ApiClient.getTokenManager()
        val storedId = runCatching { tokenManager.getUserIdSync() }.getOrNull()
        val storedEmail = runCatching { tokenManager.getUserEmailSync() }.getOrNull()
        val storedRole = runCatching { tokenManager.getUserRoleSync() }.getOrNull()
        var storedNom = runCatching { tokenManager.getUserNomSync() }.getOrNull()
        var storedResponsable = runCatching { tokenManager.getUserResponsableSync() }.getOrNull()
        var storedPhone = runCatching { tokenManager.getUserPhoneSync() }.getOrNull()
        var storedDescription = runCatching { tokenManager.getUserDescriptionSync() }.getOrNull()
        val roleEnum = storedRole?.let { roleValue ->
            runCatching { UserRole.valueOf(roleValue.uppercase()) }.getOrNull()
        }

        val cached = storedEmail?.let { runCatching { tokenManager.getAgencyProfileCache(it) }.getOrNull() }
        if (cached != null) {
            if (storedNom.isNullOrBlank()) storedNom = cached.nom
            if (storedResponsable.isNullOrBlank()) storedResponsable = cached.responsable
            if (storedDescription.isNullOrBlank()) storedDescription = cached.description
            if (storedPhone.isNullOrBlank()) storedPhone = cached.phone
        }

        fun buildLocalProfile(): User? {
            val email = storedEmail ?: return null
            return User(
                id = storedId,
                nom = storedNom,
                prenom = storedResponsable,
                email = email,
                role = roleEnum,
                bio = storedDescription
            )
        }

        buildLocalProfile()?.let { local ->
            if (roleEnum == UserRole.RECRUTEUR) {
                return Result.success(local)
            }
        }

        if (!storedId.isNullOrBlank()) {
            val result = getUserById(storedId)
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    tokenManager.saveUserInfo(
                        user.actualId,
                        user.email,
                        user.role?.name,
                        user.nom,
                        user.prenom,
                        storedPhone,
                        user.bio
                    )
                }
                return result
            } else {
                val local = buildLocalProfile()
                if (local != null) {
                    return Result.success(local)
                }
                return result
            }
        }

        buildLocalProfile()?.let { return Result.success(it) }

        return Result.failure(
            ApiException.NotFoundException("Profil utilisateur indisponible")
        )
    }
}

