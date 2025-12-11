package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle de données pour un utilisateur
 * Selon l'API : nom, prenom, email, role, bio, cvUrl, photoProfil
 */
data class User(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("id")
    val idAlt: String? = null,
    
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: UserRole? = null,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("cvUrl")
    val cvUrl: String? = null,
    
    @SerializedName("photoProfil")
    val photoProfil: String? = null,

    @SerializedName("media")
    val media: UserMedia? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

enum class UserRole {
    @SerializedName("ACTEUR")
    ACTEUR,
    
    @SerializedName("RECRUTEUR")
    RECRUTEUR,
    
    @SerializedName("ADMIN")
    ADMIN
}

