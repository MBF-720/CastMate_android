# 🔐 Guide Complet : Réinitialisation de Mot de Passe pour iOS - CastMate

## 📋 Informations Critiques

**⚠️ IMPORTANT** : L'application **ENVOIE L'EMAIL DIRECTEMENT** depuis l'app (comme Android). Le backend **NE PASSE PAS** l'email. Le backend stocke seulement le token et valide le changement de mot de passe.

---

## 🔑 Credentials Gmail (Pour Envoi d'Emails)

### Compte Gmail
```
Email : castemate4@gmail.com
Mot de passe d'application : pizdboqftoyrnfje
```

**Type** : Mot de passe d'application Gmail (App Password)  
**Usage** : Envoi d'emails SMTP via Gmail  
**⚠️ SÉCURITÉ** : Ces credentials sont exposés dans le code. En production, utilisez un service backend sécurisé.

### Configuration SMTP Gmail
```
Host : smtp.gmail.com
Port : 587
Protocol : STARTTLS
Auth : true
Username : castemate4@gmail.com
Password : pizdboqftoyrnfje
```

---

## 📧 Templates d'Emails

### Template pour Acteurs (Anglais)

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Password Reset</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
        <div style="background: linear-gradient(135deg, #1a73e8 0%, #004db3 100%); padding: 30px; text-align: center;">
            <h1 style="color: white; margin: 0; font-size: 28px;">🎬 CastMate</h1>
        </div>
        <div style="padding: 40px;">
            <h2 style="color: #1a73e8; margin-top: 0;">Password Reset Request</h2>
            <p>Hello,</p>
            <p>You requested to reset your password for your CastMate actor account.</p>
            <p>Click the button below to reset your password:</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="[RESET_LINK]" 
                   style="background-color: #1a73e8; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                    Reset Password
                </a>
            </div>
            <p>Or copy and paste this link in your browser:</p>
            <p style="word-break: break-all; color: #666; background: #f5f5f5; padding: 10px; border-radius: 5px; font-size: 12px;">[RESET_LINK]</p>
            <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee;">
                <p style="color: #999; font-size: 14px; margin: 5px 0;">⚠️ If you didn't request this, please ignore this email.</p>
                <p style="color: #999; font-size: 14px; margin: 5px 0;">⏱️ This link will expire in 1 hour.</p>
            </div>
        </div>
        <div style="background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee;">
            <p style="color: #999; font-size: 12px; margin: 0;">© 2024 CastMate - Find Your Next Role</p>
        </div>
    </div>
</body>
</html>
```

### Template pour Agences (Français)

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Réinitialisation Mot de Passe</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
        <div style="background: linear-gradient(135deg, #1a73e8 0%, #004db3 100%); padding: 30px; text-align: center;">
            <h1 style="color: white; margin: 0; font-size: 28px;">🎬 CastMate</h1>
        </div>
        <div style="padding: 40px;">
            <h2 style="color: #1a73e8; margin-top: 0;">Réinitialisation de Mot de Passe</h2>
            <p>Bonjour,</p>
            <p>Vous avez demandé à réinitialiser le mot de passe de votre compte agence CastMate.</p>
            <p>Cliquez sur le bouton ci-dessous pour réinitialiser votre mot de passe :</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="[RESET_LINK]" 
                   style="background-color: #1a73e8; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                    Réinitialiser le Mot de Passe
                </a>
            </div>
            <p>Ou copiez et collez ce lien dans votre navigateur :</p>
            <p style="word-break: break-all; color: #666; background: #f5f5f5; padding: 10px; border-radius: 5px; font-size: 12px;">[RESET_LINK]</p>
            <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee;">
                <p style="color: #999; font-size: 14px; margin: 5px 0;">⚠️ Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>
                <p style="color: #999; font-size: 14px; margin: 5px 0;">⏱️ Ce lien expirera dans 1 heure.</p>
            </div>
        </div>
        <div style="background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee;">
            <p style="color: #999; font-size: 12px; margin: 0;">© 2024 CastMate - Trouvez Votre Prochain Talent</p>
        </div>
    </div>
</body>
</html>
```

---

## 🔗 Format du Lien de Réinitialisation

