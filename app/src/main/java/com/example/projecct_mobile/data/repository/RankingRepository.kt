package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.TrainingApiService
import com.example.projecct_mobile.data.model.*
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository pour gérer le classement des acteurs
 */
class RankingRepository {
    
    private val apiService: TrainingApiService = ApiClient.getTrainingService()
    
    /**
     * Récupère le classement d'un acteur
     */
    suspend fun getActorRanking(acteurId: String): Result<ActorRanking> {
        return try {
            val response = apiService.getActorRanking(acteurId)
            
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Response body is null"))
            } else {
                when (response.code()) {
                    404 -> Result.failure(RankingException.ActorNotFound)
                    401 -> Result.failure(RankingException.Unauthorized)
                    else -> Result.failure(
                        RankingException.ServerError(response.code())
                    )
                }
            }
        } catch (e: IOException) {
            Result.failure(RankingException.NetworkError(e))
        } catch (e: HttpException) {
            Result.failure(RankingException.ServerError(e.code()))
        } catch (e: Exception) {
            android.util.Log.e("RankingRepository", "Erreur getActorRanking: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Récupère le leaderboard avec pagination
     */
    suspend fun getLeaderboard(page: Int = 1, limit: Int = 50): Result<LeaderboardResponse> {
        return try {
            val response = apiService.getLeaderboard(page = page, limit = limit)
            
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Response body is null"))
            } else {
                when (response.code()) {
                    400 -> Result.failure(RankingException.InvalidParameters)
                    401 -> Result.failure(RankingException.Unauthorized)
                    else -> Result.failure(
                        RankingException.ServerError(response.code())
                    )
                }
            }
        } catch (e: IOException) {
            Result.failure(RankingException.NetworkError(e))
        } catch (e: HttpException) {
            Result.failure(RankingException.ServerError(e.code()))
        } catch (e: Exception) {
            android.util.Log.e("RankingRepository", "Erreur getLeaderboard: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/**
 * Exceptions spécifiques au ranking
 */
sealed class RankingException(message: String) : Exception(message) {
    object ActorNotFound : RankingException("Acteur non trouvé")
    object Unauthorized : RankingException("Non authentifié")
    object InvalidParameters : RankingException("Paramètres invalides")
    class ServerError(code: Int) : RankingException("Erreur serveur: $code")
    class NetworkError(cause: Throwable) : RankingException("Erreur réseau: ${cause.message}")
}

