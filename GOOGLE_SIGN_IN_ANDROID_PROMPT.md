# 🚀 Prompt pour Cursor IA : Implémenter Sign in with Google dans CastMate Android

## 📋 Contexte du Projet

**Application** : CastMate Android  
**Langage** : Kotlin  
**Framework UI** : Jetpack Compose  
**Architecture** : Repository Pattern avec Retrofit  
**Authentification** : JWT Token via backend NestJS

## 🎯 Objectif

Implémenter une fonctionnalité complète de **Sign in with Google** pour les utilisateurs **ACTEUR** et **RECRUTEUR** (Agence) avec :
- Vérification du rôle utilisateur
- Gestion des comptes existants vs nouveaux comptes
- Création automatique de compte si nécessaire
- Navigation selon le rôle après connexion
- Gestion d'erreurs robuste

---

## 🔑 Configuration Google OAuth 2.0

### Client ID Web (OAuth 2.0)
```
873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com
```

### Email de l'application
L'email est extrait automatiquement du compte Google sélectionné par l'utilisateur.

### Fichiers de configuration

**Fichier** : `app/src/main/res/values/strings.xml`
```xml
<string name="default_web_client_id">873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com</string>
```

**Fichier** : `app/build.gradle.kts`
```kotlin
dependencies {
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")
}
```

---

## 📐 Structure de Données

### 1. GoogleLoginRequest
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/model/GoogleLoginRequest.kt`
```kotlin
package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)
```

### 2. AuthResponse
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/model/AuthResponse.kt`
```kotlin
package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("message")
    val message: String? = null
)
```

### 3. User et UserRole
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/model/User.kt`
```kotlin
package com.example.projecct_mobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("id")
    val idAlt: String? = null,
    
    @SerializedName("nom")
    val nom: String? = null,
    
    @SerializedName("prenom")
    val prenom: String? = null,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: UserRole? = null,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("photoProfil")
    val photoProfil: String? = null
) {
    val actualId: String?
        get() = id ?: idAlt
}

enum class UserRole {
    @SerializedName("ACTEUR")
    ACTEUR,
    
    @SerializedName("RECRUTEUR")
    RECRUTEUR,
    
    @SerializedName("ADMIN")
    ADMIN
}
```

---

## 🔌 API Backend

### Endpoint : POST `/auth/google`

**Requête** :
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij..."
}
```

**Réponse Succès (200)** :
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "_id": "507f1f77bcf86cd799439011",
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@gmail.com",
    "role": "ACTEUR",
    "photoProfil": "https://..."
  }
}
```

**Réponse Erreur (404)** : Compte non trouvé (nouveau compte Google)
```json
{
  "message": "Compte Google non trouvé"
}
```

**Réponse Erreur (401)** : Authentification Google refusée
```json
{
  "message": "Authentification Google refusée"
}
```

---

## 🏗️ Architecture Existante

### 1. GoogleAuthUiClient
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/utils/GoogleAuthUiClient.kt`

Cette classe gère déjà l'interaction avec Google Sign-In :
- Configuration Google Sign-In Options
- Récupération de l'ID Token
- Gestion de l'intent de connexion

**Utilisation** :
```kotlin
val googleAuthClient = GoogleAuthUiClient(context)
val signInIntent = googleAuthClient.getSignInIntent()
// Lancer l'intent avec ActivityResultLauncher
val accountResult = googleAuthClient.getAccountFromIntent(result.data)
val idToken = accountResult?.idToken
```

### 2. AuthRepository
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/repository/AuthRepository.kt`

Méthode existante : `loginWithGoogle(idToken: String): Result<AuthResponse>`

### 3. AuthApiService
**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/api/AuthApiService.kt`

Interface existante :
```kotlin
@POST("auth/google")
suspend fun loginWithGoogle(
    @Body request: GoogleLoginRequest
): Response<AuthResponse>
```

---

## 📱 Écrans UI Existants

### 1. SignInScreen (Acteur)
**Fichier** : `app/src/main/java/com/example/projecct_mobile/ui/screens/auth/SignInScreen.kt`

