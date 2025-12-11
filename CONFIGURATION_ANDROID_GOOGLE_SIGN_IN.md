# ✅ Configuration Android Google Sign-In - CastMate

## 📋 Informations du Projet

### Projet Google Cloud
- **Nom du projet** : `CasteMate`
- **Email du compte** : `castemate4@gmail.com`
- **Project Number** : `873587147400`

### Client ID Web OAuth 2.0 (CRITIQUE)
```
873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com
```

**Type** : Application Web (Web Application)  
**Usage** : Utilisé pour obtenir l'ID Token Google nécessaire pour l'authentification backend

### Package Name Android
```
com.example.projecct_mobile
```

---

## ✅ Configuration Actuelle (Vérifiée)

### 1. Client ID Web dans strings.xml ✅

**Fichier** : `app/src/main/res/values/strings.xml`

```xml
<string name="default_web_client_id">873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com</string>
```

**Status** : ✅ Correctement configuré

---

### 2. GoogleAuthUiClient.kt ✅

**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/utils/GoogleAuthUiClient.kt`

```kotlin
private val signInClient by lazy {
    val webClientId = context.getString(R.string.default_web_client_id)
    
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        // Utilise le Client ID Web OAuth 2.0 pour obtenir l'ID token
        .requestIdToken(webClientId)
        .build()
    GoogleSignIn.getClient(context, options)
}
```

**Status** : ✅ Utilise correctement le Client ID Web avec `requestIdToken()`

---

### 3. API Backend - AuthRepository.kt ✅

**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/repository/AuthRepository.kt`

```kotlin
suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
    val request = GoogleLoginRequest(idToken = idToken)
    val response = authService.loginWithGoogle(request)
    
    if (response.isSuccessful && response.body() != null) {
        val authResponse = response.body()!!
        val accessToken = authResponse.accessToken
        
        tokenManager.saveToken(accessToken)
        tokenManager.saveUserInfo(...)
        
        Result.success(authResponse)
    } else {
        when (response.code()) {
            404 -> Result.failure(ApiException.NotFoundException(...))
            401 -> Result.failure(ApiException.UnauthorizedException(...))
            else -> Result.failure(ApiException.BadRequestException(...))
        }
    }
}
```

**Status** : ✅ Correctement implémenté avec gestion des erreurs 404, 401

---

### 4. API Service ✅

**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/api/AuthApiService.kt`

```kotlin
@POST("auth/google")
suspend fun loginWithGoogle(
    @Body request: GoogleLoginRequest
): Response<AuthResponse>
```

**Base URL** : `https://cast-mate.vercel.app`  
**Endpoint** : `POST /auth/google`  
**Status** : ✅ Correctement configuré

---

### 5. GoogleLoginRequest ✅

**Fichier** : `app/src/main/java/com/example/projecct_mobile/data/model/GoogleLoginRequest.kt`

```kotlin
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String
)
```

**Status** : ✅ Structure correcte

---

## 🔄 Flux de Connexion Google (Android)

### Pour les Acteurs (ACTEUR)

1. **Utilisateur clique sur "Continuer avec Google"** (bouton acteur)
2. **Google Sign-In s'ouvre** → Utilisateur sélectionne son compte Google
3. **Récupération de l'ID Token** depuis Google via `GoogleAuthUiClient`
4. **Appel API** : `POST /auth/google` avec `{ "idToken": "..." }`
5. **Si compte existe (200)** :
   - Sauvegarder `accessToken` (JWT) dans `TokenManager`
   - Sauvegarder les infos utilisateur (`user.id`, `user.email`, `user.role`)
   - Rediriger vers l'écran d'accueil acteur
6. **Si compte n'existe pas (404)** :
   - Extraire les infos Google : `email`, `givenName`, `familyName`, `photoURL`
   - Pré-remplir le formulaire d'inscription acteur
   - Rediriger vers l'écran d'inscription acteur (Étape 1)

### Pour les Agences (RECRUTEUR)

1. **Utilisateur clique sur "Continuer avec Google"** (bouton agence)
2. **Google Sign-In s'ouvre** → Utilisateur sélectionne son compte Google
3. **Récupération de l'ID Token** depuis Google via `GoogleAuthUiClient`
4. **Appel API** : `POST /auth/google` avec `{ "idToken": "..." }`
5. **Si compte existe (200)** :
   - Sauvegarder `accessToken` (JWT) dans `TokenManager`
   - Sauvegarder les infos utilisateur (`user.id`, `user.email`, `user.role`)
   - Rediriger vers l'écran d'accueil agence
