package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Réponse de l'API après authentification (register ou login)
 */
data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("message")
    val message: String? = null
)

