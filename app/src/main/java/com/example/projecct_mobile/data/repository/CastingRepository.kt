package com.example.projecct_mobile.data.repository

import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.api.CastingApiService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.CandidateStatusResponse
import com.example.projecct_mobile.data.model.ChatbotQueryRequest
import com.example.projecct_mobile.data.model.ChatbotResponse
import com.example.projecct_mobile.data.model.CreateCastingRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLConnection

/**
 * Repository pour gérer les castings
 */
class CastingRepository {
    
    private val castingService: CastingApiService = ApiClient.getCastingService()
    private val gson = Gson()
    
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
            // Valider l'ID avant de faire la requête
            if (id.isBlank()) {
                android.util.Log.e("CastingRepository", "❌ ID de casting vide ou invalide")
                return Result.failure(
                    ApiException.BadRequestException("ID de casting invalide")
                )
            }
            
            android.util.Log.d("CastingRepository", "📞 Appel de getCastingById avec ID: $id")
            val response = castingService.getCastingById(id)
            android.util.Log.d("CastingRepository", "📞 Réponse reçue: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    in 500..599 -> {
                        val errorBody = response.errorBody()?.string()
                        "Erreur serveur ($errorCode): ${errorBody?.take(200) ?: "Erreur interne du serveur"}"
                    }
                    404 -> "Casting non trouvé"
                    else -> "Erreur ${errorCode}: ${response.message()}"
                }
                Result.failure(
                    when (errorCode) {
                        in 500..599 -> ApiException.ServerException(errorMessage)
                        404 -> ApiException.NotFoundException(errorMessage)
                        else -> ApiException.UnknownException(errorMessage)
                    }
                )
            }
        } catch (e: java.io.IOException) {
            // Gérer les requêtes annulées et autres IOException
            if (e.message?.contains("Canceled", ignoreCase = true) == true || 
                e.message?.contains("canceled", ignoreCase = true) == true) {
                android.util.Log.d("CastingRepository", "⚠️ Requête annulée (normal)")
                Result.failure(ApiException.CanceledException("Requête annulée"))
            } else {
                Result.failure(ApiException.NetworkException("Erreur de connexion réseau: ${e.message}"))
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
     * @param prix Prix du casting (optionnel)
     * @param types Types de casting (optionnel, ex: ["Cinéma", "Télévision"])
     * @param age Tranche d'âge (optionnel, ex: "25-35 ans")
     * @param ouvert Indique si le casting accepte des candidatures (optionnel, défaut: true)
     * @param conditions Conditions du casting (optionnel)
     * @param afficheFile Fichier affiche (optionnel)
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
     *     prix = 5000.0,
     *     types = listOf("Cinéma", "Télévision"),
     *     age = "25-35 ans",
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
        prix: Double? = null,
        types: List<String>? = null,
        age: String? = null,
        ouvert: Boolean? = true,
        conditions: String? = null,
        afficheFile: File? = null
    ): Result<Casting> {
        return try {
            val request = CreateCastingRequest(
                titre = titre,
                descriptionRole = descriptionRole,
                synopsis = synopsis,
                lieu = lieu,
                dateDebut = dateDebut,
                dateFin = dateFin,
                prix = prix,
                types = types?.takeIf { it.isNotEmpty() },
                age = age?.takeIf { it.isNotBlank() },
                ouvert = ouvert,
                conditions = conditions
            )
            
            val payloadJson = gson.toJson(request)
            val payloadBody = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val affichePart = afficheFile?.let { createFilePart("affiche", it) }

            val response = castingService.createCasting(payloadBody, affichePart)
            
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
        prix: Double? = null,
        types: List<String>? = null,
        age: String? = null,
        ouvert: Boolean? = null,
        conditions: String? = null,
        afficheFile: File? = null
    ): Result<Casting> {
        return try {
            val request = CreateCastingRequest(
                titre = titre,
                descriptionRole = descriptionRole,
                synopsis = synopsis,
                lieu = lieu,
                dateDebut = dateDebut,
                dateFin = dateFin,
                prix = prix,
                types = types?.takeIf { it.isNotEmpty() },
                age = age?.takeIf { it.isNotBlank() },
                ouvert = ouvert,
                conditions = conditions
            )
            
            val payloadJson = gson.toJson(request)
            val payloadBody = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val affichePart = afficheFile?.let { createFilePart("affiche", it) }

            val response = castingService.updateCasting(id, payloadBody, affichePart)
            
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

    private fun createFilePart(fieldName: String, file: File): MultipartBody.Part {
        val mimeType = guessMimeType(file) ?: "application/octet-stream"
        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, body)
    }

    /**
     * Postuler à un casting (route protégée - Acteur uniquement)
     */
    suspend fun applyToCasting(id: String): Result<Unit> {
        return try {
            android.util.Log.d("CastingRepository", "📝 Postulation au casting: $id")
            val response = castingService.applyToCasting(id)
            
            if (response.isSuccessful) {
                android.util.Log.d("CastingRepository", "✅ Candidature envoyée avec succès")
                Result.success(Unit)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CastingRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                // Extraire le message d'erreur du body si possible
                val errorMessage = try {
                    if (errorBody != null && errorBody.isNotBlank()) {
                        val jsonObject = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        jsonObject.get("message")?.asString ?: errorBody
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    errorBody
                }
                
                val exception = when (errorCode) {
                    401 -> ApiException.UnauthorizedException("Vous devez être connecté pour postuler")
                    403 -> ApiException.ForbiddenException("Vous ne pouvez pas postuler à ce casting")
                    404 -> ApiException.NotFoundException("Casting non trouvé")
                    409 -> ApiException.ConflictException(errorMessage ?: "Vous avez déjà postulé à ce casting")
                    400 -> ApiException.BadRequestException(errorMessage ?: "Erreur lors de la candidature")
                    in 500..599 -> ApiException.ServerException("Erreur serveur: ${errorMessage ?: errorBody}")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorMessage ?: errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("CastingRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("CastingRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Postuler à un casting avec vidéo d'audition et feedback IA
     * 
     * @param id ID du casting
     * @param context Context Android pour lire la vidéo
     * @param videoUri URI de la vidéo (optionnel)
     * @param aiFeedback Feedback IA en JSON string (optionnel)
     */
    suspend fun applyToCastingWithVideo(
        id: String,
        context: android.content.Context,
        videoUri: android.net.Uri? = null,
        aiFeedback: String? = null
    ): Result<Unit> {
        return try {
            android.util.Log.d("CastingRepository", "📝 Postulation au casting avec vidéo: $id")
            
            // Préparer les parts multipart
            var videoPart: MultipartBody.Part? = null
            var aiFeedbackBody: okhttp3.RequestBody? = null
            
            // Si vidéo fournie, créer la part multipart
            if (videoUri != null) {
                val inputStream = context.contentResolver.openInputStream(videoUri)
                    ?: throw Exception("Impossible de lire la vidéo")
                
                // Créer un fichier temporaire
                val tempFile = File.createTempFile("audition_video", ".mp4", context.cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                
                // Vérifier la taille (max 10 MB - limite stricte du backend Vercel/Firebase)
                val fileSizeBytes = tempFile.length()
                val fileSizeMB = fileSizeBytes / (1024.0 * 1024.0) // Conversion en Double
                val maxSizeMB = 10.0 // Réduit à 10 MB car le backend rejette même 20 MB
                if (fileSizeMB > maxSizeMB) {
                    tempFile.delete()
                    return Result.failure(
                        ApiException.BadRequestException(
                            "La vidéo est trop volumineuse (${String.format("%.1f", fileSizeMB)} MB). " +
                            "Maximum: ${maxSizeMB.toInt()} MB. Veuillez compresser ou réduire la durée de la vidéo."
                        )
                    )
                }
                
                android.util.Log.d("CastingRepository", "📹 Taille de la vidéo: ${String.format("%.1f", fileSizeMB)} MB")
                
                val requestFile = tempFile.asRequestBody(
                    URLConnection.guessContentTypeFromName(tempFile.name)?.toMediaTypeOrNull()
                        ?: "video/mp4".toMediaTypeOrNull()
                )
                
                videoPart = MultipartBody.Part.createFormData("video", tempFile.name, requestFile)
            }
            
            // Si feedback IA fourni, créer le RequestBody
            if (aiFeedback != null) {
                aiFeedbackBody = aiFeedback.toRequestBody("application/json".toMediaTypeOrNull())
            }
            
            val response = castingService.applyToCastingWithVideo(id, videoPart, aiFeedbackBody)
            
            if (response.isSuccessful) {
                android.util.Log.d("CastingRepository", "✅ Candidature avec vidéo envoyée avec succès")
                Result.success(Unit)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CastingRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val errorMessage = try {
                    if (errorBody != null && errorBody.isNotBlank()) {
                        val jsonObject = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        jsonObject.get("message")?.asString ?: errorBody
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    errorBody
                }
                
                val exception = when (errorCode) {
                    401 -> ApiException.UnauthorizedException("Vous devez être connecté pour postuler")
                    403 -> ApiException.ForbiddenException("Vous ne pouvez pas postuler à ce casting")
                    404 -> ApiException.NotFoundException("Casting non trouvé")
                    409 -> ApiException.ConflictException(errorMessage ?: "Vous avez déjà postulé à ce casting")
                    400 -> ApiException.BadRequestException(errorMessage ?: "Erreur lors de la candidature")
                    in 500..599 -> ApiException.ServerException("Erreur serveur: ${errorMessage ?: errorBody}")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorMessage ?: errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("CastingRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("CastingRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Accepter un candidat (route protégée - Recruteur/Admin uniquement)
     */
    suspend fun acceptCandidate(castingId: String, acteurId: String): Result<Unit> {
        return try {
            val response = castingService.acceptCandidate(castingId, acteurId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiException.BadRequestException("Erreur lors de l'acceptation du candidat")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Accepter un candidat avec interview (route protégée - Recruteur/Admin uniquement)
     * 
     * @param castingId ID du casting
     * @param acteurId ID de l'acteur
     * @param dates Liste de EXACTEMENT 3 dates avec heures (format: "YYYY-MM-DD" et "HH:mm")
     * @return Result<AcceptCandidateWithInterviewResponse>
     */
    suspend fun acceptCandidateWithInterview(
        castingId: String,
        acteurId: String,
        dates: List<com.example.projecct_mobile.data.model.InterviewDateOption>
    ): Result<com.example.projecct_mobile.data.model.AcceptCandidateWithInterviewResponse> {
        return try {
            // Validation: doit avoir exactement 3 dates
            if (dates.size != 3) {
                return Result.failure(
                    ApiException.BadRequestException("Vous devez proposer exactement 3 dates")
                )
            }
            
            // Validation: toutes les dates doivent avoir une heure
            if (dates.any { it.time.isNullOrBlank() }) {
                return Result.failure(
                    ApiException.BadRequestException("Chaque date doit avoir une heure")
                )
            }
            
            val request = com.example.projecct_mobile.data.model.AcceptCandidateWithInterviewRequest(
                proposedDates = dates
            )
            
            val response = castingService.acceptCandidateWithInterview(castingId, acteurId, request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    android.util.Log.d("CastingRepository", "✅ Candidat accepté avec interview créée")
                    Result.success(body)
                } else {
                    Result.failure(ApiException.UnknownException("Réponse vide"))
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CastingRepository", "❌ Erreur ${errorCode}: $errorBody")
                
                val exception = when (errorCode) {
                    400 -> ApiException.BadRequestException("Requête invalide: ${errorBody ?: "Dates invalides"}")
                    401 -> ApiException.UnauthorizedException("Non autorisé")
                    403 -> ApiException.ForbiddenException("Accès refusé")
                    404 -> ApiException.NotFoundException("Casting ou candidat non trouvé")
                    else -> ApiException.UnknownException("Erreur ${errorCode}: ${errorBody ?: response.message()}")
                }
                Result.failure(exception)
            }
        } catch (e: ApiException) {
            android.util.Log.e("CastingRepository", "❌ ApiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("CastingRepository", "❌ Exception: ${e.message}", e)
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Refuser un candidat (route protégée - Recruteur/Admin uniquement)
     */
    suspend fun rejectCandidate(castingId: String, acteurId: String): Result<Unit> {
        return try {
            val response = castingService.rejectCandidate(castingId, acteurId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiException.BadRequestException("Erreur lors du refus du candidat")
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }
    
    /**
     * Obtenir le statut de candidature de l'acteur connecté (route protégée - Acteur uniquement)
     */
    suspend fun getMyStatus(castingId: String): Result<CandidateStatusResponse> {
        return try {
            val response = castingService.getMyStatus(castingId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    401 -> "Non autorisé - vous n'avez pas encore postulé à ce casting"
                    404 -> "Statut de candidature non trouvé"
                    else -> "Erreur ${errorCode}: ${response.message()}"
                }
                Result.failure(
                    when (errorCode) {
                        401 -> ApiException.UnauthorizedException(errorMessage)
                        404 -> ApiException.NotFoundException(errorMessage)
                        else -> ApiException.UnknownException(errorMessage)
                    }
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownException("Erreur inconnue: ${e.message}"))
        }
    }

    private fun guessMimeType(file: File): String? {
        return URLConnection.guessContentTypeFromName(file.name)
    }
}

