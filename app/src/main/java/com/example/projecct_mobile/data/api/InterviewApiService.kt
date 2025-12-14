package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.InterviewResponse
import com.example.projecct_mobile.data.model.InterviewsListResponse
import com.example.projecct_mobile.data.model.SelectInterviewDateRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les interviews
 */
interface InterviewApiService {
    
    /**
     * Obtenir les interviews d'un acteur
     * GET /interviews/actor?status={PENDING|CONFIRMED|CANCELLED}
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     */
    @GET("interviews/actor")
    suspend fun getActorInterviews(
        @Query("status") status: String? = null
    ): Response<InterviewsListResponse>
    
    /**
     * Obtenir les interviews d'une agence
     * GET /interviews/agency?status={PENDING|CONFIRMED|CANCELLED}&castingId={optional}
     * Auth: Requise (JWT)
     * Rôle: RECRUTEUR ou ADMIN
     */
    @GET("interviews/agency")
    suspend fun getAgencyInterviews(
        @Query("status") status: String? = null,
        @Query("castingId") castingId: String? = null
    ): Response<InterviewsListResponse>
    
    /**
     * Sélectionner une date d'interview (Acteur uniquement)
     * PATCH /interviews/{interviewId}/select-date
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     */
    @PATCH("interviews/{interviewId}/select-date")
    suspend fun selectInterviewDate(
        @Path("interviewId") interviewId: String,
        @Body request: SelectInterviewDateRequest
    ): Response<InterviewResponse>
    
    /**
     * Obtenir une interview par ID
     * GET /interviews/{interviewId}
     * Auth: Requise (JWT)
     */
    @GET("interviews/{interviewId}")
    suspend fun getInterviewById(
        @Path("interviewId") interviewId: String
    ): Response<InterviewResponse>
    
    /**
     * Annuler une interview
     * PATCH /interviews/{interviewId}/cancel
     * Auth: Requise (JWT)
     */
    @PATCH("interviews/{interviewId}/cancel")
    suspend fun cancelInterview(
        @Path("interviewId") interviewId: String
    ): Response<InterviewResponse>
}

