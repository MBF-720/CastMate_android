# 🔴 URGENT - Erreur 403 sur POST /castings/{id}/apply

**Date:** 2025-12-14 23:16:09 GMT  
**Endpoint:** `POST /castings/693f0a9897caff2222dc87f1/apply`  
**User ID:** `690cda42dc65a1a3fc2c6a20`  
**Email:** `user@user.com`  
**Rôle:** `ACTEUR`  

---

## 🚨 Problème

Un acteur authentifié avec un token JWT valide reçoit une erreur **403 Forbidden** lors de la candidature à un casting.

### Réponse Backend

```
HTTP 403 Forbidden
Content-Type: text/plain; charset=utf-8

Forbidden

Forbidden
cdg1::57vbj-1765750567254-adc75dbc91fa
```

**Aucun message d'erreur détaillé**, ce qui indique que la requête est bloquée **avant** d'atteindre le controller ou que les logs ne sont pas activés.

---

## ✅ Côté Android - TOUT EST CORRECT

### Token JWT envoyé

```json
{
  "id": "690cda42dc65a1a3fc2c6a20",
  "sub": "690cda42dc65a1a3fc2c6a20",
  "email": "user@user.com",
  "role": "ACTEUR",
  "type": "ACTEUR",
  "iat": 1734543000,
  "exp": 1766345000
}
```

✅ Token valide (expire dans 165 heures)  
✅ ID utilisateur présent : `690cda42dc65a1a3fc2c6a20`  
✅ Rôle : `ACTEUR` (requis pour postuler)  
✅ Type : `ACTEUR`  

### Headers envoyés

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: multipart/form-data; boundary=...
```

✅ Authorization header présent et correct  
✅ Format multipart/form-data correct  

### Logs Android complets

```
🌐 Intercepteur appelé pour: POST https://cast-mate.vercel.app/castings/693f0a9897caff2222dc87f1/apply
📍 Path: /castings/693f0a9897caff2222dc87f1/apply, Method: POST
🔓 Route publique? isPublicRoute=false, isPublicRouteWithMethod=false
🔍 Vérification du token pour: POST https://cast-mate.vercel.app/castings/693f0a9897caff2222dc87f1/apply
✅ Token JWT valide: expire dans 165h 7min
🔍 Token expiré? false
✅ Token JWT valide et présent (247 caractères)
📋 Token info: id=690cda42dc65a1a3fc2c6a20, role=ACTEUR, type=ACTEUR, email=user@user.com, exp=1766345000
📋 Toutes les clés du token: email, sub, role, type, iat, exp
✅ Token JWT présent et valide: eyJhbGciOiJIUzI1NiIs...
📤 Envoi requête: POST https://cast-mate.vercel.app/castings/693f0a9897caff2222dc87f1/apply
📎 Requête multipart détectée
📋 Rôle dans le token: ACTEUR
📋 Type dans le token: ACTEUR
📤 Header Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
✅ Header Authorization confirmé: Bearer eyJhbGciOiJIUzI1NiIsInR...
📋 Tous les headers de la requête:
  - Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...

❌❌❌ ERREUR 403: Forbidden ❌❌❌
```

**Conclusion Android** : L'application envoie **TOUT** correctement. Le problème est **100% côté backend**.

---

## 🔍 Diagnostic Backend Requis

### 1. Vérifier les logs Vercel

**URGENT** : Vérifier les logs dans Vercel pour voir exactement où le blocage se produit.

#### Logs attendus (si tout fonctionne)

```
[JWT Strategy] Recherche acteur avec ID: 690cda42dc65a1a3fc2c6a20
[JWT Strategy] Acteur trouvé: { nom: "ben fredj", prenom: "mohamed", email: "user@user.com", ... }
[JWT Strategy] Objet utilisateur retourné (ACTEUR): { id: "690cda42dc65a1a3fc2c6a20", role: "ACTEUR", ... }

[RolesGuard] Vérification des rôles: {
  requiredRoles: ["ACTEUR"],
  userRole: "ACTEUR",
  user: { id: "690cda42dc65a1a3fc2c6a20", role: "ACTEUR" }
}
[RolesGuard] Comparaison rôle: { userRole: "ACTEUR", requiredRole: "ACTEUR", matches: true }
[RolesGuard] ✅ Accès autorisé pour le rôle: ACTEUR

