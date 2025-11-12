package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Représentation des métadonnées médias renvoyées par l'API CastMate.
 */
data class UserMedia(
    @SerializedName("photoFileId")
    val photoFileId: String? = null,

    @SerializedName("documentFileId")
    val documentFileId: String? = null,

    @SerializedName("gallery")
    val gallery: List<MediaFileRef>? = null
)

/**
 * Référence vers un fichier stocké dans GridFS.
 */
data class MediaFileRef(
    @SerializedName("fileId")
    val fileId: String? = null,

    @SerializedName("mimeType")
    val mimeType: String? = null,

    @SerializedName("uploadedAt")
    val uploadedAt: String? = null,

    @SerializedName("thumbnailFileId")
    val thumbnailFileId: String? = null
)

