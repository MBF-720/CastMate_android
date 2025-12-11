# 🔐 Guide Complet : Google Sign-In pour iOS - CastMate

## 📋 Informations du Projet Android (Référence)

Ce document contient **TOUTES** les informations nécessaires pour implémenter Google Sign-In dans l'application iOS CastMate, basées sur l'implémentation Android existante.

---

## 🔑 Clés Google Cloud Console

### Projet Google Cloud
- **Nom du projet** : `CasteMate`
- **Email du compte** : `castemate4@gmail.com`

### Client ID Web OAuth 2.0 (CRITIQUE)
```
873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com
```

**Type** : Application Web (Web Application)  
**Usage** : Utilisé pour obtenir l'ID Token Google nécessaire pour l'authentification backend

### Informations Supplémentaires
- **Project Number** : `873587147400`
- **Package Name Android** : `com.example.projecct_mobile` (pour référence)

---

## 🎯 Configuration iOS dans Google Cloud Console

### Étape 1 : Créer un Client OAuth iOS

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Sélectionnez le projet **"CasteMate"**
3. Allez dans **"APIs & Services"** → **"Credentials"**
4. Cliquez sur **"+ Créer des identifiants"** → **"ID client OAuth"**
5. Sélectionnez **"iOS"**
6. Remplissez :
   - **Nom** : `CasteMate iOS`
   - **ID du bundle** : Votre Bundle ID iOS (ex: `com.castemate.ios`)
7. Cliquez sur **"Créer"**
8. **IMPORTANT** : Notez le **Client ID iOS** généré

### Étape 2 : Activer l'API Google Sign-In

1. Dans Google Cloud Console, allez sur **"APIs & Services"** → **"Library"**
2. Recherchez **"Google Sign-In API"** ou **"Identity Toolkit API"**
3. Cliquez sur **"Enable"** (Activer)

---

## 📱 Implémentation iOS

### 1. Installation des Dépendances

Ajoutez Google Sign-In SDK à votre projet iOS :

**CocoaPods** (recommandé) :
```ruby
pod 'GoogleSignIn'
```

**Swift Package Manager** :
```
https://github.com/google/GoogleSignIn-iOS
```

### 2. Configuration dans Info.plist

Ajoutez votre **Client ID iOS** dans `Info.plist` :

```xml
<key>GIDClientID</key>
<string>VOTRE_CLIENT_ID_IOS_ICI</string>
```

### 3. Configuration URL Scheme

Ajoutez l'URL Scheme pour Google Sign-In dans `Info.plist` :

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>VOTRE_CLIENT_ID_IOS_INVERSE.apps.googleusercontent.com</string>
        </array>
    </dict>
</array>
```

**Note** : L'URL Scheme est l'inverse du Client ID iOS (ex: si Client ID = `123-abc.apps.googleusercontent.com`, URL Scheme = `com.googleusercontent.apps.abc-123`)

### 4. Code Swift - Implémentation Google Sign-In

```swift
import GoogleSignIn

class GoogleAuthManager {
    // ⚠️ IMPORTANT : Utilisez le Client ID Web pour obtenir l'ID Token
    private let webClientID = "873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com"
    
    func configure() {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let plist = NSDictionary(contentsOfFile: path),
              let clientId = plist["CLIENT_ID"] as? String else {
            fatalError("GoogleService-Info.plist not found or CLIENT_ID missing")
        }
        
        let config = GIDConfiguration(clientID: clientId)
        GIDSignIn.redInstance.configuration = config
    }
    
    func signIn(presentingViewController: UIViewController, userType: UserType) async throws -> GoogleSignInResult {
        guard let presentingViewController = presentingViewController as? UIViewController else {
            throw GoogleSignInError.invalidViewController
        }
        
        // Configuration avec le Client ID Web pour obtenir l'ID Token
        let config = GIDConfiguration(clientID: webClientID)
        GIDSignIn.sharedInstance.configuration = config
        
        guard let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) else {
            throw GoogleSignInError.signInFailed
        }
        