[CastingsController] Candidature à un casting: {
  castingId: "693f0a9897caff2222dc87f1",
  hasUser: true,
  userId: "690cda42dc65a1a3fc2c6a20",
  userRole: "ACTEUR"
}
```

#### Si les logs n'apparaissent pas

**Scénario 1 - Blocage dans JwtAuthGuard**
```
[JWT Strategy] Token reçu: eyJhbGciOiJIUzI1NiIs...
[JWT Strategy] ❌ Erreur lors de la vérification du token: ...
```
→ Le token n'est pas correctement décodé ou vérifié

**Scénario 2 - Blocage dans JwtStrategy**
```
[JWT Strategy] Recherche acteur avec ID: 690cda42dc65a1a3fc2c6a20
[JWT Strategy] ❌ Acteur non trouvé
```
→ L'acteur n'existe pas en base de données (vérifier que l'ID `690cda42dc65a1a3fc2c6a20` existe)

**Scénario 3 - Blocage dans RolesGuard**
```
[RolesGuard] Vérification des rôles: {
  requiredRoles: ["ACTEUR"],
  userRole: undefined,
  user: { ... }
}
[RolesGuard] ❌ Utilisateur sans rôle
```
→ Le rôle n'est pas correctement extrait de l'objet utilisateur

**Scénario 4 - Autre Guard/Middleware**
```
Aucun log [JWT Strategy] ou [RolesGuard]
```
→ Un autre guard ou middleware bloque la requête avant l'authentification

---

## 🔧 Corrections suggérées Backend

### 1. Vérifier que l'endpoint existe et est configuré

```typescript
// Dans castings.controller.ts
@Post(':id/apply')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('ACTEUR')
@UseInterceptors(FileInterceptor('video'))
async applyToCasting(
  @Param('id') castingId: string,
  @UploadedFile() video: Express.Multer.File,
  @Body() body: { aiFeedback?: string },
  @Req() req: Request,
) {
  console.log('[CastingsController] Candidature à un casting:', {
    castingId,
    hasUser: !!req.user,
    userId: req.user?.id || req.user?._id || req.user?.sub,
    userRole: req.user?.role,
    hasVideo: !!video,
    hasAiFeedback: !!body?.aiFeedback,
  });
  
  // ... reste du code
}
```

### 2. Vérifier le JWT_SECRET

Assurez-vous que le même `JWT_SECRET` est utilisé pour :
- Encoder le token (lors du login)
- Décoder le token (dans JwtStrategy)

```typescript
// Dans jwt.strategy.ts
constructor(
  private acteurService: ActeurService,
  @Inject(forwardRef(() => ConfigService)) private configService: ConfigService,
) {
  super({
    jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
    ignoreExpiration: false,
    secretOrKey: configService.get<string>('JWT_SECRET'), // ⚠️ Vérifier cette valeur
  });
  
  console.log('[JWT Strategy] JWT_SECRET chargé:', 
    configService.get<string>('JWT_SECRET')?.substring(0, 10) + '...');
}
```

### 3. Vérifier la base de données

```typescript
// Vérifier que l'acteur existe
const acteur = await acteurModel.findById('690cda42dc65a1a3fc2c6a20');
console.log('Acteur existe?', !!acteur);
console.log('Acteur role:', acteur?.role);
console.log('Acteur email:', acteur?.email);
```

### 4. Ajouter des logs de diagnostic dans JwtAuthGuard

```typescript
// Dans jwt-auth.guard.ts
@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {
  canActivate(context: ExecutionContext) {
    const request = context.switchToHttp().getRequest();
    console.log('[JwtAuthGuard] Requête entrante:', {
      method: request.method,
      url: request.url,
      hasAuthHeader: !!request.headers.authorization,
      authHeaderStart: request.headers.authorization?.substring(0, 30),
    });
    
    return super.canActivate(context);
  }

  handleRequest(err, user, info, context) {
    console.log('[JwtAuthGuard] handleRequest:', {
      hasError: !!err,
      errorMessage: err?.message,
      hasUser: !!user,
      userId: user?.id || user?._id,
      userRole: user?.role,
      info: info?.message,
    });
    
    if (err || !user) {
      console.error('[JwtAuthGuard] ❌ Authentification échouée:', {
        error: err?.message,
        info: info?.message,
      });
      throw err || new UnauthorizedException();
    }
    
    console.log('[JwtAuthGuard] ✅ Authentification réussie pour:', user.id);
    return user;
  }
}
```

---

## 🧪 Tests Backend à effectuer

### Test 1 : Vérifier que l'endpoint répond sans guards

```typescript
// Temporairement retirer les guards pour tester
@Post(':id/apply')
// @UseGuards(JwtAuthGuard, RolesGuard)  // ⚠️ Commenter temporairement
// @Roles('ACTEUR')  // ⚠️ Commenter temporairement
async applyToCasting(
  @Param('id') castingId: string,
  @Req() req: Request,
) {
  console.log('[TEST] Endpoint atteint sans guards');
  return { message: 'Endpoint fonctionnel sans guards' };
}
```

**Si ça fonctionne** → Le problème vient des guards  
**Si ça ne fonctionne pas** → Le problème vient du routing ou d'un autre middleware

### Test 2 : Vérifier l'extraction du token

```typescript
// Dans jwt.strategy.ts
async validate(payload: any) {
  console.log('[JWT Strategy] ========== VALIDATION TOKEN ==========');
  console.log('[JWT Strategy] Payload reçu:', payload);
  console.log('[JWT Strategy] ID extrait:', payload.id || payload._id || payload.sub);
  console.log('[JWT Strategy] Rôle extrait:', payload.role);
  console.log('[JWT Strategy] Email extrait:', payload.email);
  
  // ... reste du code
}
```

### Test 3 : Vérifier le RolesGuard

```typescript
// Dans roles.guard.ts
canActivate(context: ExecutionContext): boolean {
  const requiredRoles = this.reflector.getAllAndOverride<string[]>(ROLES_KEY, [
    context.getHandler(),
    context.getClass(),
  ]);

  console.log('[RolesGuard] ========== VÉRIFICATION RÔLES ==========');
  console.log('[RolesGuard] Rôles requis:', requiredRoles);
  console.log('[RolesGuard] Endpoint:', context.switchToHttp().getRequest().url);

  if (!requiredRoles) {
    console.log('[RolesGuard] ✅ Aucun rôle requis');
    return true;
  }

  const { user } = context.switchToHttp().getRequest();
  console.log('[RolesGuard] Utilisateur:', {
    hasUser: !!user,
    id: user?.id || user?._id,
    role: user?.role,
  });

  if (!user) {
    console.error('[RolesGuard] ❌ Utilisateur non défini');
    return false;
  }

  const hasRole = requiredRoles.some((role) => user.role?.toUpperCase() === role.toUpperCase());
  console.log('[RolesGuard] Correspondance rôle:', {
    userRole: user.role,
    requiredRoles,
    hasRole,
  });

  return hasRole;
}
```

---

## 📊 Informations de contexte

### Casting ID
- **ID** : `693f0a9897caff2222dc87f1`
- Vérifier que ce casting existe en base de données
- Vérifier que le casting est ouvert (`status: "ouvert"`)

### Acteur
- **ID** : `690cda42dc65a1a3fc2c6a20`
- **Email** : `user@user.com`
- **Nom** : Mohamed Ben Fredj
- **Rôle** : `ACTEUR`

### Token JWT
- **Émis le (iat)** : 1734543000 (2024-12-18 18:50:00 GMT)
- **Expire le (exp)** : 1766345000 (2025-12-21 06:50:00 GMT)
- **Durée de validité** : 165 heures restantes
- **Longueur** : 247 caractères

---

## ⚠️ Hypothèses à vérifier

### Hypothèse 1 : Secret JWT différent
Le JWT_SECRET utilisé pour encoder le token (au login) est différent du JWT_SECRET utilisé pour décoder (dans JwtStrategy).

**Test** :
```typescript
const decodedWithoutVerification = jwt.decode(token);
console.log('Payload sans vérification:', decodedWithoutVerification);

