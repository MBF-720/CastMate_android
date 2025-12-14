package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèles de données pour le système de classement (Ranking)
 */

/**
 * Enum pour les rangs des acteurs
 */
enum class ActorRank(val displayName: String) {
    @SerializedName("AMATEUR")
    AMATEUR("Amateur"),
    
    @SerializedName("SEMI_PRO")
    SEMI_PRO("Semi-Pro"),
    
    @SerializedName("PRO")
    PRO("Pro"),
    
    @SerializedName("FAMOUS")
    FAMOUS("Famous"),
    
    @SerializedName("STAR")
    STAR("Star"),
    
    @SerializedName("SUPER_STAR")
    SUPER_STAR("Super Star"),
    
    @SerializedName("LEGEND")
    LEGEND("Legend"),
    
    @SerializedName("ICON")
    ICON("Icon")
}

/**
 * Modèle de classement d'un acteur
 */
data class ActorRanking(
    @SerializedName("globalScore")
    val globalScore: Int,
    
    @SerializedName("rank")
    val rank: ActorRank,
    
    @SerializedName("averageScore")
    val averageScore: Double,
    
    @SerializedName("totalSessions")
    val totalSessions: Int,
    
    @SerializedName("totalNFTs")
    val totalNFTs: Int,
    
    @SerializedName("maxLevel")
    val maxLevel: Int,
    
    @SerializedName("consistency")
    val consistency: Double,
    
    @SerializedName("recentPerformance")
    val recentPerformance: Double,
    
    @SerializedName("winStreak")
    val winStreak: Int,
    
    @SerializedName("globalPosition")
    val globalPosition: Int?,
    
    @SerializedName("percentile")
    val percentile: Int,
    
    @SerializedName("lastUpdated")
    val lastUpdated: String
) {
    /**
     * Obtient la date de dernière mise à jour
     */
    fun getLastUpdatedDate(): java.util.Date? {
        return try {
            java.time.Instant.parse(lastUpdated).let {
                java.util.Date.from(it)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formate la position globale pour l'affichage
     */
    fun getFormattedPosition(): String {
        return globalPosition?.toString() ?: "N/A"
    }
    
    /**
     * Formate le percentile pour l'affichage
     */
    fun getFormattedPercentile(): String {
        return "$percentile%"
    }
}

/**
 * Modèle pour un acteur dans le leaderboard
 */
data class LeaderboardActor(
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("nom")
    val nom: String,
    
    @SerializedName("prenom")
    val prenom: String,
    
    @SerializedName("photoFileId")
    val photoFileId: String? = null,
    
    @SerializedName("ranking")
    val ranking: ActorRanking
) {
    /**
     * Obtient le nom complet de l'acteur
     */
    fun getFullName(): String = "$prenom $nom"
    
    /**
     * Obtient les initiales de l'acteur
     */
    fun getInitials(): String = "${prenom.firstOrNull()?.uppercase() ?: ""}${nom.firstOrNull()?.uppercase() ?: ""}"
}

/**
 * Réponse du leaderboard avec pagination
 */
data class LeaderboardResponse(
    @SerializedName("actors")
    val actors: List<LeaderboardActor>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int
)

