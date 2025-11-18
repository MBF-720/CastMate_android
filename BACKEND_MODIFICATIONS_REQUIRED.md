# 🔧 Modifications Backend Requises - Réinitialisation de mot de passe

## 📋 Contexte

L'application Android envoie maintenant les emails de réinitialisation de mot de passe directement depuis l'app (Gmail SMTP). Le backend doit accepter et stocker les tokens générés par Android pour pouvoir les valider lors de la réinitialisation.

## ⚠️ Problème Actuel

Quand l'utilisateur clique sur le lien de réinitialisation dans l'email, l'application Android appelle `POST /auth/reset-password` avec le token, mais le backend retourne **404 "Token introuvable"** car il n'a jamais stocké le token envoyé par Android.

## ✅ Modifications Nécessaires

### 1️⃣ Modifier `POST /auth/forgot-password`

**Actuellement** : Le backend reçoit seulement `{ email, userType }` et génère son propre token.

**Nouveau comportement** : Le backend doit accepter un champ optionnel `token` dans la requête.

#### Structure de la requête acceptée :

```json
{
  "email": "user@example.com",
  "userType": "ACTEUR" | "RECRUTEUR",
  "token": "hex-64-chars"  // ⚠️ NOUVEAU : Optionnel, token généré par Android
}
```

#### Logique à implémenter :

