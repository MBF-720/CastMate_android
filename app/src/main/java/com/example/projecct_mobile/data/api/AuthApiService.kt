package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.AgenceSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.GoogleLoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST

/**
 * Service API pour l'authentification
 */
interface AuthApiService {
    
    /**
     * Connexion d'un utilisateur (acteur ou agence)
     * POST /auth/login
     * 
     * Exemple de requête :
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     * 
     * Réponse :
     * {
     *   "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "user": { ... }
     * }
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
    
    /**
     * Inscription d'un nouvel acteur
     * POST /acteur/signup
     * 
     * Multipart attendu :
     * - Part "payload": JSON sérialisé du CreateActeurDto
     * - Part "photo": (optionnel) image JPEG/PNG
     * - Part "document": (optionnel) PDF
     */
    @Multipart
    @POST("acteur/signup")
    suspend fun signupActeur(
        @Part("payload") payload: RequestBody,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<AuthResponse>
    
    /**
     * Authentifie un utilisateur via Google Sign-In.
     * POST /auth/google
     */
    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>
    
    /**
     * Inscription d'une nouvelle agence
     * POST /agence/signup
     * 
     * Multipart attendu :
     * - Part "payload": JSON sérialisé du CreateAgenceDto
     * - Part "photo": (optionnel) image JPEG/PNG pour le logo
     * - Part "document": (optionnel) PDF
     * 
     * Exemple de requête selon l'API :
     * {
     *   "nomAgence": "Agence de Casting Tunis",
     *   "responsable": "Mohamed Ben Ali",
     *   "email": "contact@agence-casting.tn",
     *   "motDePasse": "password123",
     *   "tel": "+21612345678",
     *   "gouvernorat": "Tunis",
     *   "siteWeb": "https://www.agence-casting.tn",
     *   "description": "Agence spécialisée dans le casting"
     * }
     */
    @Multipart
    @POST("agence/signup")
    suspend fun signupAgence(
        @Part("payload") payload: RequestBody,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<AuthResponse>
}

