package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les interviews
 * Documentation: INTERVIEWS — Endpoints
 * 
 * Base URL: https://cast-mate.vercel.app
 * Auth: JWT Bearer Token (requis pour toutes les routes)
 */
interface InterviewApiService {
    
    /**
     * Sélectionner une date d'interview (Acteur)
     * 
     * Méthode: PATCH
     * URL: /interviews/:interviewId/select-date
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     * 
     * @param interviewId ID de l'interview
     * @param request Requête avec la date sélectionnée
     * 
     * Réponse 200: Interview mise à jour avec la date sélectionnée
     * 
     * Erreurs:
     * - 400: Date non proposée, date invalide, statut incorrect
     * - 403: Vous n'êtes pas l'acteur concerné
     * - 404: Interview non trouvée
     */
    @PATCH("interviews/{interviewId}/select-date")
    suspend fun selectInterviewDate(
        @Path("interviewId") interviewId: String,
        @Body request: SelectInterviewDateRequest
    ): Response<InterviewResponse>
    
    /**
     * Obtenir les interviews d'un acteur
     * 
     * Méthode: GET
     * URL: /interviews/actor
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     * 
     * @param status (optionnel) : PENDING, CONFIRMED, ou CANCELLED
     * 
     * Réponse 200: Liste des interviews de l'acteur
     */
    @GET("interviews/actor")
    suspend fun getActorInterviews(
        @Query("status") status: String? = null
    ): Response<InterviewsListResponse>
    
    /**
     * Obtenir les interviews d'une agence
     * 
     * Méthode: GET
     * URL: /interviews/agency
     * Auth: Requise (JWT)
     * Rôle: RECRUTEUR ou ADMIN
     * 
     * @param status (optionnel) : PENDING, CONFIRMED, ou CANCELLED
     * @param castingId (optionnel) : Filtrer par ID de casting
     * 
     * Réponse 200: Liste des interviews de l'agence
     */
    @GET("interviews/agency")
    suspend fun getAgencyInterviews(
        @Query("status") status: String? = null,
        @Query("castingId") castingId: String? = null
    ): Response<InterviewsListResponse>
    
    /**
     * Obtenir une interview par ID
     * 
     * Méthode: GET
     * URL: /interviews/:interviewId
     * Auth: Requise (JWT)
     * Doit être l'acteur concerné ou l'agence propriétaire
     * 
     * @param interviewId ID de l'interview
     * 
     * Réponse 200: Détails de l'interview
     * 
     * Erreurs:
     * - 403: Vous n'êtes pas autorisé à voir cette interview
     * - 404: Interview non trouvée
     */
    @GET("interviews/{interviewId}")
    suspend fun getInterviewById(
        @Path("interviewId") interviewId: String
    ): Response<InterviewResponse>
    
    /**
     * Annuler une interview
     * 
     * Méthode: PATCH
     * URL: /interviews/:interviewId/cancel
     * Auth: Requise (JWT)
     * Doit être l'acteur concerné ou l'agence propriétaire
     * 
     * @param interviewId ID de l'interview
     * 
     * Réponse 200: Interview annulée
     * 
     * Erreurs:
     * - 400: Interview déjà annulée
     * - 403: Vous n'êtes pas autorisé à annuler cette interview
     * - 404: Interview non trouvée
     */
    @PATCH("interviews/{interviewId}/cancel")
    suspend fun cancelInterview(
        @Path("interviewId") interviewId: String
    ): Response<InterviewResponse>
}

