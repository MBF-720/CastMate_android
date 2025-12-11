# 📧 Message pour l'Équipe Backend - Correction Erreur 401 Accès Vidéos

## ⚠️ Problème : Erreur 401 (Unauthorized) sur l'Endpoint Vidéo

Bonjour équipe backend 👋

Nous rencontrons une erreur **401 (Unauthorized)** lors de l'accès aux vidéos d'audition par les recruteurs propriétaires des castings.

---

## 🔍 Détails du Problème

### Endpoint concerné :
```
GET /castings/:id/candidates/:acteurId/video
```

### Erreur actuelle :
- **Code HTTP** : `401 Unauthorized`
- **Message** : `{"message":"Unauthorized","statusCode":401}`
- **Contexte** : Un recruteur propriétaire d'un casting essaie d'accéder à la vidéo d'audition d'un candidat

### Logs d'erreur :
```
⚠️ Erreur 401 (Unauthorized): Unauthorized
URL: https://cast-mate.vercel.app/castings/6928b6b3ce7e3cc84670b9a1/candidates/690cda42dc65a1a3fc2c6a20/video
```

---

## ✅ Comportement Attendu

L'endpoint `/castings/:id/candidates/:acteurId/video` **DOIT** autoriser l'accès si l'utilisateur connecté est :

1. ✅ **L'AGENCE/RECRUTEUR propriétaire du casting** (CAS PRINCIPAL - PRIORITÉ)
   - Toute agence qui a créé le casting doit pouvoir voir toutes les vidéos de ses candidats
   - Vérification : `casting.agenceId === user.agenceId` OU `casting.createdBy === user.id`
2. ✅ L'acteur propriétaire de la vidéo (pour voir sa propre vidéo)
3. ✅ Un administrateur

---

## 🔧 Correction Suggérée

### Vérification des permissions à implémenter :

1. **Vérifier que l'utilisateur est authentifié** (JWT valide)
2. **Vérifier que le casting existe** (`castingId`)
3. **Vérifier que le candidat existe** dans ce casting (`acteurId`)
4. **Vérifier les permissions** :
   - Si l'utilisateur est un **recruteur** → vérifier qu'il est **propriétaire du casting**
   - Si l'utilisateur est un **acteur** → vérifier qu'il est le **propriétaire de la vidéo** (`acteurId` correspond)
   - Si l'utilisateur est un **admin** → autoriser l'accès

### Exemple de logique (pseudo-code) :

```typescript
// Dans le contrôleur ou middleware
async getCandidateVideo(castingId, acteurId, user) {
  // 1. Vérifier que le casting existe
  const casting = await Casting.findById(castingId);
  if (!casting) {
    throw new NotFoundException('Casting non trouvé');
  }

  // 2. Vérifier que le candidat existe dans ce casting
  const candidat = casting.candidats.find(c => c.acteurId === acteurId);
  if (!candidat) {
    throw new NotFoundException('Candidat non trouvé dans ce casting');
  }

  // 3. VÉRIFIER LES PERMISSIONS - PRIORITÉ À L'AGENCE
  let hasAccess = false;

  // PRIORITÉ 1: L'agence/recruteur est propriétaire du casting
  if (user.role === 'AGENCE' || user.role === 'RECRUTEUR') {
    // Vérifier que l'agence est propriétaire du casting
    const isOwner = casting.agenceId?.toString() === user.agenceId?.toString() ||
                    casting.createdBy?.toString() === user.id?.toString();
    
    if (isOwner) {
      hasAccess = true; // ✅ L'AGENCE A LE DROIT DE VOIR LA VIDÉO
    }
  }
  
  // PRIORITÉ 2: L'acteur est propriétaire de sa propre vidéo
  if (!hasAccess && user.role === 'ACTEUR') {
    if (user.id === acteurId) {
      hasAccess = true; // ✅ L'ACTEUR PEUT VOIR SA PROPRE VIDÉO
    }
  }
  
  // PRIORITÉ 3: L'utilisateur est admin
  if (!hasAccess && user.role === 'ADMIN') {
    hasAccess = true; // ✅ L'ADMIN A TOUS LES DROITS
  }

  // Si aucune permission n'est trouvée, refuser l'accès
  if (!hasAccess) {
    throw new ForbiddenException('Vous n\'êtes pas autorisé à accéder à cette vidéo');
  }

  // 4. Récupérer et retourner la vidéo
  const videoFileId = candidat.videoFileId;
  if (!videoFileId) {
    throw new NotFoundException('Vidéo non trouvée pour ce candidat');
  }
  
  // ... logique de récupération de la vidéo depuis le stockage
  // Retourner le fichier vidéo
}
```

