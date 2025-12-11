package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.UpdateActeurRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Streaming

/**
 * Service API pour les acteurs
 */
@JvmSuppressWildcards
interface ActeurApiService {
    
    /**
     * Récupère le profil de l'acteur connecté
     * GET /acteur/me
     */
    @GET("acteur/me")
    suspend fun getCurrentActeur(): Response<ActeurProfile>
    
    /**
     * Récupère un acteur par son ID
     * GET /acteur/:id
     */
    @GET("acteur/{id}")
    suspend fun getActeurById(
        @Path("id") id: String
    ): Response<ActeurProfile>
    
    /**
     * Met à jour le profil de l'acteur connecté
     * PATCH /acteur/me
     */
    @PATCH("acteur/me")
    suspend fun updateCurrentActeur(
        @Body request: UpdateActeurRequest
    ): Response<ActeurProfile>
    
    /**
     * Met à jour le profil d'un acteur par ID
     * PATCH /acteur/:id
     */
    @PATCH("acteur/{id}")
    suspend fun updateActeur(
        @Path("id") id: String,
        @Body request: UpdateActeurRequest
    ): Response<ActeurProfile>

    /**
     * Met à jour la photo de profil et/ou le document (CV) d'un acteur.
     * PATCH /acteur/{id}/media/profile
     */
    @Multipart
    @PATCH("acteur/{id}/media/profile")
    suspend fun updateProfileMedia(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<ActeurProfile>

    /**
     * Ajoute une ou plusieurs photos à la galerie d'un acteur.
     * POST /acteur/{id}/media/gallery
     */
    @Multipart
    @POST("acteur/{id}/media/gallery")
    suspend fun addGalleryPhotos(
        @Path("id") id: String,
        @Part photos: List<MultipartBody.Part>
    ): Response<ActeurProfile>

    /**
     * Supprime une photo spécifique de la galerie d'un acteur.
     * DELETE /acteur/{id}/media/gallery/{fileId}
     */
    @DELETE("acteur/{id}/media/gallery/{fileId}")
    suspend fun deleteGalleryPhoto(
        @Path("id") id: String,
        @Path("fileId") fileId: String
    ): Response<ActeurProfile>

    /**
     * Télécharge un média stocké dans GridFS.
     * GET /media/{fileId}
     */
    @Streaming
    @GET("media/{fileId}")
    suspend fun downloadMedia(
        @Path("fileId") fileId: String
    ): Response<ResponseBody>
    
    /**
     * Ajoute un casting aux favoris d'un acteur.
     * POST /acteur/{id}/favoris/{castingId}
     */
    @POST("acteur/{id}/favoris/{castingId}")
    suspend fun addFavorite(
        @Path("id") id: String,
        @Path("castingId") castingId: String
    ): Response<ResponseBody>
    
    /**
     * Retire un casting des favoris d'un acteur.
     * DELETE /acteur/{id}/favoris/{castingId}
     */
    @DELETE("acteur/{id}/favoris/{castingId}")
    suspend fun removeFavorite(
        @Path("id") id: String,
        @Path("castingId") castingId: String
    ): Response<ResponseBody>
    
    /**
     * Consulte la liste des favoris d'un acteur.
     * GET /acteur/{id}/favoris
     */
    @GET("acteur/{id}/favoris")
    suspend fun getFavorites(
        @Path("id") id: String
    ): Response<List<com.example.projecct_mobile.data.model.Casting>>
}

