# 🎬 CastMate Android

CastMate est une plateforme mobile Android qui connecte les acteurs et les agences de casting en Tunisie. L'application permet aux agences de publier des castings et aux acteurs de postuler avec des vidéos d'audition analysées par l'IA.

## 📱 À propos

CastMate facilite le processus de casting en offrant :
- **Gestion de castings** : Les agences peuvent créer et gérer des castings
- **Candidatures vidéo** : Les acteurs peuvent postuler avec des vidéos d'audition
- **Analyse IA** : Évaluation automatique des performances avec Google Gemini
- **Chatbot intelligent** : Assistant IA pour filtrer et trouver les meilleurs candidats
- **Profils détaillés** : Gestion complète des profils acteurs et agences

## ✨ Fonctionnalités principales

### Pour les Acteurs
- ✅ Inscription et gestion de profil
- ✅ Recherche et filtrage de castings
- ✅ Candidature avec vidéo d'audition
- ✅ Analyse IA de la performance (émotions, posture, intonation, expressivité)
- ✅ Suivi des candidatures
- ✅ Favoris et agenda
- ✅ Carte interactive des castings
- ✅ Entraînement avec feedback IA

### Pour les Agences
- ✅ Inscription et gestion de profil
- ✅ Création et gestion de castings
- ✅ Visualisation des candidatures
- ✅ Chatbot IA pour filtrer les candidats
- ✅ Analyse des scores IA des vidéos
- ✅ Gestion des statuts (EN_ATTENTE, ACCEPTE, REFUSE)

### Fonctionnalités transverses
- ✅ Authentification (Email/Mot de passe + Google Sign-In)
- ✅ Réinitialisation de mot de passe
- ✅ Persistance des conversations chatbot (Room Database)
- ✅ Upload de médias (photos, vidéos, PDF)
- ✅ Recadrage d'images intégré

## 🛠️ Technologies utilisées

### Framework & Langage
- **Kotlin** - Langage de programmation
- **Jetpack Compose** - UI moderne et déclarative
- **Android SDK** - Plateforme Android

### Architecture
- **MVVM (Model-View-ViewModel)** - Architecture propre
- **Repository Pattern** - Abstraction de la couche données
- **Navigation Compose** - Navigation entre écrans

### Bibliothèques principales
- **Retrofit 2.9.0** - Client HTTP pour les APIs REST
- **OkHttp 4.12.0** - Client HTTP avec intercepteurs
- **Gson 2.10.1** - Sérialisation JSON
- **Room 2.6.1** - Base de données locale
- **DataStore 1.1.1** - Stockage des préférences
- **Coroutines** - Programmation asynchrone
- **Google Gemini 2.5 Pro** - IA pour chatbot et analyse vidéo
- **Google Sign-In** - Authentification Google
- **CropKit** - Recadrage d'images

## 📋 Prérequis

- **Android Studio** Hedgehog (2023.1.1) ou supérieur
- **JDK 11** ou supérieur
- **Android SDK** API 24 (Android 7.0) minimum
- **Target SDK** API 36
- **Gradle** 8.13.1
- **Kotlin** 2.0.21

## 🚀 Installation

### 1. Cloner le repository

```bash
git clone <repository-url>
cd CastMate_android
```

### 2. Configuration

