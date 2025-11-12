package com.example.projecct_mobile.data.examples

import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Fichier d'exemples d'utilisation de l'API
 * 
 * Ce fichier contient des exemples complets pour :
 * 1. Inscription Acteur (POST /acteur/signup)
 * 2. Inscription Agence (POST /agence/signup)
 * 3. Connexion (POST /auth/login)
 * 4. Requêtes protégées (GET /users/:id, GET /users/me)
 * 5. Création de casting (POST /castings)
 * 6. Gestion des erreurs HTTP
 * 7. Gestion de l'expiration du token
 */

/**
 * EXEMPLE 1 : Inscription d'un acteur avec tous les champs
 */
fun exampleSignupActeur(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    
    scope.launch {
        val photo = File("/storage/emulated/0/Pictures/photo-profil.jpg")
        val document = File("/storage/emulated/0/Download/cv.pdf")

        val result = authRepository.signupActeur(
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            motDePasse = "password123",
            tel = "+21612345678",
            age = 25,
            gouvernorat = "Tunis",
            experience = 5,
            centresInteret = listOf("Théâtre", "Cinéma", "Télévision"),
            photoFile = photo.takeIf { it.exists() },
            documentFile = document.takeIf { it.exists() },
            instagram = "https://instagram.com/acteur",
            youtube = "https://youtube.com/@acteur",
            tiktok = "https://tiktok.com/@acteur"
        )
        
        result.onSuccess { authResponse ->
            // Le token JWT est automatiquement stocké dans TokenManager
            println("Inscription réussie!")
            println("Token: ${authResponse.accessToken}")
            println("User: ${authResponse.user}")
        }
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.BadRequestException -> {
                    println("Erreur 400: Vérifiez vos informations - ${exception.message}")
                }
                is ApiException.NetworkException -> {
                    println("Erreur réseau: ${exception.message}")
                }
                else -> {
                    println("Erreur: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 2 : Inscription d'une agence
 */
fun exampleSignupAgence(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    
    scope.launch {
        val result = authRepository.signupAgence(
            nomAgence = "Agence de Casting Tunis",
            responsable = "Mohamed Ben Ali",
            email = "contact@agence-casting.tn",
            motDePasse = "password123",
            tel = "+21612345678",
            gouvernorat = "Tunis",
            siteWeb = "https://www.agence-casting.tn",
            description = "Agence spécialisée dans le casting",
            logoUrl = "https://example.com/logo.png",
            documents = "https://example.com/documents.pdf"
        )
        
        result.onSuccess { authResponse ->
            println("Inscription réussie!")
            println("Token: ${authResponse.accessToken}")
            // Le token est automatiquement stocké
        }
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.BadRequestException -> {
                    println("Erreur 400: Vérifiez vos informations - ${exception.message}")
                }
                is ApiException.NetworkException -> {
                    println("Erreur réseau: ${exception.message}")
                }
                else -> {
                    println("Erreur: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 3 : Connexion et extraction du token JWT
 */
fun exampleLogin(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    
    scope.launch {
        val result = authRepository.login(
            email = "user@example.com",
            password = "password123"
        )
        
        result.onSuccess { authResponse ->
            // Le token JWT est automatiquement stocké dans TokenManager
            // Vous pouvez accéder au token via authResponse.accessToken
            val token = authResponse.accessToken
            val user = authResponse.user
            
            println("Connexion réussie!")
            println("Token JWT: $token")
            println("User ID: ${user?.id}")
            println("User Email: ${user?.email}")
            println("User Role: ${user?.role}")
            
            // Le token sera automatiquement ajouté dans les headers
            // pour toutes les requêtes protégées suivantes
        }
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.UnauthorizedException -> {
                    println("Erreur 401: Email ou mot de passe incorrect")
                }
                is ApiException.NetworkException -> {
                    println("Erreur réseau: ${exception.message}")
                }
                else -> {
                    println("Erreur: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 4 : Requête GET protégée - Récupérer un utilisateur par ID
 */
fun exampleGetUserById(scope: CoroutineScope, userId: String) {
    val userRepository = UserRepository()
    
    scope.launch {
        val result = userRepository.getUserById(userId)
        
        result.onSuccess { user ->
            println("Utilisateur trouvé:")
            println("ID: ${user.id}")
            println("Email: ${user.email}")
            println("Nom: ${user.prenom} ${user.nom}")
            println("Rôle: ${user.role}")
            // Le token JWT est automatiquement ajouté dans le header
            // Authorization: Bearer <token>
        }
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.UnauthorizedException -> {
                    println("Erreur 401: Token invalide ou expiré")
                    println("Le token a été automatiquement supprimé")
                    // Rediriger vers l'écran de connexion
                }
                is ApiException.ForbiddenException -> {
                    println("Erreur 403: Accès refusé")
                }
                is ApiException.NotFoundException -> {
                    println("Erreur 404: Utilisateur non trouvé")
                }
                else -> {
                    println("Erreur: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 5 : Requête GET protégée - Récupérer le profil de l'utilisateur connecté
 */
fun exampleGetCurrentUser(scope: CoroutineScope) {
    val userRepository = UserRepository()
    
    scope.launch {
        val result = userRepository.getCurrentUser()
        
        result.onSuccess { user ->
            println("Profil utilisateur:")
            println("Email: ${user.email}")
            println("Nom complet: ${user.prenom} ${user.nom}")
            println("Bio: ${user.bio}")
            println("CV: ${user.cvUrl}")
            println("Photo: ${user.photoProfil}")
        }
        
        result.onFailure { exception ->
            if (exception is ApiException.UnauthorizedException) {
                println("Token expiré - redirection vers la connexion nécessaire")
            }
        }
    }
}

/**
 * EXEMPLE 6 : Créer un casting (POST protégé)
 */
fun exampleCreateCasting(scope: CoroutineScope) {
    val castingRepository = CastingRepository()
    
    scope.launch {
        val result = castingRepository.createCasting(
            titre = "Recherche acteur principal",
            descriptionRole = "Rôle de protagoniste dans une série",
            synopsis = "Paul Atreides faces new political and spiritual challenges as the universe around him evolves.",
            lieu = "Paris",
            dateDebut = "2024-01-15", // Format: YYYY-MM-DD
            dateFin = "2024-02-15", // Format: YYYY-MM-DD
            remuneration = "1000€/jour",
            conditions = "Disponibilité totale requise, expérience en cinéma"
        )
        
        result.onSuccess { casting ->
            println("Casting créé avec succès!")
            println("ID: ${casting.id}")
            println("Titre: ${casting.titre}")
            println("Lieu: ${casting.lieu}")
            println("Date début: ${casting.dateDebut}")
            println("Date fin: ${casting.dateFin}")
            println("Rémunération: ${casting.remuneration}")
        }
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.UnauthorizedException -> {
                    println("Erreur 401: Token invalide ou expiré")
                    // Le token est automatiquement supprimé
                    // Rediriger vers l'écran de connexion
                }
                is ApiException.ForbiddenException -> {
                    println("Erreur 403: Vous n'avez pas les permissions nécessaires")
                    println("Seuls les RECRUTEUR ou ADMIN peuvent créer des castings")
                }
                is ApiException.BadRequestException -> {
                    println("Erreur 400: ${exception.message}")
                }
                else -> {
                    println("Erreur: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 7 : Récupérer tous les castings (route publique)
 */
fun exampleGetAllCastings(scope: CoroutineScope) {
    val castingRepository = CastingRepository()
    
    scope.launch {
        val result = castingRepository.getAllCastings()
        
        result.onSuccess { castings ->
            println("${castings.size} castings trouvés:")
            castings.forEach { casting ->
                val dateRange = if (casting.dateDebut != null && casting.dateFin != null) {
                    "${casting.dateDebut} - ${casting.dateFin}"
                } else {
                    casting.dateDebut ?: casting.dateFin ?: "Date non spécifiée"
                }
                println("- ${casting.titre} ($dateRange)")
            }
        }
        
        result.onFailure { exception ->
            println("Erreur lors de la récupération des castings: ${exception.message}")
        }
    }
}

/**
 * EXEMPLE 8 : Gestion complète des erreurs HTTP
 */
fun exampleErrorHandling(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    
    scope.launch {
        val result = authRepository.login("wrong@email.com", "wrongpassword")
        
        result.onFailure { exception ->
            when (exception) {
                is ApiException.UnauthorizedException -> {
                    // 401 - Token invalide ou expiré
                    // Le token est automatiquement supprimé par ErrorInterceptor
                    println("401: Non autorisé")
                    println("Redirection vers l'écran de connexion nécessaire")
                }
                is ApiException.ForbiddenException -> {
                    // 403 - Accès refusé
                    println("403: Accès refusé")
                    println("Vous n'avez pas les permissions nécessaires")
                }
                is ApiException.NotFoundException -> {
                    // 404 - Ressource non trouvée
                    println("404: Ressource non trouvée")
                }
                is ApiException.ConflictException -> {
                    // 409 - Conflit (ressource existe déjà)
                    println("409: Conflit")
                    println("Cette ressource existe déjà")
                }
                is ApiException.BadRequestException -> {
                    // 400 - Requête invalide
                    println("400: Requête invalide")
                    println("Vérifiez les données envoyées")
                }
                is ApiException.ServerException -> {
                    // 500-599 - Erreur serveur
                    println("500+: Erreur serveur")
                    println("Réessayez plus tard")
                }
                is ApiException.NetworkException -> {
                    // Erreur de connexion réseau
                    println("Erreur réseau")
                    println("Vérifiez votre connexion internet")
                }
                else -> {
                    // Erreur inconnue
                    println("Erreur inconnue: ${exception.message}")
                }
            }
        }
    }
}

/**
 * EXEMPLE 9 : Gestion de l'expiration du token et reconnexion
 */
fun exampleTokenExpiration(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    val userRepository = UserRepository()
    
    scope.launch {
        // Vérifier si l'utilisateur est connecté
        if (authRepository.isLoggedIn()) {
            // Essayer de récupérer le profil utilisateur
            val result = userRepository.getCurrentUser()
            
            result.onSuccess { user ->
                println("Utilisateur connecté: ${user.email}")
            }
            
            result.onFailure { exception ->
                if (exception is ApiException.UnauthorizedException) {
                    // Token expiré - le token a été automatiquement supprimé
                    println("Token expiré - déconnexion automatique")
                    
                    // Rediriger vers l'écran de connexion
                    // navController.navigate("signIn") { popUpTo("signIn") { inclusive = true } }
                }
            }
        } else {
            println("Utilisateur non connecté")
            // Rediriger vers l'écran de connexion
            // navController.navigate("signIn")
        }
    }
}

/**
 * EXEMPLE 10 : Déconnexion manuelle
 */
fun exampleLogout(scope: CoroutineScope) {
    val authRepository = AuthRepository()
    
    scope.launch {
        // Supprime le token stocké
        authRepository.logout()
        
        println("Déconnexion réussie")
        // Rediriger vers l'écran de connexion
        // navController.navigate("signIn") { popUpTo("signIn") { inclusive = true } }
    }
}

/**
 * EXEMPLE 11 : Format de réponse d'erreur JSON
 * 
 * L'API retourne des erreurs au format suivant :
 * 
 * Erreur simple (401, 403, 404, 409, etc.) :
 * {
 *   "statusCode": 401,
 *   "message": "Token invalide ou expiré",
 *   "error": "Unauthorized"
 * }
 * 
 * Erreur de validation (400) :
 * {
 *   "statusCode": 400,
 *   "message": "Validation failed",
 *   "error": "Bad Request",
 *   "details": {
 *     "email": ["email doit être une adresse email valide"],
 *     "password": ["password doit contenir au moins 8 caractères"]
 *   }
 * }
 * 
 * Ces erreurs sont automatiquement parsées par ErrorInterceptor
 * et converties en ApiException correspondante.
 */

