package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la requête d'inscription d'une agence
 * POST /agence/signup
 */
data class AgenceSignupRequest(
    @SerializedName("nomAgence")
    val nomAgence: String,
    
    @SerializedName("responsable")
    val responsable: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("motDePasse")
    val motDePasse: String,
    
    @SerializedName("tel")
    val tel: String,
    
    @SerializedName("gouvernorat")
    val gouvernorat: String,
    
    @SerializedName("siteWeb")
    val siteWeb: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null,
    
    @SerializedName("documents")
    val documents: String? = null,
    
    @SerializedName("socialLinks")
    val socialLinks: AgenceSocialLinks? = null
)

/**
 * Modèle pour les liens sociaux des agences
 */
data class AgenceSocialLinks(
    @SerializedName("facebook")
    val facebook: String? = null,
    
    @SerializedName("instagram")
    val instagram: String? = null
)

