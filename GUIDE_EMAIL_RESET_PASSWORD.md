# Guide : Envoi d'Email pour Réinitialisation de Mot de Passe

## 📱 Partie Android (TERMINÉE ✅)

L'application Android appelle maintenant l'endpoint : `POST https://cast-mate.vercel.app/auth/forgot-password`

**Corps de la requête :**
```json
{
  "email": "user@example.com",
  "userType": "ACTEUR" // ou "RECRUTEUR"
}
```

---

## 🔧 Partie Backend NestJS (À IMPLÉMENTER)

### 1️⃣ **Installer les dépendances**

```bash
npm install @nestjs-modules/mailer nodemailer
npm install @types/nodemailer --save-dev
npm install googleapis  # Pour utiliser Gmail API avec Google Cloud Console
```

### 2️⃣ **Configuration Gmail API avec Google Cloud Console**

#### A. Créer un projet Google Cloud (si pas déjà fait)

1. Allez sur https://console.cloud.google.com/
2. Sélectionnez votre projet existant "castemate" (celui utilisé pour Sign with Google)
3. Activez Gmail API :
   - Dans la barre de recherche, cherchez "Gmail API"
   - Cliquez sur "ACTIVER"

#### B. Créer un compte de service

1. Allez dans **APIs & Services** > **Credentials**
2. Cliquez sur **+ CREATE CREDENTIALS** > **Service Account**
3. Remplissez :
   - **Service account name** : `email-service`
   - **Service account ID** : `email-service`
   - Cliquez sur **CREATE AND CONTINUE**
4. Sélectionnez le rôle : **Project > Editor**
5. Cliquez sur **DONE**

#### C. Télécharger la clé JSON

1. Dans la liste des comptes de service, cliquez sur `email-service`
2. Onglet **KEYS**
3. **ADD KEY** > **Create new key**
4. Choisissez **JSON**
5. Téléchargez le fichier (ex: `email-service-key.json`)
6. **IMPORTANT** : Placez ce fichier dans votre backend et ajoutez-le au `.gitignore`

### 3️⃣ **Configuration Backend NestJS**

#### A. Créer le module d'email (`src/email/email.service.ts`)

