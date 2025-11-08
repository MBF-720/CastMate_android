package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.LoginRequest
import com.example.projecct_mobile.data.model.ActeurSignupRequest
import com.example.projecct_mobile.data.model.AgenceSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
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
     * Exemple de requête selon l'API :
     * {
     *   "nom": "Dupont",
     *   "prenom": "Jean",
     *   "email": "jean.dupont@example.com",
     *   "motDePasse": "password123",
     *   "tel": "+21612345678",
     *   "age": 25,
     *   "gouvernorat": "Tunis",
     *   "experience": 5,
     *   "cvPdf": "https://example.com/cv.pdf",
     *   "centresInteret": ["Théâtre", "Cinéma", "Télévision"],
     *   "photoProfil": "https://example.com/photo.jpg",
     *   "socialLinks": {
     *     "instagram": "https://instagram.com/acteur",
     *     "youtube": "https://youtube.com/@acteur",
     *     "tiktok": "https://tiktok.com/@acteur"
     *   }
     * }
     */
    @POST("acteur/signup")
    suspend fun signupActeur(
        @Body request: ActeurSignupRequest
    ): Response<AuthResponse>
    
    /**
     * Inscription d'une nouvelle agence
     * POST /agence/signup
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
     *   "description": "Agence spécialisée dans le casting",
     *   "logoUrl": "https://example.com/logo.png",
     *   "documents": "https://example.com/documents.pdf"
     * }
     */
    @POST("agence/signup")
    suspend fun signupAgence(
        @Body request: AgenceSignupRequest
    ): Response<AuthResponse>
}

