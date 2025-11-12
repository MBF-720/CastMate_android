package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Représentation d'un acteur telle que renvoyée par l'API CastMate.
 */
data class ActeurProfile(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("id")
    val idAlt: String? = null,

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

    @SerializedName("centresInteret")
    val centresInteret: List<String>? = null,

    @SerializedName("socialLinks")
    val socialLinks: SocialLinks? = null,

    @SerializedName("media")
    val media: UserMedia? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