```typescript
import { Injectable, Logger } from '@nestjs/common';
import { google } from 'googleapis';
import * as nodemailer from 'nodemailer';
import { readFileSync } from 'fs';
import { join } from 'path';

@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);
  private transporter: nodemailer.Transporter;

  constructor() {
    this.initializeGmailTransporter();
  }

  /**
   * Initialise le transporteur Gmail avec OAuth2 et le compte de service
   */
  private async initializeGmailTransporter() {
    try {
      // Charger les credentials du compte de service
      const keyFile = join(__dirname, '../../email-service-key.json');
      const credentials = JSON.parse(readFileSync(keyFile, 'utf-8'));

      // Créer un client OAuth2
      const oauth2Client = new google.auth.JWT(
        credentials.client_email,
        null,
        credentials.private_key,
        ['https://www.googleapis.com/auth/gmail.send'],
      );

      // Obtenir le token d'accès
      const accessToken = await oauth2Client.getAccessToken();

      // Créer le transporteur avec Gmail
      this.transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
          type: 'OAuth2',
          user: 'votre-email@gmail.com', // Remplacer par votre email Gmail
          clientId: credentials.client_id,
          clientSecret: credentials.client_secret,
          refreshToken: null, // Pas nécessaire avec compte de service
          accessToken: accessToken.token,
        },
      });

      this.logger.log('✅ Gmail transporter initialisé avec succès');
    } catch (error) {
      this.logger.error('❌ Erreur initialisation Gmail:', error.message);
      throw error;
    }
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
      const resetLink = `https://votre-app.com/reset-password?token=${resetToken}&type=${userType}`;

      // Texte selon le type d'utilisateur
      const isAgency = userType === 'RECRUTEUR';
      const subject = isAgency
        ? 'Réinitialisation de votre mot de passe - CastMate Agence'
        : 'Password Reset - CastMate Actor';

      const htmlContent = isAgency
        ? this.getAgencyEmailTemplate(resetLink, resetToken)
        : this.getActorEmailTemplate(resetLink, resetToken);

      // Envoyer l'email
      const info = await this.transporter.sendMail({
        from: '"CastMate" <noreply@castmate.com>',
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
  private getActorEmailTemplate(resetLink: string, token: string): string {
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
  private getAgencyEmailTemplate(resetLink: string, token: string): string {
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

#### B. Créer le DTO (`src/auth/dto/forgot-password.dto.ts`)

```typescript
import { IsEmail, IsEnum } from 'class-validator';

export class ForgotPasswordDto {
  @IsEmail()
  email: string;

  @IsEnum(['ACTEUR', 'RECRUTEUR'])
  userType: 'ACTEUR' | 'RECRUTEUR';
}
```

#### C. Mettre à jour le contrôleur (`src/auth/auth.controller.ts`)

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

#### D. Mettre à jour le service (`src/auth/auth.service.ts`)

```typescript
import { Injectable, NotFoundException } from '@nestjs/common';
import { EmailService } from '../email/email.service';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from './entities/user.entity';
import * as crypto from 'crypto';

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

  /**
   * Réinitialise le mot de passe avec le token
   */
  async resetPassword(token: string, newPassword: string): Promise<void> {
    // Chercher l'utilisateur avec le token
    const user = await this.userRepository.findOne({
      where: {
        resetPasswordToken: token,
      },
    });

    if (!user) {
      throw new NotFoundException('Token invalide ou expiré');
    }

    // Vérifier si le token a expiré
    if (user.resetPasswordExpiry < new Date()) {
      throw new NotFoundException('Token expiré');
    }

    // Hasher le nouveau mot de passe (utilisez bcrypt)
    const hashedPassword = await bcrypt.hash(newPassword, 10);

    // Mettre à jour le mot de passe et supprimer le token
    user.password = hashedPassword;
    user.resetPasswordToken = null;
    user.resetPasswordExpiry = null;
    await this.userRepository.save(user);
  }
}
```

#### E. Ajouter les champs dans l'entité User (`src/auth/entities/user.entity.ts`)

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

### 4️⃣ **Variables d'environnement (`.env`)**

```env
# Gmail Configuration
GMAIL_USER=votre-email@gmail.com
GMAIL_CLIENT_ID=votre-client-id.apps.googleusercontent.com
GMAIL_CLIENT_SECRET=votre-client-secret
GMAIL_SERVICE_ACCOUNT_KEY_PATH=./email-service-key.json

# Frontend URL (pour les liens de réinitialisation)
FRONTEND_URL=https://votre-app.com
```

### 5️⃣ **Alternative Simple : Utiliser Gmail avec App Password**

Si vous ne voulez pas utiliser le compte de service, vous pouvez utiliser un mot de passe d'application Gmail :

1. Allez sur https://myaccount.google.com/security
2. Activez la validation en 2 étapes
3. Créez un mot de passe d'application
4. Utilisez ce mot de passe dans votre configuration :

```typescript
this.transporter = nodemailer.createTransporter({
  service: 'gmail',
  auth: {
    user: 'votre-email@gmail.com',
    pass: 'votre-app-password', // 16 caractères généré par Google
  },
});
```

---

## 🔄 Flux Complet

1. **Utilisateur clique "Forgot Password"** dans l'app Android
2. **App envoie** : `POST /auth/forgot-password` avec email et userType
3. **Backend vérifie** si l'email existe pour ce type d'utilisateur
4. **Backend génère** un token unique et l'enregistre en base
5. **Backend envoie** un email avec le lien de réinitialisation
6. **Utilisateur clique** sur le lien dans l'email
7. **App ou Web affiche** un formulaire pour entrer le nouveau mot de passe
8. **Backend vérifie** le token et met à jour le mot de passe

---

## ✅ Résumé

- ✅ **Android** : Appel API implémenté (ne touche pas aux fichiers `data/`)
- ⏳ **Backend** : À implémenter selon ce guide
- 🔑 **Google Cloud** : Utilise les mêmes credentials que Sign with Google

**Note** : Assurez-vous d'ajouter `email-service-key.json` au `.gitignore` pour la sécurité !