- Paramètre `role: String` ("actor" ou "agency")
- Callback `onGoogleSignInClick: () -> Unit`
- Paramètre `isGoogleLoading: Boolean`

### 2. SignInAgencyScreen (Agence)
**Fichier** : `app/src/main/java/com/example/projecct_mobile/ui/screens/agence/auth/SignInAgencyScreen.kt`

- Callback `onGoogleSignInClick: () -> Unit`
- Paramètre `isLoading: Boolean`

### 3. MainActivity (Navigation)
**Fichier** : `app/src/main/java/com/example/projecct_mobile/MainActivity.kt`

- Gestion de la navigation après connexion
- Variables d'état pour Google Sign-In

---

## 🎯 Fonctionnalités à Implémenter

### 1. Vérification du Rôle

**Règles de vérification** :
- Si l'utilisateur se connecte depuis **SignInScreen** avec `role = "actor"` → Vérifier que `user.role == "ACTEUR"`
- Si l'utilisateur se connecte depuis **SignInAgencyScreen** → Vérifier que `user.role == "RECRUTEUR"`
- Si le rôle ne correspond pas → Afficher un message d'erreur approprié

### 2. Gestion des Comptes Existants vs Nouveaux

**Scénario 1 : Compte existant**
- L'API retourne 200 avec `access_token` et `user`
- Stocker le token et les infos utilisateur
- Naviguer vers l'écran approprié selon le rôle

**Scénario 2 : Nouveau compte (404)**
- L'API retourne 404 "Compte Google non trouvé"
- Créer automatiquement un nouveau compte avec :
  - Email du compte Google
  - Prénom/Nom depuis le profil Google
  - Photo de profil depuis Google (optionnel)
  - Rôle selon la page de connexion (ACTEUR ou RECRUTEUR)

### 3. Création Automatique de Compte

**Pour ACTEUR** :
- Endpoint : `POST /acteur/signup` (Multipart)
- Créer un mot de passe aléatoire (optionnel pour Google Sign-In uniquement)
- Télécharger la photo Google si disponible
- Stocker le mot de passe généré pour une connexion future si nécessaire

**Pour RECRUTEUR** :
- Endpoint : `POST /agence/signup` (Multipart)
- Créer un mot de passe aléatoire
- Télécharger le logo Google si disponible

### 4. Navigation après Connexion

**ACTEUR** :
- Naviguer vers `"actorHome"` avec `popUpTo("home") { inclusive = true }`

**RECRUTEUR** :
- Naviguer vers `"agencyCastingList"` avec `popUpTo("home") { inclusive = true }`

---

## 🔄 Flow Complet

### Flow Acteur (SignInScreen)

```
1. Utilisateur clique sur "Continuer avec Google"
2. Lancement de Google Sign-In Intent
3. Utilisateur sélectionne un compte Google
4. Récupération de l'ID Token
5. Appel API : POST /auth/google avec idToken
6. Si succès (200) :
   - Vérifier que user.role == "ACTEUR"
   - Si OK : Stocker token + infos utilisateur → Naviguer vers "actorHome"
   - Si KO : Afficher erreur "Ce compte Google est associé à un compte agence"
7. Si erreur 404 (Compte non trouvé) :
   - Récupérer prénom/nom/email/photo depuis GoogleAccount
   - Créer nouveau compte : POST /acteur/signup avec :
     * email: GoogleAccount.email
     * prenom: GoogleAccount.givenName
     * nom: GoogleAccount.familyName
     * password: Générer un mot de passe aléatoire (optionnel)
     * photo: Télécharger depuis GoogleAccount.photoUrl (si disponible)
   - Après création : Se connecter automatiquement avec le nouveau compte
   - Naviguer vers "actorHome"
```

### Flow Agence (SignInAgencyScreen)

