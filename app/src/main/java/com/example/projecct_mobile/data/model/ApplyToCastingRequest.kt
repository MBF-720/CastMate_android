package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Requête pour postuler à un casting avec vidéo et feedback IA
 */
data class ApplyToCastingRequest(
    @SerializedName("aiFeedback")
    val aiFeedback: String? = null // JSON string du feedback IA (optionnel)
)