        guard let idToken = result.user.idToken?.tokenString else {
            throw GoogleSignInError.idTokenMissing
        }
        
        let email = result.user.profile?.email ?? ""
        let givenName = result.user.profile?.givenName ?? ""
        let familyName = result.user.profile?.familyName ?? ""
        let displayName = result.user.profile?.name ?? ""
        let photoURL = result.user.profile?.imageURL(withDimension: 200)?.absoluteString
        
        return GoogleSignInResult(
            idToken: idToken,
            email: email,
            givenName: givenName,
            familyName: familyName,
            displayName: displayName,
            photoURL: photoURL,
            userType: userType
        )
    }
}

enum UserType {
    case actor
    case agency
}

struct GoogleSignInResult {
    let idToken: String
    let email: String
    let givenName: String
    let familyName: String
    let displayName: String
    let photoURL: String?
    let userType: UserType
}

enum GoogleSignInError: Error {
    case invalidViewController
    case signInFailed
    case idTokenMissing
}
```

### 5. Appel API Backend

```swift
struct GoogleLoginRequest: Codable {
    let idToken: String
}

struct AuthResponse: Codable {
    let accessToken: String
    let user: User?
}

struct User: Codable {
    let id: String?
    let email: String?
    let role: UserRole?
    let nom: String?
    let prenom: String?
    let bio: String?
}

enum UserRole: String, Codable {
    case ACTEUR = "ACTEUR"
    case RECRUTEUR = "RECRUTEUR"
}

class AuthService {
    private let baseURL = "https://cast-mate.vercel.app"
    
    func loginWithGoogle(idToken: String) async throws -> AuthResponse {
        let url = URL(string: "\(baseURL)/auth/google")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = GoogleLoginRequest(idToken: idToken)
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw AuthError.invalidResponse
        }
        
        if httpResponse.statusCode == 404 {
            // Compte Google non trouvé - rediriger vers inscription
            throw AuthError.accountNotFound
        }
        
        if httpResponse.statusCode != 200 {
            throw AuthError.loginFailed
        }
        
        let authResponse = try JSONDecoder().decode(AuthResponse.self, from: data)
        return authResponse
    }
}

enum AuthError: Error {
    case invalidResponse
    case accountNotFound
    case loginFailed
}
```

---

## 🔄 Flux de Connexion Google

### Pour les Acteurs (ACTEUR)

1. **Utilisateur clique sur "Continuer avec Google"** (bouton acteur)
2. **Google Sign-In s'ouvre** → Utilisateur sélectionne son compte Google
3. **Récupération de l'ID Token** depuis Google
4. **Appel API** : `POST /auth/google` avec `{ "idToken": "..." }`
5. **Si compte existe (200)** :
   - Sauvegarder `accessToken` (JWT)
   - Sauvegarder les infos utilisateur (`user.id`, `user.email`, `user.role`)
   - Rediriger vers l'écran d'accueil acteur
6. **Si compte n'existe pas (404)** :
   - Extraire les infos Google : `email`, `givenName`, `familyName`, `photoURL`
   - Pré-remplir le formulaire d'inscription acteur
   - Rediriger vers l'écran d'inscription acteur (Étape 1)

### Pour les Agences (RECRUTEUR)

1. **Utilisateur clique sur "Continuer avec Google"** (bouton agence)
2. **Google Sign-In s'ouvre** → Utilisateur sélectionne son compte Google
3. **Récupération de l'ID Token** depuis Google
4. **Appel API** : `POST /auth/google` avec `{ "idToken": "..." }`
5. **Si compte existe (200)** :
   - Sauvegarder `accessToken` (JWT)
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

## 📝 Structure des Données

### GoogleLoginRequest
```swift
struct GoogleLoginRequest: Codable {
    let idToken: String  // ID Token obtenu de Google
}
```

### AuthResponse
```swift
struct AuthResponse: Codable {
    let accessToken: String  // JWT token pour les requêtes authentifiées
    let user: User?
}

struct User: Codable {
    let id: String?
    let email: String?
    let role: UserRole?
    let nom: String?
    let prenom: String?
    let bio: String?
}

