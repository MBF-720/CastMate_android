package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle de données pour un casting
 * Selon l'API : titre, descriptionRole, synopsis, lieu, dateDebut, dateFin, prix, types, age, conditions
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

    @SerializedName("afficheFileId")
    val afficheFileId: String? = null,

    @SerializedName("conditions")
    val conditions: String? = null,
    
    @SerializedName("recruteur")
    val recruteur: Any? = null, // Peut être un String (ID) ou un objet (recruteur complet)
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    // Propriété calculée pour obtenir l'ID (priorité à _id)
    val actualId: String?
        get() = id ?: idAlt
}

/**
 * Requête pour créer un nouveau casting
 * Selon l'API : titre, descriptionRole, synopsis, lieu, dateDebut, dateFin, prix, types, age, conditions
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

    @SerializedName("conditions")
    val conditions: String? = null
)

