package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.RegisterRequest
import com.example.projecct_mobile.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Service API pour l'authentification
 */
interface AuthApiService {
    
    /**
     * Inscription d'un nouvel utilisateur
     * POST /auth/register
     * 
     * Exemple de requête selon l'API :
     * {
     *   "nom": "Dupont",
     *   "prenom": "Jean",
     *   "email": "jean@example.com",
     *   "password": "password123",
     *   "role": "ACTEUR",
     *   "bio": "Acteur professionnel",
     *   "cvUrl": "https://example.com/cv.pdf",
     *   "photoProfil": "https://example.com/photo.jpg"
     * }
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>
    
    /**
     * Connexion d'un utilisateur
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
}

