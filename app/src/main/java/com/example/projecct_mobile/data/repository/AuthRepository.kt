package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.AuthApiService
import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.RegisterRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.ApiException
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gérer l'authentification
 */
class AuthRepository {
    
    private val authService: AuthApiService = ApiClient.getAuthService()
    private val tokenManager = ApiClient.getTokenManager()
    
    /**
     * Inscription d'un nouvel utilisateur
     * 
     * @param email Email de l'utilisateur (obligatoire)
     * @param password Mot de passe (obligatoire)
     * @param nom Nom de famille (optionnel)
     * @param prenom Prénom (optionnel)
     * @param bio Biographie (optionnel)
     * @param role Rôle: ACTEUR, RECRUTEUR, ADMIN (optionnel)
     * @param cvUrl URL du CV (optionnel)
     * @param photoProfil URL de la photo de profil (optionnel)
     * 
     * @return Result<AuthResponse> avec le token JWT et les informations utilisateur
     */
    suspend fun register(
        email: String,
        password: String,
        nom: String? = null,
        prenom: String? = null,
        bio: String? = null,
        role: String? = null,
        cvUrl: String? = null,
        photoProfil: String? = null
    ): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(
                nom = nom,
                prenom = prenom,
                email = email,
                password = password,
                role = try {
                    role?.let { com.example.projecct_mobile.data.model.UserRole.valueOf(it) }
                } catch (e: IllegalArgumentException) {
                    null
                },
                bio = bio,
                cvUrl = cvUrl,
                photoProfil = photoProfil
            )
            
            val response = authService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Sauvegarde du token JWT
                tokenManager.saveToken(authResponse.accessToken)
                
                // Sauvegarde des informations utilisateur
                authResponse.user?.let { user ->
                    tokenManager.saveUserInfo(user.id, user.email)
                }
                
                Result.success(authResponse)
            } else {
                Result.failure(
                    ApiException.BadRequestException(
                        "Erreur lors de l'inscription: ${response.message()}"
                    )
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Connexion d'un utilisateur
     * 
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     * 
     * @return Flow<Result<AuthResponse>> avec le token JWT et les informations utilisateur
     * 
     * Exemple d'utilisation :
     * ```kotlin
     * val result = authRepository.login("user@example.com", "password123")
     * result.onSuccess { authResponse ->
     *     // Le token est automatiquement stocké
     *     val token = authResponse.accessToken
     *     val user = authResponse.user
     * }
     * result.onFailure { exception ->
     *     // Gérer l'erreur
     * }
     * ```
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> {
        return try {
            val request = LoginRequest(
                email = email,
                password = password
            )
            
            val response = authService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Sauvegarde du token JWT
                tokenManager.saveToken(authResponse.accessToken)
                
                // Sauvegarde des informations utilisateur
                authResponse.user?.let { user ->
                    tokenManager.saveUserInfo(user.id, user.email)
                }
                
                Result.success(authResponse)
            } else {
                Result.failure(
                    ApiException.UnauthorizedException(
                        "Email ou mot de passe incorrect"
                    )
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Déconnexion de l'utilisateur
     * Supprime le token stocké
     */
    suspend fun logout() {
        tokenManager.clearToken()
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
    
    /**
     * Récupère le token JWT actuel
     */
    fun getToken(): Flow<String?> {
        return tokenManager.token
    }
}

