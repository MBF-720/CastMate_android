package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.AgenceProfile
import com.example.projecct_mobile.data.model.UpdateAgenceRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Streaming

/**
 * Service API pour les agences
 */
@JvmSuppressWildcards
interface AgenceApiService {
    
    /**
     * Récupère une agence par son ID
     * GET /agence/:id
     */
    @GET("agence/{id}")
    suspend fun getAgenceById(
        @Path("id") id: String
    ): Response<AgenceProfile>
    
    /**
     * Met à jour le profil d'une agence
     * PATCH /agence/{id}
     */
    @PATCH("agence/{id}")
    suspend fun updateAgence(
        @Path("id") id: String,
        @Body request: UpdateAgenceRequest
    ): Response<AgenceProfile>
    
    /**
     * Met à jour la photo de profil (logo) et/ou le document d'une agence.
     * PATCH /agence/{id}/media/profile
     */
    @Multipart
    @PATCH("agence/{id}/media/profile")
    suspend fun updateProfileMedia(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<AgenceProfile>

    /**
     * Télécharge un média stocké dans GridFS.
     * GET /media/{fileId}
     */
    @Streaming
    @GET("media/{fileId}")
    suspend fun downloadMedia(
        @Path("fileId") fileId: String
    ): Response<ResponseBody>
}

