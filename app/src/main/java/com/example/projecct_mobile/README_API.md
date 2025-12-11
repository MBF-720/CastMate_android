# Documentation API CastMate

## Résumé de l'intégration

L'application Android est maintenant configurée pour consommer l'API NestJS déployée sur Vercel.

### URL de base
- **API Base URL**: `https://cast-mate.vercel.app`
- **Documentation Swagger**: `https://cast-mate.vercel.app/api`

## Architecture

### Structure des dossiers

```
app/src/main/java/com/example/projecct_mobile/
├── data/
│   ├── api/                    # Services API Retrofit
│   │   ├── ApiClient.kt        # Configuration Retrofit
│   │   ├── AuthApiService.kt    # Service d'authentification
│   │   ├── CastingApiService.kt # Service des castings
│   │   ├── UserApiService.kt    # Service des utilisateurs
│   │   ├── AuthInterceptor.kt   # Ajoute automatiquement le token JWT
│   │   └── ErrorInterceptor.kt  # Gère les erreurs HTTP
│   ├── local/
│   │   └── TokenManager.kt     # Stockage sécurisé du token JWT
│   ├── model/                   # Modèles de données
│   │   ├── User.kt
│   │   ├── AuthRequest.kt
│   │   ├── AuthResponse.kt
│   │   ├── Casting.kt
│   │   └── ApiError.kt
│   ├── repository/              # Repositories (logique métier)
│   │   ├── AuthRepository.kt
│   │   ├── CastingRepository.kt
│   │   └── UserRepository.kt
│   └── examples/
│       └── ApiExamples.kt        # Exemples d'utilisation
├── ui/
│   └── screens/
│       ├── SignInScreen.kt      # Intégration API ✅
│       ├── SignUpScreen.kt      # Intégration API ✅
│       └── CastingListScreen.kt # À intégrer avec l'API
└── MainActivity.kt              # Initialisation API ✅
```

## Fonctionnalités implémentées

### ✅ 1. Inscription (POST /auth/register)

**Exemple complet avec tous les champs :**
```kotlin
val authRepository = AuthRepository()

val result = authRepository.register(
    email = "john.doe@example.com",
    password = "password123",
    firstName = "John",
    lastName = "Doe",
    phone = "+33123456789",
    address = "123 Rue Example, 75001 Paris",
    dateOfBirth = "1990-01-15",
    gender = "MALE",
    bio = "Acteur passionné",
    role = "ACTEUR"
)
```

**Le token JWT est automatiquement stocké** après une inscription réussie.

### ✅ 2. Connexion (POST /auth/login)

```kotlin
val authRepository = AuthRepository()

val result = authRepository.login(
    email = "user@example.com",
    password = "password123"
)

result.onSuccess { authResponse ->
    // Token stocké automatiquement
    val token = authResponse.accessToken
    val user = authResponse.user
}
```

### ✅ 3. Requêtes protégées (GET /users/:id, GET /users/me)

**Le token JWT est automatiquement ajouté** dans le header `Authorization: Bearer <token>` pour toutes les requêtes protégées.

```kotlin
val userRepository = UserRepository()

// Récupérer le profil de l'utilisateur connecté
val result = userRepository.getCurrentUser()

// Récupérer un utilisateur par ID
val result = userRepository.getUserById("user-id")
```

