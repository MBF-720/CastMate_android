package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Statut d'une interview
 */
enum class InterviewStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

/**
 * Modèle de données pour une date proposée dans une interview
 */
data class ProposedDate(
    @SerializedName("date")
    val date: String, // Format: "YYYY-MM-DD"
    
    @SerializedName("time")
    val time: String? = null, // Format: "HH:mm"
    
    @SerializedName("isSelected")
    val isSelected: Boolean = false
)

/**
 * Modèle de données pour une date proposée dans une réponse API
 */
data class ProposedDateResponse(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("time")
    val time: String?,
    
    @SerializedName("isSelected")
    val isSelected: Boolean
)

/**
 * Modèle de données pour une interview (réponse API)
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
    val proposedDates: List<ProposedDateResponse>,
    
    @SerializedName("selectedDate")
    val selectedDate: String? = null,
    
    @SerializedName("selectedTime")
    val selectedTime: String? = null,
    
    @SerializedName("status")
    val status: String, // PENDING, CONFIRMED, CANCELLED
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    /**
     * Convertit le statut string en enum
     */
    val statusEnum: InterviewStatus
        get() = when (status.uppercase()) {
            "PENDING" -> InterviewStatus.PENDING
            "CONFIRMED" -> InterviewStatus.CONFIRMED
            "CANCELLED" -> InterviewStatus.CANCELLED
            else -> InterviewStatus.PENDING
        }
}

/**
 * Modèle de données pour une liste d'interviews (réponse API)
 */
data class InterviewsListResponse(
    @SerializedName("interviews")
    val interviews: List<InterviewResponse>
)

/**
 * Requête pour accepter un candidat avec dates d'interview
 */
data class AcceptCandidateWithInterviewRequest(
    @SerializedName("proposedDates")
    val proposedDates: List<InterviewDateOption>
)

/**
 * Option de date pour une interview
 */
data class InterviewDateOption(
    @SerializedName("date")
    val date: String, // Format: "YYYY-MM-DD"
    
    @SerializedName("time")
    val time: String? = null // Format: "HH:mm" (optionnel)
)

/**
 * Réponse pour accepter un candidat avec interview
 */
data class AcceptCandidateWithInterviewResponse(
    @SerializedName("casting")
    val casting: Casting,
    
    @SerializedName("interview")
    val interview: InterviewResponse
)

/**
 * Requête pour sélectionner une date d'interview
 */
data class SelectInterviewDateRequest(
    @SerializedName("selectedDate")
    val selectedDate: String, // Format: "YYYY-MM-DD"
    
    @SerializedName("selectedTime")
    val selectedTime: String? = null // Format: "HH:mm" (optionnel)
)

