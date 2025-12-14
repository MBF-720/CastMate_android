package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.InterviewApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.InterviewResponse
import com.example.projecct_mobile.data.model.InterviewStatus
import com.example.projecct_mobile.data.model.SelectInterviewDateRequest

/**
 * Repository pour gérer les interviews
 */
class InterviewRepository {
    
    private val interviewService: InterviewApiService = ApiClient.getInterviewService()
    
    /**
     * Récupère les interviews d'un acteur
     * 
     * @param status Filtre optionnel par statut (PENDING, CONFIRMED, CANCELLED)
     * @return Result<List<InterviewResponse>>
     */
    suspend fun getActorInterviews(status: InterviewStatus? = null): Result<List<InterviewResponse>> {
        return try {
            val statusString = status?.name
            val response = interviewService.getActorInterviews(statusString)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val interviews = body.interviews
                    android.util.Log.d("InterviewRepository", "✅ Interviews acteur récupérées: ${interviews.size}")
                    Result.success(interviews)
                } else {
                    android.util.Log.w("InterviewRepository", "Response body is null")
                    Result.success(emptyList())
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val exception = when (errorCode) {
                    401 -> ApiException.UnauthorizedException("Non autorisé")
                    403 -> ApiException.ForbiddenException("Accès refusé")
                    404 -> ApiException.NotFoundException("Interviews non trouvées")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("InterviewRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Récupère les interviews d'une agence
     * 
     * @param status Filtre optionnel par statut (PENDING, CONFIRMED, CANCELLED)
     * @param castingId Filtre optionnel par casting
     * @return Result<List<InterviewResponse>>
     */
    suspend fun getAgencyInterviews(
        status: InterviewStatus? = null,
        castingId: String? = null
    ): Result<List<InterviewResponse>> {
        return try {
            val statusString = status?.name
            val response = interviewService.getAgencyInterviews(statusString, castingId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val interviews = body.interviews
                    android.util.Log.d("InterviewRepository", "✅ Interviews agence récupérées: ${interviews.size}")
                    Result.success(interviews)
                } else {
                    android.util.Log.w("InterviewRepository", "Response body is null")
                    Result.success(emptyList())
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val exception = when (errorCode) {
                    401 -> ApiException.UnauthorizedException("Non autorisé")
                    403 -> ApiException.ForbiddenException("Accès refusé")
                    404 -> ApiException.NotFoundException("Interviews non trouvées")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("InterviewRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Sélectionne une date d'interview (Acteur uniquement)
     * 
     * @param interviewId ID de l'interview
     * @param date Date sélectionnée (format: "YYYY-MM-DD")
     * @param time Heure sélectionnée (format: "HH:mm")
     * @return Result<InterviewResponse>
     */
    suspend fun selectInterviewDate(
        interviewId: String,
        date: String,
        time: String?
    ): Result<InterviewResponse> {
        return try {
            val request = SelectInterviewDateRequest(
                selectedDate = date,
                selectedTime = time
            )
            
            val response = interviewService.selectInterviewDate(interviewId, request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    android.util.Log.d("InterviewRepository", "✅ Date d'interview sélectionnée avec succès")
                    Result.success(body)
                } else {
                    android.util.Log.w("InterviewRepository", "Response body is null")
                    Result.failure(ApiException.UnknownException("Réponse vide"))
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val exception = when (errorCode) {
                    400 -> ApiException.BadRequestException("Requête invalide: ${errorBody ?: "Date ou heure invalide"}")
                    401 -> ApiException.UnauthorizedException("Non autorisé")
                    403 -> ApiException.ForbiddenException("Accès refusé - Seuls les acteurs peuvent sélectionner une date")
                    404 -> ApiException.NotFoundException("Interview non trouvée")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("InterviewRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Récupère une interview par son ID
     * 
     * @param interviewId ID de l'interview
     * @return Result<InterviewResponse>
     */
    suspend fun getInterviewById(interviewId: String): Result<InterviewResponse> {
        return try {
            val response = interviewService.getInterviewById(interviewId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    android.util.Log.d("InterviewRepository", "✅ Interview récupérée: ${body.id}")
                    Result.success(body)
                } else {
                    android.util.Log.w("InterviewRepository", "Response body is null")
                    Result.failure(ApiException.UnknownException("Réponse vide"))
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val exception = when (errorCode) {
                    401 -> ApiException.UnauthorizedException("Non autorisé")
                    403 -> ApiException.ForbiddenException("Accès refusé")
                    404 -> ApiException.NotFoundException("Interview non trouvée")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("InterviewRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
}

