# 🔑 Guide : Configuration Email pour Réinitialisation de Mot de Passe

## ⚠️ **IMPORTANT : Différence entre les Clés**

### ❌ **NON - Les clés "Sign with Google" NE PEUVENT PAS être utilisées pour envoyer des emails**

- **OAuth 2.0 Client ID** (utilisé pour Sign with Google) = Pour **authentifier** les utilisateurs
- **Gmail API / App Password** (nécessaire pour envoyer des emails) = Pour **envoyer** des emails

**Ce sont deux choses différentes !** ✅

---

## 🎯 **Solution Simple : Utiliser Gmail App Password** (RECOMMANDÉ)

C'est la solution la plus simple et rapide pour commencer.

### 1️⃣ **Créer un Mot de Passe d'Application Gmail**

1. Allez sur **https://myaccount.google.com/security**
2. Activez la **Validation en 2 étapes** (si ce n'est pas déjà fait)
3. Dans **Validation en 2 étapes**, cherchez **Mots de passe des applications**
4. Sélectionnez **Autre (nom personnalisé)**
5. Entrez : `CastMate Backend`
6. Cliquez sur **Générer**
7. **Copiez le mot de passe** (16 caractères, exemple : `abcd efgh ijkl mnop`)
   - ⚠️ **IMPORTANT** : Vous ne pourrez plus le voir après ! Sauvegardez-le maintenant !

### 2️⃣ **Configurer le Backend NestJS**

#### A. Installer les dépendances

```bash
npm install nodemailer
npm install @types/nodemailer --save-dev
```

#### B. Créer le service d'email (`src/email/email.service.ts`)

```typescript
import { Injectable, Logger } from '@nestjs/common';
import * as nodemailer from 'nodemailer';
import { Transporter } from 'nodemailer';

@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);
  private transporter: Transporter;

  constructor() {
    // Configuration Gmail avec App Password
    this.transporter = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: process.env.GMAIL_USER, // Votre email Gmail (ex: your-email@gmail.com)
        pass: process.env.GMAIL_APP_PASSWORD, // Le mot de passe d'application (16 caractères)
      },
    });

    this.logger.log('✅ Gmail transporter initialisé');
  }

  /**
   * Envoie un email de réinitialisation de mot de passe
   */
  async sendPasswordResetEmail(
    email: string,
    resetToken: string,
    userType: 'ACTEUR' | 'RECRUTEUR',
  ): Promise<void> {
    try {
      // Générer le lien de réinitialisation
      const frontendUrl = process.env.FRONTEND_URL || 'https://votre-app.com';
      const resetLink = `${frontendUrl}/reset-password?token=${resetToken}&type=${userType}`;

      // Texte selon le type d'utilisateur
      const isAgency = userType === 'RECRUTEUR';
      const subject = isAgency
        ? 'Réinitialisation de votre mot de passe - CastMate Agence'
        : 'Password Reset - CastMate Actor';

      const htmlContent = isAgency
        ? this.getAgencyEmailTemplate(resetLink)
        : this.getActorEmailTemplate(resetLink);

      // Envoyer l'email
      const info = await this.transporter.sendMail({
        from: `"CastMate" <${process.env.GMAIL_USER}>`,
        to: email,
        subject: subject,
        html: htmlContent,
      });

      this.logger.log(`✅ Email envoyé à ${email} - Message ID: ${info.messageId}`);
    } catch (error) {
      this.logger.error(`❌ Erreur envoi email à ${email}:`, error.message);
      throw new Error('Erreur lors de l\'envoi de l\'email');
    }
  }

  /**
   * Template email pour les acteurs
   */
  private getActorEmailTemplate(resetLink: string): string {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Password Reset</title>
      </head>
      <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
          <h2 style="color: #1a73e8;">Password Reset Request</h2>
          <p>Hello,</p>
          <p>You requested to reset your password for your CastMate actor account.</p>
          <p>Click the button below to reset your password:</p>
          <div style="text-align: center; margin: 30px 0;">
            <a href="${resetLink}" 
               style="background-color: #1a73e8; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
              Reset Password
            </a>
          </div>
          <p>Or copy and paste this link in your browser:</p>
          <p style="word-break: break-all; color: #666;">${resetLink}</p>
          <p style="color: #666; font-size: 14px; margin-top: 30px;">
            If you didn't request this, please ignore this email.
            <br>
            This link will expire in 1 hour.
          </p>
          <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
          <p style="color: #999; font-size: 12px;">
            CastMate - Find Your Next Role
          </p>
        </div>
      </body>
      </html>
    `;
  }

  /**
   * Template email pour les agences
   */
  private getAgencyEmailTemplate(resetLink: string): string {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Réinitialisation Mot de Passe</title>
      </head>
      <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
          <h2 style="color: #1a73e8;">Réinitialisation de Mot de Passe</h2>
          <p>Bonjour,</p>
          <p>Vous avez demandé à réinitialiser le mot de passe de votre compte agence CastMate.</p>
          <p>Cliquez sur le bouton ci-dessous pour réinitialiser votre mot de passe :</p>
          <div style="text-align: center; margin: 30px 0;">
            <a href="${resetLink}" 
               style="background-color: #1a73e8; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
              Réinitialiser le Mot de Passe
            </a>
          </div>
          <p>Ou copiez et collez ce lien dans votre navigateur :</p>
          <p style="word-break: break-all; color: #666;">${resetLink}</p>
          <p style="color: #666; font-size: 14px; margin-top: 30px;">
            Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
            <br>
            Ce lien expirera dans 1 heure.
          </p>
          <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
          <p style="color: #999; font-size: 12px;">
            CastMate - Trouvez Votre Prochain Talent
          </p>
        </div>
      </body>
      </html>
    `;
  }
}
```

#### C. Ajouter les variables d'environnement (`.env`)

```env
# Gmail Configuration (pour envoyer des emails)
GMAIL_USER=votre-email@gmail.com
GMAIL_APP_PASSWORD=abcdefghijklmnop  # Le mot de passe d'application (16 caractères sans espaces)

# Frontend URL (pour les liens de réinitialisation)
FRONTEND_URL=https://votre-app.com
```

#### D. Créer le module d'email (`src/email/email.module.ts`)

```typescript
import { Module } from '@nestjs/common';
import { EmailService } from './email.service';

@Module({
  providers: [EmailService],
  exports: [EmailService],
})
export class EmailModule {}
```

#### E. Ajouter le module dans `app.module.ts`

```typescript
import { EmailModule } from './email/email.module';

@Module({
  imports: [
    // ... autres modules
    EmailModule,
  ],
  // ...
})
export class AppModule {}
```

#### F. Créer le DTO (`src/auth/dto/forgot-password.dto.ts`)

```typescript
import { IsEmail, IsEnum } from 'class-validator';

export class ForgotPasswordDto {
  @IsEmail()
  email: string;

  @IsEnum(['ACTEUR', 'RECRUTEUR'])
  userType: 'ACTEUR' | 'RECRUTEUR';
}
```

#### G. Ajouter l'endpoint dans `auth.controller.ts`

```typescript
import { Controller, Post, Body } from '@nestjs/common';
import { AuthService } from './auth.service';
import { ForgotPasswordDto } from './dto/forgot-password.dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('forgot-password')
  async forgotPassword(@Body() dto: ForgotPasswordDto) {
    await this.authService.forgotPassword(dto.email, dto.userType);
    return {
      message: 'Un email de réinitialisation a été envoyé',
      success: true,
    };
  }
}
```

#### H. Ajouter la méthode dans `auth.service.ts`

```typescript
import { Injectable, NotFoundException } from '@nestjs/common';
import { EmailService } from '../email/email.service';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from './entities/user.entity';
import * as crypto from 'crypto';
import * as bcrypt from 'bcrypt';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private readonly userRepository: Repository<User>,
    private readonly emailService: EmailService,
  ) {}

  /**
   * Gère la demande de réinitialisation de mot de passe
   */
  async forgotPassword(
    email: string,
    userType: 'ACTEUR' | 'RECRUTEUR',
  ): Promise<void> {
    // Chercher l'utilisateur par email et type
    const user = await this.userRepository.findOne({
      where: {
        email: email.toLowerCase(),
        role: { name: userType },
      },
    });

    if (!user) {
      throw new NotFoundException(
        `Aucun compte ${userType.toLowerCase()} trouvé avec cet email`,
      );
    }

    // Générer un token de réinitialisation unique
    const resetToken = crypto.randomBytes(32).toString('hex');
    const resetTokenExpiry = new Date();
    resetTokenExpiry.setHours(resetTokenExpiry.getHours() + 1); // Expire dans 1 heure

    // Sauvegarder le token dans la base de données
    user.resetPasswordToken = resetToken;
    user.resetPasswordExpiry = resetTokenExpiry;
    await this.userRepository.save(user);

    // Envoyer l'email
    await this.emailService.sendPasswordResetEmail(email, resetToken, userType);
  }
}
```

#### I. Ajouter les champs dans l'entité User

```typescript
import { Entity, Column } from 'typeorm';

@Entity()
export class User {
  // ... autres champs existants ...

  @Column({ nullable: true })
  resetPasswordToken?: string;

  @Column({ type: 'timestamp', nullable: true })
  resetPasswordExpiry?: Date;
}
```

---

## ✅ **Résumé**

### **Ce qui est fait (Android)** ✅
- L'app Android appelle l'API `POST /auth/forgot-password`
- Gestion des erreurs et messages clairs

### **Ce qu'il reste à faire (Backend)** ⏳

1. **Créer un mot de passe d'application Gmail** (5 minutes)
2. **Installer `nodemailer`** : `npm install nodemailer`
3. **Créer `EmailService`** avec le code ci-dessus
4. **Créer l'endpoint** `POST /auth/forgot-password`
5. **Ajouter les champs** `resetPasswordToken` et `resetPasswordExpiry` dans l'entité User
6. **Configurer les variables d'environnement** (GMAIL_USER, GMAIL_APP_PASSWORD)

---

## 🔍 **Vérification**

Une fois le backend configuré, testez :

1. **Ouvrez l'app Android**
2. **Allez sur "Forgot Password"**
3. **Entrez un email valide**
4. **Cliquez sur "Submit"**
5. **Vérifiez les logs** dans Android Studio :
   - ✅ `📨 Réponse HTTP 200` = Succès
   - ❌ `📨 Réponse HTTP 404` = Email non trouvé
   - ❌ `📨 Réponse HTTP 500` = Erreur serveur (backend pas encore configuré)
6. **Vérifiez votre boîte Gmail** (et les spams) pour l'email de réinitialisation

---

## ⚠️ **Important**

- Les clés **OAuth 2.0 Client ID** (Sign with Google) **NE PEUVENT PAS** être utilisées pour envoyer des emails
- Vous devez utiliser un **mot de passe d'application Gmail** ou **Service Account** séparé
- Le backend **DOIT** être configuré pour envoyer les emails - ce n'est pas automatique !

---

**Une fois le backend configuré, les emails seront envoyés automatiquement !** 🎉

