package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la mise à jour d'une agence
 * PATCH /agence/:id
 */
data class UpdateAgenceRequest(
    @SerializedName("nomAgence")
    val nomAgence: String? = null,
    
    @SerializedName("responsable")
    val responsable: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("tel")
    val tel: String? = null,
    
    @SerializedName("gouvernorat")
    val gouvernorat: String? = null,
    
    @SerializedName("siteWeb")
    val siteWeb: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("socialLinks")
    val socialLinks: AgenceSocialLinks? = null
)

