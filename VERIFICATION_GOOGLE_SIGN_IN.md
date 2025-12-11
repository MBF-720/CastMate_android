# 🔍 Vérification Google Sign-In - Erreur 10

## 📋 Informations de Votre Application

### SHA-1 de Debug (OBLIGATOIRE dans Google Cloud Console)
```
ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B
```

### Package Name
```
com.example.projecct_mobile
```

### Client ID Web (déjà configuré dans strings.xml)
```
873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com
```

---

## ✅ Checklist de Vérification dans Google Cloud Console

### 1. Vérifier le Client OAuth Android

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Sélectionnez le projet **"CasteMate"**
3. Allez dans **"APIs & Services"** → **"Credentials"**
4. **Cherchez un Client OAuth de type "Android"**

#### Si le Client OAuth Android EXISTE :

Cliquez dessus et vérifiez :

- [ ] **Nom du package** : Doit être exactement `com.example.projecct_mobile`
- [ ] **SHA-1** : Doit contenir `ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B`
- [ ] **Client ID** : Notez-le (format: `873587147400-xxxxx.apps.googleusercontent.com`)

**⚠️ IMPORTANT** : Si le SHA-1 est différent ou manquant, **MODIFIEZ-LE** :
1. Cliquez sur **"Modifier"** (icône crayon)
2. Dans **"Empreintes SHA-1"**, ajoutez ou modifiez :
   ```
   ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B
   ```
3. Cliquez sur **"Enregistrer"**
4. **ATTENDEZ 10-15 MINUTES** pour la propagation

#### Si le Client OAuth Android N'EXISTE PAS :

**CRÉEZ-LE IMMÉDIATEMENT** :

1. Cliquez sur **"+ Créer des identifiants"** → **"ID client OAuth"**
2. Sélectionnez **"Android"**
3. Remplissez :
   - **Nom** : `CasteMate Android`
   - **Nom du package** : `com.example.projecct_mobile`
   - **Empreinte SHA-1** : `ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B`
4. Cliquez sur **"Créer"**
5. **ATTENDEZ 10-15 MINUTES** pour la propagation

---

### 2. Vérifier le Client OAuth Web

1. Toujours dans **"Credentials"**
2. **Cherchez un Client OAuth de type "Application Web"**
3. Vérifiez que le **Client ID** est :
   ```
   873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com
   ```

**✅ Ce Client ID est déjà configuré dans votre `strings.xml`**

---

### 3. Vérifier que l'API Google Sign-In est Activée

1. Allez dans **"APIs & Services"** → **"Library"**
2. Recherchez **"Google Sign-In API"** ou **"Identity Toolkit API"**
3. Vérifiez que l'API est **activée** (bouton "Manage" visible = activée)
4. Si **non activée**, cliquez sur **"Enable"**

---

## 🔧 Actions Correctives

### Si le SHA-1 est incorrect dans Google Cloud Console :

1. **Modifiez le Client OAuth Android** :
   - Cliquez sur le client Android
   - Cliquez sur **"Modifier"** (icône crayon)
   - Dans **"Empreintes SHA-1"**, supprimez l'ancien et ajoutez :
     ```
     ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B
     ```
   - Cliquez sur **"Enregistrer"**

2. **ATTENDEZ 10-15 MINUTES** après la modification

3. **Désinstallez complètement** l'application de votre appareil/émulateur

4. **Réinstallez** l'application

5. **Testez** Google Sign-In

---

### Si le Package Name est incorrect :

1. **Vérifiez dans `build.gradle.kts`** :
   ```kotlin
   applicationId = "com.example.projecct_mobile"
   ```

2. **Vérifiez dans Google Cloud Console** :
   - Le package name doit être **exactement** `com.example.projecct_mobile`
   - Pas d'espaces, pas de majuscules

3. **Si différent**, modifiez le Client OAuth Android dans Google Cloud Console

---

## 🧪 Test Après Correction

1. **Attendez 10-15 minutes** après toute modification dans Google Cloud Console
2. **Désinstallez complètement** l'application
3. **Réinstallez** l'application
4. **Testez** Google Sign-In
5. **Vérifiez les logs** dans Logcat avec le filtre `GoogleSignIn`

---

## 📝 Résumé des Vérifications

- [ ] Client OAuth Android existe dans Google Cloud Console
- [ ] Package name dans Google Cloud Console = `com.example.projecct_mobile`
- [ ] SHA-1 dans Google Cloud Console = `ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B`
- [ ] Client OAuth Web existe avec ID = `873587147400-icf35npmrbm9m47aprejgo8l67clohvd.apps.googleusercontent.com`
- [ ] API Google Sign-In activée
- [ ] Attendu 10-15 minutes après modifications
- [ ] Application désinstallée et réinstallée
- [ ] Test effectué

---

## 🆘 Si l'Erreur Persiste

1. **Vérifiez les logs détaillés** dans Logcat :
   ```
   Filter: GoogleSignIn
   ```

2. **Vérifiez que vous utilisez le bon keystore** :
   - Debug : `%USERPROFILE%\.android\debug.keystore`
   - Le SHA-1 doit correspondre à celui dans Google Cloud Console

3. **Vérifiez que le Client ID Web est correct** :
   - Dans Google Cloud Console → Credentials
   - Trouvez le client "Application Web"
   - Vérifiez que l'ID correspond à celui dans `strings.xml`

4. **Essayez de créer un nouveau Client OAuth Android** :
   - Supprimez l'ancien (si possible)
   - Créez-en un nouveau avec les bonnes valeurs
   - Attendez 15 minutes

---

**Date de création** : 2025-01-16  
**SHA-1 Debug** : `ED:32:45:0C:8B:44:58:1B:6A:F9:21:7B:66:2B:D3:CD:DD:D5:44:2B`

