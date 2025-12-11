# 🎭 Guide Entraînement Acteur avec Gemini AI

## Vue d'ensemble

Cette fonctionnalité permet aux acteurs d'envoyer une vidéo de leur performance (max 30 secondes) et de recevoir un feedback détaillé généré par **Gemini 1.5 Flash** pour améliorer leur jeu d'acteur.

---

## ✅ Fonctionnalités Implémentées

### 1. **Écran d'Entraînement**
- 📹 Sélection de vidéo depuis la galerie
- ⏳ Analyse en temps réel avec Gemini AI
- 📊 Affichage détaillé du feedback
- 🎯 Score global de performance

### 2. **Analyse IA Complète**
L'IA Gemini analyse 4 aspects principaux :

#### 🎭 **Émotions** (0-100)
- Émotions détectées (joie, tristesse, colère, etc.)
- Cohérence émotionnelle
- Intensité des émotions
- Commentaire personnalisé

#### 🧍 **Posture** (0-100)
- Score de posture corporelle
- Points forts identifiés
- Points à améliorer
- Conseils spécifiques

#### 🎤 **Intonation** (0-100)
- Clarté de la diction
- Rythme vocal
- Expressivité de la voix
- Recommandations vocales

#### ✨ **Expressivité** (0-100)
- Expressions faciales
- Langage corporel
- Amplification émotionnelle
- Conseils d'amélioration

### 3. **Feedback Structuré**
- ✅ 3-5 points forts à conserver
- 📝 3-5 recommandations prioritaires
- 📄 Résumé en 2-3 phrases
- 🎯 Score global (moyenne pondérée)

---

## 📁 Structure des Fichiers

```
app/src/main/java/com/example/projecct_mobile/
├── utils/
│   └── GeminiConfig.kt                    # Configuration centralisée de la clé API
├── ai/
│   └── GeminiTrainingService.kt           # Service d'analyse vidéo avec Gemini
├── data/
│   └── model/
│       └── TrainingModels.kt              # Modèles de données (feedback, émotions, etc.)
└── ui/
    └── screens/
        └── acteur/
            ├── ActorTrainingScreen.kt      # Écran d'entraînement
            └── ActorSettingsScreen.kt      # Settings avec bouton entraînement
```

---

## 🔑 Configuration Gemini

### Clé API Centralisée

La clé API Gemini est centralisée dans `GeminiConfig.kt` :

```kotlin
// app/src/main/java/com/example/projecct_mobile/utils/GeminiConfig.kt

object GeminiConfig {
    const val GEMINI_API_KEY = "AIzaSyBr0weABCTBdyPXER8pnAPWPANfVTQZn5Q"
    const val MODEL_FLASH = "gemini-1.5-flash"
    const val MODEL_PRO = "gemini-1.5-pro"
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}
```

**Note** : Cette clé est partagée avec le chatbot agence existant.

---

## 🚀 Utilisation

### Navigation vers l'Entraînement

Depuis **Paramètres Acteur** → **Entraînement IA**

```kotlin
// MainActivity.kt - Route ajoutée
composable("actorTraining") {
    ActorTrainingScreen(
        onBackClick = { navController.popBackStack() }
    )
}
```

### Flux Utilisateur

1. **Sélectionner une vidéo** (bouton "Choisir une vidéo")
2. **Analyser** (bouton "Analyser ma performance")
3. **Attendre 30-60 secondes** (traitement par Gemini)
4. **Voir le feedback** détaillé avec scores et recommandations

---

## 🔧 Implémentation Technique

### Service Gemini

```kotlin
// GeminiTrainingService.kt

class GeminiTrainingService(private val context: Context) {
    
    suspend fun analyzeActingVideo(videoUri: Uri): Result<TrainingFeedback> {
        // 1. Lire la vidéo
        val videoBytes = readVideoFromUri(videoUri)
        
        // 2. Encoder en base64
        val videoBase64 = Base64.encodeToString(videoBytes, Base64.NO_WRAP)
        
        // 3. Construire le prompt d'analyse
        val prompt = buildAnalysisPrompt()
        
        // 4. Appeler l'API Gemini
        val response = callGeminiApi(videoBase64, prompt)
        
        // 5. Parser le feedback JSON
        val feedback = parseGeminiFeedback(response)
        
        return Result.success(feedback)
    }
}
```

### Appel API Direct

Contrairement au chatbot qui utilise Retrofit, l'entraînement utilise **HttpURLConnection** pour :
- Envoyer des vidéos encodées en base64
- Gérer des timeouts plus longs (60 secondes)
- Simplifier l'envoi de multipart data

```kotlin
val connection = URL(url).openConnection() as HttpURLConnection
connection.requestMethod = "POST"
connection.setRequestProperty("Content-Type", "application/json")
connection.connectTimeout = 60000
connection.readTimeout = 60000
```

---

## 📊 Limites et Quotas

### Gemini API (Gratuit)
- ✅ **15 requêtes/minute**
- ✅ **1 500 requêtes/jour**
- ✅ **1 million tokens/mois**

### Taille Vidéo
- ✅ **Max 50 MB** (environ 30 secondes en 1080p)
- ✅ **Max 30 secondes** de durée
- ✅ Formats supportés : MP4, MOV, AVI, WEBM

### Estimation Tokens
- 📹 Vidéo 30s : ~5 000 tokens
- 💬 Chatbot agence : ~1 000 tokens

**Total quotidien gratuit** : ~200 analyses vidéo + 500 questions chatbot

---

## 🎨 Interface Utilisateur

