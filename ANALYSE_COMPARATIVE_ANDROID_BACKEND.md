# ✅ Analyse comparative : Android vs Backend (POST /castings/{id}/apply)

**Date:** 2025-12-14  
**Statut Backend:** ✅ Fonctionnel (confirmé via Swagger)  
**Statut Android:** 🔍 À tester avec les nouveaux logs

---

## 📊 Tableau comparatif

| Aspect | Backend (Attendu) | Android (Actuel) | Statut |
|--------|------------------|------------------|--------|
| **URL** | `/castings/{id}/apply` | `/castings/{id}/apply` | ✅ Correct |
| **Méthode** | `POST` | `POST` | ✅ Correct |
| **Content-Type** | `multipart/form-data` | `multipart/form-data` (Retrofit `@Multipart`) | ✅ Correct |
| **Authorization** | `Bearer <JWT>` | Ajouté par `AuthInterceptor` | ✅ Correct |
| **Rôle requis** | `ACTEUR` | Token contient `role=ACTEUR` | ✅ Correct |
| **Paramètre `video`** | `MultipartBody.Part` (optionnel) | `@Part video: MultipartBody.Part?` | ✅ Correct |
| **Paramètre `aiFeedback`** | `String` (JSON, optionnel) | `@Part("aiFeedback") aiFeedback: RequestBody?` | ✅ Correct |
| **Content-Type de `aiFeedback`** | `text/plain` ou `application/json` | `text/plain; charset=utf-8` | ✅ Correct |

---

## ✅ Code Android actuel (CastingApiService.kt)

```kotlin
@Multipart
@POST("castings/{id}/apply")
suspend fun applyToCastingWithVideo(
    @Path("id") id: String,
    @Part video: MultipartBody.Part? = null,
    @Part("aiFeedback") aiFeedback: RequestBody? = null
): Response<Unit>
```

**Analyse :**
- ✅ `@Multipart` : Correct pour `multipart/form-data`
- ✅ `@Path("id")` : Injection de l'ID du casting dans l'URL
- ✅ `@Part video` : Partie multipart pour la vidéo (optionnelle)
- ✅ `@Part("aiFeedback")` : Champ multipart nommé `aiFeedback` (optionnel)

---

## ✅ Code Android actuel (CastingRepository.kt)

```kotlin
// Créer la part vidéo
val requestFile = tempFile.asRequestBody(
    URLConnection.guessContentTypeFromName(tempFile.name)?.toMediaTypeOrNull()
        ?: "video/mp4".toMediaTypeOrNull()
)
videoPart = MultipartBody.Part.createFormData("video", tempFile.name, requestFile)

// Créer le RequestBody pour aiFeedback
if (aiFeedback != null) {
    aiFeedbackBody = aiFeedback.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
}

// Envoyer la requête
val response = castingService.applyToCastingWithVideo(id, videoPart, aiFeedbackBody)
```

**Analyse :**
- ✅ Vidéo : Créée avec `MultipartBody.Part.createFormData("video", ...)` - Correct
- ✅ AI Feedback : Créé avec `toRequestBody("text/plain; charset=utf-8")` - Correct
- ✅ Les deux paramètres sont optionnels (peuvent être `null`) - Correct

---

## ✅ Logs de diagnostic ajoutés

```kotlin
android.util.Log.d("CastingRepository", "========== CANDIDATURE AU CASTING ==========")
android.util.Log.d("CastingRepository", "📝 Postulation au casting: $id")
android.util.Log.d("CastingRepository", "📹 Vidéo fournie: ${videoUri != null}")
android.util.Log.d("CastingRepository", "🤖 Feedback IA fourni: ${aiFeedback != null}")
android.util.Log.d("CastingRepository", "📹 Taille de la vidéo: ${String.format("%.1f", fileSizeMB)} MB")
android.util.Log.d("CastingRepository", "📤 Envoi de la candidature:")
android.util.Log.d("CastingRepository", "   - Casting ID: $id")
android.util.Log.d("CastingRepository", "   - Vidéo: ${if (videoPart != null) "OUI" else "NON"}")
android.util.Log.d("CastingRepository", "   - AI Feedback: ${if (aiFeedbackBody != null) "OUI" else "NON"}")
```

