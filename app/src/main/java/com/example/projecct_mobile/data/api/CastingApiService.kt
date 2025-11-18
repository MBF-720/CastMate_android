package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.CandidateStatusResponse
import com.example.projecct_mobile.data.model.Casting
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les castings
 * Documentation: CASTINGS — Endpoints
 * 
 * Base URL: https://cast-mate.vercel.app
 * Auth: JWT Bearer Token (pour routes protégées)
 * Headers: Content-Type: application/json (pour JSON) | multipart/form-data (pour uploads)
 */
interface CastingApiService {
    
    /**
     * Liste tous les castings (Public)
     * 
     * Méthode: GET
     * URL: /castings
     * Auth: Non requise
     * 
     * Réponse 200: Liste de castings avec toutes les informations (recruteur peuplé, candidats, media)
     * 
     * Structure de réponse:
     * [
     *   {
     *     "id": "...",
     *     "titre": "...",
     *     "descriptionRole": "...",
     *     "synopsis": "...",
     *     "lieu": "...",
     *     "dateDebut": "2024-01-15T00:00:00.000Z",
     *     "dateFin": "2024-02-15T00:00:00.000Z",
     *     "prix": 5000,
     *     "types": ["Cinéma", "Télévision"],
     *     "age": "25-35 ans",
     *     "conditions": "...",
     *     "ouvert": true,
     *     "recruteur": { ... },
     *     "candidats": [ ... ],
     *     "media": { ... }
     *   }
     * ]
     */
    @GET("castings")
    suspend fun getAllCastings(): Response<List<Casting>>
    
    /**
     * Détail d'un casting (Public)
     * 
     * Méthode: GET
     * URL: /castings/:id
     * Auth: Non requise
     * 
     * @param id ID du casting (MongoDB ObjectId)
     * 
     * Réponse 200: Même structure qu'un élément de la liste
     * 
     * Erreurs:
     * - 404: Casting non trouvé
     */
    @GET("castings/{id}")
    suspend fun getCastingById(
        @Path("id") id: String
    ): Response<Casting>
    
    /**
     * Créer un casting (Recruteur/Admin)
     * 
     * Méthode: POST
     * URL: /castings
     * Auth: Requise (JWT)
     * Rôle: RECRUTEUR ou ADMIN
     * Content-Type: multipart/form-data
     * 
     * Body (multipart/form-data):
     * - payload (string JSON): Données du casting
     * - affiche (file, optionnel): Image JPEG/PNG (max 10 Mo)
     * 
     * Exemple payload JSON:
     * {
     *   "titre": "Recherche acteur principal",
     *   "descriptionRole": "Rôle principal masculin 30-40 ans",
     *   "synopsis": "Série dramatique...",
     *   "lieu": "Paris",
     *   "dateDebut": "2024-01-15",
     *   "dateFin": "2024-02-15",
     *   "prix": 5000,
     *   "types": ["Cinéma", "Télévision"],
     *   "age": "25-35 ans",
     *   "ouvert": true,
     *   "conditions": "Disponibilité totale requise"
     * }
     * 
     * Réponse 201: Casting créé avec recruteur peuplé (objet)
     * 
     * Erreurs:
     * - 401: Non authentifié
     * - 403: Accès refusé (recruteur/admin uniquement)
     * - 400: Données invalides
     */
    @Multipart
    @POST("castings")
    suspend fun createCasting(
        @Part("payload") payload: RequestBody,
        @Part affiche: MultipartBody.Part? = null
    ): Response<Casting>
    
    /**
     * Modifier un casting (Propriétaire/Admin)
     * 
     * Méthode: PATCH
     * URL: /castings/:id
     * Auth: Requise (JWT)
     * Content-Type: multipart/form-data
     * 
     * Body (multipart/form-data):
     * - payload (string JSON, optionnel): Champs à mettre à jour
     * - affiche (file, optionnel): Nouvelle affiche
     * 
     * Réponse 200: Casting mis à jour
     * 
     * Erreurs:
     * - 403: Vous ne pouvez modifier que vos propres castings
     * - 404: Casting non trouvé
     */
    @Multipart
    @PATCH("castings/{id}")
    suspend fun updateCasting(
        @Path("id") id: String,
        @Part("payload") payload: RequestBody,
        @Part affiche: MultipartBody.Part? = null
    ): Response<Casting>
    
    /**
     * Supprimer un casting (Propriétaire/Admin)
     * 
     * Méthode: DELETE
     * URL: /castings/:id
     * Auth: Requise (JWT)
     * 
     * Réponse 200:
     * {
     *   "message": "Casting supprimé avec succès"
     * }
     */
    @DELETE("castings/{id}")
    suspend fun deleteCasting(
        @Path("id") id: String
    ): Response<Unit>
    
    /**
     * Postuler à un casting (Acteur)
     * 
     * Méthode: POST
     * URL: /castings/:id/apply
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     * 
     * Réponse 200: Casting mis à jour avec la candidature
     * 
     * Erreurs:
     * - 400: Le casting n'accepte plus de candidatures
     * - 409: Vous avez déjà postulé à ce casting
     * - 404: Casting non trouvé
     */
    @POST("castings/{id}/apply")
    suspend fun applyToCasting(
        @Path("id") id: String
    ): Response<Unit>
    
    /**
     * Accepter un candidat (Recruteur/Admin)
     * 
     * Méthode: PATCH
     * URL: /castings/:id/candidates/:acteurId/accept
     * Auth: Requise (JWT)
     * 
     * Paramètres:
     * - id: ID du casting
     * - acteurId: ID de l'acteur à accepter
     * 
     * Réponse 200: Casting mis à jour avec statut ACCEPTE
     */
    @PATCH("castings/{id}/candidates/{acteurId}/accept")
    suspend fun acceptCandidate(
        @Path("id") id: String,
        @Path("acteurId") acteurId: String
    ): Response<Unit>
    
    /**
     * Refuser un candidat (Recruteur/Admin)
     * 
     * Méthode: PATCH
     * URL: /castings/:id/candidates/:acteurId/reject
     * Auth: Requise (JWT)
     * 
     * Paramètres:
     * - id: ID du casting
     * - acteurId: ID de l'acteur à refuser
     * 
     * Réponse 200: Casting mis à jour avec statut REFUSE
     */
    @PATCH("castings/{id}/candidates/{acteurId}/reject")
    suspend fun rejectCandidate(
        @Path("id") id: String,
        @Path("acteurId") acteurId: String
    ): Response<Unit>
    
    /**
     * Obtenir mon statut de candidature (Acteur)
     * 
     * Méthode: GET
     * URL: /castings/:id/my-status
     * Auth: Requise (JWT)
     * Rôle: ACTEUR
     * 
     * Réponse 200:
     * {
     *   "hasApplied": true,
     *   "statut": "EN_ATTENTE" | "ACCEPTE" | "REFUSE",
     *   "dateCandidature": "2024-01-15T10:30:00.000Z"
     * }
     */
    @GET("castings/{id}/my-status")
    suspend fun getMyStatus(
        @Path("id") id: String
    ): Response<CandidateStatusResponse>
}

