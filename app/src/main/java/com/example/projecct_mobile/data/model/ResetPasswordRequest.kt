package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la requête de réinitialisation de mot de passe (application)
 * POST /auth/reset-password
 */
data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("newPassword")
    val newPassword: String,
    
    @SerializedName("email")
    val email: String
)

/**
 * Modèle pour la réponse de réinitialisation de mot de passe (application)
 * Réponse 200 de POST /auth/reset-password
 */
data class ResetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String
)

