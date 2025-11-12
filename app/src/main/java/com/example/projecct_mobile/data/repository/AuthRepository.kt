package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.AuthApiService
import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.ActeurSignupRequest
import com.example.projecct_mobile.data.model.AgenceSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.SocialLinks
import com.example.projecct_mobile.data.model.GoogleLoginRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLConnection

/**
 * Repository pour gérer l'authentification
 */
class AuthRepository {
    
    private val authService: AuthApiService = ApiClient.getAuthService()
    private val tokenManager = ApiClient.getTokenManager()
    
    private val gson: Gson = Gson()

    /**
     * Inscription d'un nouvel acteur via multipart.
     *
     * @param photoFile Fichier image JPEG/PNG optionnel pour la photo de profil.
     * @param documentFile Fichier PDF optionnel pour le CV.
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
        centresInteret: List<String>? = null,
        photoFile: File? = null,
        documentFile: File? = null,
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
                centresInteret = centresInteret,
                socialLinks = socialLinks
            )

            val payloadJson = gson.toJson(request)
            val payloadBody = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val photoPart = photoFile?.let { createFilePart("photo", it) }
            val documentPart = documentFile?.let { createFilePart("document", it, "application/pdf") }

            val response = authService.signupActeur(payloadBody, photoPart, documentPart)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    val loginResult = login(request.email, request.motDePasse, expectedRole = "ACTEUR")
                    return loginResult
                }
                
                tokenManager.saveToken(accessToken)
                
                val fallbackUserId = authResponse.user?.actualId ?: tokenManager.getUserIdSync()
                val fallbackEmail = authResponse.user?.email ?: request.email
                val fallbackRole = authResponse.user?.role?.name ?: "ACTEUR"
                tokenManager.saveUserInfo(
                    fallbackUserId,
                    fallbackEmail,
                    fallbackRole,
                    authResponse.user?.nom ?: nom,
                    authResponse.user?.prenom ?: prenom,
                    tel,
                    authResponse.user?.bio
                )
                
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
            
            // Appel HTTP vers l'API NestJS (POST /agence/signup)
            val response = authService.signupAgence(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    // Si le backend ne renvoie pas de token, on relance un login classique agence
                    val loginResult = login(email, motDePasse, expectedRole = "RECRUTEUR")
                    return loginResult
                }
                
                tokenManager.saveToken(accessToken)
                
                val fallbackUserId = authResponse.user?.actualId ?: tokenManager.getUserIdSync()
                val fallbackEmail = authResponse.user?.email ?: email
                val fallbackRole = authResponse.user?.role?.name ?: "RECRUTEUR"
                tokenManager.saveUserInfo(
                    fallbackUserId,
                    fallbackEmail,
                    fallbackRole,
                    authResponse.user?.nom ?: nomAgence,
                    authResponse.user?.prenom ?: responsable,
                    tel,
                    authResponse.user?.bio ?: description
                )
                
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
     * Connexion d'un utilisateur via Google Sign-In.
     * @param idToken Jeton renvoyé par Google contenant l'identité de l'utilisateur.
     * @return Result<AuthResponse> succès si l'utilisateur existe déjà, sinon une erreur.
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val request = GoogleLoginRequest(idToken = idToken)
            val response = authService.loginWithGoogle(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    return Result.failure(
                        ApiException.UnknownException("Réponse invalide: token manquant")
                    )
                }
                
                tokenManager.saveToken(accessToken)
                
                val fallbackUserId = authResponse.user?.actualId ?: tokenManager.getUserIdSync()
                val fallbackEmail = authResponse.user?.email
                val fallbackRole = authResponse.user?.role?.name
                tokenManager.saveUserInfo(
                    fallbackUserId,
                    fallbackEmail,
                    fallbackRole,
                    authResponse.user?.nom,
                    authResponse.user?.prenom,
                    null,
                    authResponse.user?.bio
                )
                
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                when (response.code()) {
                    404 -> Result.failure(
                        ApiException.NotFoundException(
                            errorBody ?: "Compte Google non trouvé"
                        )
                    )
                    401 -> Result.failure(
                        ApiException.UnauthorizedException(
                            errorBody ?: "Authentification Google refusée"
                        )
                    )
                    else -> Result.failure(
                        ApiException.BadRequestException(
                            errorBody ?: response.message()
                        )
                    )
                }
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur Google: ${e.message}"))
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
        password: String,
        expectedRole: String? = null
    ): Result<AuthResponse> {
        return try {
            val request = LoginRequest(
                email = email,
                password = password
            )
            
            // Appel HTTP vers POST /auth/login
            val response = authService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val accessToken = authResponse.accessToken
                
                if (accessToken.isNullOrBlank()) {
                    return Result.failure(
                        ApiException.UnknownException("Réponse invalide: token manquant")
                    )
                }
                
                tokenManager.saveToken(accessToken)
                
                val fallbackUserId = authResponse.user?.actualId ?: tokenManager.getUserIdSync()
                val fallbackEmail = authResponse.user?.email ?: email
                val fallbackRole = authResponse.user?.role?.name ?: expectedRole
                tokenManager.saveUserInfo(
                    fallbackUserId,
                    fallbackEmail,
                    fallbackRole,
                    authResponse.user?.nom,
                    authResponse.user?.prenom,
                    null,
                    authResponse.user?.bio
                )
                
                Result.success(authResponse)
            } else {
                // Gérer les erreurs HTTP
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    401 -> "Email ou mot de passe incorrect"
                    404 -> "Utilisateur non trouvé dans la base de données"
                    400 -> errorBody ?: "Vérifiez vos informations"
                    else -> errorBody ?: response.message() ?: "Erreur de connexion"
                }
                
                Result.failure(ApiException.UnauthorizedException(errorMessage))
            }
        } catch (e: ApiException.UnauthorizedException) {
            // Erreur 401 : identifiants invalides ou utilisateur non trouvé
            Result.failure(e)
        } catch (e: ApiException.NotFoundException) {
            // Erreur 404 : utilisateur non trouvé
            Result.failure(ApiException.UnauthorizedException("Utilisateur non trouvé dans la base de données"))
        } catch (e: ApiException.NetworkException) {
            // Erreur réseau
            Result.failure(e)
        } catch (e: ApiException) {
            // Autres erreurs API
            Result.failure(e)
        } catch (e: Exception) {
            // Erreur inconnue
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

    private fun createFilePart(
        fieldName: String,
        file: File,
        forcedMimeType: String? = null
    ): MultipartBody.Part {
        val mimeType = forcedMimeType ?: guessMimeType(file) ?: "application/octet-stream"
        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, body)
    }

    private fun guessMimeType(file: File): String? {
        return URLConnection.guessContentTypeFromName(file.name)
    }
}

