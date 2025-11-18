package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.AgenceSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.GoogleLoginRequest
import com.example.projecct_mobile.data.model.ForgotPasswordRequest
import com.example.projecct_mobile.data.model.ForgotPasswordResponse
import com.example.projecct_mobile.data.model.ResetPasswordRequest
import com.example.projecct_mobile.data.model.ResetPasswordResponse
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
     * Inscription d'une agence (Public)
     * 
     * Méthode: POST
     * URL: /agence/signup
     * Auth: Non requise
     * Content-Type: multipart/form-data
     * 
     * Body (multipart/form-data):
     * - payload (string JSON): Données de l'agence
     * - photo (file, optionnel): Logo JPEG/PNG (max 10 Mo)
     * - document (file, optionnel): PDF (max 10 Mo)
     * 
     * Exemple payload JSON:
     * {
     *   "nomAgence": "Agence de Casting Tunis",
     *   "responsable": "Mohamed Ben Ali",
     *   "email": "contact@agence-casting.tn",
     *   "motDePasse": "password123",
     *   "tel": "+21612345678",
     *   "gouvernorat": "Tunis",
     *   "siteWeb": "https://agence-casting.tn",
     *   "description": "Agence spécialisée dans le casting.",
     *   "socialLinks": {
     *     "facebook": "https://facebook.com/agence-casting-tunis",
     *     "instagram": "https://instagram.com/agence_casting_tunis"
     *   }
     * }
     * 
     * Réponse 201:
     * {
     *   "id": "507f1f77bcf86cd799439011",
     *   "nomAgence": "Agence de Casting Tunis",
     *   "responsable": "Mohamed Ben Ali",
     *   "email": "contact@agence-casting.tn",
     *   "role": "RECRUTEUR",
     *   "media": {
     *     "photoFileId": "65b9f1f77bcf86cd799439031",
     *     "photoMimeType": "image/png",
     *     "documentFileId": "65b9f1f77bcf86cd799439045",
     *     "documentMimeType": "application/pdf"
     *   },
     *   "socialLinks": { ... }
     * }
     */
    @Multipart
    @POST("agence/signup")
    suspend fun signupAgence(
        @Part("payload") payload: RequestBody,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<AuthResponse>
    
    /**
     * Demande de réinitialisation de mot de passe
     * POST /auth/forgot-password
     * 
     * Body:
     * {
     *   "email": "user@example.com",
     *   "userType": "ACTEUR" | "RECRUTEUR"
     * }
     * 
     * Réponse 200:
     * {
     *   "success": true,
     *   "message": "Reset email sent",
     *   "token": "hex-64-chars",  // Optionnel
     *   "link": "castmate://reset-password?token=...&email=...&type=...",  // Optionnel
     *   "expiresIn": 3600  // Optionnel
     * }
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>
    
    /**
     * Appliquer la réinitialisation de mot de passe
     * POST /auth/reset-password
     * 
     * Body:
     * {
     *   "token": "hex-64-chars",
     *   "newPassword": "NewPass!23",
     *   "email": "user@example.com"
     * }
     * 
     * Réponse 200:
     * {
     *   "success": true,
     *   "message": "Password updated"
     * }
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>
}

