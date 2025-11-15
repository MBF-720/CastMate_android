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
            
            android.util.Log.d("ActeurRepository", "✅ ID utilisateur récupéré depuis le token: $userId")
            android.util.Log.d("ActeurRepository", "📞 Appel de getActeurById($userId)")
            
            // 2. Récupérer le profil acteur avec cet ID
            val acteurResult = getActeurById(userId)
            
            if (acteurResult.isSuccess) {
                val profile = acteurResult.getOrNull()
                if (profile != null) {
                    android.util.Log.d("ActeurRepository", "✅ Profil désérialisé: ${profile.nom} ${profile.prenom}")
                    android.util.Log.d("ActeurRepository", "✅ Profil ID depuis actualId: ${profile.actualId}")
                    android.util.Log.d("ActeurRepository", "✅ Profil ID depuis id: ${profile.id}")
                    android.util.Log.d("ActeurRepository", "✅ Profil ID depuis idAlt: ${profile.idAlt}")
                    android.util.Log.d("ActeurRepository", "✅ ID utilisateur depuis token: $userId")
                    
                    // Vérifier si l'ID du profil correspond à l'ID du token
                    if (profile.actualId != userId) {
                        android.util.Log.e("ActeurRepository", "⚠️⚠️⚠️ ATTENTION: L'ID du profil (${profile.actualId}) ne correspond pas à l'ID du token ($userId) ⚠️⚠️⚠️")
                        android.util.Log.e("ActeurRepository", "⚠️ Le backend vérifie probablement l'ID dans le token, pas l'ID dans l'URL")
                    } else {
                        android.util.Log.d("ActeurRepository", "✅ Les IDs correspondent: $userId = ${profile.actualId}")
                    }
                    
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
     * PRIORITÉ: Utiliser l'ID depuis le token JWT décodé (le plus fiable)
     * Fallback: Utiliser l'ID stocké dans le TokenManager
     */
    private suspend fun getStoredActeurId(): String? {
        // 1. Essayer d'extraire l'ID depuis le token JWT décodé (le plus fiable)
        val tokenId = tokenManager.getUserIdFromToken()
        if (!tokenId.isNullOrBlank()) {
            android.util.Log.d("ActeurRepository", "✅ ID extrait du token JWT: $tokenId")
            return tokenId
        }
        
        // 2. Fallback: utiliser l'ID stocké dans le TokenManager
        val storedId = tokenManager.getUserIdSync()
        if (!storedId.isNullOrBlank()) {
            android.util.Log.d("ActeurRepository", "✅ ID depuis TokenManager: $storedId")
            return storedId
        }
        
        android.util.Log.e("ActeurRepository", "❌ Aucun ID disponible (ni depuis le token JWT, ni depuis TokenManager)")
        return null
    }
    
    /**
     * Récupère l'ID de l'acteur depuis le token JWT (pour usage externe)
     */
    suspend fun getCurrentActeurId(): String? {
        return getStoredActeurId()
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
    suspend fun updateCurrentActeur(request: UpdateActeurRequest, acteurId: String? = null): Result<ActeurProfile> {
        return try {
            // IMPORTANT: Le backend n'a pas d'endpoint /me
            // Le backend vérifie que l'ID dans le token (sub) correspond à l'ID de l'acteur dans l'URL
            // Mais les IDs correspondent et le backend retourne quand même 403
            // Cela signifie que le backend vérifie probablement autre chose
            // 
            // SOLUTION: Utiliser l'ID depuis le token JWT directement (sub)
            // Le backend devrait vérifier que token.sub == acteur._id
            
            // 1. Récupérer l'ID depuis le token JWT (sub) - c'est l'ID que le backend utilise
            val tokenId = getStoredActeurId()
            if (tokenId.isNullOrBlank()) {
                android.util.Log.e("ActeurRepository", "❌ ID du token JWT est null ou vide")
                return Result.failure(
                    ApiException.UnauthorizedException("Impossible de récupérer l'ID depuis le token JWT")
                )
            }
            
            android.util.Log.d("ActeurRepository", "🔑 ID depuis le token JWT (sub): $tokenId")
            
            // 2. Si un ID est fourni, vérifier s'il correspond à l'ID du token
            val providedId = acteurId
            if (providedId != null && providedId.isNotBlank()) {
                android.util.Log.d("ActeurRepository", "📞 ID fourni: $providedId")
                android.util.Log.d("ActeurRepository", "📞 ID du token: $tokenId")
                android.util.Log.d("ActeurRepository", "📞 IDs correspondent: ${providedId == tokenId}")
                
                // Si l'ID fourni correspond à l'ID du token, l'utiliser
                if (providedId == tokenId) {
                    android.util.Log.d("ActeurRepository", "✅ Utilisation de l'ID fourni (correspond au token): $providedId")
                    return updateActeur(providedId, request)
                } else {
                    android.util.Log.w("ActeurRepository", "⚠️ L'ID fourni ($providedId) ne correspond pas à l'ID du token ($tokenId)")
                    android.util.Log.w("ActeurRepository", "⚠️ Utilisation de l'ID du token à la place pour éviter l'erreur 403")
                }
            }
            
            // 3. Utiliser l'ID depuis le token JWT (sub) - c'est l'ID que le backend vérifie
            // Le backend doit vérifier que token.sub == acteur._id
            android.util.Log.d("ActeurRepository", "📞 Mise à jour du profil avec l'ID du token JWT: $tokenId")
            android.util.Log.d("ActeurRepository", "📞 Le backend doit vérifier que token.sub ($tokenId) correspond à l'ID de l'acteur dans l'URL")
            
            // Mettre à jour le profil avec l'ID du token
            updateActeur(tokenId, request)
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            android.util.Log.e("ActeurRepository", "❌ ApiException lors de la mise à jour: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "❌ Exception lors de la mise à jour: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur lors de la mise à jour: ${e.message}"))
        }
    }
    
    /**
     * Met à jour le profil d'un acteur par ID
     */
    suspend fun updateActeur(id: String, request: UpdateActeurRequest): Result<ActeurProfile> {
        return try {
            android.util.Log.d("ActeurRepository", "📞 Appel de updateActeur avec l'ID: $id")
            android.util.Log.d("ActeurRepository", "📞 Request: $request")
            val response = acteurService.updateActeur(id, request)
            android.util.Log.d("ActeurRepository", "📞 Réponse reçue: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val updatedProfile = response.body()!!
                android.util.Log.d("ActeurRepository", "✅ Profil mis à jour - ID dans la réponse: ${updatedProfile.actualId}")
                android.util.Log.d("ActeurRepository", "✅ Profil mis à jour - ID depuis id: ${updatedProfile.id}")
                android.util.Log.d("ActeurRepository", "✅ Profil mis à jour - ID depuis idAlt: ${updatedProfile.idAlt}")
                Result.success(updatedProfile)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ActeurRepository", "❌ Erreur mise à jour profil: code=${response.code()}, message=$errorBody")
                android.util.Log.e("ActeurRepository", "❌ ID utilisé pour la mise à jour: $id")
                
                // Logger l'ID du token pour le débogage
                val tokenId = getStoredActeurId()
                android.util.Log.e("ActeurRepository", "❌ ID depuis le token JWT (sub): $tokenId")
                android.util.Log.e("ActeurRepository", "❌ ID utilisé dans l'URL: $id")
                android.util.Log.e("ActeurRepository", "❌ IDs correspondent: ${tokenId == id}")
                
                // IMPORTANT: Le backend vérifie que l'ID dans le token (sub) correspond à l'ID de l'acteur
                // Mais les IDs correspondent et le backend retourne quand même 403
                // Cela signifie que le backend vérifie probablement autre chose, comme :
                // 1. L'ID dans le token correspond à l'ID de l'acteur dans la base de données (vérification côté serveur)
                // 2. L'email dans le token correspond à l'email de l'acteur
                // 3. Le backend utilise un système de vérification différent
                
                // Pour résoudre ce problème, nous devons vérifier comment le backend vérifie la propriété
                // Peut-être que le backend vérifie que l'ID dans le token correspond à l'ID de l'acteur
                // mais il y a un problème avec la façon dont le backend extrait l'ID du token
                
                // Gérer les différents codes d'erreur
                when (response.code()) {
                    403 -> {
                        android.util.Log.e("ActeurRepository", "❌❌❌ ERREUR 403: Le backend rejette la requête malgré l'ID correspondant ❌❌❌")
                        android.util.Log.e("ActeurRepository", "❌ ID du token (sub): $tokenId")
                        android.util.Log.e("ActeurRepository", "❌ ID utilisé dans l'URL: $id")
                        android.util.Log.e("ActeurRepository", "❌ Les IDs correspondent, mais le backend retourne 403")
                        android.util.Log.e("ActeurRepository", "❌ Cela signifie que le backend vérifie probablement autre chose")
                        android.util.Log.e("ActeurRepository", "❌ SOLUTION: Le backend doit vérifier que l'ID dans le token (sub) correspond à l'ID de l'acteur")
                        android.util.Log.e("ActeurRepository", "❌ OU: Le backend doit utiliser un endpoint /me qui utilise automatiquement l'ID du token")
                        
                        Result.failure(
                            ApiException.ForbiddenException(errorBody ?: "Vous n'avez pas la permission de modifier ce profil. Le backend rejette la requête malgré l'ID correspondant. Vérifiez la configuration du backend.")
                        )
                    }
                    404 -> Result.failure(
                        ApiException.NotFoundException(errorBody ?: "Profil introuvable")
                    )
                    else -> Result.failure(
                        ApiException.BadRequestException(
                            errorBody ?: "Erreur lors de la mise à jour"
                        )
                    )
                }
            }
        } catch (e: ApiException.CanceledException) {
            // Les requêtes annulées ne sont pas des erreurs - retourner un Result.failure silencieux
            Result.failure(e)
        } catch (e: ApiException) {
            android.util.Log.e("ActeurRepository", "❌ ApiException lors de la mise à jour: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "❌ Exception lors de la mise à jour: ${e.message}", e)
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
            android.util.Log.d("ActeurRepository", "📤 Début de l'upload du média pour l'ID: $id")
            android.util.Log.d("ActeurRepository", "📤 Photo fournie: ${photoFile != null}")
            android.util.Log.d("ActeurRepository", "📤 Document fourni: ${documentFile != null}")
            
            if (photoFile == null && documentFile == null) {
                android.util.Log.e("ActeurRepository", "❌ Aucun fichier fourni pour l'upload")
                return Result.failure(
                    ApiException.BadRequestException("Au moins un fichier (photo ou document) doit être fourni")
                )
            }
            
            val photoPart = photoFile?.let { 
                android.util.Log.d("ActeurRepository", "📤 Création du part photo: ${it.name}, taille: ${it.length()} bytes")
                createFilePart("photo", it) 
            }
            val documentPart = documentFile?.let { 
                android.util.Log.d("ActeurRepository", "📤 Création du part document: ${it.name}, taille: ${it.length()} bytes")
                createFilePart("document", it, "application/pdf") 
            }
            
            android.util.Log.d("ActeurRepository", "📤 Appel de updateProfileMedia avec l'ID: $id")
            val response = acteurService.updateProfileMedia(id, photoPart, documentPart)
            android.util.Log.d("ActeurRepository", "📤 Réponse reçue: code=${response.code()}, isSuccessful=${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ActeurRepository", "✅ Média uploadé avec succès")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = errorBody ?: response.message()
                android.util.Log.e("ActeurRepository", "❌ Erreur upload média: code=${response.code()}, message=$message")
                
                // Gérer les différents codes d'erreur
                when (response.code()) {
                    403 -> Result.failure(
                        ApiException.ForbiddenException(message ?: "Vous n'avez pas la permission de modifier ce profil")
                    )
                    404 -> Result.failure(
                        ApiException.NotFoundException(message ?: "Profil introuvable")
                    )
                    else -> Result.failure(
                        ApiException.BadRequestException(message ?: "Erreur mise à jour média")
                    )
                }
            }
        } catch (e: ApiException) {
            android.util.Log.e("ActeurRepository", "❌ ApiException lors de l'upload: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("ActeurRepository", "❌ Exception lors de l'upload: ${e.message}", e)
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
                val exception = when (response.code()) {
                    403 -> ApiException.ForbiddenException(
                        errorBody ?: "Accès refusé à ce fichier"
                    )
                    404 -> ApiException.NotFoundException(
                        errorBody ?: "Média introuvable"
                    )
                    else -> ApiException.UnknownException(
                        errorBody ?: "Erreur HTTP ${response.code()}"
                    )
                }
                Result.failure(exception)
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
    
    /**
     * Ajoute un casting aux favoris d'un acteur.
     */
    suspend fun addFavorite(
        id: String,
        castingId: String
    ): Result<Unit> {
        return try {
            val response = acteurService.addFavorite(id, castingId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(
                    ApiException.BadRequestException(message ?: "Erreur lors de l'ajout aux favoris")
                )
            }
        } catch (e: java.io.IOException) {
            // Gérer les erreurs de connexion réseau
            if (e.message?.contains("Canceled", ignoreCase = true) == true || 
                e.message?.contains("canceled", ignoreCase = true) == true) {
                android.util.Log.d("ActeurRepository", "⚠️ Requête annulée (normal)")
                Result.failure(ApiException.CanceledException("Requête annulée"))
            } else {
                android.util.Log.e("ActeurRepository", "❌ Erreur réseau: ${e.message}")
                Result.failure(ApiException.NetworkException("Erreur de connexion réseau: ${e.message}"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Retire un casting des favoris d'un acteur.
     */
    suspend fun removeFavorite(
        id: String,
        castingId: String
    ): Result<Unit> {
        return try {
            val response = acteurService.removeFavorite(id, castingId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(
                    ApiException.BadRequestException(message ?: "Erreur lors de la suppression des favoris")
                )
            }
        } catch (e: java.io.IOException) {
            // Gérer les erreurs de connexion réseau
            if (e.message?.contains("Canceled", ignoreCase = true) == true || 
                e.message?.contains("canceled", ignoreCase = true) == true) {
                android.util.Log.d("ActeurRepository", "⚠️ Requête annulée (normal)")
                Result.failure(ApiException.CanceledException("Requête annulée"))
            } else {
                android.util.Log.e("ActeurRepository", "❌ Erreur réseau: ${e.message}")
                Result.failure(ApiException.NetworkException("Erreur de connexion réseau: ${e.message}"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Consulte la liste des favoris d'un acteur.
     */
    suspend fun getFavorites(id: String): Result<List<com.example.projecct_mobile.data.model.Casting>> {
        return try {
            val response = acteurService.getFavorites(id)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val message = response.errorBody()?.string() ?: response.message()
                Result.failure(
                    ApiException.BadRequestException(message ?: "Erreur lors de la récupération des favoris")
                )
            }
        } catch (e: java.io.IOException) {
            // Gérer les erreurs de connexion réseau
            if (e.message?.contains("Canceled", ignoreCase = true) == true || 
                e.message?.contains("canceled", ignoreCase = true) == true) {
                android.util.Log.d("ActeurRepository", "⚠️ Requête annulée (normal)")
                Result.failure(ApiException.CanceledException("Requête annulée"))
            } else {
                android.util.Log.e("ActeurRepository", "❌ Erreur réseau: ${e.message}")
                Result.failure(ApiException.NetworkException("Erreur de connexion réseau: ${e.message}"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
}