### ✅ 4. Création de casting (POST /castings)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.createCasting(
    title = "Dune : Part 3",
    description = "Description du casting...",
    date = "2025-10-30",
    role = "Arven",
    age = "20+",
    compensation = "20$",
    location = "Paris, France"
)
```

### ✅ 5. Gestion des erreurs HTTP

Les erreurs sont automatiquement gérées et converties en exceptions :

- **401 Unauthorized** : Token invalide ou expiré → `ApiException.UnauthorizedException`
  - Le token est automatiquement supprimé
  - Redirection vers la connexion nécessaire
  
- **403 Forbidden** : Accès refusé → `ApiException.ForbiddenException`
  
- **404 Not Found** : Ressource non trouvée → `ApiException.NotFoundException`
  
- **409 Conflict** : Ressource existe déjà → `ApiException.ConflictException`
  
- **400 Bad Request** : Requête invalide → `ApiException.BadRequestException`
  
- **500+ Server Error** : Erreur serveur → `ApiException.ServerException`
  
- **Network Error** : Erreur réseau → `ApiException.NetworkException`

### ✅ 6. Gestion de l'expiration du token

**Fonctionnement automatique :**

1. Le token JWT est stocké dans `DataStore` (sécurisé)
2. L'`AuthInterceptor` ajoute automatiquement le token dans les headers
3. L'`ErrorInterceptor` détecte les erreurs 401 et supprime automatiquement le token
4. Vous devez rediriger l'utilisateur vers l'écran de connexion

**Exemple :**
```kotlin
result.onFailure { exception ->
    if (exception is ApiException.UnauthorizedException) {
        // Token expiré - déjà supprimé automatiquement
        navController.navigate("signIn") {
            popUpTo("signIn") { inclusive = true }
        }
    }
}
```

## Routes publiques vs protégées

### Routes publiques (pas de token nécessaire)
- ✅ `POST /auth/register` - Inscription
- ✅ `POST /auth/login` - Connexion
- ✅ `GET /castings` - Liste des castings
- ✅ `GET /castings/:id` - Détails d'un casting
- ✅ `GET /api` - Documentation

### Routes protégées (token JWT requis)
- ✅ `POST /castings` - Créer un casting
- ✅ `PUT /castings/:id` - Modifier un casting
- ✅ `DELETE /castings/:id` - Supprimer un casting
- ✅ `GET /users/:id` - Informations d'un utilisateur
- ✅ `GET /users/me` - Profil de l'utilisateur connecté

## Configuration

### Initialisation

L'API est initialisée dans `MainActivity.onCreate()` :

```kotlin
ApiClient.initialize(this)
```

### Stockage du token

Le token JWT est stocké de manière sécurisée avec **DataStore** :

```kotlin
val tokenManager = ApiClient.getTokenManager()
tokenManager.saveToken(token)      // Sauvegarder
tokenManager.getTokenSync()        // Récupérer
tokenManager.clearToken()          // Supprimer
```

## Prochaines étapes

1. **Intégrer CastingListScreen** : Charger les castings depuis l'API au lieu des données mockées
2. **Gérer le refresh** : Implémenter le pull-to-refresh pour recharger les castings
3. **Détails du casting** : Créer un écran pour afficher les détails d'un casting
4. **Profil utilisateur** : Créer un écran de profil utilisateur
5. **Gestion des favoris** : Implémenter la fonctionnalité de favoris côté API

## Gestion des médias GridFS (Android)

- Les routes multipart exigent toujours un champ `payload` (JSON sérialisé) et des fichiers optionnels (`photo`, `document`, `photos`…).
- `AuthRepository.signupActeur()` sérialise automatiquement le payload et ajoute `photoFile` / `documentFile`.
- `ActeurRepository` offre :
  - `updateProfileMedia()` pour mettre à jour photo de profil et CV,
  - `addGalleryPhotos()` / `deleteGalleryPhoto()` pour la galerie,
  - `downloadMedia()` pour récupérer un fichier GridFS sous forme de `ByteArray`.
- `ProfileScreen` consomme `media.photoFileId` et affiche la photo via `downloadMedia()`.
- `CastingRepository.createCasting()` et `updateCasting()` acceptent un fichier `affiche` optionnel ainsi qu’un champ `prix`.

> Pensez à fournir un `File` local valide pour chaque pièce jointe afin que Retrofit puisse créer les `MultipartBody.Part`.

## Fichiers de référence

- **Guide d'utilisation complet** : `API_USAGE_GUIDE.md`
- **Exemples de code** : `data/examples/ApiExamples.kt`
- **Documentation Swagger** : `https://cast-mate.vercel.app/api`

## Notes importantes

1. **Token automatique** : Le token est ajouté automatiquement pour toutes les requêtes protégées
2. **Expiration** : Les tokens expirent après 7 jours
3. **Sécurité** : Le token est stocké de manière sécurisée avec DataStore
4. **Logging** : En mode debug, toutes les requêtes sont loggées. Désactivez en production.