### Composants Visuels

#### 1. **Carte Score Global**
```kotlin
// Couleur adaptative selon le score
when {
    score >= 80 -> Vert (Excellent)
    score >= 60 -> Orange (Bien)
    else -> Rouge (À améliorer)
}
```

#### 2. **Cartes de Catégorie**
- Titre + Score
- Barre de progression colorée
- Commentaire du coach
- Détails spécifiques

#### 3. **Points Forts** (fond vert)
- Icône ThumbUp
- Liste avec checkmarks ✓

#### 4. **Recommandations** (fond jaune)
- Icône Lightbulb
- Liste avec flèches →

---

## 🔐 Sécurité

### ⚠️ Clé API Exposée

**Problème actuel** : La clé API est dans le code source.

**Solutions recommandées** :

#### Option 1 : BuildConfig
```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"]}\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}
```

#### Option 2 : Backend Proxy
- Créer un endpoint NestJS : `/api/training/analyze`
- Backend appelle Gemini avec clé secrète
- Android envoie la vidéo au backend
- **Avantage** : Historique, quotas, sécurité

---

## 🧪 Tests Recommandés

### Tests Fonctionnels
1. ✅ Sélection de vidéo valide
2. ✅ Vidéo > 30 secondes (doit rejeter)
3. ✅ Vidéo > 50 MB (doit rejeter)
4. ✅ Format non supporté (doit rejeter)
5. ✅ Analyse réussie avec feedback
6. ✅ Gestion des erreurs API

### Tests UI
1. ✅ Navigation depuis Settings
2. ✅ Bouton "Choisir une vidéo"
3. ✅ Indicateur de chargement pendant l'analyse
4. ✅ Affichage du feedback avec toutes les sections
5. ✅ Bouton retour

---

## 📱 Exemples d'Utilisation

### Cas 1 : Performance Excellente
```json
{
  "globalScore": 88,
  "emotions": {
    "detected": ["joie", "enthousiasme"],
    "coherence": 90,
    "intensity": 85
  },
  "strengths": [
    "Excellent contact visuel avec la caméra",
    "Émotions authentiques et naturelles",
    "Voix claire et bien projetée"
  ],
  "recommendations": [
    "Essayer d'amplifier encore plus les émotions fortes",
    "Varier davantage le rythme de parole"
  ]
}
```

### Cas 2 : Performance À Améliorer
```json
{
  "globalScore": 52,
  "emotions": {
    "detected": ["neutre"],
    "coherence": 50,
    "intensity": 40
  },
  "strengths": [
    "Bonne diction",
    "Posture stable"
  ],
  "recommendations": [
    "Travailler l'expressivité des émotions",
    "Utiliser plus le langage corporel",
    "Varier l'intonation de voix",
    "Amplifier les expressions faciales"
  ]
}
```

---

## 🐛 Gestion des Erreurs

### Erreurs Possibles

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Vidéo trop volumineuse` | > 50 MB | Compresser la vidéo |
| `Durée trop longue` | > 30s | Couper la vidéo |
| `Format non supporté` | .avi, .flv | Convertir en MP4 |
| `Erreur API Gemini` | Quota dépassé | Attendre ou upgrader |
| `Impossible de lire la vidéo` | URI invalide | Réessayer |

### Messages Utilisateur
```kotlin
errorMessage?.let { error ->
    Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))) {
        Row {
            Icon(Icons.Default.Error, tint = Color.Red)
            Text(error, color = Color.Red)
        }
    }
}
```

---

## 🔄 Améliorations Futures

### Court Terme
- [ ] Historique des entraînements (sauvegarder dans MongoDB via backend)
- [ ] Graphiques de progression (évolution des scores)
- [ ] Partage de feedback (export PDF)
- [ ] Comparaison avant/après

### Moyen Terme
- [ ] Défis d'entraînement hebdomadaires
- [ ] Scènes recommandées pour pratiquer
- [ ] Feedback vocal (Text-to-Speech)
- [ ] Mode hors ligne (traitement local)

### Long Terme
- [ ] Entraînement avec scripts spécifiques
- [ ] Analyse de scènes à plusieurs acteurs
- [ ] Recommandations de castings basées sur les scores
- [ ] Matching IA acteur-rôle

---

## 📚 Ressources

### Documentation Gemini
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Multimodal Prompting (Video)](https://ai.google.dev/docs/multimodal_concepts)
- [API Reference](https://ai.google.dev/api/rest)

### Tarification
- [Gemini Pricing](https://ai.google.dev/pricing)
- Gratuit : 15 RPM, 1500 RPD, 1M tokens/mois
- Pay-as-you-go : $0.35 / 1M input tokens (Flash)

---

## ✅ Checklist Déploiement

- [x] Créer GeminiConfig
- [x] Créer TrainingModels
- [x] Créer GeminiTrainingService
- [x] Créer ActorTrainingScreen
- [x] Intégrer dans navigation
- [x] Ajouter bouton dans Settings
- [x] Centraliser clé API
- [ ] Tester avec vraies vidéos
- [ ] Optimiser les prompts
- [ ] Ajouter analytics
- [ ] Documenter pour l'équipe

---

## 🎬 Conclusion

Cette fonctionnalité offre aux acteurs un **coach IA personnel** disponible 24/7 pour améliorer leurs performances. Avec Gemini 1.5 Flash, l'analyse est rapide, précise et constructive.

**Prochaine étape** : Tester avec de vraies vidéos d'acteurs et itérer sur le prompt pour améliorer la qualité du feedback ! 🚀

