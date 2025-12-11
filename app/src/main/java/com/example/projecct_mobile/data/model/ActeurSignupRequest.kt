package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la requête d'inscription d'un acteur
 * POST /acteur/signup
 */
data class ActeurSignupRequest(
    @SerializedName("nom")
    val nom: String,
    
    @SerializedName("prenom")
    val prenom: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("motDePasse")
    val motDePasse: String,
    
    @SerializedName("tel")
    val tel: String,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gouvernorat")
    val gouvernorat: String,
    
    @SerializedName("experience")
    val experience: Int,

    @SerializedName("centresInteret")
    val centresInteret: List<String>? = null,

    @SerializedName("socialLinks")
    val socialLinks: SocialLinks? = null
)

/**
 * Modèle pour les liens sociaux
 */
data class SocialLinks(
    @SerializedName("instagram")
    val instagram: String? = null,
    
    @SerializedName("youtube")
    val youtube: String? = null,
    
    @SerializedName("tiktok")
    val tiktok: String? = null
)