### Deep Link iOS (URL Scheme)

```
castmate://reset-password?token=[TOKEN]&email=[EMAIL]&type=[USER_TYPE]
```

**Exemple** :
```
castmate://reset-password?token=550e8400-e29b-41d4-a716-446655440000&email=user@example.com&type=ACTEUR
```

**Paramètres** :
- `token` : Token de réinitialisation (UUID généré par l'app)
- `email` : Email de l'utilisateur
- `type` : `ACTEUR` ou `RECRUTEUR`

### Configuration URL Scheme dans Info.plist

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>com.castemate.resetpassword</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>castmate</string>
        </array>
    </dict>
</array>
```

---

## 🔄 Flux Complet de Réinitialisation

### Étape 1 : Demande de Réinitialisation (Forgot Password)

1. **Utilisateur entre son email** sur la page "Mot de passe oublié"
2. **L'app génère un token localement** (UUID)
3. **L'app envoie le token au backend** via `POST /auth/forgot-password` (optionnel, pour que le backend le stocke)
4. **L'app envoie l'email directement** via SMTP Gmail avec le token
5. **L'email contient un deep link** : `castmate://reset-password?token=XXX&email=XXX&type=XXX`

### Étape 2 : Clic sur le Lien dans l'Email

1. **L'utilisateur clique sur le lien** dans l'email
2. **iOS ouvre l'application** via le deep link `castmate://`
3. **L'app parse les paramètres** : `token`, `email`, `type`
4. **L'app affiche l'écran de réinitialisation** avec les champs pour le nouveau mot de passe

### Étape 3 : Changement de Mot de Passe

1. **Utilisateur entre le nouveau mot de passe** (minimum 8 caractères)
2. **L'app appelle** `POST /auth/reset-password` avec :
   - `token` : Le token du lien
   - `newPassword` : Le nouveau mot de passe
   - `email` : L'email de l'utilisateur
3. **Le backend valide le token** et change le mot de passe
4. **L'app affiche un message de succès** et redirige vers la page de login

---

## 🌐 Endpoints API Backend

### Base URL
```
https://cast-mate.vercel.app
```

### 1. Demande de Réinitialisation

```
POST /auth/forgot-password
```

**Headers** :
```
Content-Type: application/json
```

**Body** :
```json
{
  "email": "user@example.com",
  "userType": "ACTEUR",  // ou "RECRUTEUR"
  "token": "550e8400-e29b-41d4-a716-446655440000"  // Optionnel : token généré par l'app
}
```

**Réponse Succès (200)** :
```json
{
  "success": true,
  "message": "Reset email sent",
  "token": "550e8400-e29b-41d4-a716-446655440000",  // Optionnel
  "link": "castmate://reset-password?token=...&email=...&type=...",  // Optionnel
  "expiresIn": 3600  // Optionnel, en secondes
}
```

**Réponse Erreur (404)** :
```json
{
  "message": "Compte non trouvé"
}
```

**Réponse Erreur (429)** :
```json
{
  "message": "Trop de demandes. Veuillez réessayer plus tard."
}
```

**⚠️ IMPORTANT** : L'app iOS doit **TOUJOURS** envoyer l'email, même si l'appel API échoue. L'appel API est **non-bloquant**.

### 2. Application de la Réinitialisation

```
POST /auth/reset-password
```

**Headers** :
```
Content-Type: application/json
```

**Body** :
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPassword123!",
  "email": "user@example.com"
}
```

**Réponse Succès (200)** :
```json
{
  "success": true,
  "message": "Password updated"
}
```

**Réponse Erreur (400)** :
```json
{
  "message": "Token invalide ou expiré, ou mot de passe non conforme"
}
```

**Réponse Erreur (404)** :
```json
{
  "message": "Token non trouvé ou expiré"
}
```

---

## 💻 Code Swift - Implémentation Complète

### 1. Gestionnaire d'Email (SMTP)

```swift
import Foundation
import MailCore2  // Ou utiliser une autre bibliothèque SMTP

class EmailSender {
    private static let gmailUser = "castemate4@gmail.com"
    private static let gmailAppPassword = "pizdboqftoyrnfje"
    private static let smtpHost = "smtp.gmail.com"
    private static let smtpPort: UInt32 = 587
    
