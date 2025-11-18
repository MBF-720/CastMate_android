package com.example.projecct_mobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Service API pour les médias
 * Documentation: MÉDIAS — Récupération des fichiers
 * 
 * Base URL: https://cast-mate.vercel.app
 * 
 * Ce service gère le téléchargement des fichiers médias stockés dans GridFS.
 * Les fichiers peuvent être des images (JPEG/PNG) ou des documents (PDF).
 */
interface MediaApiService {
    
    /**
     * Télécharge un fichier média stocké dans GridFS.
     * 
     * Méthode: GET
     * URL: /media/:fileId
     * Auth: Requise (JWT)
     * 
     * @param fileId ID du fichier (trouvé dans media.photoFileId, media.afficheFileId, etc.)
     * 
     * Réponse 200:
     * - Type: Flux binaire (image ou PDF)
     * - Headers: 
     *   - Content-Type: image/jpeg, image/png, application/pdf, etc.
     *   - Content-Disposition: inline; filename="..."
     * 
     * Règles d'accès:
     * - Photos d'agences: Accessibles à tous les utilisateurs authentifiés
     * - Photos d'acteurs, CV, galerie: Accessibles au propriétaire, aux admins, et aux recruteurs (agences)
     * 
     * Erreurs:
     * - 401: Non authentifié
     * - 403: Accès refusé (si l'utilisateur n'a pas le droit de voir ce média)
     * - 404: Fichier non trouvé
     * 
     * Exemple d'utilisation:
     * ```kotlin
     * val fileId = casting.media?.afficheFileId ?: casting.afficheFileId
     * if (fileId != null) {
     *     val response = mediaService.downloadMedia(fileId)
     *     if (response.isSuccessful && response.body() != null) {
     *         val imageBytes = response.body()!!.bytes()
     *         // Afficher l'image
     *     }
     * }
     * ```
     */
    @Streaming
    @GET("media/{fileId}")
    suspend fun downloadMedia(
        @Path("fileId") fileId: String
    ): Response<ResponseBody>
}