### ⚠️ POINT CRITIQUE

**L'AGENCE DOIT AVOIR ACCÈS** : Si l'utilisateur connecté est une agence/recruteur ET qu'elle est propriétaire du casting, elle **DOIT** pouvoir accéder à la vidéo, même si d'autres vérifications échouent.

---

## 🧪 Test Recommandé

Après correction, vérifier que :

1. ✅ **UN RECRUTEUR/AGENCE PROPRIÉTAIRE DU CASTING PEUT ACCÉDER À TOUTES LES VIDÉOS** (TEST PRINCIPAL)
   - Se connecter en tant qu'agence
   - Créer un casting
   - Un acteur postule avec une vidéo
   - L'agence doit pouvoir télécharger et voir la vidéo via l'endpoint
2. ✅ Un recruteur non-propriétaire reçoit une erreur 403 (Forbidden)
3. ✅ Un acteur peut accéder à sa propre vidéo
4. ✅ Un acteur ne peut pas accéder aux vidéos des autres candidats
5. ✅ Un utilisateur non authentifié reçoit une erreur 401 (Unauthorized)

### 🎯 Test de Validation Principal

```bash
# 1. Se connecter en tant qu'agence
POST /auth/login
Body: { "email": "agence@example.com", "password": "..." }
Response: { "token": "JWT_TOKEN_AGENCE", "user": { "id": "agence_id", "role": "AGENCE" } }

# 2. Créer un casting (en tant qu'agence)
POST /castings
Headers: { "Authorization": "Bearer JWT_TOKEN_AGENCE" }
Response: { "id": "casting_id", "agenceId": "agence_id", ... }

# 3. Un acteur postule avec une vidéo
POST /castings/casting_id/apply
Headers: { "Authorization": "Bearer JWT_TOKEN_ACTEUR" }
Body: FormData avec vidéo
Response: { "success": true }

# 4. L'AGENCE DOIT POUVOIR VOIR LA VIDÉO (TEST CRITIQUE)
GET /castings/casting_id/candidates/acteur_id/video
Headers: { "Authorization": "Bearer JWT_TOKEN_AGENCE" }
Expected: 200 OK avec le fichier vidéo
Current: 401 Unauthorized ❌
```

---

## 📋 Informations Techniques

- **Base URL** : `https://cast-mate.vercel.app`
- **Méthode** : `GET`
- **Headers requis** : `Authorization: Bearer <JWT_TOKEN>`
- **Paramètres** :
  - `castingId` : ID du casting
  - `acteurId` : ID de l'acteur (candidat)

---

## 🆘 Impact Utilisateur

**BLOQUANT** : Actuellement, les agences/recruteurs ne peuvent **PAS** visualiser les vidéos d'audition de leurs candidats, ce qui **BLOQUE COMPLÈTEMENT** le processus de sélection.

Les agences ont besoin de voir les vidéos pour :
- Évaluer les performances des acteurs
- Comparer les candidats
- Prendre des décisions d'embauche
- Voir le score IA associé à chaque vidéo

**Cette fonctionnalité est essentielle pour le fonctionnement de l'application.**

---

## 📝 Note

Le frontend gère déjà l'affichage des erreurs avec des messages clairs pour l'utilisateur. Une fois la correction backend effectuée, l'application fonctionnera correctement.

---

Merci pour votre attention ! 🚀

**Date** : 27 novembre 2025  
**Priorité** : 🔴 **CRITIQUE** (bloque une fonctionnalité principale essentielle)

---

## 📌 RÉSUMÉ EXÉCUTIF

**PROBLÈME** : Les agences reçoivent une erreur 401 lorsqu'elles tentent d'accéder aux vidéos d'audition de leurs candidats.

**SOLUTION** : Modifier la logique de permissions pour autoriser explicitement les agences propriétaires d'un casting à accéder aux vidéos de tous leurs candidats.

**ACTION REQUISE** : Corriger la vérification des permissions dans l'endpoint `GET /castings/:id/candidates/:acteurId/video` pour donner accès aux agences propriétaires du casting.

