# 🔐 Guide Complet : Configuration Google Sign-In pour Android

## 📋 Résumé : Quels Clients OAuth Utiliser ?

Pour Google Sign-In sur Android, vous avez besoin de **DEUX clients OAuth** :

1. **Client Android** (Type: Android) - Pour identifier votre application
2. **Client Web OAuth 2.0** (Type: Application Web) - Pour obtenir l'ID token (authentification backend)

---

## 🎯 Partie 1 : Configuration dans Google Cloud Console

### Étape 1 : Activer l'API Google Sign-In

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Sélectionnez votre projet **"CasteMate"**
3. Dans le menu latéral, cliquez sur **"APIs & Services"** → **"Library"**
4. Recherchez **"Google Sign-In API"** ou **"Identity Toolkit API"**
5. Cliquez sur **"Enable"** (Activer)

### Étape 2 : Obtenir le SHA-1 de votre Keystore

**Pour Debug (développement) :**
```bash
# Windows (PowerShell)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Trouvez la ligne "SHA1:" et copiez la valeur
# Exemple : SHA1: 41:28:AD:20:4A:65:D9:C1:46:F2:21:CC:89:EE:1F:BF:6C:50:0B:9B
```

**Pour Release (production) :**
```bash
keytool -list -v -keystore "chemin/vers/votre/keystore.jks" -alias votre_alias
```

### Étape 3 : Créer le Client OAuth Android

1. Dans Google Cloud Console, allez sur **"APIs & Services"** → **"Credentials"**
2. Cliquez sur **"+ Créer des identifiants"** → **"ID client OAuth"**
3. Sélectionnez **"Android"**
4. Remplissez :
   - **Nom** : `CasteMate Android` (ou votre nom)
   - **Nom du package** : `com.example.projecct_mobile`
   - **Empreinte SHA-1** : Collez votre SHA-1 obtenu à l'étape 2
5. Cliquez sur **"Créer"**
6. **IMPORTANT** : Notez le **Client ID** (ex: `873587147400-3htc...`)

### Étape 4 : Créer le Client OAuth Web (pour ID Token)

1. Toujours dans **"Credentials"**, cliquez sur **"+ Créer des identifiants"** → **"ID client OAuth"**
2. Sélectionnez **"Application Web"**
3. Remplissez :
   - **Nom** : `CasteMate Web` (ou votre nom)
   - **Origines JavaScript autorisées** : Laissez vide pour Android
   - **URI de redirection autorisés** : Laissez vide pour Android
4. Cliquez sur **"Créer"**
5. **IMPORTANT** : Notez le **Client ID Web** (ex: `873587147400-icf3...`)

---

## 🔧 Partie 2 : Configuration dans l'Application Android

### Étape 5 : Ajouter le Client ID Web dans strings.xml

Le Client ID Web doit être stocké dans `strings.xml` pour être utilisé dans le code.

**Fichier :** `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">CastMate</string>
    <string name="default_web_client_id">VOTRE_CLIENT_ID_WEB_ICI</string>
</resources>
```

**Remplacez** `VOTRE_CLIENT_ID_WEB_ICI` par votre Client ID Web de l'étape 4.

### Étape 6 : Modifier GoogleAuthUiClient.kt

Le code doit utiliser le Client ID Web pour obtenir l'ID token.

**Fichier :** `app/src/main/java/com/example/projecct_mobile/data/utils/GoogleAuthUiClient.kt`

Le code doit inclure `requestIdToken()` avec le Client ID Web.

---

## ✅ Partie 3 : Vérification

### Étape 7 : Vérifier la Configuration

1. Assurez-vous que votre **package name** dans `build.gradle.kts` correspond à celui dans Google Cloud Console :
   ```kotlin
   applicationId = "com.example.projecct_mobile"
   ```

2. Vérifiez que le **SHA-1** dans Google Cloud Console correspond à votre keystore

3. Vérifiez que le **Client ID Web** est correctement configuré dans `strings.xml`

### Étape 8 : Tester

1. Compilez et lancez l'application
2. Cliquez sur "Continuer avec Google"
3. Connectez-vous avec votre compte Google
4. Vérifiez que l'authentification fonctionne

---

## 🔍 Troubleshooting

### Erreur : "10: " (Developer Error)
- Vérifiez que le package name correspond
- Vérifiez que le SHA-1 correspond
- Attendez 5-10 minutes après avoir créé le client OAuth

### Erreur : "12501" (Sign-in cancelled)
- L'utilisateur a annulé la connexion (normal)

### Erreur : "12500" (Sign-in failed)
- Vérifiez que l'API Google Sign-In est activée
- Vérifiez les credentials OAuth

---

## 📝 Résumé des Identifiants Nécessaires

1. **Client ID Android** : Identifie votre app Android (détecté automatiquement)
2. **Client ID Web** : Pour obtenir l'ID token (à configurer dans `strings.xml`)
3. **SHA-1** : Empreinte du keystore (pour client Android)
4. **Package Name** : `com.example.projecct_mobile`

---

## 🚀 Prochaines Étapes

Une fois configuré, le code dans `GoogleAuthUiClient.kt` utilisera automatiquement :
- Le client Android pour identifier l'app
- Le client Web pour obtenir l'ID token

L'ID token peut ensuite être envoyé à votre backend pour authentification.

