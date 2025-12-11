package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Représentation d'une agence telle que renvoyée par l'API CastMate.
 */
data class AgenceProfile(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("id")
    val idAlt: String? = null,

    @SerializedName("nomAgence")
    val nomAgence: String? = null,

    @SerializedName("responsable")
    val responsable: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("tel")
    val tel: String? = null,

    @SerializedName("gouvernorat")
    val gouvernorat: String? = null,

    @SerializedName("siteWeb")
    val siteWeb: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("media")
    val media: UserMedia? = null,

    @SerializedName("socialLinks")
    val socialLinks: AgenceSocialLinks? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