```
1. Utilisateur clique sur "Continuer avec Google"
2. Lancement de Google Sign-In Intent
3. Utilisateur sélectionne un compte Google
4. Récupération de l'ID Token
5. Appel API : POST /auth/google avec idToken
6. Si succès (200) :
   - Vérifier que user.role == "RECRUTEUR"
   - Si OK : Stocker token + infos utilisateur → Naviguer vers "agencyCastingList"
   - Si KO : Afficher erreur "Ce compte Google est associé à un compte acteur"
7. Si erreur 404 (Compte non trouvé) :
   - Récupérer infos depuis GoogleAccount
   - Créer nouveau compte agence : POST /agence/signup avec :
     * email: GoogleAccount.email
     * responsable: "${GoogleAccount.givenName} ${GoogleAccount.familyName}"
     * nomAgence: Utiliser nom du compte Google ou demander à l'utilisateur
     * password: Générer un mot de passe aléatoire
     * photo: Télécharger depuis GoogleAccount.photoUrl (si disponible)
   - Après création : Se connecter automatiquement
   - Naviguer vers "agencyCastingList"
```

---

## 🛠️ Implémentation Détaillée

### 1. MainActivity.kt - Google Sign-In Launcher

**Variables d'état** :
```kotlin
val googleAuthClient = remember { GoogleAuthUiClient(context) }
var googleSignInLoading by remember { mutableStateOf(false) }
var googleSignInError by remember { mutableStateOf<String?>(null) }
var isGoogleSignInForAgency by remember { mutableStateOf(false) }
```

**ActivityResultLauncher pour Acteur** :
```kotlin
val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val accountResult = googleAuthClient.getAccountFromIntent(result.data)
    
    accountResult.onSuccess { account ->
        val idToken = account.idToken
        if (idToken != null) {
            scope.launch {
                googleSignInLoading = true
                googleSignInError = null
                
                // Appel API loginWithGoogle
                val loginResult = authRepository.loginWithGoogle(idToken)
                
                loginResult.onSuccess { authResponse ->
                    val userRole = authResponse.user?.role?.name ?: "UNKNOWN"
                    
                    // Vérifier le rôle
                    if (userRole == "ACTEUR") {
                        // Succès : Naviguer vers actorHome
                        navController.navigate("actorHome") {
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        // Erreur : Mauvais rôle
                        googleSignInError = "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence."
                    }
                }.onFailure { exception ->
                    if (exception is ApiException.NotFoundException) {
                        // Nouveau compte : Créer automatiquement
                        createNewActorAccount(account)
                    } else {
                        googleSignInError = exception.message ?: "Erreur de connexion Google"
                    }
                }
                
                googleSignInLoading = false
            }
        } else {
            googleSignInError = "ID Token non disponible"
            googleSignInLoading = false
        }
    }.onFailure { error ->
        googleSignInError = "Erreur Google Sign-In: ${error.message}"
        googleSignInLoading = false
    }
}
```

**Fonction de création de compte Acteur** :
```kotlin
suspend fun createNewActorAccount(googleAccount: GoogleSignInAccount) {
    val email = googleAccount.email ?: run {
        googleSignInError = "Email Google non disponible"
        googleSignInLoading = false
        return
    }
    
    val prenom = googleAccount.givenName ?: "Utilisateur"
    val nom = googleAccount.familyName ?: "Google"
    val photoUrl = googleAccount.photoUrl?.toString()
    
    // Générer un mot de passe aléatoire (optionnel pour Google Sign-In uniquement)
    val randomPassword = "Google_${System.currentTimeMillis()}_CastMate"
    
    // Télécharger la photo si disponible
    var photoFile: File? = null
    photoUrl?.let { url ->
        try {
            // Télécharger la photo et la sauvegarder temporairement
            val photoBytes = downloadImage(url)
            photoFile = File(context.cacheDir, "google_photo_${System.currentTimeMillis()}.jpg")
            photoFile?.writeBytes(photoBytes)
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Erreur téléchargement photo: ${e.message}")
        }
    }
    
    // Créer le compte
    val signupResult = authRepository.signupActeur(
        email = email,
        password = randomPassword,
        prenom = prenom,
        nom = nom,
        photo = photoFile
    )
    
    signupResult.onSuccess { authResponse ->
        // Compte créé : Naviguer vers actorHome
        navController.navigate("actorHome") {
            popUpTo("home") { inclusive = true }
        }
        googleSignInLoading = false
    }.onFailure { exception ->
        googleSignInError = "Erreur création compte: ${exception.message}"
        googleSignInLoading = false
    }
}
```

