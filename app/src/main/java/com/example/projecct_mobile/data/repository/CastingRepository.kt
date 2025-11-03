package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.CastingApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.CreateCastingRequest

/**
 * Repository pour gérer les castings
 */
class CastingRepository {
    
    private val castingService: CastingApiService = ApiClient.getCastingService()
    
    /**
     * Récupère tous les castings (route publique)
     * 
     * @return Result<List<Casting>> Liste de tous les castings
     */
    suspend fun getAllCastings(): Result<List<Casting>> {
        return try {
            val response = castingService.getAllCastings()
            
            android.util.Log.d("CastingRepository", "Response code: ${response.code()}")
            android.util.Log.d("CastingRepository", "Response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    android.util.Log.d("CastingRepository", "Nombre de castings: ${body.size}")
                    Result.success(body)
                } else {
                    android.util.Log.w("CastingRepository", "Response body is null")
                    Result.success(emptyList())
                }
            } else {
                val errorMsg = "Erreur ${response.code()}: ${response.message()}"
                android.util.Log.e("CastingRepository", errorMsg)
                Result.failure(
                    ApiException.UnknownException(errorMsg)
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("CastingRepository", "ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("CastingRepository", "Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Récupère un casting par son ID (route publique)
     * 
     * @param id ID du casting
     * @return Result<Casting> Le casting trouvé
     */
    suspend fun getCastingById(id: String): Result<Casting> {
        return try {
            val response = castingService.getCastingById(id)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    ApiException.NotFoundException("Casting non trouvé")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Crée un nouveau casting (route protégée - nécessite un token JWT)
     * 
     * @param titre Titre du casting (obligatoire)
     * @param descriptionRole Description du rôle (optionnel)
     * @param synopsis Synopsis du projet (optionnel)
     * @param lieu Lieu du casting (optionnel)
     * @param dateDebut Date de début format "YYYY-MM-DD" (optionnel)
     * @param dateFin Date de fin format "YYYY-MM-DD" (optionnel)
     * @param remuneration Rémunération (optionnel, ex: "1000€/jour")
     * @param conditions Conditions du casting (optionnel)
     * 
     * @return Result<Casting> Le casting créé
     * 
     * Exemple d'utilisation :
     * ```kotlin
     * val result = castingRepository.createCasting(
     *     titre = "Recherche acteur principal",
     *     descriptionRole = "Rôle de protagoniste dans une série",
     *     synopsis = "Synopsis du projet...",
     *     lieu = "Paris",
     *     dateDebut = "2024-01-15",
     *     dateFin = "2024-02-15",
     *     remuneration = "1000€/jour",
     *     conditions = "Disponibilité totale requise"
     * )
     * ```
     */
    suspend fun createCasting(
        titre: String,
        descriptionRole: String? = null,
        synopsis: String? = null,
        lieu: String? = null,
        dateDebut: String? = null,
        dateFin: String? = null,
        remuneration: String? = null,
        conditions: String? = null
    ): Result<Casting> {
        return try {
            val request = CreateCastingRequest(
                titre = titre,
                descriptionRole = descriptionRole,
                synopsis = synopsis,
                lieu = lieu,
                dateDebut = dateDebut,
                dateFin = dateFin,
                remuneration = remuneration,
                conditions = conditions
            )
            
            val response = castingService.createCasting(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    ApiException.BadRequestException("Erreur lors de la création du casting")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Met à jour un casting (route protégée)
     */
    suspend fun updateCasting(
        id: String,
        titre: String,
        descriptionRole: String? = null,
        synopsis: String? = null,
        lieu: String? = null,
        dateDebut: String? = null,
        dateFin: String? = null,
        remuneration: String? = null,
        conditions: String? = null
    ): Result<Casting> {
        return try {
            val request = CreateCastingRequest(
                titre = titre,
                descriptionRole = descriptionRole,
                synopsis = synopsis,
                lieu = lieu,
                dateDebut = dateDebut,
                dateFin = dateFin,
                remuneration = remuneration,
                conditions = conditions
            )
            
            val response = castingService.updateCasting(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    ApiException.BadRequestException("Erreur lors de la mise à jour du casting")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Supprime un casting (route protégée)
     */
    suspend fun deleteCasting(id: String): Result<Unit> {
        return try {
            val response = castingService.deleteCasting(id)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiException.UnknownException("Erreur lors de la suppression du casting")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
}

