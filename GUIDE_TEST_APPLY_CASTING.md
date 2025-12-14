# 🧪 Guide de Test - Candidature aux Castings (Apply)

**Date:** 2025-12-14  
**Endpoint testé:** `POST /castings/{id}/apply`  
**Objectif:** Vérifier que l'erreur 403 est résolue après les corrections backend

---

## ✅ Corrections effectuées

### Côté Backend
1. ✅ Logs détaillés ajoutés dans `JwtAuthGuard`, `JwtStrategy`, `RolesGuard`, `CastingsController`
2. ✅ Amélioration de la comparaison des rôles (enums vs strings, trim, normalisation)
3. ✅ Extraction robuste de l'ID utilisateur avec plusieurs fallbacks

### Côté Android
1. ✅ Message d'erreur 403 corrigé : maintenant affiche "ACTEUR requis" au lieu de "RECRUTEUR requis"
2. ✅ Logs de diagnostic détaillés ajoutés dans `CastingRepository.applyToCastingWithVideo()`
3. ✅ Vérification du token avant l'envoi de la requête

---

## 🧪 Test 1 : Candidature sans vidéo

### Scénario
Un acteur connecté postule à un casting **sans** vidéo d'audition.

### Étapes
1. **Ouvrir l'app Android**
2. **Se connecter en tant qu'acteur** :
   - Email : `user@user.com`
   - Mot de passe : (votre mot de passe)
3. **Aller sur la liste des castings** (écran Home Acteur)
4. **Cliquer sur un casting ouvert** pour voir les détails
5. **Cliquer sur le bouton "Postuler"** (sans vidéo)

### Résultat attendu

#### ✅ Si le backend est corrigé (SUCCÈS)
```
Logs Android :
========== CANDIDATURE AU CASTING ==========
📝 Postulation au casting avec vidéo: 693f0a9897caff2222dc87f1
🔑 Token présent: true
🔑 Token length: 247
📋 Token info:
   - sub: 690cda42dc65a1a3fc2c6a20
   - role: ACTEUR
   - type: ACTEUR
   - email: user@user.com
📤 Envoi de la candidature:
   - Casting ID: 693f0a9897caff2222dc87f1
   - Vidéo: NON
   - AI Feedback: NON
============================================
✅ Candidature avec vidéo envoyée avec succès

UI : Message "Candidature envoyée avec succès"
```

#### ❌ Si l'erreur 403 persiste (ÉCHEC)
```
Logs Android :
========== CANDIDATURE AU CASTING ==========
📝 Postulation au casting avec vidéo: 693f0a9897caff2222dc87f1
🔑 Token présent: true
🔑 Token length: 247
📋 Token info:
   - sub: 690cda42dc65a1a3fc2c6a20
   - role: ACTEUR
   - type: ACTEUR
   - email: user@user.com
📤 Envoi de la candidature:
   - Casting ID: 693f0a9897caff2222dc87f1
   - Vidéo: NON
   - AI Feedback: NON
============================================
❌❌❌ ERREUR 403: Forbidden ❌❌❌
❌ URL de la requête: https://cast-mate.vercel.app/castings/693f0a9897caff2222dc87f1/apply
❌ Méthode: POST
⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (ACTEUR requis pour postuler à un casting)
❌ Erreur 403: Forbidden

UI : Message d'erreur "Vous ne pouvez pas postuler à ce casting"
```

**Action si échec** : Vérifier les **logs backend dans Vercel** pour voir exactement où ça bloque.

---

## 🧪 Test 2 : Candidature avec vidéo

### Scénario
Un acteur connecté postule à un casting **avec** une vidéo d'audition.

### Étapes
1. **Se connecter en tant qu'acteur**
2. **Aller sur la liste des castings**
3. **Cliquer sur un casting ouvert**
4. **Cliquer sur "Postuler avec vidéo"**
5. **Sélectionner une vidéo** (< 10 MB)
6. **Enregistrer une audition** ou **choisir une vidéo existante**
7. **Soumettre la candidature**

### Résultat attendu

#### ✅ Si succès
```
Logs Android :
========== CANDIDATURE AU CASTING ==========
📝 Postulation au casting avec vidéo: 693f0a9897caff2222dc87f1
🔑 Token présent: true
📋 Token info:
   - sub: 690cda42dc65a1a3fc2c6a20
   - role: ACTEUR
   - type: ACTEUR
   - email: user@user.com
📹 Taille de la vidéo: 2.5 MB
🤖 AI Feedback présent: 1234 caractères
📤 Envoi de la candidature:
   - Casting ID: 693f0a9897caff2222dc87f1
   - Vidéo: OUI
   - AI Feedback: OUI
============================================
✅ Candidature avec vidéo envoyée avec succès

UI : Message "Candidature envoyée avec succès"
```

