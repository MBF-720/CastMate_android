# 🐛 Bug Backend - Erreur 403 Forbidden lors de la création de casting

## 📋 Résumé du problème

L'endpoint `POST /castings` retourne une erreur **403 Forbidden** même lorsque l'utilisateur a les droits requis (`role=RECRUTEUR` et `type=AGENCE` dans le token JWT).

## 🔍 Preuve du problème

D'après les logs côté Android, **le token JWT est valide** et contient les bonnes informations :

```
✅ Token JWT présent et valide
📋 Rôle dans le token: RECRUTEUR
📋 Type dans le token: AGENCE
✅ Rôle et type valides pour créer un casting
✅ Header Authorization confirmé: Bearer eyJhbGciOiJIUzI1NiIsInR...
📎 Requête multipart détectée
```

**Pourtant**, le backend retourne :
```
❌ 403 Forbidden
Body: "Forbidden\n\nForbidden\ncdg1::5jtsr-1765734936503-7f13da42f689"
```

## 🔧 Détails techniques

### Requête envoyée
- **Endpoint**: `POST /castings`
- **Content-Type**: `multipart/form-data`
- **Headers**: 
  - `Authorization: Bearer <JWT_TOKEN>`
- **Body**:
  - `payload`: JSON string (text/plain) contenant les données du casting
  - `affiche`: Fichier image (optionnel)

### Token JWT décodé
```json
{
  "role": "RECRUTEUR",
  "type": "AGENCE",
  "exp": 1766339692
}
```

**Note**: Le champ `id` n'est pas présent dans le token (affiche "N/A" dans les logs), mais cela ne devrait pas être nécessaire pour la vérification du rôle.

## 🎯 Causes possibles

1. **Problème avec les requêtes multipart**
   - Le guard de rôle ne lit peut-être pas correctement le header `Authorization` pour les requêtes `multipart/form-data`
   - Le middleware d'authentification pourrait avoir un problème avec ce type de requête

2. **Problème de vérification du rôle**
   - Le `RolesGuard` ou le décorateur `@Roles()` vérifie peut-être autre chose que `role` et `type`
   - La comparaison des rôles pourrait être sensible à la casse ou au format

3. **Problème avec l'ID utilisateur**
   - Si le guard vérifie l'existence de l'utilisateur en base avec l'ID du token (`id` ou `sub`), et que ce champ est absent ou incorrect, cela pourrait causer un 403
   - Le token ne contient pas de champ `id` visible dans les logs (affiche "N/A")

## ✅ Vérifications à faire côté backend

1. **Vérifier que le header Authorization est bien lu**
   ```typescript
   // Dans votre guard ou middleware
   console.log('Authorization header:', request.headers.authorization);
   console.log('Token extrait:', jwtService.decode(token));
   ```

2. **Vérifier la logique du RolesGuard**
   ```typescript
   // Vérifier que le guard vérifie bien :
   // - role === 'RECRUTEUR' ou role === 'ADMIN'
   // - type === 'AGENCE'
   console.log('User role:', user.role);
   console.log('User type:', user.type);
   console.log('Can create casting:', user.role === 'RECRUTEUR' && user.type === 'AGENCE');
   ```

3. **Vérifier si l'ID utilisateur est requis**
   - Si le guard vérifie l'existence de l'utilisateur en base, s'assurer que le token contient un `id` ou `sub` valide
   - Ou que cette vérification n'est pas bloquante si l'ID est absent

4. **Vérifier les requêtes multipart**
   - Tester si le problème existe uniquement pour les requêtes `multipart/form-data`
   - Vérifier si d'autres endpoints multipart fonctionnent correctement

## 🔍 Code à vérifier

### 1. Guard de rôle (exemple NestJS)
```typescript
// Vérifier que le guard accepte bien les requêtes multipart
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('RECRUTEUR', 'ADMIN')
@Post('castings')
@UseInterceptors(FileInterceptor('affiche'))
async createCasting(
  @Body('payload') payload: string,
  @UploadedFile() affiche?: Express.Multer.File,
) {
  // ...
}
```

### 2. Décorateur @Roles() ou RolesGuard
```typescript
// Vérifier la logique de vérification
const canActivate = user.role === 'RECRUTEUR' && user.type === 'AGENCE' 
                 || user.role === 'ADMIN' && user.type === 'AGENCE';
```

### 3. Extraction du token JWT
```typescript
// Vérifier que le token est bien extrait du header pour les requêtes multipart
const token = request.headers.authorization?.replace('Bearer ', '');
const payload = jwtService.decode(token);
```

## 📝 Requête de test

Pour tester, voici ce qui est envoyé :

**URL**: `POST https://cast-mate.vercel.app/castings`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
```

**Body (multipart/form-data)**:
```
------WebKitFormBoundary...
Content-Disposition: form-data; name="payload"
Content-Type: text/plain; charset=utf-8

{"titre":"...","descriptionRole":"...","synopsis":"...","lieu":"...","dateDebut":"...","dateFin":"...","prix":...,"types":[...],"age":"...","ouvert":true,"conditions":"..."}
------WebKitFormBoundary...
Content-Disposition: form-data; name="affiche"; filename="image.jpg"
Content-Type: image/jpeg

<fichier binaire>
------WebKitFormBoundary...--
```

## 🚨 Impact

- **Blocage**: Les recruteurs (agences) ne peuvent pas créer de castings
- **Impact utilisateur**: Fonctionnalité principale indisponible pour les agences
- **Sévérité**: 🔴 **Critique** - Bloque une fonctionnalité essentielle

## 📞 Contact

Pour plus d'informations ou de détails techniques, n'hésitez pas à demander les logs complets ou à tester avec le token JWT fourni.

## ✅ Solution attendue

Le backend doit accepter les requêtes `POST /castings` lorsque :
- Le token JWT contient `role=RECRUTEUR` ou `role=ADMIN`
- Le token JWT contient `type=AGENCE`
- Le header `Authorization: Bearer <token>` est présent
- La requête est au format `multipart/form-data`

**Statut actuel**: ❌ Rejeté avec 403 Forbidden malgré ces conditions remplies