6. **Si compte n'existe pas (404)** :
   - Extraire les infos Google : `email`, `givenName`, `familyName`, `photoURL`
   - Pré-remplir le formulaire d'inscription agence
   - Rediriger vers l'écran d'inscription agence (Étape 1)

---

## 🌐 Endpoints API Backend

### Base URL
```
https://cast-mate.vercel.app
```

### Endpoint Google Sign-In
```
POST /auth/google
```

**Headers** :
```
Content-Type: application/json
```

**Body** :
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij..."
}
```

**Réponse Succès (200)** :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "email": "user@example.com",
    "role": {
      "name": "ACTEUR" // ou "RECRUTEUR"
    },
    "nom": "Doe",
    "prenom": "John",
    "bio": "Acteur professionnel"
  }
}
```

**Réponse Compte Non Trouvé (404)** :
```json
{
  "message": "Compte Google non trouvé"
}
```

**Réponse Erreur (401)** :
```json
{
  "message": "Authentification Google refusée"
}
```

---

## ⚠️ Points Importants

1. **Client ID Web** : Utilisez toujours le **Client ID Web** (`873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com`) pour obtenir l'ID Token, pas un Client ID Android.

2. **ID Token** : L'ID Token est **essentiel** pour l'authentification backend. Sans ID Token, l'API retournera une erreur.

3. **Gestion des Erreurs** :
   - **404** : Compte non trouvé → Rediriger vers inscription
   - **401** : Token invalide → Réessayer la connexion Google
   - **500** : Erreur serveur → Afficher message d'erreur

4. **Deux Types de Connexion** : L'application distingue si l'utilisateur veut se connecter en tant qu'**Acteur** ou **Agence** avant d'appeler Google Sign-In.

5. **Pré-remplissage Inscription** : Si le compte n'existe pas (404), utilisez les informations Google (`email`, `givenName`, `familyName`, `photoURL`) pour pré-remplir le formulaire d'inscription.

---

## 🔐 Stockage Local (TokenManager)

Après une connexion réussie, stockez :

1. **Access Token** (JWT) → `TokenManager.saveToken()` (sécurisé)
2. **User ID** → `TokenManager.saveUserInfo()`
3. **Email** → `TokenManager.saveUserInfo()`
4. **Role** (`ACTEUR` ou `RECRUTEUR`) → `TokenManager.saveUserInfo()`
5. **Nom** et **Prénom** → `TokenManager.saveUserInfo()`

**Important** : L'Access Token est stocké de manière sécurisée via `TokenManager` car il est utilisé pour toutes les requêtes authentifiées.

---

## ✅ Checklist Configuration

- [x] Client ID Web configuré dans `strings.xml`
- [x] `GoogleAuthUiClient` utilise `requestIdToken()` avec Client ID Web
- [x] API `/auth/google` correctement implémentée
- [x] Gestion des erreurs 404, 401, 500
- [x] Stockage du token via `TokenManager`
- [x] Flux de connexion pour Acteur
- [x] Flux de connexion pour Agence
- [x] Pré-remplissage inscription si compte n'existe pas

---

## 🎯 Configuration Google Cloud Console Requise

### 1. Client OAuth Android (Optionnel mais recommandé)

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Sélectionnez le projet **"CasteMate"**
3. Allez dans **"APIs & Services"** → **"Credentials"**
4. Créez un Client OAuth de type **"Android"** avec :
   - **Nom** : `CasteMate Android`
   - **Package name** : `com.example.projecct_mobile`
   - **SHA-1** : (optionnel, peut être laissé vide selon configuration)

### 2. Client OAuth Web (OBLIGATOIRE) ✅

- **Client ID** : `873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com`
- **Status** : ✅ Déjà configuré dans `strings.xml`

### 3. API Google Sign-In (OBLIGATOIRE)

1. Allez dans **"APIs & Services"** → **"Library"**
2. Recherchez **"Google Sign-In API"** ou **"Identity Toolkit API"**
3. Vérifiez qu'elle est **activée**

---

**Date de création** : 2025-01-16  
**Version** : 1.0  
**Projet** : CastMate Android