    static func sendPasswordResetEmail(
        recipientEmail: String,
        userType: String,
        resetToken: String
    ) async throws {
        // Générer le deep link
        let resetLink = "castmate://reset-password?token=\(resetToken)&email=\(recipientEmail.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? recipientEmail)&type=\(userType)"
        
        let isAgency = userType.uppercased() == "RECRUTEUR"
        let subject = isAgency 
            ? "Réinitialisation de votre mot de passe - CastMate Agence"
            : "Password Reset - CastMate Actor"
        
        let htmlContent = isAgency
            ? getAgencyEmailTemplate(resetLink: resetLink)
            : getActorEmailTemplate(resetLink: resetLink)
        
        // Configuration SMTP
        let smtpSession = MCOSMTPSession()
        smtpSession.hostname = smtpHost
        smtpSession.port = smtpPort
        smtpSession.username = gmailUser
        smtpSession.password = gmailAppPassword
        smtpSession.authType = .saslPlain
        smtpSession.connectionType = .startTLS
        
        // Créer le message
        let builder = MCOMessageBuilder()
        builder.header.from = MCOAddress(displayName: "CastMate", mailbox: gmailUser)
        builder.header.to = [MCOAddress(mailbox: recipientEmail)]
        builder.header.subject = subject
        builder.htmlBody = htmlContent
        
        // Envoyer l'email
        let sendOperation = smtpSession.sendOperation(with: builder.data())
        try await sendOperation?.start()
    }
    
    private static func getActorEmailTemplate(resetLink: String) -> String {
        // Utiliser le template HTML pour acteur (voir section Templates)
        return """
        <!DOCTYPE html>
        <html>
        ...
        [Utiliser le template HTML fourni ci-dessus]
        ...
        </html>
        """
    }
    
    private static func getAgencyEmailTemplate(resetLink: String) -> String {
        // Utiliser le template HTML pour agence (voir section Templates)
        return """
        <!DOCTYPE html>
        <html>
        ...
        [Utiliser le template HTML fourni ci-dessus]
        ...
        </html>
        """
    }
}
```

### 2. Génération de Token

```swift
import Foundation

extension UUID {
    static func generateResetToken() -> String {
        return UUID().uuidString
    }
}
```

### 3. Stockage Local du Token

```swift
import Foundation

class TokenManager {
    private let userDefaults = UserDefaults.standard
    private let tokenKeyPrefix = "reset_token_"
    private let tokenExpirationKeyPrefix = "reset_token_exp_"
    
    // Stocker le token avec expiration (1 heure)
    func saveResetToken(email: String, token: String) {
        let key = "\(tokenKeyPrefix)\(email)"
        let expirationKey = "\(tokenExpirationKeyPrefix)\(email)"
        let expirationDate = Date().addingTimeInterval(3600) // 1 heure
        
        userDefaults.set(token, forKey: key)
        userDefaults.set(expirationDate, forKey: expirationKey)
    }
    
    // Récupérer le token (vérifie l'expiration)
    func getResetToken(email: String) -> String? {
        let key = "\(tokenKeyPrefix)\(email)"
        let expirationKey = "\(tokenExpirationKeyPrefix)\(email)"
        
        guard let token = userDefaults.string(forKey: key),
              let expirationDate = userDefaults.object(forKey: expirationKey) as? Date,
              expirationDate > Date() else {
            // Token expiré ou inexistant
            clearResetToken(email: email)
            return nil
        }
        
        return token
    }
    
    // Supprimer le token
    func clearResetToken(email: String) {
        let key = "\(tokenKeyPrefix)\(email)"
        let expirationKey = "\(tokenExpirationKeyPrefix)\(email)"
        
        userDefaults.removeObject(forKey: key)
        userDefaults.removeObject(forKey: expirationKey)
    }
}
```

### 4. Service API

```swift
import Foundation

struct ForgotPasswordRequest: Codable {
    let email: String
    let userType: String  // "ACTEUR" ou "RECRUTEUR"
    let token: String?  // Optionnel
}

