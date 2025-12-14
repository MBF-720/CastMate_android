package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Statut d'une interview
 */
enum class InterviewStatus {
    PENDING,    // Interview créée, en attente de sélection par l'acteur
    CONFIRMED,  // Date sélectionnée par l'acteur
    CANCELLED   // Interview annulée
}

/**
 * Modèle pour une date proposée dans une interview
 */
data class ProposedDate(
    val date: String,        // Format: "YYYY-MM-DD"
    val time: String? = null, // Format: "HH:mm"
    val isSelected: Boolean = false
)

/**
 * Réponse API pour une date proposée
 */
data class ProposedDateResponse(
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String?,
    @SerializedName("isSelected")
    val isSelected: Boolean
) {
    fun toProposedDate(): ProposedDate {
        return ProposedDate(
            date = date,
            time = time,
            isSelected = isSelected
        )
    }
}

/**
 * Réponse API pour une interview
 */
data class InterviewResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("castingId")
    val castingId: String,
    
    @SerializedName("castingTitle")
    val castingTitle: String,
    
    @SerializedName("acteurId")
    val acteurId: String,
    
    @SerializedName("acteurName")
    val acteurName: String,
    
    @SerializedName("agenceId")
    val agenceId: String,
    
    @SerializedName("agenceName")
    val agenceName: String,
    
    @SerializedName("proposedDates")
    val proposedDates: List<ProposedDateResponse>,  // EXACTEMENT 3 dates
    
    @SerializedName("selectedDate")
    val selectedDate: String? = null,               // null si PENDING
    
    @SerializedName("selectedTime")
    val selectedTime: String? = null,               // null si PENDING
    
    @SerializedName("status")
    val status: String,                             // "PENDING", "CONFIRMED", "CANCELLED"
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    val statusEnum: InterviewStatus
        get() = when (status.uppercase()) {
            "PENDING" -> InterviewStatus.PENDING
            "CONFIRMED" -> InterviewStatus.CONFIRMED
            "CANCELLED" -> InterviewStatus.CANCELLED
            else -> InterviewStatus.PENDING
        }
    
    fun toProposedDates(): List<ProposedDate> {
        return proposedDates.map { it.toProposedDate() }
    }
}

/**
 * Modèle pour proposer des dates d'interview (création)
 */
data class InterviewDateOption(
    @SerializedName("date")
    val date: String,        // Format: "YYYY-MM-DD"
    @SerializedName("time")
    val time: String? = null // Format: "HH:mm" (OBLIGATOIRE en pratique)
)

/**
 * Requête pour accepter un candidat avec interview
 */
data class AcceptCandidateWithInterviewRequest(
    @SerializedName("proposedDates")
    val proposedDates: List<InterviewDateOption>  // EXACTEMENT 3 dates
)

/**
 * Réponse pour accepter un candidat avec interview
 */
data class AcceptCandidateWithInterviewResponse(
    @SerializedName("casting")
    val casting: Casting? = null,
    
    @SerializedName("interview")
    val interview: InterviewResponse? = null
)

/**
 * Requête pour sélectionner une date d'interview
 */
data class SelectInterviewDateRequest(
    @SerializedName("selectedDate")
    val selectedDate: String,  // Format: "YYYY-MM-DD"
    @SerializedName("selectedTime")
    val selectedTime: String? = null    // Format: "HH:mm" (optionnel)
)

/**
 * Réponse pour la liste d'interviews
 */
data class InterviewsListResponse(
    @SerializedName("interviews")
    val interviews: List<InterviewResponse>
)

