# Configuration des Variables d'Environnement (.env)

## 📋 Vue d'ensemble

La clé API Groq est maintenant chargée depuis un fichier `.env` au lieu d'être codée en dur dans le code source. Cela améliore la sécurité et permet de gérer différentes clés pour le développement et la production.

## 📁 Structure des fichiers

```
CastMate_android_3/
├── .env                          # Fichier .env à la racine (pour référence)
└── app/src/main/assets/
    └── .env                      # Fichier .env utilisé par l'application Android
```

## 🔧 Configuration

### 1. Fichier `.env` dans `app/src/main/assets/`

Le fichier doit contenir :

```env
# Clé API Groq pour l'analyse vidéo et le chatbot
# Obtenez votre clé sur https://console.groq.com/
GROQ_API_KEY=gsk_VOTRE_CLE_API_ICI
```

### 2. Initialisation dans `MainActivity`

La clé est automatiquement chargée au démarrage de l'application :

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialiser l'API client
        ApiClient.initialize(this)
        
        // Initialiser GroqConfig pour charger la clé API depuis .env
        GroqConfig.initialize(this)
        
        // ... reste du code
    }
}
```

## 🔒 Sécurité

- ✅ Le fichier `.env` est ajouté à `.gitignore` pour éviter qu'il soit commité sur GitHub
- ✅ La clé est chargée depuis les assets Android (pas accessible directement)
- ⚠️ **Important** : Ne partagez jamais votre clé API publiquement

## 🚀 Utilisation

Une fois initialisé, la clé API est accessible via :

```kotlin
val apiKey = GroqConfig.GROQ_API_KEY
```

## 📝 Notes

- Si le fichier `.env` n'existe pas ou si la clé est absente, l'application lancera une `IllegalStateException` au démarrage
- La clé est mise en cache après le premier chargement pour améliorer les performances
- Pour changer la clé, modifiez le fichier `app/src/main/assets/.env` et relancez l'application

## 🔄 Migration depuis l'ancien système

Si vous aviez la clé codée en dur dans `GroqConfig.kt`, elle a été automatiquement migrée vers le fichier `.env`. Assurez-vous que le fichier `app/src/main/assets/.env` contient bien votre clé API.