#### Configuration Google Sign-In
1. Créez un projet dans [Google Cloud Console](https://console.cloud.google.com/)
2. Activez l'API Google Sign-In
3. Configurez le SHA-1 de votre clé de signature
4. Ajoutez le fichier `google-services.json` (si nécessaire)

#### Configuration Gemini API
La clé API Gemini est configurée dans `app/src/main/java/com/example/projecct_mobile/utils/GeminiConfig.kt`

⚠️ **Note de sécurité** : En production, utilisez des variables d'environnement ou `BuildConfig` pour stocker les clés API.

### 3. Build et exécution

```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

Ou utilisez Android Studio :
1. Ouvrez le projet dans Android Studio
2. Synchronisez Gradle
3. Exécutez l'application (Shift+F10)

## 📁 Structure du projet

```
app/src/main/java/com/example/projecct_mobile/
├── data/
│   ├── api/                    # Services API Retrofit
│   │   ├── ApiClient.kt
│   │   ├── AuthApiService.kt
│   │   ├── CastingApiService.kt
│   │   ├── ActeurApiService.kt
│   │   ├── AgenceApiService.kt
│   │   ├── MediaApiService.kt
│   │   ├── GeminiApiService.kt
│   │   ├── AuthInterceptor.kt
│   │   └── ErrorInterceptor.kt
│   ├── local/
│   │   ├── database/           # Room Database
│   │   │   ├── CastMateDatabase.kt
│   │   │   ├── ChatConversationEntity.kt
│   │   │   └── ChatMessageEntity.kt
│   │   └── TokenManager.kt
│   ├── model/                  # Modèles de données
│   │   ├── User.kt
│   │   ├── Casting.kt
│   │   ├── ActeurProfile.kt
│   │   ├── AgenceProfile.kt
│   │   └── ...
│   └── repository/             # Repositories
│       ├── AuthRepository.kt
│       ├── CastingRepository.kt
│       ├── GeminiChatbotRepository.kt
│       └── ...
├── ui/
│   ├── screens/                # Écrans de l'application
│   │   ├── auth/               # Authentification
│   │   ├── acteur/             # Écrans acteurs
│   │   ├── agence/             # Écrans agences
│   │   ├── casting/            # Castings
│   │   ├── map/                # Carte
│   │   └── agenda/             # Agenda
│   ├── components/             # Composants réutilisables
│   └── theme/                  # Thème et styles
├── utils/                       # Utilitaires
│   ├── GeminiConfig.kt
│   └── SocialLinkValidator.kt
└── MainActivity.kt              # Point d'entrée
```

## 🔌 API Backend

L'application consomme une API REST NestJS déployée sur Vercel :

- **URL de base** : `https://cast-mate.vercel.app/`
- **Documentation Swagger** : `https://cast-mate.vercel.app/api`
- **Authentification** : JWT Bearer Token

### Endpoints principaux

- `POST /auth/signin` - Connexion
- `POST /auth/signup/acteur` - Inscription acteur
- `POST /auth/signup/agence` - Inscription agence
- `GET /castings` - Liste des castings
- `POST /castings` - Créer un casting
- `POST /castings/:id/apply` - Postuler à un casting
- `GET /acteurs/:id` - Profil acteur
- `GET /agences/:id` - Profil agence

Consultez `API_USAGE_GUIDE.md` pour plus de détails.

## 🤖 Intégration IA (Google Gemini)

### Chatbot pour agences
- Analyse des candidatures en langage naturel
- Filtrage intelligent selon critères (âge, expérience, score IA)
- Suggestions d'acteurs avec scores de correspondance
- Persistance des conversations (Room Database)

### Analyse vidéo d'audition
- Score global de performance (0-100)
- Analyse des émotions
- Évaluation de la posture
- Analyse de l'intonation
- Mesure de l'expressivité
- Recommandations personnalisées

Consultez `GUIDE_CHATBOT_CONSOMMATION.md` pour plus de détails.

## 🔐 Authentification

### Méthodes supportées
1. **Email / Mot de passe** - Authentification classique
2. **Google Sign-In** - Connexion avec compte Google

### Gestion des tokens
- Stockage sécurisé avec `TokenManager` (DataStore)
- Ajout automatique du token JWT via `AuthInterceptor`
- Gestion de l'expiration avec `ErrorInterceptor`
- Redirection automatique vers login si token expiré

### Réinitialisation de mot de passe
- Deep link : `castmate://reset-password?token=...`
- Envoi d'email depuis l'application (JavaMail)

## 💾 Persistance locale

### Room Database
- **Conversations chatbot** : Historique des conversations avec le chatbot IA
- **Messages** : Messages utilisateur et bot avec suggestions d'acteurs

### DataStore
- **Token JWT** : Stockage sécurisé du token d'authentification
- **Préférences utilisateur** : Paramètres de l'application

## 🎨 Thème et Design

L'application utilise Material Design 3 avec un thème personnalisé :
- Couleurs principales définies dans `ui/theme/Color.kt`
- Typographie dans `ui/theme/Type.kt`
- Thème complet dans `ui/theme/Theme.kt`

## 📝 Documentation supplémentaire

Le projet contient plusieurs guides détaillés :

- `GUIDE_CHATBOT_CONSOMMATION.md` - Guide complet du chatbot IA
- `API_USAGE_GUIDE.md` - Guide d'utilisation de l'API
- `CONFIGURATION_ANDROID_GOOGLE_SIGN_IN.md` - Configuration Google Sign-In
- `GUIDE_EMAIL_CONFIGURATION_SIMPLE.md` - Configuration email
- `GUIDE_IMAGE_CROP_INTEGRATION.md` - Intégration recadrage d'images
- Et d'autres guides spécifiques...

## 🧪 Tests

```bash
# Tests unitaires
./gradlew test

# Tests instrumentés
./gradlew connectedAndroidTest
```

## 📦 Build de production

```bash
# Générer un AAB (Android App Bundle)
./gradlew bundleRelease

# Générer un APK
./gradlew assembleRelease
```

Le fichier AAB se trouve dans : `app/release/app-release.aab`

## 🐛 Dépannage

### Erreurs courantes

1. **Erreur 401 (Unauthorized)**
   - Vérifiez que le token JWT est valide
   - Le token peut avoir expiré, reconnectez-vous

2. **Erreur de connexion réseau**
   - Vérifiez votre connexion internet
   - Vérifiez que l'API backend est accessible

3. **Erreur Google Sign-In**
   - Vérifiez la configuration SHA-1 dans Google Cloud Console
   - Vérifiez que l'API est activée

4. **Erreur Gemini API**
   - Vérifiez que la clé API est valide
   - Vérifiez les quotas de l'API

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence propriétaire. Tous droits réservés.

## 👥 Équipe

Développé par l'équipe CastMate :
- **Oussema Negzaoui**
- **Mohamed Ben Fredj**

## 📞 Support

Pour toute question ou problème, contactez-nous :
- **Email** : [mohamedbenfredj8@gmail.com](mailto:mohamedbenfredj8@gmail.com)
- Ou ouvrez une issue sur le repository

---

**Version** : 1.0  
**Dernière mise à jour** : 30 novembre 2025

