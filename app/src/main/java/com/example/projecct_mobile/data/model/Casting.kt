package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle de données pour les médias d'un casting
 */
data class CastingMedia(
    @SerializedName("afficheFileId")
    val afficheFileId: String? = null,
    
    @SerializedName("afficheMimeType")
    val afficheMimeType: String? = null,
    
    @SerializedName("afficheOriginalName")
    val afficheOriginalName: String? = null,
    
    @SerializedName("afficheLength")
    val afficheLength: Double? = null,
    
    @SerializedName("afficheUploadDate")
    val afficheUploadDate: String? = null
)

/**
 * Modèle de données pour les médias d'un recruteur (agence)
 */
data class RecruteurMedia(
    @SerializedName("photoFileId")
    val photoFileId: String? = null,
    
    @SerializedName("photoMimeType")
    val photoMimeType: String? = null
)

/**
 * Modèle de données pour les liens sociaux d'une agence
 */
data class RecruteurSocialLinks(
    @SerializedName("facebook")
    val facebook: String? = null,
    
    @SerializedName("instagram")
    val instagram: String? = null
)

/**
 * Modèle de données pour un recruteur (agence)
 */
data class RecruteurInfo(
    @SerializedName("_id")
    val idAlt: String? = null, // Format MongoDB (_id)
    
    @SerializedName("id")
    val id: String? = null, // Format alternatif (id)
    
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
    
    @SerializedName("socialLinks")
    val socialLinks: RecruteurSocialLinks? = null,
    
    @SerializedName("media")
    val media: RecruteurMedia? = null
) {
    // Propriété calculée pour obtenir l'ID (priorité à _id)
    val actualId: String?
        get() = idAlt ?: id
}

/**
 * Modèle de données pour les médias d'un acteur (utilisé dans les candidats)
 */
data class ActeurMedia(
    @SerializedName("photoFileId")
    val photoFileId: String? = null,
    
    @SerializedName("photoMimeType")
    val photoMimeType: String? = null
)

/**
 * Modèle de données pour un acteur (utilisé dans les candidats)
 */
data class ActeurInfo(
    @SerializedName("_id")
    val idAlt: String? = null, // Format MongoDB (_id)
    
    @SerializedName("id")
    val id: String? = null, // Format alternatif (id)
    
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("media")
    val media: ActeurMedia? = null
) {
    // Propriété calculée pour obtenir l'ID (priorité à _id)
    val actualId: String?
        get() = idAlt ?: id
}

/**
 * Modèle de données pour un candidat à un casting
 */
data class Candidat(
    @SerializedName("acteurId")
    val acteurId: ActeurInfo? = null,
    
    @SerializedName("statut")
    val statut: String? = null, // "EN_ATTENTE", "ACCEPTE", "REFUSE"
    
    @SerializedName("dateCandidature")
    val dateCandidature: String? = null // Format ISO
)

/**
 * Modèle de données pour un casting
 * Selon l'API : titre, descriptionRole, synopsis, lieu, dateDebut, dateFin, prix, types, age, ouvert, conditions
 */
data class Casting(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("id")
    val idAlt: String? = null, // Alternative si l'API retourne "id" au lieu de "_id"
    
    @SerializedName("titre")
    val titre: String? = null, // Rendre nullable pour gérer les cas où l'API ne le retourne pas
    
    @SerializedName("descriptionRole")
    val descriptionRole: String? = null,
    
    @SerializedName("synopsis")
    val synopsis: String? = null,
    
    @SerializedName("lieu")
    val lieu: String? = null,
    
    @SerializedName("dateDebut")
    val dateDebut: String? = null, // Format: "YYYY-MM-DD"
    
    @SerializedName("dateFin")
    val dateFin: String? = null, // Format: "YYYY-MM-DD"
    
    @SerializedName("prix")
    val prix: Double? = null,
    
    @SerializedName("types")
    val types: List<String>? = null, // ⭐ NOUVEAU - Optionnel, tableau de types (ex: ["Cinéma", "Télévision"])
    
    @SerializedName("age")
    val age: String? = null, // ⭐ NOUVEAU - Optionnel, tranche d'âge (ex: "25-35 ans")
    
    @SerializedName("ouvert")
    val ouvert: Boolean = true, // ⭐ NOUVEAU - Indique si le casting accepte des candidatures (défaut: true)

    @SerializedName("afficheFileId")
    val afficheFileId: String? = null, // Ancien format (pour compatibilité)

    @SerializedName("media")
    val media: CastingMedia? = null, // Nouveau format avec media.afficheFileId

    @SerializedName("conditions")
    val conditions: String? = null,
    
    @SerializedName("recruteur")
    val recruteur: RecruteurInfo? = null, // Objet recruteur complet selon la documentation
    
    @SerializedName("candidats")
    val candidats: List<Candidat>? = null, // ⭐ MODIFIÉ - Nouvelle structure avec statut
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    // Propriété calculée pour obtenir l'ID (priorité à _id)
    val actualId: String?
        get() = id ?: idAlt
    
    // Propriété calculée pour obtenir l'afficheFileId (priorité à media.afficheFileId)
    val actualAfficheFileId: String?
        get() = media?.afficheFileId ?: afficheFileId
}

/**
 * Requête pour créer un nouveau casting
 * Selon l'API : titre, descriptionRole, synopsis, lieu, dateDebut, dateFin, prix, types, age, ouvert, conditions
 */
data class CreateCastingRequest(
    @SerializedName("titre")
    val titre: String,
    
    @SerializedName("descriptionRole")
    val descriptionRole: String? = null,
    
    @SerializedName("synopsis")
    val synopsis: String? = null,
    
    @SerializedName("lieu")
    val lieu: String? = null,
    
    @SerializedName("dateDebut")
    val dateDebut: String? = null, // Format: "YYYY-MM-DD"
    
    @SerializedName("dateFin")
    val dateFin: String? = null, // Format: "YYYY-MM-DD"
 
    @SerializedName("prix")
    val prix: Double? = null,
    
    @SerializedName("types")
    val types: List<String>? = null, // ⭐ NOUVEAU - Optionnel, tableau de types (ex: ["Cinéma", "Télévision"])
    
    @SerializedName("age")
    val age: String? = null, // ⭐ NOUVEAU - Optionnel, tranche d'âge (ex: "25-35 ans")
    
    @SerializedName("ouvert")
    val ouvert: Boolean? = true, // ⭐ NOUVEAU - Indique si le casting accepte des candidatures (défaut: true)

    @SerializedName("conditions")
    val conditions: String? = null
)

/**
 * Réponse pour le statut de candidature d'un acteur
 */
data class CandidateStatusResponse(
    @SerializedName("hasApplied")
    val hasApplied: Boolean = false,
    
    @SerializedName("statut")
    val statut: String? = null, // "EN_ATTENTE", "ACCEPTE", "REFUSE", ou null
    
    @SerializedName("dateCandidature")
    val dateCandidature: String? = null // Format ISO
)

