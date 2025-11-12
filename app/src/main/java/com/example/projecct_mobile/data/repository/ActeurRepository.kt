package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.ActeurApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.UpdateActeurRequest
import com.example.projecct_mobile.data.local.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLConnection

/**
 * Repository pour gérer les acteurs
 */
class ActeurRepository {
    
    private val acteurService: ActeurApiService = ApiClient.getActeurService()
    private val tokenManager: TokenManager = ApiClient.getTokenManager()
    
    /**
     * Récupère le profil de l'acteur connecté
     * Récupère d'abord l'utilisateur actuel pour obtenir son ID, puis appelle /acteur/{id}
     */
    suspend fun getCurrentActeur(): Result<ActeurProfile> {
        return try {
            android.util.Log.d("ActeurRepository", "📞 Appel de getCurrentActeur() - récupération de l'ID utilisateur")
            
            // 1. Récupérer l'utilisateur actuel pour obtenir son ID
            val userRepository = UserRepository()
            val userResult = userRepository.getCurrentUser()
            
            if (!userResult.isSuccess) {
                android.util.Log.e("ActeurRepository", "❌ Impossible de récupérer l'utilisateur actuel: ${userResult.exceptionOrNull()?.message}")
                return Result.failure(
                    ApiException.NotFoundException("Impossible de récupérer l'utilisateur actuel")
                )
            }
            
            val user = userResult.getOrNull()
            if (user == null) {
                android.util.Log.e("ActeurRepository", "❌ Utilisateur actuel est null")
                return Result.failure(
                    ApiException.NotFoundException("Utilisateur actuel introuvable")
                )
            }
            
            val userId = user.actualId
            if (userId.isNullOrBlank()) {
                android.util.Log.e("ActeurRepository", "❌ ID utilisateur est null ou vide")
                return Result.failure(
                    ApiException.NotFoundException("ID utilisateur introuvable")
                )
            }
            
            android.util.Log.d("ActeurRepository", "✅ ID utilisateur récupéré: $userId")
            android.util.Log.d("ActeurRepository", "📞 Appel de getActeurById($userId)")
            
            // 2. Récupérer le profil acteur avec cet ID
            val acteurResult = getActeurById(userId)
            
            if (acteurResult.isSuccess) {
                val profile = acteurResult.getOrNull()
                if (profile != null) {
                    android.util.Log.d("ActeurRepository", "✅ Profil désérialisé: ${profile.nom} ${profile.prenom}")
                    android.util.Log.d("ActeurRepository", "✅ Profil ID: ${profile.actualId}")
                    android.util.Log.d("ActeurRepository", "✅ Media object: ${profile.media}")
                    android.util.Log.d("ActeurRepository", "✅ Media is null: ${profile.media == null}")
                    if (profile.media != null) {
                        android.util.Log.d("ActeurRepository", "✅ photoFileId: '${profile.media?.photoFileId}'")
                        android.util.Log.d("ActeurRepository", "✅ photoFileId length: ${profile.media?.photoFileId?.length}")
                        android.util.Log.d("ActeurRepository", "✅ photoFileId isBlank: ${profile.media?.photoFileId.isNullOrBlank()}")
                        android.util.Log.d("ActeurRepository", "✅ documentFileId: '${profile.media?.documentFileId}'")
                        if (!profile.media?.photoFileId.isNullOrBlank()) {
                            android.util.Log.d("ActeurRepository", "✅✅✅ photoFileId VALIDE: '${profile.media?.photoFileId}' ✅✅✅")
                        } else {
                            android.util.Log.e("ActeurRepository", "❌❌❌ photoFileId est NULL ou VIDE! ❌❌❌")
                        }
                    } else {
                        android.util.Log.e("ActeurRepository", "❌❌❌ Media est NULL dans le profil acteur! ❌❌❌")
                    }
                }
            } else {
                val exception = acteurResult.exceptionOrNull()
                android.util.Log.e("ActeurRepository", "❌ Erreur getActeurById: ${exception?.message}", exception)
            }
            
            acteurResult
        } catch (e: ApiException.CanceledException) {
            android.util.Log.w("ActeurRepository", "⚠️ getCurrentActeur annulé")
            Result.failure(e)
        } catch (e: ApiException) {
            android.util.Log.e("ActeurRepository", "❌ ApiException getCurrentActeur: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "❌ Exception getCurrentActeur: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de la récupération du profil: ${e.message}"))
        }
    }
    
    /**
     * Extrait l'ID de l'acteur depuis le token JWT
     */
    private suspend fun getStoredActeurId(): String? {
        val storedId = tokenManager.getUserIdSync()
        if (!storedId.isNullOrBlank()) {
            return storedId
        }
        return null
    }
    
    /**
     * Récupère un acteur par son ID
     */
    suspend fun getActeurById(id: String): Result<ActeurProfile> {
        return try {
            val response = acteurService.getActeurById(id)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Si l'ID est invalide, retourner une erreur gracieuse
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody?.contains("ID invalide") == true || response.code() == 400) {
                    "L'ID fourni n'est pas valide pour récupérer le profil acteur"
                } else {
                    "Acteur non trouvé"
                }
                Result.failure(
                    ApiException.NotFoundException(errorMessage)
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            // Si c'est une erreur BadRequest avec "ID invalide", retourner une erreur gracieuse
            if (e is ApiException.BadRequestException && e.message?.contains("ID invalide") == true) {
                Result.failure(
                    ApiException.NotFoundException("L'ID fourni n'est pas valide pour récupérer le profil acteur")
                )
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Erreur getActeurById: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil de l'acteur connecté
     * Utilise l'ID depuis le token JWT pour appeler /acteur/:id
     */
    suspend fun updateCurrentActeur(request: UpdateActeurRequest): Result<ActeurProfile> {
        return try {
            // Ne pas appeler /acteur/me car il n'existe pas
            // Utiliser directement l'ID depuis le token
            val userId = getStoredActeurId()
            
            if (userId != null) {
                // Mettre à jour le profil avec l'ID
                updateActeur(userId, request)
            } else {
                // Si on ne peut pas obtenir l'ID, retourner une erreur gracieuse
                Result.failure(
                    ApiException.UnauthorizedException("Impossible de récupérer l'ID de l'acteur pour la mise à jour")
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Erreur lors de la mise à jour: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de la mise à jour: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil d'un acteur par ID
     */
    suspend fun updateActeur(id: String, request: UpdateActeurRequest): Result<ActeurProfile> {
        return try {
            val response = acteurService.updateActeur(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    ApiException.BadRequestException(
                        errorBody ?: "Erreur lors de la mise à jour"
                    )
                )
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }

    /**
     * Met à jour la photo de profil et/ou le CV d'un acteur.
     */
    suspend fun updateProfileMedia(
        id: String,
        photoFile: File? = null,
        documentFile: File? = null
    ): Result<ActeurProfile> {
        return try {
            val photoPart = photoFile?.let { createFilePart("photo", it) }
            val documentPart = documentFile?.let { createFilePart("document", it, "application/pdf") }
            val response = acteurService.updateProfileMedia(id, photoPart, documentPart)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(ApiException.BadRequestException(message ?: "Erreur mise à jour média"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }

    /**
     * Ajoute des photos à la galerie d'un acteur.
     */
    suspend fun addGalleryPhotos(
        id: String,
        photos: List<File>
    ): Result<ActeurProfile> {
        return try {
            if (photos.isEmpty()) {
                return Result.failure(ApiException.BadRequestException("Aucune photo fournie"))
            }

            val parts = photos.map { file ->
                createFilePart("photos", file)
            }

            val response = acteurService.addGalleryPhotos(id, parts)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(ApiException.BadRequestException(message ?: "Erreur ajout galerie"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }

    /**
     * Supprime une photo de la galerie d'un acteur.
     */
    suspend fun deleteGalleryPhoto(
        id: String,
        fileId: String
    ): Result<ActeurProfile> {
        return try {
            val response = acteurService.deleteGalleryPhoto(id, fileId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(ApiException.BadRequestException(message ?: "Erreur suppression galerie"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }

    /**
     * Télécharge un média GridFS et retourne le flux de réponse.
     */
    suspend fun downloadMedia(fileId: String): Result<ByteArray> {
        return try {
            android.util.Log.d("ActeurRepository", "Début du téléchargement du média: $fileId")
            val response = acteurService.downloadMedia(fileId)
            android.util.Log.d("ActeurRepository", "Réponse reçue: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("ActeurRepository", "Body reçu: ${body != null}")
                if (body != null) {
                    val bytes = body.bytes()
                    body.close()
                    android.util.Log.d("ActeurRepository", "Bytes téléchargés: ${bytes.size} bytes")
                    if (bytes.isNotEmpty()) {
                        Result.success(bytes)
                    } else {
                        android.util.Log.e("ActeurRepository", "Bytes vides")
                        Result.failure(
                            ApiException.UnknownException("Flux média vide")
                        )
                    }
                } else {
                    android.util.Log.e("ActeurRepository", "Body est null")
                    Result.failure(
                        ApiException.UnknownException("Flux média vide")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ActeurRepository", "Erreur HTTP ${response.code()}: $errorBody")
                Result.failure(
                    ApiException.NotFoundException(
                        errorBody ?: "Média introuvable"
                    )
                )
            }
        } catch (e: ApiException) {
            android.util.Log.e("ActeurRepository", "ApiException lors du téléchargement: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "Exception lors du téléchargement: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur téléchargement: ${e.message}"))
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

