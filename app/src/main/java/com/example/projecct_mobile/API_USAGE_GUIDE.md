# Guide complet d'utilisation de l'API CastMate

> **Version mise à jour** : Novembre 2024  
> **URL de base** : `https://cast-mate.vercel.app/`  
> **Documentation Swagger** : `https://cast-mate.vercel.app/api`

Ce guide explique comment consommer l'API NestJS dans l'application Android avec des exemples pratiques et des conseils pour les développeurs frontend.

---

## 📋 Table des matières

1. [Architecture et initialisation](#architecture-et-initialisation)
2. [Authentification](#authentification)
3. [Gestion des acteurs](#gestion-des-acteurs)
4. [Gestion des agences](#gestion-des-agences)
5. [Gestion des castings](#gestion-des-castings)
6. [Gestion des médias](#gestion-des-médias)
7. [Gestion des erreurs](#gestion-des-erreurs)
8. [Conseils pour développeurs frontend](#conseils-pour-développeurs-frontend)
9. [Routes publiques vs protégées](#routes-publiques-vs-protégées)

---

## 🏗️ Architecture et initialisation

### Structure du projet

```
app/src/main/java/com/example/projecct_mobile/
├── data/
│   ├── api/                    # Services API Retrofit
│   │   ├── ApiClient.kt        # Configuration Retrofit
│   │   ├── AuthApiService.kt   # Service d'authentification
│   │   ├── CastingApiService.kt # Service des castings
│   │   ├── ActeurApiService.kt # Service des acteurs
│   │   ├── AgenceApiService.kt  # Service des agences
│   │   ├── UserApiService.kt    # Service des utilisateurs
│   │   ├── AuthInterceptor.kt   # Ajoute automatiquement le token JWT
│   │   └── ErrorInterceptor.kt  # Gère les erreurs HTTP
│   ├── local/
│   │   └── TokenManager.kt     # Stockage sécurisé du token JWT (DataStore)
│   ├── model/                   # Modèles de données
│   │   ├── User.kt
│   │   ├── Casting.kt
│   │   ├── ActeurProfile.kt
│   │   ├── AgenceProfile.kt
│   │   └── ApiException.kt
│   └── repository/              # Repositories (logique métier)
│       ├── AuthRepository.kt
│       ├── CastingRepository.kt
│       ├── ActeurRepository.kt
│       └── AgenceRepository.kt
```

### Initialisation

L'API doit être initialisée **une seule fois** dans votre `MainActivity` :

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ⚠️ IMPORTANT : Initialiser l'API client AVANT tout
        ApiClient.initialize(this)
        
        // ... reste du code
    }
}
```

**⚠️ Conseil** : Ne pas initialiser plusieurs fois `ApiClient`, cela pourrait causer des problèmes de mémoire.

---

## 🔐 Authentification

### 1. Inscription d'un acteur

```kotlin
val authRepository = AuthRepository()

val result = authRepository.signupActeur(
    nom = "Dupont",
    prenom = "Jean",
    email = "jean.dupont@example.com",
    motDePasse = "password123",
    tel = "+33123456789",
    age = 28,
    gouvernorat = "Tunis",
    experience = 5,
    centresInteret = listOf("Cinéma", "Théâtre"),
    photoFile = photoFile, // File? - Optionnel (JPEG/PNG)
    documentFile = cvFile, // File? - Optionnel (PDF)
    instagram = "https://instagram.com/jean.dupont", // Optionnel
    youtube = "https://youtube.com/@jean.dupont", // Optionnel
    tiktok = "https://tiktok.com/@jean.dupont" // Optionnel
)

result.onSuccess { authResponse ->
    // ✅ Le token JWT est automatiquement stocké
    val user = authResponse.user
    android.util.Log.d("Auth", "Acteur inscrit: ${user.email}")
    
    // Navigation vers l'écran principal
    navController.navigate("actorHome")
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.ConflictException -> {
            // Email déjà utilisé (409)
            showError("Cet email est déjà utilisé")
        }
        is ApiException.BadRequestException -> {
            // Données invalides (400)
            showError("Vérifiez vos informations: ${exception.message}")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

**📝 Notes importantes** :
- Les fichiers `photoFile` et `documentFile` sont optionnels
- Le format de `photoFile` doit être JPEG ou PNG
- Le format de `documentFile` doit être PDF
- Les réseaux sociaux sont optionnels

### 2. Inscription d'une agence

```kotlin
val authRepository = AuthRepository()

val result = authRepository.signupAgence(
    nomAgence = "Agence de Casting Tunis",
    responsable = "Mohamed Ben Ali",
    email = "contact@agence-casting.tn",
    motDePasse = "password123",
    tel = "+21612345678",
    gouvernorat = "Tunis",
    description = "Agence spécialisée dans le casting",
    siteWeb = "https://agence-casting.tn", // Optionnel
    logoFile = logoFile, // File? - Optionnel (JPEG/PNG)
    documentFile = documentFile, // File? - Optionnel (PDF)
    facebook = "https://facebook.com/agence-casting-tunis", // Optionnel
    instagram = "https://instagram.com/agence_casting_tunis" // Optionnel
)

result.onSuccess { authResponse ->
    // ✅ Le token JWT est automatiquement stocké
    android.util.Log.d("Auth", "Agence inscrite: ${authResponse.user?.email}")
    navController.navigate("agencyHome")
}

result.onFailure { exception ->
    // Gestion des erreurs (identique à l'inscription acteur)
}
```

### 3. Connexion

```kotlin
val authRepository = AuthRepository()

val result = authRepository.login(
    email = "user@example.com",
    password = "password123"
)

result.onSuccess { authResponse ->
    // ✅ Le token JWT est automatiquement stocké dans TokenManager
    val user = authResponse.user
    val token = authResponse.accessToken
    
    // Navigation selon le rôle
    when (user.role?.name) {
        "ACTEUR" -> navController.navigate("actorHome")
        "RECRUTEUR" -> navController.navigate("agencyHome")
        else -> navController.navigate("home")
    }
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

### 4. Vérifier si un utilisateur est connecté

```kotlin
val authRepository = AuthRepository()

if (authRepository.isLoggedIn()) {
    // L'utilisateur est connecté
    val user = authRepository.getCurrentUser()
    navController.navigate("home")
} else {
    // L'utilisateur n'est pas connecté
    navController.navigate("signIn")
}
```

### 5. Déconnexion

```kotlin
val authRepository = AuthRepository()
authRepository.logout() // Supprime le token et les infos utilisateur
navController.navigate("signIn") {
    popUpTo("signIn") { inclusive = true }
}
```

---

## 👤 Gestion des acteurs

### 1. Récupérer le profil de l'acteur connecté

```kotlin
val acteurRepository = ActeurRepository()

val result = acteurRepository.getCurrentActeur()

result.onSuccess { profile ->
    // Afficher les informations du profil
    val nom = profile.nom
    val prenom = profile.prenom
    val email = profile.email
    val photoFileId = profile.media?.photoFileId
    val documentFileId = profile.media?.documentFileId
    
    // Télécharger la photo de profil si disponible
    photoFileId?.let { fileId ->
        acteurRepository.downloadMedia(fileId) { bitmap ->
            // Afficher l'image
        }
    }
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.UnauthorizedException -> {
            navController.navigate("signIn")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

### 2. Mettre à jour le profil

```kotlin
val acteurRepository = ActeurRepository()

val result = acteurRepository.updateCurrentActeur(
    nom = "Nouveau nom",
    prenom = "Nouveau prénom",
    tel = "+33123456789",
    age = 30,
    gouvernorat = "Sfax",
    experience = 7,
    centresInteret = listOf("Cinéma", "Publicité"),
    instagram = "https://instagram.com/nouveau",
    youtube = "https://youtube.com/@nouveau",
    tiktok = "https://tiktok.com/@nouveau"
)

result.onSuccess { updatedProfile ->
    showSuccess("Profil mis à jour avec succès")
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.ForbiddenException -> {
            // Vous ne pouvez modifier que votre propre profil
            showError("Accès refusé")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

### 3. Mettre à jour la photo de profil et/ou le CV

```kotlin
val acteurRepository = ActeurRepository()

// Mettre à jour uniquement la photo
val result = acteurRepository.updateProfileMedia(
    acteurId = null, // null = utilise l'ID de l'utilisateur connecté
    photoFile = newPhotoFile, // File? - Optionnel
    documentFile = null // File? - Optionnel
)

// Mettre à jour uniquement le CV
val result = acteurRepository.updateProfileMedia(
    acteurId = null,
    photoFile = null,
    documentFile = newCvFile
)

// Mettre à jour les deux
val result = acteurRepository.updateProfileMedia(
    acteurId = null,
    photoFile = newPhotoFile,
    documentFile = newCvFile
)

result.onSuccess { updatedProfile ->
    showSuccess("Médias mis à jour avec succès")
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### 4. Télécharger un média (photo, CV, etc.)

```kotlin
val acteurRepository = ActeurRepository()

val result = acteurRepository.downloadMedia(fileId = "507f1f77bcf86cd799439011")

result.onSuccess { responseBody ->
    // Convertir en Bitmap pour les images
    val inputStream = responseBody.byteStream()
    val bitmap = BitmapFactory.decodeStream(inputStream)
    
    // Ou sauvegarder dans un fichier pour les PDFs
    val file = File(context.cacheDir, "cv.pdf")
    file.outputStream().use { output ->
        responseBody.byteStream().use { input ->
            input.copyTo(output)
        }
    }
}

result.onFailure { exception ->
    showError("Erreur lors du téléchargement: ${exception.message}")
}
```

---

## 🏢 Gestion des agences

### 1. Récupérer le profil de l'agence connectée

```kotlin
val agenceRepository = AgenceRepository()

val result = agenceRepository.getCurrentAgence()

result.onSuccess { profile ->
    val nomAgence = profile.nomAgence
    val responsable = profile.responsable
    val email = profile.email
    val siteWeb = profile.siteWeb
    val facebook = profile.socialLinks?.facebook
    val instagram = profile.socialLinks?.instagram
    val logoFileId = profile.media?.photoFileId
    val documentFileId = profile.media?.documentFileId
}

result.onFailure { exception ->
    // Gestion des erreurs
}
```

### 2. Mettre à jour le profil d'une agence

```kotlin
val agenceRepository = AgenceRepository()

val result = agenceRepository.updateAgence(
    nomAgence = "Nouveau nom d'agence",
    responsable = "Nouveau responsable",
    tel = "+21612345678",
    gouvernorat = "Sfax",
    description = "Nouvelle description",
    siteWeb = "https://nouveau-site.tn",
    facebook = "https://facebook.com/nouveau",
    instagram = "https://instagram.com/nouveau"
)

result.onSuccess { updatedProfile ->
    showSuccess("Profil mis à jour")
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### 3. Mettre à jour le logo et/ou le document

```kotlin
val agenceRepository = AgenceRepository()

val result = agenceRepository.updateProfileMedia(
    agenceId = null, // null = utilise l'ID de l'agence connectée
    logoFile = newLogoFile, // File? - Optionnel
    documentFile = newDocumentFile // File? - Optionnel
)

result.onSuccess { updatedProfile ->
    showSuccess("Médias mis à jour")
}
```

---

## 🎬 Gestion des castings

### 1. Récupérer tous les castings (route publique)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.getAllCastings()

result.onSuccess { castings ->
    // Afficher la liste des castings
    castingList = castings
    
    // Filtrer les castings ouverts
    val castingsOuverts = castings.filter { it.ouvert }
    
    // Filtrer par type
    val castingsCinema = castings.filter { 
        it.types?.contains("Cinéma") == true 
    }
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### 2. Récupérer un casting par ID (route publique)

**Endpoint :** `GET /castings/:id`

**Caractéristiques :**
- Route publique : pas d'authentification requise
- Token JWT optionnel : peut être fourni si disponible
- Méthode : GET
- Paramètre : `id` (MongoDB ObjectId)

**Exemple d'utilisation :**

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.getCastingById(castingId = "507f1f77bcf86cd799439011")

result.onSuccess { casting ->
    // Informations de base
    val id = casting.actualId // Utilise id ou idAlt selon ce qui est disponible
    val titre = casting.titre
    val descriptionRole = casting.descriptionRole
    val synopsis = casting.synopsis
    val lieu = casting.lieu
    val dateDebut = casting.dateDebut // Format ISO: "2024-01-15T00:00:00.000Z"
    val dateFin = casting.dateFin // Format ISO: "2024-02-15T00:00:00.000Z"
    val prix = casting.prix
    val types = casting.types // List<String>? - ex: ["Cinéma", "Télévision"]
    val age = casting.age // String? - ex: "25-35 ans"
    val ouvert = casting.ouvert // Boolean - Indique si le casting accepte des candidatures
    val conditions = casting.conditions
    
    // Informations sur le recruteur (agence)
    val recruteur = casting.recruteur
    if (recruteur != null) {
        val recruteurId = recruteur.id
        val nomAgence = recruteur.nomAgence
        val responsable = recruteur.responsable
        val email = recruteur.email
        val recruteurPhotoFileId = recruteur.media?.photoFileId
        val recruteurPhotoMimeType = recruteur.media?.photoMimeType
    }
    
    // Informations sur les candidats
    val candidats = casting.candidats // List<Candidat>?
    candidats?.forEach { candidat ->
        val acteur = candidat.acteurId
        if (acteur != null) {
            val acteurId = acteur.id
            val acteurNom = acteur.nom
            val acteurPrenom = acteur.prenom
            val acteurEmail = acteur.email
            val acteurPhotoFileId = acteur.media?.photoFileId
            val acteurPhotoMimeType = acteur.media?.photoMimeType
        }
        val statut = candidat.statut // "EN_ATTENTE", "ACCEPTE", "REFUSE"
        val dateCandidature = candidat.dateCandidature // Format ISO
    }
    
    // Affiche du casting (⭐ Important pour l'affichage)
    val afficheFileId = casting.actualAfficheFileId // Utilise media.afficheFileId ou afficheFileId
    val afficheMimeType = casting.media?.afficheMimeType
    val afficheOriginalName = casting.media?.afficheOriginalName
    val afficheLength = casting.media?.afficheLength
    val afficheUploadDate = casting.media?.afficheUploadDate
    
    // Télécharger l'affiche si disponible
    if (afficheFileId != null) {
        val acteurRepository = ActeurRepository() // Peut être utilisé pour télécharger les médias
        scope.launch {
            val mediaResult = acteurRepository.downloadMedia(afficheFileId)
            mediaResult.onSuccess { bytes ->
                // Afficher l'image avec BitmapFactory
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                // Utiliser bitmap.asImageBitmap() dans Compose
            }
            mediaResult.onFailure { exception ->
                // Gérer l'erreur (peut être 403 si permissions backend restrictives)
                if (exception is ApiException.ForbiddenException) {
                    // Afficher un placeholder
                }
            }
        }
    }
    
    // Dates de création et mise à jour
    val createdAt = casting.createdAt
    val updatedAt = casting.updatedAt
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.NotFoundException -> {
            showError("Casting non trouvé")
        }
        is ApiException.ServerException -> {
            showError("Erreur serveur: ${exception.message}")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

**Structure de la réponse (200 OK) :**

Le casting retourné contient :
- **Informations de base** : `id`, `titre`, `descriptionRole`, `synopsis`, `lieu`, `dateDebut`, `dateFin`, `prix`, `types`, `age`, `ouvert`, `conditions`
- **Recruteur** : Objet `RecruteurInfo` avec `id`, `nomAgence`, `responsable`, `email`, et `media` (photo de l'agence)
- **Candidats** : Liste de `Candidat` avec `acteurId` (objet `ActeurInfo`), `statut`, et `dateCandidature`
- **Media** : Objet `CastingMedia` avec `afficheFileId` (⭐ utiliser pour afficher l'affiche), `afficheMimeType`, `afficheOriginalName`, `afficheLength`, `afficheUploadDate`
- **Métadonnées** : `createdAt`, `updatedAt`

**Réponse 404 Not Found :**

```json
{
  "statusCode": 404,
  "message": "Casting non trouvé"
}
```

**Notes importantes :**
- L'affiche du casting est accessible via `casting.actualAfficheFileId` qui gère automatiquement `media.afficheFileId` ou `afficheFileId`
- Pour télécharger l'affiche, utilisez `ActeurRepository.downloadMedia(afficheFileId)`
- Les requêtes peuvent retourner 403 Forbidden pour les médias si les permissions backend sont restrictives (afficher un placeholder dans ce cas)

### 3. Créer un casting (route protégée - Recruteur uniquement)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.createCasting(
    titre = "Recherche acteur principal",
    descriptionRole = "Rôle de protagoniste dans une série",
    synopsis = "Série dramatique sur la vie d'un détective privé",
    lieu = "Paris, France",
    dateDebut = "2024-01-15", // Format: YYYY-MM-DD
    dateFin = "2024-02-15", // Format: YYYY-MM-DD
    prix = 5000.0,
    types = listOf("Cinéma", "Télévision"), // Optionnel - Liste des types
    age = "25-35 ans", // Optionnel - Tranche d'âge recherchée
    ouvert = true, // Optionnel - Défaut: true (accepte des candidatures)
    conditions = "Disponibilité totale requise",
    afficheFile = afficheFile // File? - Optionnel (image JPEG/PNG)
)

result.onSuccess { casting ->
    showSuccess("Casting créé: ${casting.titre}")
    navController.popBackStack()
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.UnauthorizedException -> {
            // Token invalide ou expiré (401)
            navController.navigate("signIn")
        }
        is ApiException.ForbiddenException -> {
            // Accès refusé (403) - Seuls les recruteurs peuvent créer des castings
            showError("Vous n'avez pas les permissions nécessaires")
        }
        is ApiException.BadRequestException -> {
            // Données invalides (400)
            showError("Vérifiez les informations saisies: ${exception.message}")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

**📝 Notes importantes** :
- Le champ `remuneration` a été **supprimé** (utiliser `prix` à la place)
- Le champ `types` est un tableau de strings optionnel (ex: `["Cinéma", "Télévision"]`)
- Le champ `age` est une string optionnelle (ex: `"25-35 ans"`)
- Le champ `ouvert` indique si le casting accepte des candidatures (défaut: `true`)
- Les castings expirés sont automatiquement fermés par le backend (`ouvert: false`)

### 4. Mettre à jour un casting

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.updateCasting(
    id = "507f1f77bcf86cd799439011",
    titre = "Nouveau titre",
    prix = 6500.0,
    types = listOf("Cinéma"), // Modifier les types
    age = "30-40 ans", // Modifier la tranche d'âge
    ouvert = false, // Fermer le casting
    afficheFile = newAfficheFile // Optionnel - Nouvelle affiche
)

result.onSuccess { updatedCasting ->
    showSuccess("Casting mis à jour")
}

result.onFailure { exception ->
    // Gestion des erreurs
}
```

### 5. Supprimer un casting

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.deleteCasting(id = "507f1f77bcf86cd799439011")

result.onSuccess {
    showSuccess("Casting supprimé")
    navController.popBackStack()
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### 6. Postuler à un casting (Acteur uniquement)

```kotlin
val castingRepository = CastingRepository()

// Vérifier d'abord si le casting est ouvert
val castingResult = castingRepository.getCastingById(castingId)
castingResult.onSuccess { casting ->
    if (!casting.ouvert) {
        showError("Ce casting n'accepte plus de candidatures")
        return@onSuccess
    }
    
    // Vérifier si la date de fin est dépassée
    val dateFin = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .parse(casting.dateFin ?: "")
    if (dateFin != null && dateFin.before(Date())) {
        showError("La date de fin est dépassée")
        return@onSuccess
    }
    
    // Postuler
    val result = castingRepository.applyToCasting(castingId)
    
    result.onSuccess {
        showSuccess("Candidature envoyée avec succès")
    }
    
    result.onFailure { exception ->
        when (exception) {
            is ApiException.BadRequestException -> {
                // Casting fermé ou expiré, ou déjà postulé
                showError(exception.message ?: "Impossible de postuler")
            }
            is ApiException.ConflictException -> {
                // Déjà postulé à ce casting
                showError("Vous avez déjà postulé à ce casting")
            }
            else -> {
                showError("Erreur: ${exception.message}")
            }
        }
    }
}
```

### 7. Voir son statut de candidature (Acteur uniquement)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.getMyStatus(castingId = "507f1f77bcf86cd799439011")

result.onSuccess { status ->
    if (status.hasApplied) {
        when (status.statut) {
            "EN_ATTENTE" -> {
                showInfo("Votre candidature est en attente")
            }
            "ACCEPTE" -> {
                showSuccess("✅ Vous avez été accepté !")
            }
            "REFUSE" -> {
                showError("❌ Votre candidature a été refusée")
            }
        }
        
        status.dateCandidature?.let { date ->
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(date)?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                }
            showInfo("Date de candidature: $formattedDate")
        }
    } else {
        showInfo("Vous n'avez pas encore postulé à ce casting")
    }
}

result.onFailure { exception ->
    showError("Erreur: ${exception.message}")
}
```

### 8. Accepter un candidat (Recruteur/Admin uniquement)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.acceptCandidate(
    castingId = "507f1f77bcf86cd799439011",
    acteurId = "507f1f77bcf86cd799439012"
)

result.onSuccess {
    showSuccess("Candidat accepté")
    // Rafraîchir la liste des candidats
    refreshCandidates()
}

result.onFailure { exception ->
    when (exception) {
        is ApiException.ForbiddenException -> {
            showError("Vous n'avez pas les permissions nécessaires")
        }
        is ApiException.NotFoundException -> {
            showError("Candidat ou casting non trouvé")
        }
        else -> {
            showError("Erreur: ${exception.message}")
        }
    }
}
```

### 9. Refuser un candidat (Recruteur/Admin uniquement)

```kotlin
val castingRepository = CastingRepository()

val result = castingRepository.rejectCandidate(
    castingId = "507f1f77bcf86cd799439011",
    acteurId = "507f1f77bcf86cd799439012"
)

result.onSuccess {
    showSuccess("Candidat refusé")
    refreshCandidates()
}

result.onFailure { exception ->
    // Gestion des erreurs (identique à acceptCandidate)
}
```

---

## 📸 Gestion des médias

### Télécharger un média (photo, CV, document)

Tous les médias sont stockés dans GridFS et accessibles via l'endpoint `/media/{fileId}`.

```kotlin
// Pour les acteurs
val acteurRepository = ActeurRepository()
val result = acteurRepository.downloadMedia(fileId)

// Pour les agences
val agenceRepository = AgenceRepository()
val result = agenceRepository.downloadMedia(fileId)

// Pour les castings (via CastingRepository si implémenté)
```

**Exemple complet** :

```kotlin
suspend fun loadProfileImage(fileId: String?): ImageBitmap? {
    if (fileId == null) return null
    
    return try {
        val acteurRepository = ActeurRepository()
        val result = acteurRepository.downloadMedia(fileId)
        
        result.getOrNull()?.let { responseBody ->
            val inputStream = responseBody.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.asImageBitmap()
        }
    } catch (e: Exception) {
        android.util.Log.e("Media", "Erreur chargement image: ${e.message}")
        null
    }
}

// Utilisation dans un Composable
@Composable
fun ProfileImage(fileId: String?) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(fileId) {
        if (fileId != null) {
            withContext(Dispatchers.IO) {
                imageBitmap = loadProfileImage(fileId)
            }
        }
    }
    
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Photo de profil",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        // Afficher une image par défaut
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Photo de profil",
            modifier = Modifier.size(120.dp)
        )
    }
}
```

---

## ⚠️ Gestion des erreurs

### Codes d'erreur HTTP et leurs significations

#### 401 - Unauthorized (Token invalide ou expiré)

```kotlin
result.onFailure { exception ->
    if (exception is ApiException.UnauthorizedException) {
        // ⚠️ Le token est automatiquement supprimé par ErrorInterceptor
        // Rediriger vers l'écran de connexion
        navController.navigate("signIn") {
            popUpTo("signIn") { inclusive = true }
        }
        showError("Votre session a expiré. Veuillez vous reconnecter.")
    }
}
```

#### 403 - Forbidden (Accès refusé)

```kotlin
catch (e: ApiException.ForbiddenException) {
    showError("Vous n'avez pas les permissions nécessaires pour cette action.")
}
```

**Note** : Pour certaines routes (comme la mise à jour de profil), l'`ErrorInterceptor` ne lance pas d'exception pour les 403, mais retourne la réponse. Vérifiez `response.isSuccessful` dans votre repository.

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
    // Le message d'erreur peut contenir des détails de validation
    showError("Vérifiez les informations saisies: ${e.message}")
}
```

#### 500+ - Server Error (Erreur serveur)

```kotlin
catch (e: ApiException.ServerException) {
    showError("Erreur serveur. Veuillez réessayer plus tard.")
}
```

#### Network Error (Erreur réseau)

```kotlin
catch (e: ApiException.NetworkException) {
    showError("Erreur de connexion réseau. Vérifiez votre connexion internet.")
}
```

### Format des réponses d'erreur

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

---

## 💡 Conseils pour développeurs frontend

### 1. Gestion du token JWT

✅ **À faire** :
- Laisser l'`AuthInterceptor` gérer automatiquement l'ajout du token
- Vérifier `isLoggedIn()` avant d'accéder aux routes protégées
- Rediriger vers l'écran de connexion en cas d'erreur 401

❌ **À éviter** :
- Ne pas stocker le token manuellement
- Ne pas ajouter le header `Authorization` manuellement
- Ne pas ignorer les erreurs 401

### 2. Gestion des fichiers (multipart)

✅ **À faire** :
- Utiliser `File` pour les fichiers locaux
- Vérifier le type MIME avant l'upload
- Compresser les images avant l'upload si nécessaire
- Gérer les erreurs de taille de fichier

```kotlin
// Exemple : Vérifier la taille d'un fichier
fun isFileSizeValid(file: File, maxSizeMB: Int = 10): Boolean {
    val maxSizeBytes = maxSizeMB * 1024 * 1024
    return file.length() <= maxSizeBytes
}

// Exemple : Compresser une image
fun compressImage(bitmap: Bitmap, quality: Int = 80): File {
    val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }
    return file
}
```

### 3. Gestion des dates

✅ **À faire** :
- Utiliser le format `"yyyy-MM-dd"` pour les dates (ex: `"2024-01-15"`)
- Utiliser `SimpleDateFormat` pour parser et formater les dates
- Vérifier que la date de fin n'est pas dans le passé avant de créer un casting

```kotlin
val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

// Parser une date
val date = dateFormat.parse("2024-01-15")

// Formater une date
val dateString = dateFormat.format(Date())

// Vérifier si une date est dans le passé
fun isDateInPast(dateString: String): Boolean {
    val date = dateFormat.parse(dateString) ?: return false
    return date.before(Date())
}
```

### 4. Gestion des listes et filtres

✅ **À faire** :
- Filtrer les castings ouverts avant d'afficher
- Gérer les listes vides avec un message approprié
- Utiliser `filter` et `map` pour transformer les données

```kotlin
// Filtrer les castings ouverts et non expirés
val activeCastings = castings.filter { casting ->
    casting.ouvert && !isDateInPast(casting.dateFin ?: "")
}

// Grouper par type
val castingsByType = castings.groupBy { it.types?.firstOrNull() }
```

### 5. Gestion de l'état de chargement

✅ **À faire** :
- Afficher un indicateur de chargement pendant les requêtes
- Désactiver les boutons pendant les requêtes
- Gérer les états d'erreur avec des messages clairs

```kotlin
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }

Button(
    onClick = {
        isLoading = true
        errorMessage = null
        
        scope.launch {
            val result = repository.someAction()
            result.onSuccess {
                isLoading = false
                // Succès
            }
            result.onFailure { exception ->
                isLoading = false
                errorMessage = exception.message
            }
        }
    },
    enabled = !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
    } else {
        Text("Valider")
    }
}

errorMessage?.let { message ->
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}
```

### 6. Gestion des candidats avec statut

✅ **À faire** :
- Afficher le statut de chaque candidat avec des badges colorés
- Filtrer les candidats par statut
- Gérer les actions (accepter/refuser) avec confirmation

```kotlin
@Composable
fun CandidateItem(candidat: Candidat) {
    val statusColor = when (candidat.statut) {
        "EN_ATTENTE" -> Color.Orange
        "ACCEPTE" -> Color.Green
        "REFUSE" -> Color.Red
        else -> Color.Gray
    }
    
    val statusText = when (candidat.statut) {
        "EN_ATTENTE" -> "En attente"
        "ACCEPTE" -> "Accepté"
        "REFUSE" -> "Refusé"
        else -> "Inconnu"
    }
    
    Row {
        Text("${candidat.acteurId?.prenom} ${candidat.acteurId?.nom}")
        Spacer(modifier = Modifier.width(8.dp))
        Badge(containerColor = statusColor) {
            Text(statusText)
        }
    }
}
```

### 7. Performance et optimisation

✅ **À faire** :
- Utiliser `LaunchedEffect` pour charger les données au montage du composable
- Mettre en cache les images téléchargées
- Utiliser `remember` pour éviter les recalculs inutiles
- Paginer les listes longues

```kotlin
@Composable
fun CastingListScreen() {
    var castings by remember { mutableStateOf<List<Casting>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val result = castingRepository.getAllCastings()
        result.onSuccess {
            castings = it
            isLoading = false
        }
        result.onFailure {
            isLoading = false
            // Gérer l'erreur
        }
    }
    
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(castings) { casting ->
                CastingItem(casting)
            }
        }
    }
}
```

### 8. Tests et débogage

✅ **À faire** :
- Activer les logs HTTP en développement (`HttpLoggingInterceptor.Level.BODY`)
- Désactiver les logs en production (`HttpLoggingInterceptor.Level.NONE`)
- Utiliser `android.util.Log` pour tracer les erreurs
- Tester les cas d'erreur (réseau, serveur, validation)

```kotlin
// Dans ApiClient.kt
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

---

## 🛣️ Routes publiques vs protégées

### Routes publiques (pas de token nécessaire)

- ✅ `POST /acteur/signup` - Inscription d'un acteur
- ✅ `POST /agence/signup` - Inscription d'une agence
- ✅ `POST /auth/login` - Connexion
- ✅ `GET /castings` - Liste des castings (seulement GET)
- ✅ `GET /castings/:id` - Détails d'un casting
- ✅ `GET /media/:fileId` - Télécharger un média
- ✅ `GET /api` - Documentation Swagger

### Routes protégées (token JWT requis)

#### Acteurs
- ✅ `GET /acteur/me` - Profil de l'acteur connecté
- ✅ `GET /acteur/:id` - Profil d'un acteur par ID
- ✅ `PATCH /acteur/me` - Mettre à jour son profil
- ✅ `PATCH /acteur/:id` - Mettre à jour un profil (propriétaire ou admin)
- ✅ `PATCH /acteur/:id/media/profile` - Mettre à jour photo/CV
- ✅ `POST /acteur/:id/media/gallery` - Ajouter des photos à la galerie
- ✅ `DELETE /acteur/:id/media/gallery/:fileId` - Supprimer une photo

#### Agences
- ✅ `GET /agence/:id` - Profil d'une agence
- ✅ `PATCH /agence/:id` - Mettre à jour le profil (propriétaire ou admin)
- ✅ `PATCH /agence/:id/media/profile` - Mettre à jour logo/document

#### Castings
- ✅ `POST /castings` - Créer un casting (Recruteur uniquement)
- ✅ `PATCH /castings/:id` - Modifier un casting (propriétaire ou admin)
- ✅ `DELETE /castings/:id` - Supprimer un casting (propriétaire ou admin)
- ✅ `POST /castings/:id/apply` - Postuler à un casting (Acteur uniquement)
- ✅ `PATCH /castings/:id/candidates/:acteurId/accept` - Accepter un candidat (Recruteur/Admin)
- ✅ `PATCH /castings/:id/candidates/:acteurId/reject` - Refuser un candidat (Recruteur/Admin)
- ✅ `GET /castings/:id/my-status` - Voir son statut de candidature (Acteur uniquement)

#### Utilisateurs
- ✅ `GET /users` - Liste des utilisateurs (Admin uniquement)
- ✅ `GET /users/:id` - Informations d'un utilisateur
- ✅ `GET /users/me` - Profil de l'utilisateur connecté
- ✅ `PATCH /users/:id` - Mettre à jour un utilisateur (propriétaire ou admin)
- ✅ `DELETE /users/:id` - Supprimer un utilisateur (Admin uniquement)

---

## 📝 Notes importantes

### 1. Token JWT

- **Stockage automatique** : Lors de la connexion ou de l'inscription, le token JWT est automatiquement stocké dans `TokenManager` via `DataStore`.
- **Ajout automatique** : Pour toutes les requêtes protégées, le token est automatiquement ajouté dans le header `Authorization: Bearer <token>` par l'`AuthInterceptor`.
- **Expiration** : Les tokens JWT expirent après 7 jours. Après expiration, l'utilisateur doit se reconnecter.
- **Suppression automatique** : En cas d'erreur 401, le token est automatiquement supprimé par l'`ErrorInterceptor`.

### 2. Gestion des erreurs réseau

- Les erreurs de connexion réseau sont capturées et converties en `ApiException.NetworkException`.
- Vérifiez toujours la connexion internet avant d'effectuer des requêtes critiques.

### 3. Logging

- En mode debug, toutes les requêtes HTTP sont loggées avec `HttpLoggingInterceptor.Level.BODY`.
- En production, désactivez le logging dans `ApiClient.kt` en changeant `Level.BODY` en `Level.NONE`.

### 4. Timeouts

- Les timeouts sont configurés à 30 secondes pour la connexion, la lecture et l'écriture.
- Pour les téléchargements de fichiers volumineux, considérez augmenter le timeout de lecture.

### 5. Formats de fichiers

- **Photos** : JPEG ou PNG, max 10 Mo
- **Documents** : PDF, max 10 Mo
- **Affiches de casting** : JPEG ou PNG, max 10 Mo

### 6. Structure des candidats (nouvelle version)

Les candidats ont maintenant une structure enrichie :

```kotlin
data class Candidat(
    val acteurId: ActeurInfo?, // Informations complètes de l'acteur
    val statut: String?, // "EN_ATTENTE", "ACCEPTE", "REFUSE"
    val dateCandidature: String? // Format ISO
)
```

### 7. Champ `ouvert` des castings

- Le champ `ouvert` indique si le casting accepte des candidatures.
- Les castings expirés (date de fin dépassée) sont automatiquement fermés par le backend.
- Impossible de rouvrir un casting expiré.

---

## 🔗 Ressources supplémentaires

- **Documentation Swagger** : `https://cast-mate.vercel.app/api`
- **Code source** : Voir les fichiers dans `data/repository/` pour des exemples complets
- **Modèles de données** : Voir les fichiers dans `data/model/` pour la structure complète

---

## ❓ Questions fréquentes

### Q: Comment savoir si un casting est ouvert aux candidatures ?

R: Vérifiez le champ `ouvert` ET que la date de fin n'est pas dépassée :

```kotlin
fun canApply(casting: Casting): Boolean {
    val isOpen = casting.ouvert
    val isNotExpired = !isDateInPast(casting.dateFin ?: "")
    return isOpen && isNotExpired
}
```

### Q: Comment afficher le statut d'un candidat ?

R: Utilisez le champ `statut` de l'objet `Candidat` :

```kotlin
when (candidat.statut) {
    "EN_ATTENTE" -> Badge("En attente", Color.Orange)
    "ACCEPTE" -> Badge("Accepté", Color.Green)
    "REFUSE" -> Badge("Refusé", Color.Red)
    else -> Badge("Inconnu", Color.Gray)
}
```

### Q: Comment gérer les fichiers multipart ?

R: Utilisez les méthodes du repository qui acceptent des `File?` :

```kotlin
val result = acteurRepository.updateProfileMedia(
    acteurId = null,
    photoFile = selectedPhotoFile, // File? - Optionnel
    documentFile = selectedDocumentFile // File? - Optionnel
)
```

### Q: Comment télécharger et afficher une image ?

R: Utilisez `downloadMedia` et convertissez en `Bitmap` :

```kotlin
val result = repository.downloadMedia(fileId)
result.onSuccess { responseBody ->
    val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
    val imageBitmap = bitmap.asImageBitmap()
    // Afficher l'image
}
```

---

**Dernière mise à jour** : Novembre 2025 
**Version de l'API** : Compatible avec les dernières modifications backend (types, age, ouvert, candidats avec statut)
