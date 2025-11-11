# Guide d'utilisation de l'API CastMate

Ce guide explique comment utiliser l'API NestJS dans l'application Android.

## Table des matières

1. [Initialisation](#initialisation)
2. [Inscription (Register)](#inscription-register)
3. [Connexion (Login)](#connexion-login)
4. [Requêtes protégées](#requêtes-protégées)
5. [Gestion des erreurs](#gestion-des-erreurs)
6. [Gestion de l'expiration du token](#gestion-de-lexpiration-du-token)

## Initialisation

L'API doit être initialisée dans votre `Application` ou `MainActivity` :

```kotlin
// Dans MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialiser l'API client
        ApiClient.initialize(this)
        
        // ... reste du code
    }
}
```

## Inscription (Register)

### Exemple complet avec tous les champs

```kotlin
val authRepository = AuthRepository()

// Exemple avec tous les champs optionnels
val result = authRepository.register(
    email = "john.doe@example.com",
    password = "password123",
    firstName = "John",
    lastName = "Doe",
    phone = "+33123456789",
    address = "123 Rue Example, 75001 Paris",
    dateOfBirth = "1990-01-15", // Format: YYYY-MM-DD
    gender = "MALE", // "MALE", "FEMALE", ou "OTHER"
    bio = "Acteur passionné avec 10 ans d'expérience",
    role = "ACTEUR" // "ACTEUR", "RECRUTEUR", ou "ADMIN"
)

result.onSuccess { authResponse ->
    // Le token JWT est automatiquement stocké
    val token = authResponse.accessToken
    val user = authResponse.user
    
    // Navigation vers l'écran principal
    navController.navigate("castingList")
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.ConflictException -> {
            // Email déjà utilisé (409)
            showError("Cet email est déjà utilisé")
        }
        is ApiException.BadRequestException -> {
            // Données invalides (400)
            showError("Vérifiez vos informations")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

### Exemple minimal (seulement email et password)

```kotlin
val result = authRepository.register(
    email = "user@example.com",
    password = "password123"
)
```

## Connexion (Login)

### Exemple de connexion

```kotlin
val authRepository = AuthRepository()

val result = authRepository.login(
    email = "user@example.com",
    password = "password123"
)

result.onSuccess { authResponse ->
    // Le token JWT est automatiquement stocké dans TokenManager
    // Vous pouvez accéder aux informations utilisateur
    val user = authResponse.user
    val token = authResponse.accessToken
    
    // Navigation vers l'écran principal
    navController.navigate("castingList")
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.UnauthorizedException -> {
            // Email ou mot de passe incorrect (401)
            showError("Email ou mot de passe incorrect")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

## Requêtes protégées

### Exemple 1 : Récupérer tous les castings (route publique)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.getAllCastings()

result.onSuccess { castings ->
    // Afficher la liste des castings
    castingList = castings
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### Exemple 2 : Créer un casting (route protégée - nécessite token JWT)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.createCasting(
    title = "Dune : Part 3",
    description = "Paul Atreides faces new political and spiritual challenges...",
    date = "2025-10-30", // Format: YYYY-MM-DD
    role = "Arven",
    age = "20+",
    compensation = "20$",
    location = "Paris, France",
    requirements = "Expérience requise en cinéma"
)

result.onSuccess { casting ->
    // Casting créé avec succès
    showSuccess("Casting créé: ${casting.title}")
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.UnauthorizedException -> {
            // Token invalide ou expiré (401)
            // Le token est automatiquement supprimé
            // Rediriger vers l'écran de connexion
            navController.navigate("signIn")
        }
        is ApiException.ForbiddenException -> {
            // Accès refusé (403)
            showError("Vous n'avez pas les permissions nécessaires")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

### Exemple 3 : Récupérer le profil utilisateur (route protégée)

```kotlin
val userRepository = UserRepository()

val result = userRepository.getCurrentUser()

result.onSuccess { user ->
    // Afficher les informations du profil
    displayUserProfile(user)
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.UnauthorizedException -> {
            // Token invalide ou expiré
            navController.navigate("signIn")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

## Gestion des erreurs

### Codes d'erreur HTTP et leurs significations

#### 401 - Unauthorized (Token invalide ou expiré)
```kotlin
catch (e: ApiException.UnauthorizedException) {
    // Le token est automatiquement supprimé par ErrorInterceptor
    // Rediriger vers l'écran de connexion
    navController.navigate("signIn")
    showError("Votre session a expiré. Veuillez vous reconnecter.")
}
```

#### 403 - Forbidden (Accès refusé)
```kotlin
catch (e: ApiException.ForbiddenException) {
    showError("Vous n'avez pas les permissions nécessaires pour cette action.")
}
```

#### 404 - Not Found (Ressource non trouvée)
```kotlin
catch (e: ApiException.NotFoundException) {
    showError("La ressource demandée n'existe pas.")
}
```

#### 409 - Conflict (Conflit - ressource existe déjà)
```kotlin
catch (e: ApiException.ConflictException) {
    showError("Cette ressource existe déjà. Veuillez utiliser un autre identifiant.")
}
```

#### 400 - Bad Request (Requête invalide)
```kotlin
catch (e: ApiException.BadRequestException) {
    showError("Vérifiez les informations saisies.")
}
```

### Exemple de réponse d'erreur JSON

L'API retourne des erreurs au format suivant :

```json
{
  "statusCode": 401,
  "message": "Token invalide ou expiré",
  "error": "Unauthorized"
}
```

Pour les erreurs de validation (400) :

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "error": "Bad Request",
  "details": {
    "email": ["email doit être une adresse email valide"],
    "password": ["password doit contenir au moins 8 caractères"]
  }
}
```

## Gestion de l'expiration du token

### Fonctionnement automatique

1. **Token ajouté automatiquement** : L'`AuthInterceptor` ajoute automatiquement le token JWT dans le header `Authorization: Bearer <token>` pour toutes les requêtes protégées.

2. **Détection d'expiration** : Lorsqu'une requête retourne un 401, l'`ErrorInterceptor` :
   - Détecte l'erreur 401
   - Supprime automatiquement le token stocké
   - Lance une `UnauthorizedException`

3. **Reconnexion** : Vous devez rediriger l'utilisateur vers l'écran de connexion :

```kotlin
result.onFailure { exception ->
    if (exception is ApiException.UnauthorizedException) {
        // Le token a été supprimé automatiquement
        // Rediriger vers la connexion
        authRepository.logout() // Optionnel, déjà fait par l'intercepteur
        navController.navigate("signIn") {
            // Empêcher le retour en arrière
            popUpTo("signIn") { inclusive = true }
        }
    }
}
```

### Vérifier si un utilisateur est connecté

```kotlin
val authRepository = AuthRepository()

if (authRepository.isLoggedIn()) {
    // L'utilisateur est connecté
    navController.navigate("castingList")
} else {
    // L'utilisateur n'est pas connecté
    navController.navigate("signIn")
}
```

### Déconnexion manuelle

```kotlin
val authRepository = AuthRepository()
authRepository.logout() // Supprime le token
navController.navigate("signIn")
```

## Routes publiques vs protégées

### Routes publiques (pas de token nécessaire)
- `POST /auth/register` - Inscription
- `POST /auth/login` - Connexion
- `GET /castings` - Liste des castings
- `GET /castings/:id` - Détails d'un casting
- `GET /api` - Documentation

### Routes protégées (token JWT requis)
- `POST /castings` - Créer un casting
- `PUT /castings/:id` - Modifier un casting
- `DELETE /castings/:id` - Supprimer un casting
- `GET /users/:id` - Informations d'un utilisateur
- `GET /users/me` - Profil de l'utilisateur connecté
- Toutes les autres routes sauf celles listées ci-dessus

## Notes importantes

1. **Token stocké automatiquement** : Lors de la connexion ou de l'inscription, le token JWT est automatiquement stocké dans `TokenManager` via `DataStore`.

2. **Token ajouté automatiquement** : Pour toutes les requêtes protégées, le token est automatiquement ajouté dans le header `Authorization` par l'`AuthInterceptor`.

3. **Expiration du token** : Les tokens JWT expirent après 7 jours. Après expiration, l'utilisateur doit se reconnecter.

4. **Gestion des erreurs réseau** : Les erreurs de connexion réseau sont capturées et converties en `ApiException.NetworkException`.

5. **Logging** : En mode debug, toutes les requêtes HTTP sont loggées. En production, désactivez le logging dans `ApiClient.kt` en changeant `Level.BODY` en `Level.NONE`.

