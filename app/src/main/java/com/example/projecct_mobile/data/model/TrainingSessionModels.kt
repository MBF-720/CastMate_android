package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèles de données pour les sessions d'entraînement et les récompenses NFT
 */

/**
 * Requête pour soumettre une session d'entraînement
 */
data class SubmitTrainingRequest(
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("niveau")
    val niveau: Int,
    
    @SerializedName("globalScore")
    val globalScore: Int,
    
    @SerializedName("emotions")
    val emotions: TrainingEmotions,
    
    @SerializedName("strengths")
    val strengths: List<String> = emptyList(),
    
    @SerializedName("recommendations")
    val recommendations: List<String> = emptyList()
)

/**
 * Émotions pour la soumission d'entraînement
 */
data class TrainingEmotions(
    @SerializedName("detected")
    val detected: List<String>,
    
    @SerializedName("coherence")
    val coherence: Int,
    
    @SerializedName("intensity")
    val intensity: Int
)

/**
 * Réponse après soumission d'une session d'entraînement
 */
data class SubmitTrainingResponse(
    @SerializedName("session")
    val session: TrainingSession,
    
    @SerializedName("nftReward")
    val nftReward: NFTReward? = null
)

/**
 * Session d'entraînement
 */
data class TrainingSession(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("_id")
    val idAlt: String? = null,
    
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("niveau")
    val niveau: Int,
    
    @SerializedName("globalScore")
    val globalScore: Int,
    
    @SerializedName("emotions")
    val emotions: TrainingEmotions,
    
    @SerializedName("strengths")
    val strengths: List<String> = emptyList(),
    
    @SerializedName("recommendations")
    val recommendations: List<String> = emptyList(),
    
    @SerializedName("nftRewarded")
    val nftRewarded: Boolean = false,
    
    @SerializedName("nftRewardId")
    val nftRewardId: NFTRewardInfo? = null,
    
    @SerializedName("completedAt")
    val completedAt: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

/**
 * Informations NFT (peut être un ID string ou un objet complet)
 */
data class NFTRewardInfo(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("_id")
    val idAlt: String? = null,
    
    @SerializedName("tokenId")
    val tokenId: String? = null,
    
    @SerializedName("serialNumber")
    val serialNumber: String? = null,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

/**
 * Récompense NFT
 */
data class NFTReward(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("_id")
    val idAlt: String? = null,
    
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("trainingSessionId")
    val trainingSessionId: TrainingSessionInfo? = null,
    
    @SerializedName("niveau")
    val niveau: Int,
    
    @SerializedName("score")
    val score: Int,
    
    @SerializedName("tokenId")
    val tokenId: String,
    
    @SerializedName("serialNumber")
    val serialNumber: String,
    
    @SerializedName("transactionId")
    val transactionId: String? = null,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("metadataUrl")
    val metadataUrl: String? = null,
    
    @SerializedName("status")
    val status: String = "PENDING", // PENDING, MINTED, FAILED
    
    @SerializedName("mintedAt")
    val mintedAt: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

/**
 * Informations de session d'entraînement (référence dans NFT)
 */
data class TrainingSessionInfo(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("_id")
    val idAlt: String? = null,
    
    @SerializedName("niveau")
    val niveau: Int? = null,
    
    @SerializedName("globalScore")
    val globalScore: Int? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

/**
 * Données de transaction pour le transfert NFT
 */
data class NFTTransferData(
    @SerializedName("tokenId")
    val tokenId: String,
    
    @SerializedName("serialNumber")
    val serialNumber: String,
    
    @SerializedName("recipientAccountId")
    val recipientAccountId: String,
    
    @SerializedName("treasuryAccountId")
    val treasuryAccountId: String
)

/**
 * Réponse pour initier un transfert NFT
 */
data class NFTTransferResponse(
    @SerializedName("transactionData")
    val transactionData: NFTTransferData,
    
    @SerializedName("message")
    val message: String
)

/**
 * Métadonnées NFT (format ERC-721)
 */
data class NFTMetadata(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("external_url")
    val externalUrl: String? = null,
    
    @SerializedName("attributes")
    val attributes: List<NFTAttribute>
)

/**
 * Attribut NFT
 */
data class NFTAttribute(
    @SerializedName("trait_type")
    val traitType: String,
    
    @SerializedName("value")
    val value: Any // Peut être String, Int, etc.
)

