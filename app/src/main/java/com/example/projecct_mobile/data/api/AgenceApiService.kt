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
 * Documentation: AGENCES — Endpoints
 * 
 * Base URL: https://cast-mate.vercel.app
 * Auth: JWT Bearer Token (pour routes protégées)
 * Headers: Content-Type: application/json (pour JSON) | multipart/form-data (pour uploads)
 */
@JvmSuppressWildcards
interface AgenceApiService {
    
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
     * Note: Cette fonction est définie dans AuthApiService.signupAgence()
     * Voir AuthApiService pour plus de détails
     */
    
    /**
     * Profil d'une agence (Authentifié)
     * 
     * Méthode: GET
     * URL: /agence/:id
     * Auth: Requise (JWT)
     * 
     * Réponse 200: Profil complet de l'agence avec médias
     */
    @GET("agence/{id}")
    suspend fun getAgenceById(
        @Path("id") id: String
    ): Response<AgenceProfile>
    
    /**
     * Mettre à jour le profil (Propriétaire/Admin)
     * 
     * Méthode: PATCH
     * URL: /agence/:id
     * Auth: Requise (JWT)
     * Content-Type: application/json
     * 
     * Body (JSON):
     * {
     *   "nomAgence": "Nouveau nom",
     *   "description": "Nouvelle description",
     *   "siteWeb": "https://nouveau-site.com"
     * }
     */
    @PATCH("agence/{id}")
    suspend fun updateAgence(
        @Path("id") id: String,
        @Body request: UpdateAgenceRequest
    ): Response<AgenceProfile>
    
    /**
     * Mettre à jour photo/document (Propriétaire/Admin)
     * 
     * Méthode: PATCH
     * URL: /agence/:id/media/profile
     * Auth: Requise (JWT)
     * Content-Type: multipart/form-data
     * 
     * Body:
     * - photo (file, optionnel): Nouvelle photo JPEG/PNG
     * - document (file, optionnel): Nouveau document PDF
     * 
     * Réponse 200:
     * {
     *   "id": "...",
     *   "media": {
     *     "photoFileId": "...",
     *     "photoMimeType": "image/jpeg",
     *     "documentFileId": "...",
     *     "documentMimeType": "application/pdf"
     *   }
     * }
     */
    @Multipart
    @PATCH("agence/{id}/media/profile")
    suspend fun updateProfileMedia(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part? = null,
        @Part document: MultipartBody.Part? = null
    ): Response<AgenceProfile>

    /**
     * Télécharger un fichier média (Déprécié - Utiliser MediaApiService à la place)
     * 
     * Méthode: GET
     * URL: /media/{fileId}
     * Auth: Requise (JWT)
     * 
     * ⚠️ Cette fonction est conservée pour compatibilité.
     * Il est recommandé d'utiliser MediaApiService.downloadMedia() à la place.
     * 
     * @see MediaApiService
     */
    @Streaming
    @GET("media/{fileId}")
    suspend fun downloadMedia(
        @Path("fileId") fileId: String
    ): Response<ResponseBody>
}

