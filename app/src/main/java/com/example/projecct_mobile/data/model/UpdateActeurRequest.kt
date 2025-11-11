package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la mise à jour d'un acteur
 * PATCH /acteur/:id
 */
data class UpdateActeurRequest(
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("tel")
    val tel: String? = null,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("gouvernorat")
    val gouvernorat: String? = null,
    
    @SerializedName("experience")
    val experience: Int? = null,
    
    @SerializedName("cvPdf")
    val cvPdf: String? = null,
    
    @SerializedName("centresInteret")
    val centresInteret: List<String>? = null,
    
    @SerializedName("photoProfil")
    val photoProfil: String? = null,
    
    @SerializedName("socialLinks")
    val socialLinks: SocialLinks? = null
)

