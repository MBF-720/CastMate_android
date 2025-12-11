# 🔍 Diagnostic Erreur 401 - Accès Vidéos

## ✅ État Actuel

### Code Backend : ✅ Correct
- Les permissions sont correctement configurées dans `CastingsService.getCandidateVideo()`
- Les agences propriétaires du casting peuvent accéder aux vidéos
- Double vérification dans `MediaService.assertCanAccess()`

### Code Frontend Android : ✅ Correct
- L'`AuthInterceptor` ajoute automatiquement le token JWT
- Le token est récupéré depuis `TokenManager`
- L'endpoint est bien configuré dans `CastingApiService`

### Problème : ⚠️ Erreur 401 (Unauthorized)
L'erreur se produit **AVANT** la vérification des permissions, ce qui indique un problème d'**authentification JWT**.

---

## 🔍 Causes Possibles de l'Erreur 401

### 1. Token JWT non envoyé
- ❌ Le token n'est pas dans le header `Authorization`
- ✅ **Vérification** : Les logs backend doivent montrer "Token manquant"

### 2. Token JWT invalide
- ❌ Le token est malformé ou corrompu
- ✅ **Vérification** : Les logs backend doivent montrer "Token invalide"

### 3. Token JWT expiré
- ❌ Le token a expiré (durée de vie dépassée)
- ✅ **Vérification** : Vérifier `exp` dans le payload JWT

### 4. Secret JWT qui ne correspond pas
- ❌ Le secret utilisé pour signer ne correspond pas à celui utilisé pour vérifier
- ✅ **Vérification** : Vérifier la configuration du secret JWT côté backend

### 5. Utilisateur non trouvé
- ❌ L'utilisateur extrait du token n'existe plus en base de données
- ✅ **Vérification** : Les logs backend doivent montrer "Utilisateur non trouvé"

### 6. Format du header Authorization incorrect
- ❌ Le format n'est pas exactement `Bearer <token>`
- ✅ **Vérification** : Vérifier les logs backend pour voir le header reçu

---

## 🧪 Tests de Diagnostic

### Test 1 : Vérifier que le token est envoyé

**Côté Android** : Ajouter des logs dans `AuthInterceptor.kt`

```kotlin
val newRequest: Request = if (token != null) {
    android.util.Log.d("AuthInterceptor", "✅ Token JWT présent: ${token.take(20)}...")
    android.util.Log.d("AuthInterceptor", "📤 Envoi requête: ${originalRequest.url}")
    android.util.Log.d("AuthInterceptor", "📤 Header Authorization: Bearer ${token.take(20)}...")
    originalRequest.newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
} else {
    android.util.Log.e("AuthInterceptor", "❌ Token JWT manquant!")
    originalRequest
}
```

**Côté Backend** : Vérifier les logs Vercel pour voir :
- Si le header `Authorization` est présent
- Le format exact du header reçu

### Test 2 : Vérifier la validité du token

**Côté Android** : Décoder le token JWT pour vérifier :
- La structure (3 parties séparées par `.`)
- Le payload (contient `id`, `role`, `exp`, etc.)
- La date d'expiration (`exp`)

```kotlin
// Dans TokenManager.kt, la fonction getUserIdFromToken() décode déjà le token
// Vérifier les logs pour voir le payload décodé
```

**Côté Backend** : Vérifier les logs pour voir :
- Si le token est valide (signature correcte)
- Si le token est expiré
- Si l'utilisateur existe en base

### Test 3 : Vérifier le rôle de l'utilisateur

**Côté Android** : Vérifier que le rôle est bien `AGENCE` ou `RECRUTEUR`

```kotlin
// Dans TokenManager, vérifier userRole
val userRole = tokenManager.getUserRole()
android.util.Log.d("TokenManager", "👤 Rôle utilisateur: $userRole")
```

**Côté Backend** : Vérifier que :
- Le rôle extrait du token est `RECRUTEUR` ou `AGENCE`
- L'ID utilisateur correspond à un recruteur en base

### Test 4 : Vérifier la propriété du casting

**Côté Backend** : Vérifier que :
- Le casting existe
- `casting.recruteur.toString() === userId`
- Les logs montrent ces vérifications

---

## 📋 Checklist de Diagnostic

### Côté Android (Frontend)

- [ ] Le token JWT est présent dans `TokenManager`
- [ ] Le token est envoyé dans le header `Authorization: Bearer <token>`
- [ ] Le format du header est correct (espace après "Bearer")
- [ ] Le token n'est pas expiré (vérifier `exp` dans le payload)
- [ ] Le rôle de l'utilisateur est `AGENCE` ou `RECRUTEUR`
- [ ] L'ID utilisateur correspond à celui du recruteur propriétaire

### Côté Backend

- [ ] Le header `Authorization` est reçu
- [ ] Le token est extrait correctement du header
- [ ] Le token est valide (signature correcte)
- [ ] Le token n'est pas expiré
- [ ] L'utilisateur existe en base de données
- [ ] Le rôle de l'utilisateur est `RECRUTEUR` ou `AGENCE`
- [ ] Le casting existe
- [ ] `casting.recruteur.toString() === userId`

---

## 🔧 Solutions Proposées

### Solution 1 : Ajouter des logs détaillés côté Android

Ajouter des logs dans `AuthInterceptor.kt` pour voir exactement ce qui est envoyé.

### Solution 2 : Vérifier l'expiration du token

Implémenter une vérification de l'expiration du token et rafraîchir automatiquement si nécessaire.

### Solution 3 : Vérifier le format du token

S'assurer que le token est bien au format JWT (3 parties séparées par `.`).

### Solution 4 : Vérifier la configuration du secret JWT

S'assurer que le secret JWT côté backend correspond à celui utilisé pour signer les tokens.

---

## 📝 Logs à Vérifier

### Logs Backend (Vercel)

Chercher dans les logs Vercel pour :
```
🔍 [CastingsController] getCandidateVideo - castingId: ...
🔍 [CastingsController] getCandidateVideo - acteurId: ...
🔍 [CastingsController] getCandidateVideo - user: ...
🔍 [CastingsController] getCandidateVideo - userRole: ...
🔍 [CastingsController] getCandidateVideo - userId: ...
```

### Logs Android (Logcat)

Chercher dans Logcat pour :
```
AuthInterceptor: Token JWT présent
AuthInterceptor: Envoi requête
AuthInterceptor: Header Authorization
```

---

## 🎯 Prochaines Étapes

1. **Ajouter des logs détaillés** côté Android dans `AuthInterceptor.kt`
2. **Vérifier les logs Vercel** pour voir exactement ce qui est reçu côté backend
3. **Comparer le token** envoyé avec celui attendu
4. **Vérifier l'expiration** du token
5. **Vérifier la configuration** du secret JWT

Une fois ces informations collectées, nous pourrons identifier la cause exacte de l'erreur 401.