**ActivityResultLauncher pour Agence** (similaire mais avec vérification "RECRUTEUR") :
```kotlin
val agencyGoogleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    // Même logique mais avec vérification role == "RECRUTEUR"
    // Et création via authRepository.signupAgence()
    // Navigation vers "agencyCastingList"
}
```

### 2. Boutons UI

**SignInScreen.kt** :
```kotlin
OutlinedButton(
    onClick = {
        googleSignInError = null
        googleSignInLoading = true
        googleAuthClient.signOut() // Déconnexion précédente si nécessaire
        googleSignInLauncher.launch(googleAuthClient.getSignInIntent())
    },
    enabled = !googleSignInLoading
) {
    if (googleSignInLoading) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("G+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continuer avec Google", fontSize = 16.sp)
        }
    }
}
```

**SignInAgencyScreen.kt** : Même structure mais avec `agencyGoogleSignInLauncher`

### 3. Gestion des Erreurs

**Messages d'erreur à afficher** :
- "Email Google non disponible" : Si le compte Google n'a pas d'email
- "Ce compte Google est associé à un compte agence" : Si l'utilisateur essaie de se connecter en tant qu'acteur mais a un compte agence
- "Ce compte Google est associé à un compte acteur" : Si l'utilisateur essaie de se connecter en tant qu'agence mais a un compte acteur
- "Erreur création compte: [détails]" : Si la création automatique échoue
- "Erreur de connexion Google: [détails]" : Autres erreurs

**Affichage des erreurs** :
```kotlin
googleSignInError?.let { error ->
    AlertDialog(
        onDismissRequest = { googleSignInError = null },
        title = { Text("Erreur de connexion Google") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = { googleSignInError = null }) {
                Text("OK")
            }
        }
    )
}
```

---

## ✅ Checklist d'Implémentation

- [ ] Vérifier que `GoogleAuthUiClient` est correctement configuré avec le Client ID Web
- [ ] Implémenter `googleSignInLauncher` dans `MainActivity` pour les acteurs
- [ ] Implémenter `agencyGoogleSignInLauncher` dans `MainActivity` pour les agences
- [ ] Ajouter la vérification du rôle après connexion Google
- [ ] Implémenter la création automatique de compte si 404
- [ ] Ajouter le téléchargement de la photo Google lors de la création de compte
- [ ] Ajouter la gestion des erreurs avec messages appropriés
- [ ] Tester le flow complet pour un compte existant (ACTEUR)
- [ ] Tester le flow complet pour un compte existant (RECRUTEUR)
- [ ] Tester le flow complet pour un nouveau compte (ACTEUR)
- [ ] Tester le flow complet pour un nouveau compte (RECRUTEUR)
- [ ] Tester le cas d'erreur : compte Google avec mauvais rôle
- [ ] Tester le cas d'erreur : compte Google sans email

---

## 🔍 Points Importants

1. **Client ID Web** : Utiliser TOUJOURS le Client ID Web (`873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com`) pour obtenir l'ID Token, pas un Client ID Android.

2. **Vérification du Rôle** : CRITIQUE - Vérifier que le rôle retourné par l'API correspond au rôle attendu selon la page de connexion.

3. **Création Automatique** : Si l'API retourne 404, créer automatiquement un compte avec les informations du compte Google.

4. **Stockage du Token** : Le token JWT est automatiquement stocké par `AuthRepository.loginWithGoogle()` via `tokenManager.saveToken()`.

5. **Gestion des Photos** : Télécharger la photo Google de manière asynchrone et la passer au endpoint d'inscription si disponible.

6. **Navigation** : Toujours utiliser `popUpTo("home") { inclusive = true }` pour nettoyer la pile de navigation après connexion réussie.

---

## 📝 Exemples de Code Complets

### Exemple 1 : Google Sign-In Launcher Complet (Acteur)

