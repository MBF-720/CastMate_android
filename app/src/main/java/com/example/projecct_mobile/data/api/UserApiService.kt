package com.example.projecct_mobile.data.api

import com.example.projecct_mobile.data.model.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les utilisateurs
 * Routes protégées nécessitant un token JWT
 */
interface UserApiService {
    
    /**
     * Liste des utilisateurs (Admin uniquement)
     * GET /users
     * Nécessite un token JWT avec rôle ADMIN
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
    
    /**
     * Récupère les informations d'un utilisateur par son ID
     * GET /users/:id
     * Nécessite un token JWT
     */
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): Response<User>
    
    /**
     * Récupère le profil de l'utilisateur connecté
     * GET /users/me
     * Nécessite un token JWT
     */
    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>
    
    /**
     * Met à jour le profil d'un utilisateur
     * PATCH /users/:id
     * Nécessite un token JWT (propre profil ou admin)
     */
    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: User
    ): Response<User>
    
    /**
     * Supprime un utilisateur (Admin uniquement)
     * DELETE /users/:id
     * Nécessite un token JWT avec rôle ADMIN
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    ): Response<Unit>
}

