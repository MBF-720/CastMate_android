# 🚨 Message Urgent pour l'équipe Backend - requesterId undefined

**Date:** 2025-12-14  
**Problème:** Le backend ne peut pas extraire `requesterId` depuis le token JWT  
**Erreur:** `requesterId: undefined` dans les logs MediaService

---

## 🔴 Problème identifié

Les logs backend montrent :

```javascript
[MediaService] ERREUR: ID manquant {
  hasOwnerId: true,
  hasRequesterId: false,
  ownerId: '690cda42dc65a1a3fc2c6a20',
  requesterId: undefined,
  fileField: 'document',
  ownerRole: 'ACTEUR',
  requesterRole: undefined
}
```

**Le backend ne peut pas extraire l'ID de l'utilisateur depuis le token JWT.**

---

## ✅ Confirmation côté Android

### Token JWT contient bien l'ID

D'après nos logs Android, le token JWT contient bien l'ID dans le champ `"id"` :

```
AuthInterceptor: 📋 Token info: id=690cda42dc65a1a3fc2c6a20, role=ACTEUR, exp=...
```

**Le token envoyé contient :**
- ✅ `id: "690cda42dc65a1a3fc2c6a20"`
- ✅ `role: "ACTEUR"`
- ✅ `exp: <timestamp>`

### Payload du token JWT (décodé)

Quand on décode le token JWT côté Android, nous obtenons :

```json
{
  "id": "690cda42dc65a1a3fc2c6a20",
  "role": "ACTEUR",
  "exp": <timestamp>,
  "email": "user@user.com"
}
```

**Le champ `id` est présent et contient exactement `"690cda42dc65a1a3fc2c6a20"`.**

---

## 🔍 Code Android qui extrait l'ID

Voici comment nous extrayons l'ID du token côté Android :

```kotlin
// TokenManager.kt - getUserIdFromToken()
val jsonObject = JSONObject(decodedString)

// Extraction de l'ID (on essaie plusieurs champs possibles)
val userId = jsonObject.optString("id", null)
    ?: jsonObject.optString("userId", null)
    ?: jsonObject.optString("sub", null)
    ?: jsonObject.optString("_id", null)
    ?: jsonObject.optString("actorId", null)
    ?: jsonObject.optString("user_id", null)
    ?: jsonObject.optString("actor_id", null)

// Résultat: userId = "690cda42dc65a1a1fc2c6a20" ✅
```

**Nous trouvons bien l'ID dans le champ `"id"` du token.**

---

## 📋 Structure complète du token JWT

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "id": "690cda42dc65a1a3fc2c6a20",
  "role": "ACTEUR",
  "email": "user@user.com",
  "exp": <timestamp>
}
```

### Signature
```
<signature>
```

---

## ❓ Questions pour le backend

1. **Extraction de l'ID :**
   - Comment le backend extrait-il `requesterId` depuis le token JWT ?
   - Dans quel champ le backend cherche-t-il l'ID (`id`, `userId`, `sub`, `_id`, etc.) ?
   - Le backend décode-t-il correctement le token JWT ?

2. **Code backend :**
   - Pouvez-vous partager le code qui extrait `requesterId` depuis le token ?
   - Y a-t-il une différence dans la façon dont les requêtes `/acteur/:id` et `/media/:fileId` sont traitées ?

3. **Logs backend :**
   - Le backend peut-il logger le payload complet du token JWT décodé pour les requêtes `/media/:fileId` ?
   - Cela nous aiderait à voir pourquoi `requesterId` est `undefined`

---

## 🔧 Suggestions de correction backend

### Option 1 : Vérifier le champ dans lequel l'ID est cherché

Si le backend cherche l'ID dans un champ différent (par exemple `userId` au lieu de `id`), il faut soit :
- Modifier le backend pour chercher dans le champ `id`
- Ou modifier le backend pour accepter plusieurs champs (`id`, `userId`, `sub`, etc.)

### Option 2 : Vérifier le décodage du token

Le backend doit décoder le token JWT correctement :

```javascript
// Exemple de décodage (pseudo-code)
const token = req.headers.authorization.replace('Bearer ', '');
const parts = token.split('.');
const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
const requesterId = payload.id; // ou payload.userId, payload.sub, etc.
```

### Option 3 : Logs de diagnostic

Ajouter des logs pour voir ce qui est dans le token :

```javascript
console.log('[MediaService] Token décodé:', {
  payload: payload,
  id: payload.id,
  userId: payload.userId,
  sub: payload.sub,
  _id: payload._id,
  allKeys: Object.keys(payload)
});
```

---

## 📊 Comparaison avec les autres endpoints

**Observation importante :** L'endpoint `GET /acteur/:id` fonctionne correctement avec le même token !

```http
GET /acteur/690cda42dc65a1a3fc2c6a20
Authorization: Bearer <token>
→ 200 OK ✅
```

**Question :** Pourquoi `/acteur/:id` fonctionne mais `/media/:fileId` ne fonctionne pas avec le même token ?

**Hypothèse :** Il y a peut-être une différence dans la façon dont ces deux endpoints extraient l'ID depuis le token.

---

## 🎯 Résumé

1. ✅ **Le token JWT contient bien l'ID** : `id: "690cda42dc65a1a3fc2c6a20"`
2. ✅ **Le header Authorization est correctement envoyé** : `Authorization: Bearer <token>`
3. ❌ **Le backend ne peut pas extraire `requesterId`** : `requesterId: undefined`
4. ❓ **Pourquoi `/acteur/:id` fonctionne mais `/media/:fileId` ne fonctionne pas ?**

---

## 🚀 Action demandée

**Le backend doit :**
1. Vérifier comment `requesterId` est extrait depuis le token JWT pour `/media/:fileId`
2. Comparer avec comment l'ID est extrait pour `/acteur/:id` (qui fonctionne)
3. Corriger l'extraction de `requesterId` pour qu'il trouve bien l'ID dans le champ `"id"` du token
4. Ajouter des logs de diagnostic pour voir le payload décodé du token

---

## 📞 Informations supplémentaires

**Token JWT (extrait des logs Android) :**
- ID: `690cda42dc65a1a3fc2c6a20`
- Role: `ACTEUR`
- Email: `user@user.com`
- Format: Standard JWT (header.payload.signature)

**URLs qui échouent :**
- `GET /media/693f1ecd12b354cd317d0bed` (photo de profil)
- `GET /media/6915ea5920db74c69afdd4d8` (CV)
- `GET /media/691733d49c56a56bbe55843d` (galerie)

**Merci de corriger ce problème rapidement !** 🙏

