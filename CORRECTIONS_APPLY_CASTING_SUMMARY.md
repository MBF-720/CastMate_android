# ✅ Correction terminée - Erreur 403 Apply Casting

**Date:** 2025-12-14  
**Statut:** ✅ Code corrigé et prêt pour les tests

---

## 🔧 Corrections effectuées

### 1. Message d'erreur 403 corrigé (`AuthInterceptor.kt`)

**Fichier:** `app/src/main/java/com/example/projecct_mobile/data/api/AuthInterceptor.kt`

**Changement :**
```kotlin
// Avant : Affichait toujours "RECRUTEUR ou ADMIN requis"
android.util.Log.e("AuthInterceptor", "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (RECRUTEUR ou ADMIN requis)")

// Après : Détecte l'endpoint et affiche le message approprié
val errorMessage = when {
    // Candidature à un casting - nécessite ACTEUR
    requestUrl.contains("/castings/") && requestUrl.contains("/apply") -> {
        "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (ACTEUR requis pour postuler à un casting)"
    }
    // Création de casting - nécessite RECRUTEUR ou ADMIN
    requestUrl.contains("/castings") && requestMethod == "POST" && !requestUrl.contains("/apply") -> {
        "⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (RECRUTEUR ou ADMIN requis pour créer un casting)"
    }
    // Autres endpoints protégés
    else -> {
        "⚠️ Erreur 403 (Forbidden) - Vérifier les permissions de l'utilisateur"
    }
}
```

### 2. Logs de diagnostic détaillés (`CastingRepository.kt`)

**Fichier:** `app/src/main/java/com/example/projecct_mobile/data/repository/CastingRepository.kt`

**Fonction modifiée:** `applyToCastingWithVideo()`

**Nouveaux logs ajoutés :**
```kotlin
android.util.Log.d("CastingRepository", "========== CANDIDATURE AU CASTING ==========")
android.util.Log.d("CastingRepository", "📝 Postulation au casting: $id")
android.util.Log.d("CastingRepository", "📹 Vidéo fournie: ${videoUri != null}")
android.util.Log.d("CastingRepository", "🤖 Feedback IA fourni: ${aiFeedback != null}")

// Note: Le token et le rôle sont vérifiés automatiquement par AuthInterceptor
// qui ajoute le header Authorization et log tous les détails

// ... préparation de la requête ...

android.util.Log.d("CastingRepository", "📹 Taille de la vidéo: ${String.format("%.1f", fileSizeMB)} MB")
android.util.Log.d("CastingRepository", "🤖 AI Feedback présent: ${aiFeedback.length} caractères")
android.util.Log.d("CastingRepository", "📤 Envoi de la candidature:")
android.util.Log.d("CastingRepository", "   - Casting ID: $id")
android.util.Log.d("CastingRepository", "   - Vidéo: ${if (videoPart != null) "OUI" else "NON"}")
android.util.Log.d("CastingRepository", "   - AI Feedback: ${if (aiFeedbackBody != null) "OUI" else "NON"}")
android.util.Log.d("CastingRepository", "============================================")
```

---

## 📋 Résumé des changements

### Avant
- ❌ Message d'erreur incorrect : "RECRUTEUR ou ADMIN requis" pour tous les endpoints 403
- ❌ Pas de logs détaillés dans `CastingRepository` pour diagnostiquer les problèmes

### Après
- ✅ Message d'erreur correct selon l'endpoint :
  - `/castings/{id}/apply` → "ACTEUR requis pour postuler à un casting"
  - `POST /castings` → "RECRUTEUR ou ADMIN requis pour créer un casting"
- ✅ Logs détaillés avant l'envoi de la candidature
- ✅ Logs de la taille de la vidéo, présence du feedback IA, etc.

---

## 🧪 Comment tester

### Étape 1 : Build l'application

⚠️ **Important** : Assurez-vous d'utiliser **Java 11 ou supérieur** pour builder l'application.

```bash
# Vérifier la version Java
java -version

# Si Java 8, installer Java 11 ou 17
# Puis builder l'application
./gradlew assembleDebug
```

### Étape 2 : Installer et lancer l'app

1. Installer l'APK sur votre appareil/émulateur
2. Se connecter en tant qu'acteur (`user@user.com`)
3. Aller sur la liste des castings
4. Cliquer sur un casting ouvert
5. Cliquer sur "Postuler" (sans vidéo) ou "Postuler avec vidéo"

### Étape 3 : Observer les logs

**Logs attendus dans Logcat** (filtre : `CastingRepository`, `AuthInterceptor`):

