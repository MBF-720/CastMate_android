package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les interviews
 */
class InterviewRepository {
    
    private val interviewService = ApiClient.getInterviewService()
    
    /**
     * Sélectionner une date d'interview (Acteur)
     */
    suspend fun selectInterviewDate(
        interviewId: String,
        date: String,
        time: String? = null
    ): Result<InterviewResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SelectInterviewDateRequest(
                    selectedDate = date,
                    selectedTime = time
                )
                
                val response = interviewService.selectInterviewDate(interviewId, request)
                
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("InterviewRepository", "✅ Date d'interview sélectionnée avec succès")
                    Result.success(response.body()!!)
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}: $errorBody")
                    
                    val errorMessage = when (errorCode) {
                        400 -> "Date invalide ou non proposée"
                        403 -> "Vous n'êtes pas autorisé à sélectionner cette date"
                        404 -> "Interview non trouvée"
                        else -> "Erreur lors de la sélection de la date"
                    }
                    
                    Result.failure(ApiException.BadRequestException(errorMessage))
                }
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
                Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtenir les interviews d'un acteur
     */
    suspend fun getActorInterviews(
        status: InterviewStatus? = null
    ): Result<List<InterviewResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = interviewService.getActorInterviews(
                    status = status?.name
                )
                
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("InterviewRepository", "✅ Interviews acteur récupérées: ${response.body()!!.interviews.size}")
                    Result.success(response.body()!!.interviews)
                } else {
                    val errorCode = response.code()
                    android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}")
                    Result.failure(
                        ApiException.BadRequestException("Erreur lors de la récupération des interviews")
                    )
                }
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
                Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtenir les interviews d'une agence
     */
    suspend fun getAgencyInterviews(
        status: InterviewStatus? = null,
        castingId: String? = null
    ): Result<List<InterviewResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = interviewService.getAgencyInterviews(
                    status = status?.name,
                    castingId = castingId
                )
                
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("InterviewRepository", "✅ Interviews agence récupérées: ${response.body()!!.interviews.size}")
                    Result.success(response.body()!!.interviews)
                } else {
                    val errorCode = response.code()
                    android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}")
                    Result.failure(
                        ApiException.BadRequestException("Erreur lors de la récupération des interviews")
                    )
                }
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
                Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtenir une interview par ID
     */
    suspend fun getInterviewById(interviewId: String): Result<InterviewResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = interviewService.getInterviewById(interviewId)
                
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("InterviewRepository", "✅ Interview récupérée: $interviewId")
                    Result.success(response.body()!!)
                } else {
                    val errorCode = response.code()
                    android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}")
                    Result.failure(
                        ApiException.BadRequestException("Erreur lors de la récupération de l'interview")
                    )
                }
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
                Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
            }
        }
    }
    
    /**
     * Annuler une interview
     */
    suspend fun cancelInterview(interviewId: String): Result<InterviewResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = interviewService.cancelInterview(interviewId)
                
                if (response.isSuccessful && response.body() != null) {
                    android.util.Log.d("InterviewRepository", "✅ Interview annulée: $interviewId")
                    Result.success(response.body()!!)
                } else {
                    val errorCode = response.code()
                    android.util.Log.e("InterviewRepository", "❌ Erreur ${errorCode}")
                    Result.failure(
                        ApiException.BadRequestException("Erreur lors de l'annulation de l'interview")
                    )
                }
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                android.util.Log.e("InterviewRepository", "❌ Exception: ${e.message}", e)
                Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
            }
        }
    }
}