Ces logs permettront de voir exactement ce qui est envoyé.

---

## 🔍 Points de vérification

### 1. Token JWT

**Backend attend** :
```json
{
  "sub": "690cda42dc65a1a3fc2c6a20",
  "role": "ACTEUR",
  "type": "ACTEUR",
  "email": "user@user.com",
  "exp": 1766356463
}
```

**Android envoie** :
- Le token est géré par `AuthInterceptor`
- Les logs `AuthInterceptor` confirment :
  ```
  📋 Token info: id=690cda42dc65a1a3fc2c6a20, role=ACTEUR, type=ACTEUR, email=user@user.com
  ```

**Statut** : ✅ Correct

### 2. Content-Type

**Backend attend** :
```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
```

**Android envoie** :
- Retrofit avec `@Multipart` génère automatiquement le bon Content-Type
- Vérifié dans les logs `AuthInterceptor` :
  ```
  📎 Requête multipart détectée
  ```

**Statut** : ✅ Correct

### 3. Structure multipart

**Backend attend** :
```
------WebKitFormBoundary...
Content-Disposition: form-data; name="video"; filename="video.mp4"
Content-Type: video/mp4

[binary data]
------WebKitFormBoundary...
Content-Disposition: form-data; name="aiFeedback"
Content-Type: text/plain

{"globalScore": 75, ...}
------WebKitFormBoundary...--
```

**Android envoie** :
- `video` : `MultipartBody.Part.createFormData("video", filename, requestBody)`
- `aiFeedback` : `@Part("aiFeedback") RequestBody` avec `text/plain`

**Statut** : ✅ Correct

---

## 🧪 Scénarios de test

### Test 1 : Sans vidéo, sans AI feedback

**Paramètres** :
- `video` : `null`
- `aiFeedback` : `null`

**Requête attendue** :
```http
POST /castings/693f0a9897caff2222dc87f1/apply HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: multipart/form-data; boundary=...
Content-Length: 0
```

**Résultat attendu** :
- ✅ 200 OK : "Candidature enregistrée avec succès"

### Test 2 : Avec vidéo, sans AI feedback

**Paramètres** :
- `video` : `video.mp4` (2.5 MB)
- `aiFeedback` : `null`

**Requête attendue** :
```http
POST /castings/693f0a9897caff2222dc87f1/apply HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: multipart/form-data; boundary=...

------WebKitFormBoundary...
Content-Disposition: form-data; name="video"; filename="video.mp4"
Content-Type: video/mp4

[binary data]
------WebKitFormBoundary...--
```

**Résultat attendu** :
- ✅ 200 OK : "Candidature enregistrée avec succès"

### Test 3 : Avec vidéo et AI feedback

**Paramètres** :
- `video` : `video.mp4` (2.5 MB)
- `aiFeedback` : `{"globalScore": 75, ...}` (JSON string)

**Requête attendue** :
```http
POST /castings/693f0a9897caff2222dc87f1/apply HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: multipart/form-data; boundary=...

------WebKitFormBoundary...
Content-Disposition: form-data; name="video"; filename="video.mp4"
Content-Type: video/mp4

[binary data]
------WebKitFormBoundary...
Content-Disposition: form-data; name="aiFeedback"
Content-Type: text/plain

{"globalScore": 75, ...}
------WebKitFormBoundary...--
```

**Résultat attendu** :
- ✅ 200 OK : "Candidature enregistrée avec succès"

---

## 🔴 Hypothèses sur le problème 403

Si l'erreur 403 persiste malgré un code correct, les causes possibles sont :

### Hypothèse 1 : L'acteur n'existe pas en base de données

**Symptôme** :
```
[JWT Strategy] ❌ ERREUR: Acteur non trouvé pour ID: 690cda42dc65a1a3fc2c6a20
```

