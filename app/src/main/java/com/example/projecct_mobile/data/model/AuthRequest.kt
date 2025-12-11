package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la requête d'inscription
 * Selon l'API : nom, prenom, email, password, role, bio, cvUrl, photoProfil
 * Tous les champs sauf email et password sont optionnels
 */
data class RegisterRequest(
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("role")
    val role: UserRole? = null, // ACTEUR, RECRUTEUR, ADMIN
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("cvUrl")
    val cvUrl: String? = null,
    
    @SerializedName("photoProfil")
    val photoProfil: String? = null
)

/**
 * Modèle pour la requête de connexion
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