```
========== CANDIDATURE AU CASTING ==========
📝 Postulation au casting: 693f0a9897caff2222dc87f1
📹 Vidéo fournie: true/false
🤖 Feedback IA fourni: true/false

[Si vidéo fournie]
📹 Taille de la vidéo: 2.5 MB

[Si feedback IA fourni]
🤖 AI Feedback présent: 1234 caractères

📤 Envoi de la candidature:
   - Casting ID: 693f0a9897caff2222dc87f1
   - Vidéo: OUI/NON
   - AI Feedback: OUI/NON
============================================

[AuthInterceptor logs]
🌐 Intercepteur appelé pour: POST https://cast-mate.vercel.app/castings/.../apply
📋 Token info: id=..., role=ACTEUR, type=ACTEUR, email=user@user.com
✅ Header Authorization confirmé: Bearer ...

[Si succès]
✅ Candidature avec vidéo envoyée avec succès

[Si erreur 403]
❌ Erreur 403 pour: POST https://cast-mate.vercel.app/castings/.../apply
⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (ACTEUR requis pour postuler à un casting)
```

---

## 🔍 Diagnostic si erreur 403 persiste

Si l'erreur 403 persiste après ces corrections :

### 1. Vérifier les logs Android
Les nouveaux logs vous indiqueront :
- ✅ Si le token est présent
- ✅ Si le rôle est correct (ACTEUR)
- ✅ Quelle requête est envoyée

### 2. Vérifier les logs Backend (Vercel)

**Comment accéder :**
1. https://vercel.com → Projet CastMate
2. Deployments → Dernier déploiement
3. Functions/Logs

**Logs à chercher :**
- `[JWT Guard]` : Authentification
- `[JWT Strategy]` : Extraction de l'acteur
- `[RolesGuard]` : Vérification des rôles
- `[CastingsController]` : Traitement de la candidature

**Si blocage dans JwtStrategy :**
```
[JWT Strategy] ❌ ERREUR: Acteur non trouvé pour ID: 690cda42dc65a1a3fc2c6a20
```
→ L'acteur n'existe pas en base de données

**Si blocage dans RolesGuard :**
```
[RolesGuard] Comparaison rôle: { userRole: "ACTEUR", requiredRole: "ACTEUR", matches: false }
```
→ Problème de comparaison des rôles (vérifier le type : enum vs string)

### 3. Actions selon le diagnostic

| Problème identifié | Action requise |
|-------------------|----------------|
| Acteur non trouvé | Créer un nouveau compte acteur ou vérifier l'ID |
| Comparaison des rôles échoue | Vérifier le type du rôle dans `JwtStrategy.validate()` |
| Aucun log backend | Vérifier que les corrections backend sont déployées |
| Token invalide | Se déconnecter et se reconnecter |

---

## 📄 Documents de référence

1. **`MESSAGE_BACKEND_URGENT_403_APPLY_CASTING.md`**
   - Message détaillé pour l'équipe backend
   - Toutes les informations techniques
   - Hypothèses et tests suggérés

2. **`GUIDE_TEST_APPLY_CASTING.md`**
   - Guide complet de test
   - Scénarios de test (avec/sans vidéo)
   - Checklist de diagnostic
   - Guide de dépannage

---

## ✅ Checklist avant de tester

- [ ] Java 11+ installé (pour builder l'app)
- [ ] Application buildée et installée
- [ ] Compte acteur disponible (`user@user.com`)
- [ ] Casting ouvert disponible en base de données
- [ ] Corrections backend déployées sur Vercel
- [ ] Logcat ouvert et prêt à capturer les logs

---

## 🎯 Résultat attendu

### Si succès (🎉)
```
✅ Candidature avec vidéo envoyée avec succès
UI : "Candidature envoyée avec succès"
```

### Si échec (🔴)
```
❌ Erreur 403
⚠️ Erreur 403 (Forbidden) - Vérifier le rôle de l'utilisateur (ACTEUR requis pour postuler à un casting)
→ Vérifier les logs backend dans Vercel
→ Identifier où ça bloque (JwtStrategy, RolesGuard, etc.)
```

---

## 📞 Support

Si le problème persiste après vérification des logs backend :

1. Partager les **logs backend complets** (depuis Vercel)
2. Partager les **logs Android complets** (depuis Logcat)
3. Indiquer exactement où ça bloque selon les logs backend

L'équipe backend pourra alors effectuer des corrections supplémentaires si nécessaire.

---

**Bonne chance pour les tests !** 🚀

