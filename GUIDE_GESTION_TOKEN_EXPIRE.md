# 🔐 Guide de Gestion du Token JWT Expiré

## ✅ Modifications Apportées

### 1. Vérification de l'expiration du token

Une nouvelle fonction `isTokenExpired()` a été ajoutée dans `TokenManager.kt` qui :
- Décode le token JWT
- Extrait le champ `exp` (expiration)
- Compare avec l'heure actuelle
- Supprime automatiquement le token s'il est expiré

### 2. Vérification avant l'envoi

L'`AuthInterceptor` vérifie maintenant si le token est expiré **avant** de l'envoyer :
- Si le token est expiré → il est supprimé automatiquement
- Si le token est valide → il est envoyé normalement

### 3. Gestion automatique

L'`ErrorInterceptor` supprime automatiquement le token en cas d'erreur 401 (sauf pour `/my-status`).

---

## 🔍 Comment ça fonctionne

### Vérification de l'expiration

```kotlin
// Dans TokenManager.kt
suspend fun isTokenExpired(): Boolean {
    // 1. Récupère le token
    // 2. Décode le payload JWT
    // 3. Extrait le champ "exp" (timestamp Unix)
    // 4. Compare avec l'heure actuelle
    // 5. Supprime le token s'il est expiré
    // 6. Retourne true si expiré, false sinon
}
```

### Utilisation dans AuthInterceptor

```kotlin
// Dans AuthInterceptor.kt
val token = runBlocking {
    // Vérifie l'expiration avant d'utiliser le token
    if (tokenManager.isTokenExpired()) {
        null // Token expiré, ne pas l'envoyer
    } else {
        tokenManager.getTokenSync() // Token valide
    }
}
```

---

## 🚨 Gestion de l'Erreur 401

### Côté Frontend

Quand une erreur 401 se produit :

1. **L'`ErrorInterceptor` détecte l'erreur 401**
   - Supprime automatiquement le token (sauf pour `/my-status`)
   - Log l'erreur

2. **Le repository convertit l'erreur en `ApiException.UnauthorizedException`**

3. **Vous devez rediriger l'utilisateur vers la connexion**

### Exemple de gestion dans un Composable

```kotlin
@Composable
fun MyScreen() {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    
    scope.launch {
        val result = repository.someProtectedAction()
        
        result.onFailure { exception ->
            if (exception is ApiException.UnauthorizedException) {
                // Token expiré - déjà supprimé automatiquement
                // Rediriger vers la connexion
                navController.navigate("signIn") {
                    popUpTo("signIn") { inclusive = true }
                }
            }
        }
    }
}
```

---

## 📋 Checklist pour les Développeurs

### ✅ À faire dans chaque écran qui fait des requêtes protégées

- [ ] Gérer l'erreur `ApiException.UnauthorizedException`
- [ ] Rediriger vers l'écran de connexion si le token est expiré
- [ ] Afficher un message à l'utilisateur si nécessaire

### ✅ Vérifications automatiques (déjà implémentées)

- [x] Vérification de l'expiration avant l'envoi
- [x] Suppression automatique du token expiré
- [x] Logs pour le débogage
- [x] Gestion des erreurs 401 dans `ErrorInterceptor`

---

## 🧪 Tests

### Test 1 : Token expiré

1. Se connecter
2. Attendre que le token expire (ou modifier manuellement le token)
3. Faire une requête protégée
4. Vérifier que :
   - Le token est supprimé automatiquement
   - L'erreur 401 est détectée
   - L'utilisateur est redirigé vers la connexion

### Test 2 : Token valide

1. Se connecter
2. Faire une requête protégée immédiatement
3. Vérifier que :
   - Le token est envoyé
   - La requête réussit

---

## 📝 Logs Utiles

### Logs à surveiller dans Logcat

```
TokenManager: ✅ Token JWT valide: expire dans X minutes
TokenManager: ⚠️ Token JWT expiré: exp=..., maintenant=...
AuthInterceptor: ✅ Token JWT présent et valide
AuthInterceptor: ❌ Token JWT manquant ou expiré
ErrorInterceptor: ⚠️ Erreur 401 (Unauthorized): Token invalide ou expiré
```

---

## 🔧 Dépannage

### Problème : Token toujours expiré

**Solution** : Vérifier que le backend génère correctement le champ `exp` dans le token JWT.

### Problème : Token non supprimé après expiration

**Solution** : Vérifier que `ErrorInterceptor` est bien configuré dans `ApiClient`.

### Problème : Redirection ne fonctionne pas

**Solution** : Vérifier que vous gérez bien `ApiException.UnauthorizedException` dans votre code.

---

## 📚 Ressources

- `TokenManager.kt` : Gestion du token et vérification d'expiration
- `AuthInterceptor.kt` : Ajout du token dans les requêtes
- `ErrorInterceptor.kt` : Gestion des erreurs 401
- `README_API.md` : Documentation complète de l'API

