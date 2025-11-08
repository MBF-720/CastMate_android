package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Requête envoyée au backend pour authentifier un utilisateur avec Google.
 */
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)

