# 🚨 Message pour l'équipe Backend - Erreur 403 sur POST /castings/{id}/apply

**Date:** 2025-12-14  
**Problème:** Erreur HTTP 403 (Forbidden) lors de la candidature d'un acteur à un casting  
**Endpoint:** `POST /castings/{id}/apply`

---

## 🔴 Problème identifié

Un acteur authentifié essaie de postuler à un casting mais reçoit une erreur **403 Forbidden**.

### Logs Android

```
AuthInterceptor: 📋 Token info: id=N/A, role=ACTEUR, type=ACTEUR, email=user@user.com
AuthInterceptor: 📋 Toutes les clés du token: role, type, email, exp, ...
ErrorInterceptor: ❌ ERREUR 403: Forbidden
POST https://cast-mate.vercel.app/castings/693f0a9897caff2222dc87f1/apply
```

**Observation importante :** Le log montre `id=N/A`, ce qui signifie que le token JWT **ne contient pas de champ `id`** dans le payload, ou que l'ID est stocké sous un autre nom.

---

## ✅ Informations du token JWT

D'après les logs Android, le token contient :
- ✅ `role: "ACTEUR"`
- ✅ `type: "ACTEUR"` (ou peut-être un autre type)
- ✅ `email: "user@user.com"`
- ✅ `exp: <timestamp>`
- ❌ `id: N/A` (non trouvé dans les champs standards : `id`, `userId`, `sub`, `_id`, `actorId`)

### Toutes les clés du token

Le backend doit vérifier quelles sont toutes les clés présentes dans le payload du token JWT pour identifier dans quel champ l'ID est stocké.

---

## ❓ Questions pour le backend

1. **Extraction de l'ID :**
   - Comment le backend extrait-il l'ID de l'utilisateur depuis le token JWT pour la route `/castings/{id}/apply` ?
   - Dans quel champ le backend cherche-t-il l'ID (`id`, `userId`, `sub`, `_id`, `actorId`, ou autre) ?
   - Le backend peut-il logger le payload complet du token décodé pour voir toutes les clés disponibles ?

2. **Permissions :**
   - Un acteur (`role=ACTEUR`) devrait-il pouvoir postuler à un casting via `POST /castings/{id}/apply` ?
   - Y a-t-il des vérifications supplémentaires (ownership, statut du casting, etc.) qui pourraient causer un 403 ?

3. **Différence avec d'autres endpoints :**
   - Pourquoi `GET /acteur/:id` fonctionne mais `POST /castings/{id}/apply` retourne 403 avec le même token ?
   - Y a-t-il une différence dans la façon dont ces deux endpoints extraient l'ID depuis le token ?

---

## 🔍 Hypothèses

### Hypothèse 1 : ID manquant dans le token

Si le backend cherche l'ID dans un champ spécifique (par exemple `id`) mais que le token le stocke sous un autre nom (par exemple `actorId` ou `_id`), alors le backend ne peut pas extraire l'ID et refuse la requête.

**Solution :** Le backend doit chercher l'ID dans plusieurs champs possibles, ou normaliser le nom du champ dans le token lors de la création.

### Hypothèse 2 : Vérification de permissions

Le backend vérifie peut-être :
- Que l'utilisateur est bien un acteur
- Que le casting existe
- Que l'acteur n'a pas déjà postulé
- Que le casting est ouvert aux candidatures

Si l'une de ces vérifications échoue à cause de l'ID manquant, cela pourrait expliquer le 403.

---

## 📊 Comparaison avec d'autres endpoints

### Endpoint qui fonctionne

```
GET /acteur/690cda42dc65a1a3fc2c6a20
Authorization: Bearer <token>
→ 200 OK ✅
```

### Endpoint qui échoue

```
POST /castings/693f0a9897caff2222dc87f1/apply
Authorization: Bearer <token>
Content-Type: multipart/form-data
→ 403 Forbidden ❌
```

**Question :** Pourquoi l'un fonctionne et pas l'autre avec le même token ?

---

## 🎯 Comportement attendu

Un acteur authentifié devrait pouvoir :

1. **Postuler à un casting :**
   ```
   POST /castings/{id}/apply
   Authorization: Bearer <token>
   Content-Type: multipart/form-data
   Body: {
     video: <file>,
     aiFeedback: <json>
   }
   → 200 OK ou 201 Created
   ```

2. **Voir son profil :**
   ```
   GET /acteur/:id
   Authorization: Bearer <token>
   → 200 OK ✅ (fonctionne déjà)
   ```

---

## 🚀 Action demandée

**Le backend doit :**

1. **Logger le payload complet du token JWT** pour les requêtes vers `/castings/{id}/apply` :
   ```javascript
   console.log('[ApplyController] Token payload:', {
     allKeys: Object.keys(decodedToken),
     id: decodedToken.id,
     userId: decodedToken.userId,
     sub: decodedToken.sub,
     _id: decodedToken._id,
     actorId: decodedToken.actorId,
     fullPayload: decodedToken
   });
   ```

2. **Vérifier comment l'ID est extrait** pour `/castings/{id}/apply` et comparer avec `/acteur/:id` qui fonctionne

3. **Chercher l'ID dans plusieurs champs possibles** :
   ```javascript
   const userId = decodedToken.id 
     || decodedToken.userId 
     || decodedToken.sub 
     || decodedToken._id 
     || decodedToken.actorId;
   ```

4. **Vérifier les logs backend** pour voir pourquoi le 403 est retourné exactement

---

## 📋 Informations de requête

**Requête qui échoue :**
- **Méthode:** POST
- **URL:** `/castings/693f0a9897caff2222dc87f1/apply`
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- **Body:** Contient une vidéo et des feedbacks AI
- **Rôle utilisateur:** ACTEUR
- **Token info:** `role=ACTEUR, type=ACTEUR, email=user@user.com, id=N/A`

---

## 📞 Informations supplémentaires

**ID attendu de l'acteur (d'après d'autres logs) :** `690cda42dc65a1a3fc2c6a20`

**Token JWT :**
- Format: Standard JWT (header.payload.signature)
- Rôle: ACTEUR
- Type: ACTEUR (ou autre selon le token)
- Email: user@user.com
- ID: À identifier (peut-être dans un champ non standard)

**Merci de vérifier pourquoi l'ID n'est pas trouvé dans le token et de corriger le problème !** 🙏