```kotlin
val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val accountResult = googleAuthClient.getAccountFromIntent(result.data)
    
    accountResult.onSuccess { account ->
        val idToken = account.idToken
        if (idToken == null) {
            googleSignInError = "ID Token non disponible"
            googleSignInLoading = false
            return@onSuccess
        }
        
        scope.launch {
            googleSignInLoading = true
            googleSignInError = null
            
            val loginResult = authRepository.loginWithGoogle(idToken)
            
            loginResult.onSuccess { authResponse ->
                val userRole = authResponse.user?.role?.name ?: "UNKNOWN"
                
                if (userRole == "ACTEUR") {
                    navController.navigate("actorHome") {
                        popUpTo("home") { inclusive = true }
                    }
                    googleSignInLoading = false
                    googleSignInError = null
                } else {
                    googleSignInLoading = false
                    googleSignInError = when (userRole) {
                        "RECRUTEUR" -> "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence."
                        else -> "Ce compte Google n'est pas associé à un compte acteur."
                    }
                }
            }.onFailure { exception ->
                when (exception) {
                    is ApiException.NotFoundException -> {
                        // Nouveau compte : créer automatiquement
                        createNewActorAccountFromGoogle(account)
                    }
                    else -> {
                        googleSignInLoading = false
                        googleSignInError = exception.message ?: "Erreur de connexion Google"
                    }
                }
            }
        }
    }.onFailure { error ->
        googleSignInLoading = false
        googleSignInError = when (error) {
            is GoogleApiException -> when (error.statusCode) {
                12501 -> "Connexion Google annulée"
                7 -> "Impossible de contacter Google. Vérifiez votre connexion."
                else -> "Erreur Google (${error.statusCode})"
            }
            else -> "Erreur: ${error.message}"
        }
    }
}

suspend fun createNewActorAccountFromGoogle(account: GoogleSignInAccount) {
    val email = account.email ?: run {
        googleSignInError = "Email Google non disponible"
        googleSignInLoading = false
        return
    }
    
    val prenom = account.givenName ?: "Utilisateur"
    val nom = account.familyName ?: "Google"
    val photoUrl = account.photoUrl?.toString()
    
    // Télécharger photo
    var photoFile: File? = null
    photoUrl?.let { url ->
        try {
            val response = okHttpClient.newCall(
                okhttp3.Request.Builder().url(url).build()
            ).execute()
            val bytes = response.body?.bytes()
            if (bytes != null) {
                photoFile = File(context.cacheDir, "google_photo_${System.currentTimeMillis()}.jpg")
                photoFile?.writeBytes(bytes)
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Erreur téléchargement photo: ${e.message}")
        }
    }
    
    // Créer compte
    val randomPassword = "Google_${System.currentTimeMillis()}_CastMate"
    val signupResult = authRepository.signupActeur(
        email = email,
        password = randomPassword,
        prenom = prenom,
        nom = nom,
        photo = photoFile
    )
    
    signupResult.onSuccess { authResponse ->
        navController.navigate("actorHome") {
            popUpTo("home") { inclusive = true }
        }
        googleSignInLoading = false
        googleSignInError = null
    }.onFailure { exception ->
        googleSignInLoading = false
        googleSignInError = "Erreur création compte: ${exception.message}"
    }
}
```

### Exemple 2 : Bouton Google Sign-In dans SignInScreen

```kotlin
@Composable
fun SignInScreen(
    onGoogleSignInClick: () -> Unit,
    isGoogleLoading: Boolean,
    role: String = "actor"
) {
    Column {
        // ... autres champs ...
        
        // Séparateur
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("Or", modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bouton Google Sign-In
        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isGoogleLoading
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "G+",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        "Continuer avec Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
```

---

## 🎉 Résultat Attendu

Après implémentation complète :

1. ✅ Un utilisateur peut se connecter avec Google depuis la page Acteur
2. ✅ Un utilisateur peut se connecter avec Google depuis la page Agence
3. ✅ Si le compte existe déjà, connexion directe avec vérification du rôle
4. ✅ Si le compte n'existe pas, création automatique avec les infos Google
5. ✅ Navigation automatique vers l'écran approprié selon le rôle
6. ✅ Messages d'erreur clairs en cas de problème (mauvais rôle, email manquant, etc.)
7. ✅ Téléchargement automatique de la photo de profil Google lors de la création de compte

---

**Fin du Prompt**