enum UserRole: String, Codable {
    case ACTEUR = "ACTEUR"
    case RECRUTEUR = "RECRUTEUR"
}
```

---

## 🎨 Interface Utilisateur iOS

### Page de Login Unique

Sur iOS, il y a **une seule page de login** qui doit permettre de choisir entre :
1. **Connexion Acteur** (avec bouton "Continuer avec Google" pour acteur)
2. **Connexion Agence** (avec bouton "Continuer avec Google" pour agence)

**Recommandation** : Utiliser des onglets ou un sélecteur pour basculer entre "Acteur" et "Agence", puis afficher le bouton Google Sign-In approprié.

### Bouton Google Sign-In

Le bouton doit :
- Afficher le logo Google officiel
- Avoir le texte "Continuer avec Google"
- Être clairement identifié pour "Acteur" ou "Agence"

---

## 🔐 Stockage Local (Keychain)

Après une connexion réussie, stockez :

1. **Access Token** (JWT) → Keychain (sécurisé)
2. **User ID** → UserDefaults ou Keychain
3. **Email** → UserDefaults ou Keychain
4. **Role** (`ACTEUR` ou `RECRUTEUR`) → UserDefaults ou Keychain
5. **Nom** et **Prénom** → UserDefaults (optionnel)

**Important** : L'Access Token doit être stocké de manière sécurisée (Keychain) car il est utilisé pour toutes les requêtes authentifiées.

---

## ⚠️ Points Importants

1. **Client ID Web** : Utilisez toujours le **Client ID Web** (`873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com`) pour obtenir l'ID Token, pas le Client ID iOS.

2. **ID Token** : L'ID Token est **essentiel** pour l'authentification backend. Sans ID Token, l'API retournera une erreur.

3. **Gestion des Erreurs** :
   - **404** : Compte non trouvé → Rediriger vers inscription
   - **401** : Token invalide → Réessayer la connexion Google
   - **500** : Erreur serveur → Afficher message d'erreur

4. **Deux Types de Connexion** : Même si iOS a une seule page de login, il faut distinguer si l'utilisateur veut se connecter en tant qu'**Acteur** ou **Agence** avant d'appeler Google Sign-In.

5. **Pré-remplissage Inscription** : Si le compte n'existe pas (404), utilisez les informations Google (`email`, `givenName`, `familyName`, `photoURL`) pour pré-remplir le formulaire d'inscription.

---

## 📚 Ressources

- [Google Sign-In iOS Documentation](https://developers.google.com/identity/sign-in/ios)
- [Google Cloud Console](https://console.cloud.google.com/)
- [OAuth 2.0 for Mobile Apps](https://developers.google.com/identity/protocols/oauth2/native-app)

---

## ✅ Checklist Implémentation

- [ ] Créer Client OAuth iOS dans Google Cloud Console
- [ ] Ajouter Google Sign-In SDK au projet iOS
- [ ] Configurer `Info.plist` avec Client ID iOS
- [ ] Configurer URL Scheme dans `Info.plist`
- [ ] Implémenter `GoogleAuthManager` avec Client ID Web
- [ ] Implémenter `AuthService` pour appeler `/auth/google`
- [ ] Gérer les réponses 200 (succès) et 404 (inscription)
- [ ] Stocker Access Token dans Keychain
- [ ] Créer page de login avec sélection Acteur/Agence
- [ ] Tester connexion Google pour Acteur
- [ ] Tester connexion Google pour Agence
- [ ] Tester redirection vers inscription si compte n'existe pas

---

## 🆘 Support

En cas de problème, vérifiez :
1. Le Client ID Web est correctement configuré
2. L'API Google Sign-In est activée dans Google Cloud Console
3. Le Bundle ID iOS correspond à celui configuré dans Google Cloud Console
4. L'ID Token est bien récupéré et envoyé au backend
5. Les headers HTTP sont corrects (`Content-Type: application/json`)

---

**Date de création** : 2025-11-16  
**Version** : 1.0  
**Projet** : CastMate iOS


