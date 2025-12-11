package com.example.projecct_mobile.ui.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * ⚠️ ATTENTION : NE PAS UTILISER EN PRODUCTION ! ⚠️
 * 
 * Ce service envoie des emails directement depuis l'application Android.
 * 
 * PROBLÈMES DE SÉCURITÉ :
 * - Les credentials Gmail sont EXPOSÉS dans le code
 * - N'importe qui peut décompiler l'APK et voler vos credentials
 * - Risque d'abus et de spam
 * - Pas de validation sécurisée (pas de token)
 * 
 * RECOMMANDATION : Utilisez le backend pour envoyer les emails !
 */
class EmailSender {
    
    companion object {
        private const val TAG = "EmailSender"
        
        // ⚠️ CREDENTIALS EXPOSÉS - NE PAS UTILISER EN PRODUCTION
        private const val GMAIL_USER = "castemate4@gmail.com"
        private const val GMAIL_APP_PASSWORD = "pizdboqftoyrnfje" // Sans espaces - Mot de passe d'application Gmail
        
        /**
         * Envoie un email de réinitialisation de mot de passe
         * 
         * @param recipientEmail Email du destinataire
         * @param userType Type d'utilisateur (ACTEUR ou RECRUTEUR)
         * @param resetToken Token de réinitialisation (généré localement - PAS SÉCURISÉ)
         * @return Result<String> Succès ou erreur
         */
        suspend fun sendPasswordResetEmail(
            recipientEmail: String,
            userType: String,
            resetToken: String
        ): Result<String> = withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📧 Envoi d'email de réinitialisation à: $recipientEmail")
                
                // Configuration des propriétés SMTP pour Gmail
                val properties = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.starttls.required", "true")
                    put("mail.smtp.ssl.protocols", "TLSv1.2")
                    put("mail.smtp.connectiontimeout", "10000")
                    put("mail.smtp.timeout", "10000")
                    put("mail.smtp.writetimeout", "10000")
                    // Activer le debug pour voir les logs SMTP
                    put("mail.debug", "true")
                }
                
                // Créer la session avec authentification
                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        Log.d(TAG, "🔐 Authentication: user=$GMAIL_USER, password=${GMAIL_APP_PASSWORD.take(4)}****")
                        return PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASSWORD)
                    }
                })
                
                // Activer le debug de la session pour voir les erreurs détaillées
                session.debug = true
                
                // Préparer le contenu de l'email
                val isAgency = userType.equals("RECRUTEUR", ignoreCase = true)
                val subject = if (isAgency) {
                    "Réinitialisation de votre mot de passe - CastMate Agence"
                } else {
                    "Password Reset - CastMate Actor"
                }
                
                // Lien de réinitialisation - utilise un deep link Android pour ouvrir l'app
                // Format: castmate://reset-password?token=XXX&email=XXX&type=XXX
                val resetLink = "castmate://reset-password?token=$resetToken&email=$recipientEmail&type=$userType"
                
                // Alternative: Lien web qui redirige vers l'app (si vous avez une page web)
                // val resetLink = "https://cast-mate.vercel.app/reset-password?token=$resetToken&email=$recipientEmail&type=$userType"
                
                val htmlContent = if (isAgency) {
                    getAgencyEmailTemplate(resetLink)
                } else {
                    getActorEmailTemplate(resetLink)
                }
                
                // Créer le message
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(GMAIL_USER, "CastMate"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    this.subject = subject
                    setContent(htmlContent, "text/html; charset=utf-8")
                }
                
                // Envoyer l'email
                Log.d(TAG, "📤 Tentative d'envoi de l'email...")
                Transport.send(message)
                
                Log.d(TAG, "✅ Email envoyé avec succès à: $recipientEmail")
                Log.d(TAG, "🔗 Lien de réinitialisation: $resetLink")
                Result.success("Email envoyé avec succès")
                
            } catch (e: MessagingException) {
                Log.e(TAG, "❌ Erreur d'envoi d'email: ${e.message}", e)
                Result.failure(Exception("Erreur lors de l'envoi de l'email: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur inattendue: ${e.message}", e)
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            }
        }
        
        /**
         * Génère un token simple (NON SÉCURISÉ - pour test uniquement)
         */
        fun generateResetToken(): String {
            return java.util.UUID.randomUUID().toString()
        }
        
        /**
         * Template email pour les acteurs
         */
        private fun getActorEmailTemplate(resetLink: String): String {
            return """
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
                                <a href="$resetLink" 
                                   style="background-color: #1a73e8; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                                    Reset Password
                                </a>
                            </div>
                            <p>Or copy and paste this link in your browser:</p>
                            <p style="word-break: break-all; color: #666; background: #f5f5f5; padding: 10px; border-radius: 5px; font-size: 12px;">$resetLink</p>
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
            """.trimIndent()
        }
        
        /**
         * Template email pour les agences
         */
        private fun getAgencyEmailTemplate(resetLink: String): String {
            return """
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
                                <a href="$resetLink" 
                                   style="background-color: #1a73e8; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                                    Réinitialiser le Mot de Passe
                                </a>
                            </div>
                            <p>Ou copiez et collez ce lien dans votre navigateur :</p>
                            <p style="word-break: break-all; color: #666; background: #f5f5f5; padding: 10px; border-radius: 5px; font-size: 12px;">$resetLink</p>
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
            """.trimIndent()
        }
    }
}