**Solution** :
1. Vérifier que l'acteur existe :
   ```javascript
   db.acteurs.findOne({ _id: ObjectId("690cda42dc65a1a3fc2c6a20") })
   ```
2. Si non, créer un nouveau compte acteur ou utiliser un autre acteur existant

### Hypothèse 2 : Le JWT_SECRET est différent

**Symptôme** :
```
[JWT Guard] ❌ Token invalide (signature verification failed)
```

**Solution** :
1. Vérifier que le même `JWT_SECRET` est utilisé pour encoder (login) et décoder (apply)
2. Se déconnecter et se reconnecter pour obtenir un nouveau token avec le bon secret

### Hypothèse 3 : Le rôle est un enum au lieu d'un string

**Symptôme** :
```
[RolesGuard] Comparaison rôle: {
  userRole: Role { value: "ACTEUR" },  // Enum
  requiredRole: "ACTEUR",               // String
  matches: false
}
```

**Solution** :
Backend doit normaliser le rôle dans `JwtStrategy.validate()` :
```typescript
return {
  id: acteur._id.toString(),
  role: acteur.role.toString(), // ou String(acteur.role)
  email: acteur.email
};
```

### Hypothèse 4 : Un middleware bloque avant les guards

**Symptôme** :
Aucun log `[JWT Guard]` ou `[JWT Strategy]` n'apparaît dans les logs backend.

**Solution** :
1. Vérifier les middlewares globaux dans `main.ts`
2. Vérifier les intercepteurs NestJS
3. Vérifier les configurations Vercel (edge functions, etc.)

---

## 📋 Checklist de test

Avant de tester l'application :

- [ ] Le code Android a été buildé et installé sur l'appareil
- [ ] L'acteur avec l'ID `690cda42dc65a1a3fc2c6a20` existe en base de données
- [ ] Le casting avec l'ID `693f0a9897caff2222dc87f1` existe et est ouvert
- [ ] Les corrections backend ont été déployées sur Vercel
- [ ] Logcat est prêt à capturer les logs (`CastingRepository`, `AuthInterceptor`)

Pendant le test :

- [ ] Observer les logs `========== CANDIDATURE AU CASTING ==========`
- [ ] Vérifier que le token est présent dans les logs `AuthInterceptor`
- [ ] Vérifier que le rôle est `ACTEUR` dans les logs
- [ ] Noter le code de réponse HTTP (200, 403, etc.)

Après le test (si échec) :

- [ ] Vérifier les logs backend dans Vercel
- [ ] Identifier où ça bloque (JwtStrategy, RolesGuard, Controller)
- [ ] Partager les logs Android ET backend avec l'équipe

---

## ✅ Conclusion

### Code Android

Le code Android est **CONFORME** aux spécifications backend :
- ✅ URL correcte
- ✅ Méthode POST
- ✅ Multipart/form-data
- ✅ Authorization header
- ✅ Paramètres video et aiFeedback corrects
- ✅ Content-Types corrects

### Backend

Le backend est **FONCTIONNEL** (confirmé via Swagger).

### Cause probable du 403

Si l'erreur 403 persiste, la cause est probablement :
1. **L'acteur n'existe pas** en base de données avec l'ID du token
2. **Le JWT_SECRET** utilisé pour encoder le token est différent de celui utilisé pour le décoder
3. **Le rôle est un enum** dans l'objet utilisateur au lieu d'un string

Les **logs backend dans Vercel** permettront d'identifier exactement la cause.

---

## 🚀 Prochaines étapes

1. **Tester l'application** Android avec les nouveaux logs
2. **Observer les logs Android** pour confirmer que la requête est correctement construite
3. **Consulter les logs backend Vercel** pour voir exactement où ça bloque
4. **Corriger selon le diagnostic** :
   - Si problème de BD → Créer/utiliser un acteur valide
   - Si problème de secret → Se reconnecter avec le bon secret
   - Si problème de rôle → Normaliser le rôle en string dans le backend

**Le code Android est correct. Le diagnostic se fera via les logs backend.** 🎯