1. **Si `token` est fourni** (vient de l'application Android) :
   - Vérifier que l'email existe en base de données pour le `userType` donné
   - **Stocker le token** en base de données avec :
     - `email` : l'email de l'utilisateur
     - `userType` : "ACTEUR" ou "RECRUTEUR"
     - `token` : le token fourni
     - `expiresAt` : date actuelle + 1 heure (3600 secondes)
     - `used` : false (par défaut)
   - Retourner une réponse 200 avec `success: true`

2. **Si `token` n'est pas fourni** (ancien comportement) :
   - Générer un token comme avant
   - Stocker le token en base de données
   - Envoyer l'email depuis le backend (ancien comportement)

#### Exemple de réponse 200 :

```json
{
  "success": true,
  "message": "Token stored successfully",
  "token": "hex-64-chars",  // Le token stocké (celui envoyé par Android)
  "expiresIn": 3600
}
```

#### Gestion des erreurs :

- **404** : Si l'email n'existe pas pour le userType donné
- **400** : Si le token est invalide ou si trop de demandes (rate limiting)
- **500** : Erreur serveur

---

### 2️⃣ Modifier `POST /auth/reset-password`

**Comportement actuel** : Le backend cherche un token qu'il a généré lui-même.

**Nouveau comportement** : Le backend doit chercher le token dans la base de données, qu'il ait été généré par le backend ou envoyé par Android.

#### Structure de la requête :

```json
{
  "token": "hex-64-chars",
  "newPassword": "NewPass!23",
  "email": "user@example.com"
}
```

#### Logique à implémenter :

1. **Chercher le token en base de données** avec :
   - Le `token` fourni
   - L'`email` fourni
   - Vérifier que `used = false`

2. **Vérifier la validité** :
   - Le token existe
   - Le token n'est pas expiré (`expiresAt > maintenant`)
   - Le token n'a pas déjà été utilisé (`used = false`)

3. **Si valide** :
   - Trouver l'utilisateur avec l'email et le userType associé au token
   - Hash le nouveau mot de passe (bcrypt, cost 10)
   - Mettre à jour le mot de passe de l'utilisateur
   - Marquer le token comme utilisé (`used = true`) OU le supprimer
   - Retourner 200 avec `{ success: true, message: "Password updated" }`

4. **Si invalide** :
   - **400** : Token invalide, expiré, ou déjà utilisé
   - **404** : Token non trouvé

#### Exemple de réponse 200 :

```json
{
  "success": true,
  "message": "Password updated"
}
```

---

## 🗄️ Structure de Table Suggérée (Base de Données)

Si vous n'avez pas déjà une table pour les tokens de réinitialisation, créez-en une :

```sql
CREATE TABLE password_reset_tokens (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  user_type VARCHAR(50) NOT NULL,  -- "ACTEUR" ou "RECRUTEUR"
  token VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_token (token),
  INDEX idx_email_user_type (email, user_type)
);
```

**Important** : 
- Index sur `token` pour des recherches rapides
- Index sur `email` et `user_type` pour nettoyage des anciens tokens
- Expiration automatique : supprimer les tokens expirés (> 1 heure) périodiquement

---

## 📝 Exemple de Code Backend (NestJS/TypeScript)

### 1. Modèle de Données :

```typescript
// password-reset-token.entity.ts
@Entity('password_reset_tokens')
export class PasswordResetToken {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  email: string;

  @Column()
  userType: string; // "ACTEUR" | "RECRUTEUR"

  @Column({ unique: true })
  token: string;

  @Column()
  expiresAt: Date;

  @Column({ default: false })
  used: boolean;

  @CreateDateColumn()
  createdAt: Date;
}
```

### 2. DTO pour forgot-password :

```typescript
// forgot-password.dto.ts
export class ForgotPasswordDto {
  @IsEmail()
  email: string;

  @IsIn(['ACTEUR', 'RECRUTEUR'])
  userType: string;

  @IsOptional()
  @IsString()
  token?: string; // ⚠️ NOUVEAU : Optionnel, vient d'Android
}
```

### 3. Service :

```typescript
// auth.service.ts
async forgotPassword(dto: ForgotPasswordDto) {
  // Vérifier que l'utilisateur existe
  const user = await this.findUserByEmailAndType(dto.email, dto.userType);
  if (!user) {
    throw new NotFoundException('User not found');
  }

  let token: string;

  // Si token fourni (vient d'Android), l'utiliser
  if (dto.token) {
    token = dto.token;
  } else {
    // Sinon, générer un nouveau token (ancien comportement)
    token = this.generateResetToken();
  }

  // Stocker le token en base
  const expiresAt = new Date();
  expiresAt.setHours(expiresAt.getHours() + 1); // Expire dans 1 heure

  await this.passwordResetTokenRepository.save({
    email: dto.email,
    userType: dto.userType,
    token: token,
    expiresAt: expiresAt,
    used: false,
  });

  return {
    success: true,
    message: dto.token ? 'Token stored successfully' : 'Reset email sent',
    token: token,
    expiresIn: 3600,
  };
}

async resetPassword(dto: ResetPasswordDto) {
  // Chercher le token
  const resetToken = await this.passwordResetTokenRepository.findOne({
    where: {
      token: dto.token,
      email: dto.email,
      used: false,
    },
  });

  if (!resetToken) {
    throw new NotFoundException('Token introuvable');
  }

  // Vérifier l'expiration
  if (resetToken.expiresAt < new Date()) {
    throw new BadRequestException('Token expiré');
  }

  // Trouver l'utilisateur
  const user = await this.findUserByEmailAndType(
    dto.email,
    resetToken.userType,
  );
  if (!user) {
    throw new NotFoundException('User not found');
  }

  // Hash le nouveau mot de passe
  const hashedPassword = await bcrypt.hash(dto.newPassword, 10);

  // Mettre à jour le mot de passe
  await this.userRepository.update(user.id, { password: hashedPassword });

  // Marquer le token comme utilisé
  resetToken.used = true;
  await this.passwordResetTokenRepository.save(resetToken);

  return {
    success: true,
    message: 'Password updated',
  };
}
```

---

## ✅ Checklist de Vérification

- [ ] La table `password_reset_tokens` existe avec les colonnes nécessaires
- [ ] `POST /auth/forgot-password` accepte le champ optionnel `token`
- [ ] Si `token` est fourni, il est stocké en base avec expiration (1h)
- [ ] `POST /auth/reset-password` cherche le token en base de données
- [ ] Vérification de l'expiration du token (1 heure)
- [ ] Vérification que le token n'a pas été utilisé (`used = false`)
- [ ] Le token est marqué comme utilisé après réinitialisation réussie
- [ ] Gestion des erreurs : 404 si token non trouvé, 400 si token invalide/expiré
- [ ] Validation du mot de passe : minimum 8 caractères

---

## 🎯 Résultat Attendu

Après ces modifications :

1. L'application Android génère un token et l'envoie au backend via `POST /auth/forgot-password` avec `{ email, userType, token }`
2. Le backend **stocke** le token en base de données
3. L'application Android envoie l'email avec le lien de réinitialisation
4. L'utilisateur clique sur le lien et entre dans l'app
5. L'app appelle `POST /auth/reset-password` avec `{ token, newPassword, email }`
6. Le backend **trouve** le token en base, vérifie qu'il est valide, et change le mot de passe ✅

---

## 📞 Questions ?

Si vous avez besoin de clarifications, référez-vous à cette documentation et aux logs de l'application Android qui montrent exactement quels tokens sont générés et envoyés.