struct ForgotPasswordResponse: Codable {
    let success: Bool
    let message: String
    let token: String?
    let link: String?
    let expiresIn: Int?
}

struct ResetPasswordRequest: Codable {
    let token: String
    let newPassword: String
    let email: String
}

struct ResetPasswordResponse: Codable {
    let success: Bool
    let message: String
}

class AuthService {
    private let baseURL = "https://cast-mate.vercel.app"
    
    // Demande de réinitialisation
    func forgotPassword(email: String, userType: String, token: String?) async throws -> ForgotPasswordResponse {
        let url = URL(string: "\(baseURL)/auth/forgot-password")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ForgotPasswordRequest(email: email, userType: userType, token: token)
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw AuthError.invalidResponse
        }
        
        if httpResponse.statusCode == 404 {
            throw AuthError.accountNotFound
        }
        
        if httpResponse.statusCode == 429 {
            throw AuthError.tooManyRequests
        }
        
        if httpResponse.statusCode != 200 {
            throw AuthError.requestFailed
        }
        
        return try JSONDecoder().decode(ForgotPasswordResponse.self, from: data)
    }
    
    // Application de la réinitialisation
    func resetPassword(token: String, newPassword: String, email: String) async throws -> ResetPasswordResponse {
        let url = URL(string: "\(baseURL)/auth/reset-password")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ResetPasswordRequest(token: token, newPassword: newPassword, email: email)
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw AuthError.invalidResponse
        }
        
        if httpResponse.statusCode == 400 {
            throw AuthError.invalidTokenOrPassword
        }
        
        if httpResponse.statusCode == 404 {
            throw AuthError.tokenNotFound
        }
        
        if httpResponse.statusCode != 200 {
            throw AuthError.resetFailed
        }
        
        return try JSONDecoder().decode(ResetPasswordResponse.self, from: data)
    }
}

enum AuthError: Error {
    case invalidResponse
    case accountNotFound
    case tooManyRequests
    case requestFailed
    case invalidTokenOrPassword
    case tokenNotFound
    case resetFailed
}
```

### 5. Gestion des Deep Links

```swift
import SwiftUI

class DeepLinkManager: ObservableObject {
    @Published var resetPasswordToken: String?
    @Published var resetPasswordEmail: String?
    @Published var resetPasswordType: String?
    
    func handleURL(_ url: URL) {
        guard url.scheme == "castmate",
              url.host == "reset-password" else {
            return
        }
        
        // Parser les paramètres de la query string
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        let queryItems = components?.queryItems
        
        resetPasswordToken = queryItems?.first(where: { $0.name == "token" })?.value
        resetPasswordEmail = queryItems?.first(where: { $0.name == "email" })?.value
        resetPasswordType = queryItems?.first(where: { $0.name == "type" })?.value
    }
}

// Dans App.swift ou SceneDelegate
@main
struct CastMateApp: App {
    @StateObject private var deepLinkManager = DeepLinkManager()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(deepLinkManager)
                .onOpenURL { url in
                    deepLinkManager.handleURL(url)
                }
        }
    }
}
```

### 6. Écran "Mot de Passe Oublié"

```swift
import SwiftUI

struct ForgotPasswordScreen: View {
    @State private var email: String = ""
    @State private var isLoading: Bool = false
    @State private var errorMessage: String?
    @State private var successMessage: String?
    @State private var userType: String  // "ACTEUR" ou "RECRUTEUR"
    
    private let authService = AuthService()
    private let tokenManager = TokenManager()
    
