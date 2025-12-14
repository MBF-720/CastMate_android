package com.example.projecct_mobile.utils

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.model.MediaFileRef
import com.example.projecct_mobile.data.model.UserMedia

/**
 * Helper pour construire les URLs des médias (photos de profil, CV, galerie)
 * conformément à l'API CastMate.
 */
object MediaUrlHelper {
    private val BASE_URL = ApiClient.BASE_URL.trimEnd('/')
    
    /**
     * Construit l'URL pour récupérer la photo de profil
     * @param photoFileId L'ID du fichier photo
     * @return L'URL complète ou null si pas de fileId
     */
    fun getProfilePhotoUrl(photoFileId: String?): String? {
        return photoFileId?.let { "$BASE_URL/media/$it" }
    }
    
    /**
     * Construit l'URL pour récupérer la photo de profil depuis UserMedia
     * @param media L'objet UserMedia contenant le photoFileId
     * @return L'URL complète ou null si pas de fileId
     */
    fun getProfilePhotoUrl(media: UserMedia?): String? {
        return getProfilePhotoUrl(media?.photoFileId)
    }
    
    /**
     * Construit l'URL pour récupérer le CV (PDF)
     * @param documentFileId L'ID du fichier document
     * @return L'URL complète ou null si pas de fileId
     */
    fun getCvUrl(documentFileId: String?): String? {
        return documentFileId?.let { "$BASE_URL/media/$it" }
    }
    
    /**
     * Construit l'URL pour récupérer le CV depuis UserMedia
     * @param media L'objet UserMedia contenant le documentFileId
     * @return L'URL complète ou null si pas de fileId
     */
    fun getCvUrl(media: UserMedia?): String? {
        return getCvUrl(media?.documentFileId)
    }
    
    /**
     * Construit l'URL pour récupérer une photo de galerie
     * @param fileId L'ID du fichier
     * @return L'URL complète
     */
    fun getGalleryPhotoUrl(fileId: String): String {
        return "$BASE_URL/media/$fileId"
    }
    
    /**
     * Construit l'URL pour récupérer une photo de galerie depuis MediaFileRef
     * @param mediaFileRef La référence du fichier
     * @return L'URL complète
     */
    fun getGalleryPhotoUrl(mediaFileRef: MediaFileRef): String {
        return getGalleryPhotoUrl(mediaFileRef.fileId)
    }
    
    /**
     * Récupère toutes les URLs de la galerie
     * @param gallery Liste des photos de galerie
     * @return Liste des URLs
     */
    fun getGalleryPhotoUrls(gallery: List<MediaFileRef>?): List<String> {
        return gallery?.map { getGalleryPhotoUrl(it.fileId) } ?: emptyList()
    }
    
    /**
     * Récupère toutes les URLs de la galerie depuis UserMedia
     * @param media L'objet UserMedia contenant la galerie
     * @return Liste des URLs
     */
    fun getGalleryPhotoUrls(media: UserMedia?): List<String> {
        return getGalleryPhotoUrls(media?.gallery)
    }
}

