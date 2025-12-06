package com.example.projecct_mobile.data.repository

import android.util.Log
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les entraînements et NFTs
 */
class TrainingRepository {
    
    companion object {
        private const val TAG = "TrainingRepository"
    }
    
    private val trainingService = ApiClient.getTrainingService()
    
    /**
     * Soumettre une session d'entraînement
     */
    suspend fun submitTraining(request: SubmitTrainingRequest): Result<SubmitTrainingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📤 Soumission d'une session d'entraînement - Niveau ${request.niveau}, Score: ${request.globalScore}")
                
                val response = trainingService.submitTraining(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Log.d(TAG, "✅ Session soumise avec succès - ID: ${result.session.actualId}")
                    if (result.nftReward != null) {
                        Log.d(TAG, "🎉 NFT créé! Token ID: ${result.nftReward.tokenId}")
                    }
                    Result.success(result)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Log.e(TAG, "❌ Erreur lors de la soumission: ${response.code()} - $errorBody")
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception lors de la soumission: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Récupérer toutes les sessions d'un acteur
     */
    suspend fun getActorSessions(acteurId: String): Result<List<TrainingSession>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Récupération des sessions pour l'acteur: $acteurId")
                
                val response = trainingService.getActorSessions(acteurId)
                
                if (response.isSuccessful && response.body() != null) {
                    val sessions = response.body()!!
                    Log.d(TAG, "✅ ${sessions.size} sessions récupérées")
                    Result.success(sessions)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Log.e(TAG, "❌ Erreur lors de la récupération: ${response.code()} - $errorBody")
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception lors de la récupération: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Récupérer une session spécifique
     */
    suspend fun getSession(sessionId: String): Result<TrainingSession> {
        return withContext(Dispatchers.IO) {
            try {
                val response = trainingService.getSession(sessionId)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Récupérer tous les NFTs d'un acteur
     */
    suspend fun getActorNFTs(acteurId: String): Result<List<NFTReward>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Récupération des NFTs pour l'acteur: $acteurId")
                
                val response = trainingService.getActorNFTs(acteurId)
                
                if (response.isSuccessful && response.body() != null) {
                    val nfts = response.body()!!
                    Log.d(TAG, "✅ ${nfts.size} NFTs récupérés")
                    Result.success(nfts)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Log.e(TAG, "❌ Erreur lors de la récupération: ${response.code()} - $errorBody")
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception lors de la récupération: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Initier le transfert d'un NFT vers HashPack
     */
    suspend fun transferNFT(nftId: String): Result<NFTTransferResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = trainingService.transferNFT(nftId)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Récupérer les métadonnées d'un NFT
     */
    suspend fun getNFTMetadata(tokenId: String, serialNumber: String): Result<NFTMetadata> {
        return withContext(Dispatchers.IO) {
            try {
                val response = trainingService.getNFTMetadata(tokenId, serialNumber)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Enregistrer l'adresse HashPack d'un acteur
     */
    suspend fun updateHashpackAddress(acteurId: String, hashpackAccountId: String): Result<ActeurProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val request = com.example.projecct_mobile.data.api.HashpackAddressRequest(hashpackAccountId)
                val response = trainingService.updateHashpackAddress(acteurId, request)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                    Result.failure(Exception("Erreur ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

