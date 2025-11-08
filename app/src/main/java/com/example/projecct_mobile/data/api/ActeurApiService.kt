package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.ActeurSignupRequest
import com.example.projecct_mobile.data.model.AuthResponse
import com.example.projecct_mobile.data.model.UpdateActeurRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les acteurs
 */
interface ActeurApiService {
    
    /**
     * Récupère le profil de l'acteur connecté
     * GET /acteur/me
     */
    @GET("acteur/me")
    suspend fun getCurrentActeur(): Response<ActeurSignupRequest>
    
    /**
     * Récupère un acteur par son ID
     * GET /acteur/:id
     */
    @GET("acteur/{id}")
    suspend fun getActeurById(
        @Path("id") id: String
    ): Response<ActeurSignupRequest>
    
    /**
     * Met à jour le profil de l'acteur connecté
     * PATCH /acteur/me
     */
    @PATCH("acteur/me")
    suspend fun updateCurrentActeur(
        @Body request: UpdateActeurRequest
    ): Response<ActeurSignupRequest>
    
    /**
     * Met à jour le profil d'un acteur par ID
     * PATCH /acteur/:id
     */
    @PATCH("acteur/{id}")
    suspend fun updateActeur(
        @Path("id") id: String,
        @Body request: UpdateActeurRequest
    ): Response<ActeurSignupRequest>
}

