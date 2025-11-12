package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.Casting
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les castings
 */
interface CastingApiService {
    
    /**
     * Récupère tous les castings (route publique)
     * GET /castings
     */
    @GET("castings")
    suspend fun getAllCastings(): Response<List<Casting>>
    
    /**
     * Récupère un casting par son ID (route publique)
     * GET /castings/:id
     */
    @GET("castings/{id}")
    suspend fun getCastingById(
        @Path("id") id: String
    ): Response<Casting>
    
    /**
     * Crée un nouveau casting (route protégée)
     * POST /castings
     * Nécessite un token JWT dans le header Authorization
     * 
     * Exemple de requête selon l'API :
     * {
     *   "titre": "Recherche acteur principal",
     *   "descriptionRole": "Rôle de protagoniste dans une série",
     *   "synopsis": "Synopsis du projet...",
     *   "lieu": "Paris",
     *   "dateDebut": "2024-01-15",
     *   "dateFin": "2024-02-15",
     *   "remuneration": "1000€/jour",
     *   "conditions": "Disponibilité totale requise"
     * }
     */
    @Multipart
    @POST("castings")
    suspend fun createCasting(
        @Part("payload") payload: RequestBody,
        @Part affiche: MultipartBody.Part? = null
    ): Response<Casting>
    
    /**
     * Met à jour un casting (route protégée)
     * PATCH /castings/:id
     * Nécessite un token JWT
     */
    @Multipart
    @PATCH("castings/{id}")
    suspend fun updateCasting(
        @Path("id") id: String,
        @Part("payload") payload: RequestBody,
        @Part affiche: MultipartBody.Part? = null
    ): Response<Casting>
    
    /**
     * Supprime un casting (route protégée)
     * DELETE /castings/:id
     * Nécessite un token JWT
     */
    @DELETE("castings/{id}")
    suspend fun deleteCasting(
        @Path("id") id: String
    ): Response<Unit>
    
    /**
     * Postuler à un casting (route protégée - Acteur uniquement)
     * POST /castings/:id/apply
     * Nécessite un token JWT
     */
    @POST("castings/{id}/apply")
    suspend fun applyToCasting(
        @Path("id") id: String
    ): Response<Unit>
}