#### ❌ Si erreur 403
→ Même message d'erreur que le Test 1, vérifier les logs backend.

---

## 🔍 Logs Backend à vérifier (Vercel)

Si l'erreur 403 persiste, vérifier les logs backend dans Vercel :

### Accès aux logs Vercel
1. Aller sur https://vercel.com
2. Sélectionner le projet CastMate
3. Aller dans "Deployments"
4. Cliquer sur le dernier déploiement actif
5. Aller dans "Functions" ou "Logs"
6. Chercher les logs au moment de la requête (timestamp : 2025-12-14 23:16:09 GMT)

### Logs attendus (si tout fonctionne)

```
[JWT Guard] canActivate appelé: {
  url: "/castings/693f0a9897caff2222dc87f1/apply",
  method: "POST",
  hasAuthHeader: true
}
[JWT Guard] Route protégée, vérification JWT...

[JWT Strategy] ========== VALIDATION TOKEN ==========
[JWT Strategy] Payload reçu: {
  sub: "690cda42dc65a1a3fc2c6a20",
  email: "user@user.com",
  role: "ACTEUR",
  type: "ACTEUR",
  iat: 1734543000,
  exp: 1766345000
}
[JWT Strategy] Recherche acteur avec ID: 690cda42dc65a1a3fc2c6a20
[JWT Strategy] Acteur trouvé: {
  _id: "690cda42dc65a1a3fc2c6a20",
  nom: "ben fredj",
  prenom: "mohamed",
  email: "user@user.com",
  role: "ACTEUR"
}
[JWT Strategy] Objet utilisateur retourné (ACTEUR): {
  id: "690cda42dc65a1a3fc2c6a20",
  role: "ACTEUR",
  email: "user@user.com"
}

[JWT Guard] Authentification réussie: {
  userId: "690cda42dc65a1a3fc2c6a20",
  userRole: "ACTEUR"
}
[JWT Guard] Utilisateur attaché à la requête: {
  hasUser: true,
  userId: "690cda42dc65a1a3fc2c6a20",
  userRole: "ACTEUR"
}

[RolesGuard] ========== VÉRIFICATION RÔLES ==========
[RolesGuard] Rôles requis: ["ACTEUR"]
[RolesGuard] Endpoint: /castings/693f0a9897caff2222dc87f1/apply
[RolesGuard] Utilisateur: {
  hasUser: true,
  id: "690cda42dc65a1a3fc2c6a20",
  role: "ACTEUR"
}
[RolesGuard] Comparaison rôle: {
  userRole: "ACTEUR",
  requiredRole: "ACTEUR",
  matches: true
}
[RolesGuard] ✅ Accès autorisé pour le rôle: ACTEUR

[CastingsController] ========== CANDIDATURE AU CASTING ==========
[CastingsController] Candidature à un casting: {
  castingId: "693f0a9897caff2222dc87f1",
  hasUser: true,
  userId: "690cda42dc65a1a3fc2c6a20",
  userRole: "ACTEUR",
  hasVideo: false,
  hasAiFeedback: false
}
[CastingsController] ID utilisateur extrait: 690cda42dc65a1a3fc2c6a20
[CastingsController] ✅ Candidature enregistrée avec succès
```

### Si blocage dans JwtStrategy

```
[JWT Strategy] Recherche acteur avec ID: 690cda42dc65a1a3fc2c6a20
[JWT Strategy] ❌ ERREUR: Acteur non trouvé pour ID: 690cda42dc65a1a3fc2c6a20
```

**Diagnostic** : L'acteur n'existe pas en base de données ou l'ID est incorrect.

**Action** :
1. Vérifier que l'acteur existe :
   ```javascript
   db.acteurs.findOne({ _id: ObjectId("690cda42dc65a1a3fc2c6a20") })
   ```
2. Si l'acteur n'existe pas, créer un nouveau compte ou utiliser un autre acteur

### Si blocage dans RolesGuard

```
[RolesGuard] Comparaison rôle: {
  userRole: "ACTEUR",
  requiredRole: "ACTEUR",
  matches: false  // ⚠️ PROBLÈME ICI
}
[RolesGuard] ❌ ERREUR: Rôle insuffisant pour accéder à cette ressource
```

**Diagnostic** : La comparaison des rôles échoue malgré les deux valeurs étant "ACTEUR".

**Action** :
1. Vérifier le type du rôle (enum vs string)
2. Vérifier les espaces avant/après (trim)
3. Vérifier la casse (uppercase vs lowercase)
4. Vérifier si les corrections backend ont été déployées

### Si aucun log n'apparaît

**Diagnostic** : La requête est bloquée **avant** d'atteindre les guards, probablement par un middleware (CORS, rate limiter, etc.).