try {
  const decodedWithVerification = jwt.verify(token, JWT_SECRET);
  console.log('✅ Token vérifié avec succès');
} catch (error) {
  console.error('❌ Erreur de vérification:', error.message);
}
```

### Hypothèse 2 : Acteur supprimé ou inexistant
L'acteur avec l'ID `690cda42dc65a1a3fc2c6a20` n'existe pas ou a été supprimé.

**Test** :
```typescript
const acteur = await acteurModel.findById('690cda42dc65a1a3fc2c6a20');
console.log('Acteur existe?', !!acteur);
```

### Hypothèse 3 : Middleware CORS ou autre
Un middleware (CORS, rate limiter, etc.) bloque la requête avant qu'elle n'atteigne les guards.

**Test** : Vérifier les logs Vercel pour voir si des middlewares génèrent des logs avant [JwtAuthGuard].

### Hypothèse 4 : Route non enregistrée
L'endpoint `POST /castings/:id/apply` n'est pas correctement enregistré dans le module.

**Test** :
```bash
# Dans les logs de démarrage de NestJS, vérifier :
[Nest] Mapped {/castings/:id/apply, POST} route
```

---

## 🚨 Actions immédiates requises

1. **Vérifier les logs Vercel** pour voir exactement où ça bloque
2. **Ajouter les logs de diagnostic** dans JwtAuthGuard, JwtStrategy et RolesGuard
3. **Vérifier que l'acteur existe** en base de données avec l'ID `690cda42dc65a1a3fc2c6a20`
4. **Vérifier le JWT_SECRET** utilisé pour encoder et décoder
5. **Tester sans guards** pour isoler le problème

---

## 📞 Prochaines étapes

1. **Partager les logs Vercel** avec l'équipe frontend
2. Si les logs montrent un problème dans JwtStrategy → Vérifier la base de données
3. Si les logs montrent un problème dans RolesGuard → Vérifier l'extraction du rôle
4. Si aucun log n'apparaît → Vérifier le routing et les middlewares

---

## ✅ Résumé

- ✅ **Android** : Tout est correct (token, headers, format)
- 🔴 **Backend** : Retourne 403 sans message détaillé
- 🔍 **Diagnostic** : Vérifier les logs backend pour identifier le blocage
- 📋 **Actions** : Ajouter des logs détaillés dans tous les guards et middlewares

**Le problème est 100% côté backend. Merci de vérifier les logs Vercel et de partager les résultats !** 🙏

