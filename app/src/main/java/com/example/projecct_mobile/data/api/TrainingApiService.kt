package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les entraînements et NFTs
 */
@JvmSuppressWildcards
interface TrainingApiService {
    
    /**
     * Soumettre une session d'entraînement
     * POST /training/submit
     */
    @POST("training/submit")
    suspend fun submitTraining(
        @Body request: SubmitTrainingRequest
    ): Response<SubmitTrainingResponse>
    
    /**
     * Récupérer toutes les sessions d'un acteur
     * GET /training/actor/:acteurId/sessions
     */
    @GET("training/actor/{acteurId}/sessions")
    suspend fun getActorSessions(
        @Path("acteurId") acteurId: String
    ): Response<List<TrainingSession>>
    
    /**
     * Récupérer une session spécifique
     * GET /training/session/:sessionId
     */
    @GET("training/session/{sessionId}")
    suspend fun getSession(
        @Path("sessionId") sessionId: String
    ): Response<TrainingSession>
    
    /**
     * Récupérer tous les NFTs d'un acteur
     * GET /training/actor/:acteurId/nfts
     */
    @GET("training/actor/{acteurId}/nfts")
    suspend fun getActorNFTs(
        @Path("acteurId") acteurId: String
    ): Response<List<NFTReward>>
    
    /**
     * Initier le transfert d'un NFT vers HashPack
     * POST /training/nft/:nftId/transfer
     */
    @POST("training/nft/{nftId}/transfer")
    suspend fun transferNFT(
        @Path("nftId") nftId: String
    ): Response<NFTTransferResponse>
    
    /**
     * Récupérer les métadonnées complètes d'un NFT (Public)
     * GET /training/nft-metadata/:tokenId/:serialNumber
     */
    @GET("training/nft-metadata/{tokenId}/{serialNumber}")
    suspend fun getNFTMetadata(
        @Path("tokenId") tokenId: String,
        @Path("serialNumber") serialNumber: String
    ): Response<NFTMetadata>
    
    /**
     * Enregistrer l'adresse HashPack d'un acteur
     * PATCH /acteur/:id/hashpack
     */
    @PATCH("acteur/{id}/hashpack")
    suspend fun updateHashpackAddress(
        @Path("id") id: String,
        @Body request: HashpackAddressRequest
    ): Response<ActeurProfile>
}

/**
 * Requête pour enregistrer l'adresse HashPack
 */
data class HashpackAddressRequest(
    @com.google.gson.annotations.SerializedName("hashpackAccountId")
    val hashpackAccountId: String
)

