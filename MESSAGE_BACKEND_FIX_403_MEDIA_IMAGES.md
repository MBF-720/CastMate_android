# 🖼️ Message pour l'équipe Backend - Problème 403 sur les requêtes de médias (images)

**Date:** 2025-12-14  
**Application:** Android (CastMate)  
**Problème:** Erreur HTTP 403 (Forbidden) lors du chargement des images de profil, galerie et CV via les URLs `/media/{fileId}`

---

## 📋 Résumé

L'application Android envoie correctement le token JWT dans le header `Authorization: Bearer <token>` pour toutes les requêtes vers `/media/{fileId}`, mais reçoit systématiquement une erreur **HTTP 403 (Forbidden)**.

**Important:** Les autres endpoints fonctionnent correctement (par exemple `GET /acteur/:id` retourne 200 OK avec le même token).

---

## 🔧 Comment nous chargeons les images

### Architecture

1. **Librairie utilisée:** [Coil](https://coil-kt.github.io/coil/) pour le chargement d'images asynchrone
2. **URLs construites:** `https://cast-mate.vercel.app/media/{fileId}`
3. **Authentification:** Token JWT ajouté automatiquement via un intercepteur OkHttp

### Exemple de code

```kotlin
// Intercepteur OkHttp qui ajoute le token JWT
class AuthImageInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val tokenManager = TokenManager(context.applicationContext)
        
        val token = runBlocking {
            val isExpired = tokenManager.isTokenExpired()
            if (isExpired) null else tokenManager.getTokenSync()
        }
        
        val newRequest = if (token != null && token.isNotBlank()) {
            val cleanToken = token.trim()
            originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $cleanToken")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

// Configuration Coil avec authentification
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthImageInterceptor(context))
    .build()

val imageLoader = ImageLoader.Builder(context)
    .okHttpClient(okHttpClient)
    .build()
```

### Requêtes générées

Pour chaque image, Coil génère une requête HTTP GET :

```
GET https://cast-mate.vercel.app/media/{fileId}
Authorization: Bearer <jwt_token>
```

---

## 📊 Logs de l'application

### ✅ Confirmation que le token est envoyé

```
2025-12-14 22:15:06.546 CoilConfig: 🖼️ Requête image: https://cast-mate.vercel.app/media/693f1ecd12b354cd317d0bed
2025-12-14 22:15:06.546 CoilConfig: 🔑 Token présent: true, token length: 247
2025-12-14 22:15:06.547 CoilConfig: ✅ Header Authorization ajouté pour: https://cast-mate.vercel.app/media/693f1ecd12b354cd317d0bed
2025-12-14 22:15:06.548 CoilConfig: 📤 Headers de la requête: [Authorization]
2025-12-14 22:15:06.548 CoilConfig: ✅ Authorization header présent dans la requête
```

### ❌ Réponse du backend

```
2025-12-14 22:15:07.064 RealImageLoader: 🚨 Failed - https://cast-mate.vercel.app/media/693f1ecd12b354cd317d0bed - coil.network.HttpException: HTTP 403:
```

### 📋 Informations du token JWT

D'après les logs de `AuthInterceptor` (qui fonctionne pour les autres endpoints), le token contient :

```json
{
  "id": "690cda42dc65a1a3fc2c6a20",
  "role": "ACTEUR",
  "type": null,  // ou non défini pour les acteurs
  "exp": <timestamp>
}
```

**Note:** Pour les acteurs, le champ `type` est généralement `null` ou absent du token.

---

## 🔍 Observations

1. ✅ **Le token JWT est présent et valide** (247 caractères, format correct)
2. ✅ **Le header `Authorization: Bearer <token>` est bien ajouté** à toutes les requêtes
3. ✅ **Les autres endpoints fonctionnent** avec le même token (ex: `GET /acteur/:id` → 200 OK)
4. ❌ **Seules les requêtes vers `/media/{fileId}` retournent 403**

### Requêtes qui fonctionnent

```
GET https://cast-mate.vercel.app/acteur/690cda42dc65a1a3fc2c6a20
Authorization: Bearer <token>
→ 200 OK ✅
```

### Requêtes qui échouent

```
GET https://cast-mate.vercel.app/media/693f1ecd12b354cd317d0bed
Authorization: Bearer <token>
→ 403 Forbidden ❌
```

---

## ❓ Questions pour l'équipe Backend

1. **Vérification des permissions:**
   - L'endpoint `/media/{fileId}` vérifie-t-il des permissions spécifiques (role, type, ownership) ?
   - Un acteur peut-il accéder à ses propres médias (photo de profil, galerie, CV) ?

2. **Format du header Authorization:**
   - Le backend accepte-t-il le format `Authorization: Bearer <token>` ?
   - Y a-t-il une différence de traitement entre les requêtes `/acteur/:id` et `/media/:fileId` ?

3. **Vérification du fileId:**
   - Le backend vérifie-t-il que le `fileId` appartient bien à l'utilisateur authentifié ?
   - Y a-t-il une validation spécifique qui pourrait rejeter la requête ?

4. **Headers supplémentaires:**
   - Le backend attend-il des headers supplémentaires pour les requêtes de médias ?
   - Y a-t-il une différence dans la validation des requêtes GET pour les médias ?

---

## 🎯 Comportement attendu

Nous attendons que les requêtes suivantes fonctionnent pour un acteur authentifié :

1. **Photo de profil:**
   ```
   GET /media/{photoFileId}
   Authorization: Bearer <token>
   → 200 OK avec image/jpeg
   ```

2. **Photos de galerie:**
   ```
   GET /media/{galleryFileId}
   Authorization: Bearer <token>
   → 200 OK avec image/jpeg
   ```

3. **CV (PDF):**
   ```
   GET /media/{documentFileId}
   Authorization: Bearer <token>
   → 200 OK avec application/pdf
   ```

---

## 🔄 Comparaison avec d'autres clients

- ✅ **iOS:** L'application iOS peut-elle charger les images avec le même token ?
- ✅ **Postman/Insomnia:** Les requêtes manuelles fonctionnent-elles avec le même token ?

Si oui, cela pourrait indiquer une différence subtile dans les headers ou le format de la requête.

---

## 📝 Informations techniques supplémentaires

### Structure de la réponse API pour `/acteur/:id`

```json
{
  "id": "690cda42dc65a1a3fc2c6a20",
  "nom": "ben fredj",
  "prenom": "mohamed",
  "media": {
    "photoFileId": "693f1ecd12b354cd317d0bed",
    "photoMimeType": "image/jpeg",
    "documentFileId": "6915ea5920db74c69afdd4d8",
    "documentMimeType": "application/pdf",
    "gallery": [
      {
        "fileId": "691733d49c56a56bbe55843d",
        "mimeType": "image/jpeg"
      }
    ]
  }
}
```

### Headers envoyés par l'application

```
GET /media/693f1ecd12b354cd317d0bed HTTP/1.1
Host: cast-mate.vercel.app
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
User-Agent: okhttp/4.x.x (Android)
Accept: */*
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
```

---

## 🚀 Actions demandées

1. **Vérifier les logs backend** pour voir pourquoi les requêtes `/media/{fileId}` sont rejetées avec 403
2. **Vérifier la logique d'autorisation** pour l'endpoint `/media/{fileId}`
3. **Confirmer que les acteurs peuvent accéder à leurs propres médias** avec leur token JWT
4. **Vérifier s'il y a une différence** dans le traitement des requêtes `/acteur/:id` vs `/media/:fileId`

---

## 📞 Contact

Si vous avez besoin d'informations supplémentaires ou de tests supplémentaires, n'hésitez pas à nous contacter.

**Merci pour votre aide !** 🙏