    var body: some View {
        VStack(spacing: 20) {
            Text(userType == "RECRUTEUR" ? "Mot de passe oublié ?" : "Forgot Password?")
                .font(.largeTitle)
                .bold()
            
            TextField(userType == "RECRUTEUR" ? "Email" : "Email", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
            
            if let error = errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            if let success = successMessage {
                Text(success)
                    .foregroundColor(.green)
                    .font(.caption)
            }
            
            Button(action: {
                Task {
                    await requestPasswordReset()
                }
            }) {
                if isLoading {
                    ProgressView()
                } else {
                    Text(userType == "RECRUTEUR" ? "Envoyer" : "Send")
                }
            }
            .disabled(isLoading || email.isEmpty)
        }
        .padding()
    }
    
    private func requestPasswordReset() async {
        isLoading = true
        errorMessage = nil
        successMessage = nil
        
        do {
            // Générer un token localement
            let resetToken = UUID.generateResetToken()
            
            // Stocker le token localement (expiration 1 heure)
            tokenManager.saveResetToken(email: email, token: resetToken)
            
            // Essayer d'envoyer le token au backend (non-bloquant)
            Task {
                do {
                    _ = try await authService.forgotPassword(
                        email: email,
                        userType: userType,
                        token: resetToken
                    )
                } catch {
                    // Le backend n'a pas accepté le token, mais on continue quand même
                    print("⚠️ Backend n'a pas accepté le token: \(error)")
                }
            }
            
            // TOUJOURS envoyer l'email, même si le backend a échoué
            try await EmailSender.sendPasswordResetEmail(
                recipientEmail: email,
                userType: userType,
                resetToken: resetToken
            )
            
            isLoading = false
            successMessage = userType == "RECRUTEUR"
                ? "Un email de réinitialisation a été envoyé à \(email). Vérifiez votre boîte de réception (et les spams)."
                : "A reset email has been sent to \(email). Check your inbox (and spam folder)."
            
        } catch {
            isLoading = false
            errorMessage = userType == "RECRUTEUR"
                ? "Erreur lors de l'envoi de l'email: \(error.localizedDescription)"
                : "Error sending email: \(error.localizedDescription)"
        }
    }
}
```

### 7. Écran de Réinitialisation (Depuis Deep Link)

```swift
import SwiftUI

struct ResetPasswordScreen: View {
    let token: String
    let email: String
    let userType: String
    
    @State private var newPassword: String = ""
    @State private var confirmPassword: String = ""
    @State private var isLoading: Bool = false
    @State private var errorMessage: String?
    @State private var successMessage: String?
    
    private let authService = AuthService()
    private let tokenManager = TokenManager()
    
    var body: some View {
        VStack(spacing: 20) {
            Text(userType == "RECRUTEUR" ? "Réinitialiser le mot de passe" : "Reset Password")
                .font(.largeTitle)
                .bold()
            
            SecureField(userType == "RECRUTEUR" ? "Nouveau mot de passe" : "New Password", text: $newPassword)
                .textFieldStyle(RoundedBorderTextFieldStyle())
            
            SecureField(userType == "RECRUTEUR" ? "Confirmer le mot de passe" : "Confirm Password", text: $confirmPassword)
                .textFieldStyle(RoundedBorderTextFieldStyle())
            
            if let error = errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            if let success = successMessage {
                Text(success)
                    .foregroundColor(.green)
                    .font(.caption)
            }
            
            Button(action: {
                Task {
                    await resetPassword()
                }
            }) {
                if isLoading {
                    ProgressView()
                } else {
                    Text(userType == "RECRUTEUR" ? "Réinitialiser" : "Reset")
                }
            }
            .disabled(isLoading || newPassword.isEmpty || confirmPassword.isEmpty)
        }
        .padding()
    }
    