**Action** :
1. Vérifier les logs de démarrage de NestJS pour voir si la route est bien enregistrée :
   ```
   [Nest] Mapped {/castings/:id/apply, POST} route
   ```
2. Vérifier les middlewares globaux
3. Vérifier si Vercel a des restrictions de déploiement

---

## 📋 Checklist de diagnostic

### Avant le test
- [ ] L'acteur existe en base de données avec l'ID `690cda42dc65a1a3fc2c6a20`
- [ ] L'acteur a le rôle `ACTEUR`
- [ ] Le casting existe avec l'ID `693f0a9897caff2222dc87f1`
- [ ] Le casting est ouvert (`status: "ouvert"`)
- [ ] Le token JWT est valide et non expiré
- [ ] Les corrections backend ont été déployées sur Vercel

### Pendant le test
- [ ] Les logs Android montrent que le token est présent
- [ ] Les logs Android montrent que le rôle est `ACTEUR`
- [ ] Les logs Android montrent que la requête est envoyée
- [ ] Observer la réponse HTTP (200, 403, etc.)

### Après le test (si échec)
- [ ] Vérifier les logs backend dans Vercel
- [ ] Identifier exactement où ça bloque (JwtGuard, JwtStrategy, RolesGuard, Controller)
- [ ] Partager les logs backend avec l'équipe backend
- [ ] Vérifier si le problème vient de la base de données, du JWT_SECRET, ou de la comparaison des rôles

---

## 🔧 Dépannage

### Problème : "Pas de token disponible"

**Cause** : L'utilisateur n'est pas connecté ou le token a été supprimé.

**Solution** :
1. Se déconnecter et se reconnecter
2. Vérifier les logs de login pour voir si le token est bien stocké

### Problème : "Le rôle doit être 'ACTEUR'"

**Cause** : L'utilisateur connecté n'est pas un acteur (c'est un recruteur ou admin).

**Solution** :
1. Se déconnecter
2. Se connecter avec un compte acteur valide

### Problème : "Acteur non trouvé" (dans les logs backend)

**Cause** : L'acteur avec l'ID du token n'existe pas en base de données.

**Solution** :
1. Créer un nouveau compte acteur
2. Ou corriger l'ID dans le token (problème de synchronisation entre JWT et DB)

### Problème : "Comparaison des rôles échoue" (dans les logs backend)

**Cause** : Le type du rôle dans l'objet utilisateur ne correspond pas au type requis par le decorator `@Roles()`.

**Solution** :
1. Vérifier que `JwtStrategy.validate()` retourne bien un objet avec `role: "ACTEUR"` (string)
2. Vérifier que le decorator `@Roles('ACTEUR')` utilise bien un string et non un enum

---

## ✅ Résultat attendu final

Après avoir effectué ces tests :

### Si succès (🎉)
- Les logs Android montrent `✅ Candidature avec vidéo envoyée avec succès`
- Les logs backend montrent tous les logs de diagnostic avec `✅ Accès autorisé`
- L'UI affiche "Candidature envoyée avec succès"
- Le backend enregistre la candidature en base de données

### Si échec (🔴)
- Les logs Android montrent `❌ Erreur 403`
- Les logs backend montrent où exactement ça bloque
- Partager les logs backend avec l'équipe pour correction supplémentaire

---

## 📞 Prochaines étapes

1. **Effectuer le Test 1** (candidature sans vidéo)
2. **Observer les logs Android** pour voir si le token et le rôle sont corrects
3. **Si erreur 403** :
   - Vérifier les logs backend dans Vercel
   - Identifier où ça bloque (JwtStrategy, RolesGuard, etc.)
   - Partager les logs backend avec l'équipe
4. **Si succès** :
   - Effectuer le Test 2 (candidature avec vidéo)
   - Confirmer que tout fonctionne

---

## 📄 Fichiers modifiés

### Android
- `app/src/main/java/com/example/projecct_mobile/data/api/AuthInterceptor.kt`
  - Message d'erreur 403 corrigé pour l'endpoint apply
- `app/src/main/java/com/example/projecct_mobile/data/repository/CastingRepository.kt`
  - Logs de diagnostic détaillés ajoutés dans `applyToCastingWithVideo()`
  - Vérification du token avant l'envoi

### Backend (à vérifier dans Vercel)
- `jwt-auth.guard.ts` : Logs détaillés ajoutés
- `jwt.strategy.ts` : Logs détaillés ajoutés, extraction robuste de l'ID
- `roles.guard.ts` : Logs détaillés ajoutés, amélioration de la comparaison
- `castings.controller.ts` : Logs détaillés ajoutés dans `applyToCasting()`

**Bonne chance pour les tests !** 🚀

