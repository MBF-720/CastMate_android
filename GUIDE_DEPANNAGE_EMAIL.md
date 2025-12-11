# 🔧 Guide de Dépannage - Envoi d'Emails

## Problème : Je ne reçois pas l'email de réinitialisation de mot de passe

### ✅ Corrections Apportées

1. **Mot de passe corrigé** : Le mot de passe d'application Gmail contenait des caractères unicode incorrects. Il a été corrigé avec uniquement des caractères ASCII : `pizdboqftoyrnfje`

2. **Logs de débogage ajoutés** : Des logs détaillés ont été ajoutés pour suivre l'envoi de l'email :
   - `EmailSender.kt` : Logs détaillés de la connexion SMTP et de l'envoi
   - `MainActivity.kt` : Logs de l'appel de la fonction d'envoi
   - Debug SMTP activé pour voir les erreurs détaillées

3. **Timeouts configurés** : Timeouts de 10 secondes pour éviter les blocages

### 📱 Comment Tester

1. **Lancer l'application** :
   ```bash
   .\gradlew.bat installDebug
   ```

2. **Aller sur la page "Forgot Password"** (Oublie mot de passe)

3. **Entrer votre email** et cliquer sur le bouton de soumission

4. **Vérifier les logs Android** :
   ```bash
   adb logcat | Select-String "ForgotPassword|EmailSender"
   ```

### 🔍 Logs à Surveiller

Les logs suivants devraient apparaître dans Logcat :

```
📧 Envoi de l'email de réinitialisation à: [email]
⚠️ Utilisation de l'envoi direct depuis Android
📧 Email destinataire: [email]
🎭 Type d'utilisateur: ACTEUR/RECRUTEUR
🔑 Token généré: [token...]
🔐 Authentication: user=castemate4@gmail.com, password=pizd****
📤 Tentative d'envoi de l'email...
✅ Email envoyé avec succès à: [email]
🔗 Lien de réinitialisation: castmate://reset-password?token=...
```

Si vous voyez une erreur `❌`, vérifiez le message d'erreur complet.

### 🚨 Problèmes Courants

#### 1. Erreur d'authentification (`535 Authentication failed`)
**Cause** : Le mot de passe d'application est incorrect ou expiré

**Solution** :
1. Allez sur https://myaccount.google.com/apppasswords
2. Générez un nouveau mot de passe d'application (choisir "Mail" comme type)
3. Copiez le mot de passe (16 caractères sans espaces)
4. Remplacez dans `EmailSender.kt` ligne 31

#### 2. Timeout de connexion (`Connection timed out`)
**Cause** : Problème de réseau ou firewall

**Solution** :
- Vérifiez la connexion Internet de votre appareil
- Assurez-vous que l'application a la permission INTERNET (déjà configurée)
- Testez avec un autre réseau (WiFi vs données mobiles)

#### 3. SSL/TLS Erreur (`SSLHandshakeException`)
**Cause** : Problème de certificat SSL

**Solution** :
- Vérifiez que l'appareil a l'heure correcte (importantes pour les certificats)
- Mettez à jour Android System WebView si possible

#### 4. Email dans les spams
**Cause** : Gmail peut marquer l'email comme spam car il est envoyé depuis un appareil mobile

**Solution** :
- Vérifiez le dossier "Spam" / "Courrier indésirable"
- Ajoutez `castemate4@gmail.com` dans vos contacts
- Marquez l'email comme "Non spam" si trouvé

#### 5. L'email n'arrive pas du tout
**Causes possibles** :
1. Le mot de passe d'application Gmail est incorrect
2. Gmail bloque l'envoi depuis l'appareil
3. L'email destinataire n'existe pas ou est invalide
4. Problème de réseau

**Solutions** :
1. **Vérifier le mot de passe d'application** :
   - Connectez-vous à https://myaccount.google.com/apppasswords
   - Générez un nouveau mot de passe si nécessaire
   
2. **Vérifier les paramètres Gmail** :
   - Assurez-vous que la "validation en 2 étapes" est activée (nécessaire pour les mots de passe d'application)
   - Vérifiez qu'il n'y a pas d'alerte de sécurité sur votre compte Gmail
   
3. **Tester avec un autre email** :
   - Essayez d'envoyer l'email à une autre adresse (Gmail, Outlook, etc.)
   
4. **Vérifier les logs** :
   - Les logs vous indiqueront exactement quelle erreur s'est produite

### 📧 Vérifier Votre Mot de Passe d'Application Gmail

Le mot de passe d'application Gmail doit respecter ces critères :
- ✅ Exactement 16 caractères
- ✅ Uniquement des lettres minuscules et des chiffres
- ✅ Pas d'espaces
- ✅ Format : `xxxx xxxx xxxx xxxx` (avec espaces quand affiché par Google, mais à copier SANS espaces)

**Mot de passe actuel dans le code** : `pizdboqftoyrnfje` (16 caractères)

Si ce mot de passe ne fonctionne pas :
1. Allez sur https://myaccount.google.com/apppasswords
2. Supprimez l'ancien mot de passe d'application "CastMate" (si existant)
3. Créez un nouveau mot de passe d'application
4. Copiez le mot de passe (SANS espaces)
5. Remplacez dans `app/src/main/java/com/example/projecct_mobile/ui/utils/EmailSender.kt` ligne 31

### 🧪 Test Alternatif : Utiliser l'API Backend

Si l'envoi depuis Android ne fonctionne pas, vous pouvez utiliser l'API backend :

Dans `MainActivity.kt` ligne 1721, changez :
```kotlin
val USE_ANDROID_EMAIL_SENDER = true
```
en :
```kotlin
val USE_ANDROID_EMAIL_SENDER = false
```

Cela utilisera l'endpoint backend `/auth/forgot-password` au lieu d'envoyer directement depuis Android.

### 📝 Notes Importantes

1. **Sécurité** : L'envoi d'emails depuis Android avec des credentials exposés est **NON RECOMMANDÉ EN PRODUCTION**. C'est seulement pour le développement/test.

2. **Rate Limiting** : Gmail peut limiter le nombre d'emails envoyés depuis une application mobile. Ne testez pas trop rapidement.

3. **Deep Link** : L'email contient un lien `castmate://reset-password?token=XXX&email=XXX&type=XXX` qui ouvrira directement l'application Android. Ce lien ne fonctionne que si l'application est installée.

4. **Production** : Pour la production, utilisez TOUJOURS le backend pour envoyer les emails, pas l'application Android.

### 🔄 Prochaines Étapes

1. Testez l'envoi d'email avec les corrections appliquées
2. Vérifiez les logs Logcat pour identifier l'erreur exacte
3. Si le mot de passe est incorrect, générez-en un nouveau sur Google
4. Si l'email arrive dans les spams, marquez-le comme non-spam
5. Si rien ne fonctionne, passez à l'option backend (voir ci-dessus)

### 🆘 Besoin d'Aide ?

Si le problème persiste :
1. Partagez les logs complets de Logcat (filtrer par "ForgotPassword" et "EmailSender")
2. Indiquez l'erreur exacte affichée
3. Vérifiez que le compte Gmail `castemate4@gmail.com` n'a pas de problème de sécurité