    private func resetPassword() async {
        // Validation
        guard newPassword.count >= 8 else {
            errorMessage = userType == "RECRUTEUR"
                ? "Le mot de passe doit contenir au moins 8 caractères"
                : "Password must be at least 8 characters"
            return
        }
        
        guard newPassword == confirmPassword else {
            errorMessage = userType == "RECRUTEUR"
                ? "Les mots de passe ne correspondent pas"
                : "Passwords do not match"
            return
        }
        
        isLoading = true
        errorMessage = nil
        successMessage = nil
        
        do {
            let response = try await authService.resetPassword(
                token: token,
                newPassword: newPassword,
                email: email
            )
            
            // Supprimer le token stocké localement
            tokenManager.clearResetToken(email: email)
            
            isLoading = false
            successMessage = userType == "RECRUTEUR"
                ? "Mot de passe changé avec succès ! Redirection vers la connexion..."
                : "Password changed successfully! Redirecting to login..."
            
            // Attendre 2 secondes puis rediriger
            try await Task.sleep(nanoseconds: 2_000_000_000)
            // Rediriger vers la page de login
            
        } catch AuthError.invalidTokenOrPassword {
            isLoading = false
            errorMessage = userType == "RECRUTEUR"
                ? "Token invalide ou expiré, ou mot de passe non conforme (minimum 8 caractères)."
                : "Invalid or expired token, or password does not meet requirements (minimum 8 characters)."
        } catch AuthError.tokenNotFound {
            isLoading = false
            errorMessage = userType == "RECRUTEUR"
                ? "Token non trouvé ou expiré. Veuillez demander un nouveau lien de réinitialisation."
                : "Token not found or expired. Please request a new reset link."
        } catch {
            isLoading = false
            errorMessage = userType == "RECRUTEUR"
                ? "Erreur lors du changement de mot de passe: \(error.localizedDescription)"
                : "Error changing password: \(error.localizedDescription)"
        }
        
        // Supprimer le token même en cas d'erreur
        tokenManager.clearResetToken(email: email)
    }
}
```

---

## 📦 Dépendances iOS

### Option 1 : MailCore2 (Recommandé)

**CocoaPods** :
```ruby
pod 'mailcore2-ios'
```

**Swift Package Manager** :
```
https://github.com/MailCore/mailcore2
```

### Option 2 : SwiftSMTP

**Swift Package Manager** :
```
https://github.com/Kitura/Swift-SMTP
```

### Option 3 : SendGrid (Alternative Cloud)

Si vous préférez utiliser un service cloud au lieu de SMTP direct :
```
https://github.com/sendgrid/sendgrid-swift
```

---

## ⚠️ Points Importants

1. **L'app envoie l'email** : L'application iOS doit envoyer l'email directement via SMTP Gmail, **PAS** le backend.

2. **Token généré localement** : Le token est généré par l'app iOS (UUID), pas par le backend.

3. **Appel API non-bloquant** : L'appel à `POST /auth/forgot-password` est **non-bloquant**. L'email doit être envoyé même si l'appel API échoue.

4. **Stockage local du token** : Le token doit être stocké localement (UserDefaults ou Keychain) avec une expiration de 1 heure.

5. **Deep Link obligatoire** : Le lien dans l'email doit utiliser le format `castmate://reset-password?token=XXX&email=XXX&type=XXX`.

6. **Deux types d'utilisateurs** : Gérer les deux types : `ACTEUR` (anglais) et `RECRUTEUR` (français).

7. **Validation du mot de passe** : Minimum 8 caractères.

8. **Gestion des erreurs** :
   - **404** : Compte non trouvé → Afficher message d'erreur
   - **429** : Trop de demandes → Afficher message "Réessayer plus tard"
   - **400** : Token invalide ou mot de passe non conforme
   - **404 sur reset** : Token non trouvé ou expiré

---

## ✅ Checklist Implémentation

- [ ] Configurer URL Scheme `castmate://` dans `Info.plist`
- [ ] Ajouter bibliothèque SMTP (MailCore2 ou autre)
- [ ] Implémenter `EmailSender` avec credentials Gmail
- [ ] Implémenter génération de token (UUID)
- [ ] Implémenter `TokenManager` pour stockage local
- [ ] Implémenter `AuthService` pour appels API
- [ ] Implémenter gestion des deep links
- [ ] Créer écran "Mot de passe oublié"
- [ ] Créer écran "Réinitialisation de mot de passe"
- [ ] Tester envoi d'email pour Acteur
- [ ] Tester envoi d'email pour Agence
- [ ] Tester deep link depuis email
- [ ] Tester changement de mot de passe
- [ ] Gérer les erreurs (404, 429, 400)

---

## 🆘 Support

En cas de problème :
1. Vérifier que les credentials Gmail sont corrects
2. Vérifier que le port SMTP 587 n'est pas bloqué
3. Vérifier que l'URL Scheme est bien configuré dans `Info.plist`
4. Vérifier que le token est bien généré et stocké
5. Vérifier que le deep link est bien parsé

---

**Date de création** : 2025-11-16  
**Version** : 1.0  
**Projet** : CastMate iOS  
**Note** : L'application Android envoie l'email directement. Le backend ne passe pas l'email.


