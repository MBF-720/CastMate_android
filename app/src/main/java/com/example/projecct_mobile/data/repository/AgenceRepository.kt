package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.AgenceApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.AgenceProfile
import com.example.projecct_mobile.data.local.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream
import java.net.URLConnection

/**
 * Repository pour gérer les agences
 */
class AgenceRepository {
    
    private val agenceService: AgenceApiService = ApiClient.getAgenceService()
    private val tokenManager: TokenManager = ApiClient.getTokenManager()
    
    /**
     * Récupère le profil de l'agence connectée
     * Récupère d'abord l'utilisateur actuel pour obtenir son ID, puis appelle /agence/{id}
     */
    suspend fun getCurrentAgence(): Result<AgenceProfile> {
        return try {
            android.util.Log.d("AgenceRepository", "📞 Appel de getCurrentAgence() - récupération de l'ID utilisateur")
            
            // 1. Récupérer l'utilisateur actuel pour obtenir son ID
            val userRepository = UserRepository()
            val userResult = userRepository.getCurrentUser()
            
            if (!userResult.isSuccess) {
                android.util.Log.e("AgenceRepository", "❌ Impossible de récupérer l'utilisateur actuel: ${userResult.exceptionOrNull()?.message}")
                return Result.failure(
                    ApiException.NotFoundException("Impossible de récupérer l'utilisateur actuel")
                )
            }
            
            val user = userResult.getOrNull()
            if (user == null) {
                android.util.Log.e("AgenceRepository", "❌ Utilisateur actuel est null")
                return Result.failure(
                    ApiException.NotFoundException("Utilisateur actuel introuvable")
                )
            }
            
            val userId = user.actualId
            if (userId.isNullOrBlank()) {
                android.util.Log.e("AgenceRepository", "❌ ID utilisateur est null ou vide")
                return Result.failure(
                    ApiException.NotFoundException("ID utilisateur introuvable")
                )
            }
            
            android.util.Log.d("AgenceRepository", "✅ ID utilisateur récupéré depuis le token: $userId")
            android.util.Log.d("AgenceRepository", "📞 Appel de getAgenceById($userId)")
            
            // 2. Récupérer le profil agence avec cet ID
            val agenceResult = getAgenceById(userId)
            
            if (agenceResult.isSuccess) {
                val profile = agenceResult.getOrNull()
                if (profile != null) {
                    android.util.Log.d("AgenceRepository", "✅ Profil agence chargé: ${profile.nomAgence}")
                    android.util.Log.d("AgenceRepository", "✅ photoFileId: '${profile.media?.photoFileId}'")
                    android.util.Log.d("AgenceRepository", "✅ documentFileId: '${profile.media?.documentFileId}'")
                }
            } else {
                val exception = agenceResult.exceptionOrNull()
                android.util.Log.e("AgenceRepository", "❌ Erreur getAgenceById: ${exception?.message}", exception)
            }
            
            agenceResult
        } catch (e: ApiException.CanceledException) {
            android.util.Log.w("AgenceRepository", "⚠️ getCurrentAgence annulé")
            Result.failure(e)
        } catch (e: ApiException) {
            android.util.Log.e("AgenceRepository", "ApiException lors du chargement: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("AgenceRepository", "Exception lors du chargement: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur chargement: ${e.message}"))
        }
    }
    
    /**
     * Récupère une agence par son ID
     */
    suspend fun getAgenceById(id: String): Result<AgenceProfile> {
        return try {
            android.util.Log.d("AgenceRepository", "📞 Appel de getAgenceById($id)")
            
            val response = agenceService.getAgenceById(id)
            
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                android.util.Log.d("AgenceRepository", "✅ Profil agence chargé: ${profile.nomAgence}")
                android.util.Log.d("AgenceRepository", "✅ socialLinks: ${profile.socialLinks}")
                android.util.Log.d("AgenceRepository", "✅ socialLinks?.facebook: '${profile.socialLinks?.facebook}'")
                android.util.Log.d("AgenceRepository", "✅ socialLinks?.instagram: '${profile.socialLinks?.instagram}'")
                Result.success(profile)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AgenceRepository", "❌ Erreur getAgenceById (code ${response.code()}): ${errorBody ?: response.message()}")
                
                when (response.code()) {
                    400 -> Result.failure(
                        ApiException.BadRequestException(
                            errorBody ?: "ID invalide"
                        )
                    )
                    404 -> Result.failure(
                        ApiException.NotFoundException(
                            errorBody ?: "Agence introuvable"
                        )
                    )
                    else -> Result.failure(
                        ApiException.UnknownException(
                            errorBody ?: "Erreur lors du chargement du profil agence"
                        )
                    )
                }
            }
        } catch (e: ApiException) {
            android.util.Log.e("AgenceRepository", "ApiException lors du chargement: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("AgenceRepository", "Exception lors du chargement: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur chargement: ${e.message}"))
        }
    }
    
    /**
     * Télécharge un média stocké dans GridFS.
     */
    suspend fun downloadMedia(fileId: String): Result<ByteArray> {
        return try {
            android.util.Log.d("AgenceRepository", "📥 Téléchargement média: $fileId")
            
            val response = agenceService.downloadMedia(fileId)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody: ResponseBody = response.body()!!
                val inputStream: InputStream = responseBody.byteStream()
                val bytes = inputStream.readBytes()
                android.util.Log.d("AgenceRepository", "✅ Média téléchargé: ${bytes.size} bytes")
                Result.success(bytes)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AgenceRepository", "❌ Erreur téléchargement: ${errorBody ?: response.message()}")
                Result.failure(
                    ApiException.NotFoundException(
                        errorBody ?: "Média introuvable"
                    )
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("AgenceRepository", "ApiException lors du téléchargement: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("AgenceRepository", "Exception lors du téléchargement: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur téléchargement: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil d'une agence
     */
    suspend fun updateAgence(
        id: String,
        request: com.example.projecct_mobile.data.model.UpdateAgenceRequest
    ): Result<AgenceProfile> {
        return try {
            android.util.Log.d("AgenceRepository", "📤 Mise à jour agence ID: $id")
            val response = agenceService.updateAgence(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                android.util.Log.d("AgenceRepository", "✅ Agence mise à jour avec succès")
                Result.success(profile)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AgenceRepository", "❌ Erreur updateAgence (code ${response.code()}): ${errorBody ?: response.message()}")
                Result.failure(
                    ApiException.BadRequestException(
                        errorBody ?: "Erreur lors de la mise à jour"
                    )
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("AgenceRepository", "ApiException lors de la mise à jour: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("AgenceRepository", "Exception lors de la mise à jour: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le logo et/ou le document d'une agence.
     */
    suspend fun updateProfileMedia(
        id: String,
        logoFile: File? = null,
        documentFile: File? = null
    ): Result<AgenceProfile> {
        return try {
            android.util.Log.d("AgenceRepository", "📤 Début de l'upload du média pour l'ID: $id")
            android.util.Log.d("AgenceRepository", "📤 Logo fourni: ${logoFile != null}")
            android.util.Log.d("AgenceRepository", "📤 Document fourni: ${documentFile != null}")
            
            if (logoFile == null && documentFile == null) {
                android.util.Log.e("AgenceRepository", "❌ Aucun fichier fourni pour l'upload")
                return Result.failure(
                    ApiException.BadRequestException("Au moins un fichier (logo ou document) doit être fourni")
                )
            }
            
            val logoPart = logoFile?.let { 
                android.util.Log.d("AgenceRepository", "📤 Création du part logo: ${it.name}, taille: ${it.length()} bytes")
                createFilePart("photo", it) 
            }
            val documentPart = documentFile?.let { 
                android.util.Log.d("AgenceRepository", "📤 Création du part document: ${it.name}, taille: ${it.length()} bytes")
                createFilePart("document", it, "application/pdf") 
            }
            
            android.util.Log.d("AgenceRepository", "📤 Appel de updateProfileMedia avec l'ID: $id")
            val response = agenceService.updateProfileMedia(id, logoPart, documentPart)
            
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                android.util.Log.d("AgenceRepository", "✅ Média mis à jour avec succès")
                Result.success(profile)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AgenceRepository", "❌ Erreur updateProfileMedia: ${errorBody ?: response.message()}")
                Result.failure(
                    ApiException.BadRequestException(
                        errorBody ?: "Erreur lors de la mise à jour du média"
                    )
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("AgenceRepository", "ApiException lors de la mise à jour: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("AgenceRepository", "Exception lors de la mise à jour: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    private fun createFilePart(
        fieldName: String,
        file: File,
        forcedMimeType: String? = null
    ): MultipartBody.Part {
        val mimeType = forcedMimeType ?: guessMimeType(file) ?: "application/octet-stream"
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
    }
    
    private fun guessMimeType(file: File): String? {
        return URLConnection.guessContentTypeFromName(file.name)
    }
}

