package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la requête de réinitialisation de mot de passe (demande)
 * POST /auth/forgot-password
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("userType")
    val userType: String, // "ACTEUR" ou "RECRUTEUR"
    
    @SerializedName("token")
    val token: String? = null // Optionnel : token généré par Android pour que le backend le stocke
)

/**
 * Modèle pour la réponse de réinitialisation de mot de passe (demande)
 * Réponse 200 de POST /auth/forgot-password
 */
data class ForgotPasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("token")
    val token: String? = null, // Optionnel si le backend retourne le token
    
    @SerializedName("link")
    val link: String? = null, // Optionnel, lien deep link
    
    @SerializedName("expiresIn")
    val expiresIn: Int? = null // Optionnel, durée d'expiration en secondes
)

