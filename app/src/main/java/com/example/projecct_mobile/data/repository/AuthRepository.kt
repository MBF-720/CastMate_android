package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.AuthApiService
import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.ActeurSignupRequest
import com.example.projecct_mobile.data.model.AgenceSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.SocialLinks
import com.example.projecct_mobile.data.utils.JwtDecoder
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gérer l'authentification
 */
class AuthRepository {
    
    private val authService: AuthApiService = ApiClient.getAuthService()
    private val tokenManager = ApiClient.getTokenManager()
    
    /**
     * Inscription d'un nouvel acteur
     * POST /acteur/signup
     * 
     * @param nom Nom de famille (obligatoire)
     * @param prenom Prénom (obligatoire)
     * @param email Email (obligatoire)
     * @param motDePasse Mot de passe (obligatoire)
     * @param tel Numéro de téléphone (obligatoire)
     * @param age Âge (obligatoire)
     * @param gouvernorat Gouvernorat (obligatoire)
     * @param experience Années d'expérience (obligatoire)
     * @param cvPdf URL du CV PDF (optionnel)
     * @param centresInteret Liste des centres d'intérêt (optionnel)
     * @param photoProfil URL de la photo de profil (optionnel)
     * @param instagram URL Instagram (optionnel)
     * @param youtube URL YouTube (optionnel)
     * @param tiktok URL TikTok (optionnel)
     * 
     * @return Result<AuthResponse> avec le token JWT et les informations utilisateur
     */
    suspend fun signupActeur(
        nom: String,
        prenom: String,
        email: String,
        motDePasse: String,
        tel: String,
        age: Int,
        gouvernorat: String,
        experience: Int,
        cvPdf: String? = null,
        centresInteret: List<String>? = null,
        photoProfil: String? = null,
        instagram: String? = null,
        youtube: String? = null,
        tiktok: String? = null
    ): Result<AuthResponse> {
        return try {
            val socialLinks = if (instagram != null || youtube != null || tiktok != null) {
                SocialLinks(
                    instagram = instagram,
                    youtube = youtube,
                    tiktok = tiktok
                )
            } else {
                null
            }
            
            val request = ActeurSignupRequest(
                nom = nom,
                prenom = prenom,
                email = email,
                motDePasse = motDePasse,
                tel = tel,
                age = age,
                gouvernorat = gouvernorat,
                experience = experience,
                cvPdf = cvPdf,
                centresInteret = centresInteret,
                photoProfil = photoProfil,
                socialLinks = socialLinks
            )
            
            val response = authService.signupActeur(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    // Certains backends ne renvoient pas le token à l'inscription.
                    // On tente alors une connexion immédiate avec les mêmes identifiants.
                    val loginResult = login(request.email, request.motDePasse)
                    return loginResult
                }
                
                // Sauvegarde du token JWT
                tokenManager.saveToken(accessToken)
                
                // Sauvegarde des informations utilisateur (fallback avec décodage du token si nécessaire)
                val fallbackUserId = authResponse.user?.id ?: JwtDecoder.getUserIdFromToken(accessToken)
                val fallbackEmail = authResponse.user?.email ?: request.email
                tokenManager.saveUserInfo(fallbackUserId, fallbackEmail)
                
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    ApiException.BadRequestException(
                        "Erreur lors de l'inscription: ${errorBody ?: response.message()}"
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
     * Inscription d'une nouvelle agence
     * POST /agence/signup
     * 
     * @param nomAgence Nom de l'agence (obligatoire)
     * @param responsable Nom du responsable (obligatoire)
     * @param email Email (obligatoire)
     * @param motDePasse Mot de passe (obligatoire)
     * @param tel Numéro de téléphone (obligatoire)
     * @param gouvernorat Gouvernorat (obligatoire)
     * @param siteWeb Site web (optionnel)
     * @param description Description (optionnel)
     * @param logoUrl URL du logo (optionnel)
     * @param documents URL des documents (optionnel)
     * 
     * @return Result<AuthResponse> avec le token JWT et les informations utilisateur
     */
    suspend fun signupAgence(
        nomAgence: String,
        responsable: String,
        email: String,
        motDePasse: String,
        tel: String,
        gouvernorat: String,
        siteWeb: String? = null,
        description: String? = null,
        logoUrl: String? = null,
        documents: String? = null
    ): Result<AuthResponse> {
        return try {
            val request = AgenceSignupRequest(
                nomAgence = nomAgence,
                responsable = responsable,
                email = email,
                motDePasse = motDePasse,
                tel = tel,
                gouvernorat = gouvernorat,
                siteWeb = siteWeb,
                description = description,
                logoUrl = logoUrl,
                documents = documents
            )
            
            val response = authService.signupAgence(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    // Tentative de connexion si le token n'est pas présent dans la réponse d'inscription
                    val loginResult = login(email, motDePasse)
                    return loginResult
                }
                
                // Sauvegarde du token JWT
                tokenManager.saveToken(accessToken)
                
                val fallbackUserId = authResponse.user?.id ?: JwtDecoder.getUserIdFromToken(accessToken)
                val fallbackEmail = authResponse.user?.email ?: email
                tokenManager.saveUserInfo(fallbackUserId, fallbackEmail)
                
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    ApiException.BadRequestException(
                        "Erreur lors de l'inscription: ${errorBody ?: response.message()}"
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
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    return Result.failure(
                        ApiException.UnknownException("Réponse invalide: token manquant")
                    )
                }
                
                // Sauvegarde du token JWT
                tokenManager.saveToken(accessToken)
                
                // Sauvegarde des informations utilisateur
                val fallbackUserId = authResponse.user?.id ?: JwtDecoder.getUserIdFromToken(accessToken)
                val fallbackEmail = authResponse.user?.email ?: email
                tokenManager.saveUserInfo(fallbackUserId, fallbackEmail)
                
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    ApiException.UnauthorizedException(
                        "Email ou mot de passe incorrect: ${errorBody ?: response.message()}"
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

