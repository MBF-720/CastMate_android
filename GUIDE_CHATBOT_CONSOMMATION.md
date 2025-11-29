# 🤖 Guide Complet - Chatbot CastMate : Consommation et Architecture

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture générale](#architecture-générale)
3. [Consommation des APIs](#consommation-des-apis)
4. [Modèles de données](#modèles-de-données)
5. [Flux de données](#flux-de-données)
6. [Persistance locale](#persistance-locale)
7. [Configuration](#configuration)
8. [Gestion des erreurs](#gestion-des-erreurs)
9. [Performance et optimisations](#performance-et-optimisations)
10. [Exemples d'utilisation](#exemples-dutilisation)

---

## 🎯 Vue d'ensemble

Le chatbot CastMate est un assistant IA intégré qui aide les agences de casting à filtrer et trouver les meilleurs acteurs parmi les candidats d'un casting. Il utilise **Google Gemini 2.5 Pro** pour analyser les candidatures et répondre aux questions en langage naturel.

### Fonctionnalités principales

- ✅ **Filtrage intelligent** des acteurs selon des critères (âge, expérience, localisation, score IA)
- ✅ **Analyse des scores IA** des vidéos d'audition
- ✅ **Suggestions contextuelles** avec scores de correspondance
- ✅ **Persistance des conversations** (Room Database)
- ✅ **Interface conversationnelle** naturelle en français

---

## 🏗️ Architecture générale

### Composants principaux

```
┌─────────────────────────────────────────────────────────────┐
│                    ChatbotContent (UI)                       │
│              (Jetpack Compose - Écran principal)             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│            GeminiChatbotRepository                           │
│         (Logique métier - Appels Gemini)                    │
└──────────────┬───────────────────────┬─────────────────────┘
               │                         │
               ▼                         ▼
┌─────────────────────────┐  ┌──────────────────────────────┐
│   GeminiApiService      │  │  CastingRepository            │
│   (Retrofit Interface)  │  │  (Récupération castings)      │
└──────────────┬──────────┘  └──────────────┬─────────────────┘
               │                            │
               ▼                            ▼
┌─────────────────────────┐  ┌──────────────────────────────┐
│   Google Gemini API     │  │  Backend CastMate API          │
│   (generativelanguage)  │  │  (cast-mate.vercel.app)        │
└─────────────────────────┘  └──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────┐
│         ChatConversationRepository                          │
│         (Persistance Room Database)                         │
└─────────────────────────────────────────────────────────────┘
```

### Couches de l'application

1. **UI Layer** (`ChatbotContent.kt`)
   - Interface utilisateur Jetpack Compose
   - Gestion de l'état (messages, chargement, erreurs)
   - Affichage des suggestions d'acteurs

2. **Repository Layer** (`GeminiChatbotRepository.kt`)
   - Construction du prompt système
   - Appel à l'API Gemini
   - Parsing des réponses JSON
   - Filtrage et scoring des acteurs

3. **API Layer** (`GeminiApiService.kt`, `CastingApiService.kt`)
   - Interfaces Retrofit pour les appels HTTP
   - Configuration des timeouts et intercepteurs

4. **Data Layer** (`ChatConversationRepository.kt`)
   - Persistance avec Room Database
   - Gestion des conversations et messages

---

## 🔌 Consommation des APIs

### 1. API Google Gemini

#### Endpoint utilisé

```
POST https://generativelanguage.googleapis.com/v1/models/gemini-2.5-pro:generateContent?key={API_KEY}
```

#### Configuration

**Fichier:** `GeminiConfig.kt`

```kotlin
object GeminiConfig {
    const val GEMINI_API_KEY = "AIzaSyAQLtb31U2T2C46_HLcS3BROMos9yrHIe8"
    const val MODEL_PRO_2_5 = "gemini-2.5-pro"
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}
```

#### Service Retrofit

**Fichier:** `GeminiApiService.kt`

```kotlin
interface GeminiApiService {
    @POST("v1/models/gemini-2.5-pro:generateContent")
    suspend fun generateContent(
        @Query("key") key: String,
        @Body request: GeminiGenerateContentRequest
    ): Response<GeminiGenerateContentResponse>
}
```

#### Structure de la requête

**Modèle:** `GeminiGenerateContentRequest`

```kotlin
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig?
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? // "user" ou "model"
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 2048
)
```

#### Exemple de requête

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "[PROMPT_SYSTEME_COMPLET]"
        }
      ],
      "role": "user"
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 2048
  }
}
```

#### Structure de la réponse

**Modèle:** `GeminiGenerateContentResponse`

```kotlin
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>?,
    val promptFeedback: GeminiPromptFeedback?
)

data class GeminiCandidate(
    val content: GeminiContent?,
    val finishReason: String?,
    val safetyRatings: List<GeminiSafetyRating>?
)
```

#### Exemple de réponse

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "{\"answer\": \"...\", \"suggestedActors\": [...]}"
          }
        ]
      },
      "finishReason": "STOP"
    }
  ]
}
```

#### Paramètres de génération

- **Temperature:** `0.7` (équilibre créativité/précision)
- **Max Output Tokens:** `2048` (limite de réponse)
- **Timeout:** `60 secondes` (read timeout)

#### Coûts et limites

- **Modèle:** Gemini 2.5 Pro (payant)
- **Clé API:** Partagée entre chatbot et analyse vidéo
- **Rate limiting:** Géré par Google (selon quota API)

---

### 2. API Backend CastMate

#### Endpoint utilisé

```
GET https://cast-mate.vercel.app/castings/{castingId}
```

#### Service Retrofit

**Fichier:** `CastingApiService.kt` (via `ApiClient`)

```kotlin
interface CastingApiService {
    @GET("castings/{id}")
    suspend fun getCastingById(
        @Path("id") id: String
    ): Response<Casting>
}
```

#### Structure de la réponse

**Modèle:** `Casting`

```kotlin
data class Casting(
    val id: String?,
    val titre: String?,
    val descriptionRole: String?,
    val synopsis: String?,
    val types: List<String>?,
    val age: String?,
    val lieu: String?,
    val conditions: String?,
    val prix: String?,
    val candidats: List<Candidat>?
)

data class Candidat(
    val acteurId: ActeurInfo?,
    val statut: String?, // "EN_ATTENTE", "ACCEPTE", "REFUSE"
    val dateCandidature: String?,
    val videoFileId: String?,
    val aiFeedback: TrainingFeedback?
)

data class TrainingFeedback(
    val globalScore: Int,
    val emotions: EmotionAnalysis,
    val posture: PostureAnalysis,
    val intonation: IntonationAnalysis,
    val expressivite: ExpressivityAnalysis,
    val recommendations: List<String>,
    val strengths: List<String>,
    val summary: String
)
```

#### Authentification

- **Type:** JWT Bearer Token
- **Header:** `Authorization: Bearer {token}`
- **Gestion:** `AuthInterceptor` (ajout automatique)

#### Timeouts

- **Connect:** `30 secondes`
- **Read:** `30 secondes`
- **Write:** `30 secondes`

---

## 📦 Modèles de données

### Modèles de requête/réponse

#### `ChatbotResponse`

```kotlin
data class ChatbotResponse(
    val answer: String,                    // Réponse textuelle du chatbot
    val suggestedActors: List<SuggestedActor>, // Acteurs suggérés
    val totalCandidates: Int,              // Nombre total de candidats
    val filteredCount: Int                 // Nombre d'acteurs filtrés
)
```

#### `SuggestedActor`

```kotlin
data class SuggestedActor(
    val acteurId: String,                  // ID unique de l'acteur
    val nom: String? = null,               // Nom de famille
    val prenom: String? = null,            // Prénom
    val age: Int? = null,                  // Âge
    val experience: Int? = null,           // Années d'expérience
    val gouvernorat: String? = null,       // Localisation
    val matchScore: Double = 0.0,          // Score de correspondance (0-1)
    val matchReasons: List<String> = emptyList() // Raisons de correspondance
)
```

#### `ChatMessage`

```kotlin
data class ChatMessage(
    val text: String,                      // Texte du message
    val isBot: Boolean,                    // true = bot, false = utilisateur
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActors: List<SuggestedActor>? = null // Suggestions associées
)
```

### Modèles de persistance (Room)

#### `ChatConversationEntity`

```kotlin
@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val conversationId: Long = 0,
    val castingId: String,                 // ID du casting (unique)
    val castingTitle: String,              // Titre du casting
    val lastUpdated: Long,                 // Timestamp dernière mise à jour
    val messageCount: Int = 0              // Nombre de messages
)
```

#### `ChatMessageEntity`

```kotlin
@Entity(
    tableName = "chat_messages",
    foreignKeys = [ForeignKey(
        entity = ChatConversationEntity::class,
        parentColumns = ["conversation_id"],
        childColumns = ["conversation_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val conversationId: Long,              // FK vers conversation
    val text: String,
    val isBot: Boolean,
    val timestamp: Long,
    val suggestedActorsJson: String? = null // JSON des suggestions
)
```

---

## 🔄 Flux de données

### Flux complet : Question utilisateur → Réponse

```
1. Utilisateur saisit une question
   │
   ▼
2. ChatbotContent.kt
   - Ajoute le message utilisateur à chatHistory
   - Sauvegarde dans Room (ChatConversationRepository)
   │
   ▼
3. Rechargement du casting (CastingRepository)
   - GET /castings/{id}
   - Récupère les candidats à jour avec scores IA
   │
   ▼
4. GeminiChatbotRepository.queryChatbot()
   - Construit le prompt système avec contexte casting
   - Crée GeminiGenerateContentRequest
   │
   ▼
5. GeminiApiService.generateContent()
   - POST vers Google Gemini API
   - Timeout: 60 secondes
   │
   ▼
6. Parsing de la réponse Gemini
   - Extraction du texte JSON
   - Parsing avec Gson
   - Extraction answer + suggestedActors
   │
   ▼
7. Mapping des acteurs suggérés
   - Recherche dans casting.candidats
   - Enrichissement avec données complètes
   - Calcul matchScore et matchReasons
   │
   ▼
8. Création ChatbotResponse
   - answer: texte de réponse
   - suggestedActors: liste enrichie
   - totalCandidates / filteredCount
   │
   ▼
9. ChatbotContent.kt
   - Crée ChatMessage avec suggestions
   - Ajoute à chatHistory
   - Sauvegarde dans Room
   │
   ▼
10. Affichage UI
    - Bulle de message bot
    - Cartes d'acteurs suggérés (inline)
    - Scroll automatique vers le bas
```

### Flux de persistance

```
Sauvegarde Message:
ChatbotContent → ChatConversationRepository.saveMessage()
  │
  ▼
ChatDao.addMessageAndUpdateConversation() [Transaction]
  │
  ├─→ INSERT INTO chat_messages
  ├─→ UPDATE chat_conversations (messageCount++, lastUpdated)
  └─→ COMMIT

Chargement Historique:
ChatbotContent (LaunchedEffect) → ChatConversationRepository.getMessages()
  │
  ▼
ChatDao.getMessagesByConversationId()
  │
  ▼
SELECT * FROM chat_messages WHERE conversation_id = ?
  │
  ▼
Conversion ChatMessageEntity → ChatMessage
  │
  ▼
Parsing suggestedActorsJson → List<SuggestedActor>
  │
  ▼
Retour chatHistory restauré
```

---

## 💾 Persistance locale

### Base de données Room

**Fichier:** `CastMateDatabase.kt`

```kotlin
@Database(
    entities = [
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CastMateDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
```

### Repository de persistance

**Fichier:** `ChatConversationRepository.kt`

#### Méthodes principales

```kotlin
class ChatConversationRepository(context: Context) {
    
    // Charge ou crée une conversation
    suspend fun getOrCreateConversation(
        castingId: String,
        castingTitle: String
    ): ChatConversationEntity
    
    // Sauvegarde un message
    suspend fun saveMessage(
        conversationId: Long,
        text: String,
        isBot: Boolean,
        suggestedActors: List<SuggestedActor>? = null
    ): ChatMessageEntity
    
    // Charge tous les messages d'une conversation
    suspend fun getMessages(castingId: String): List<ChatMessage>
    
    // Supprime une conversation
    suspend fun deleteConversation(castingId: String)
    
    // Statistiques
    suspend fun getStatistics(): ConversationStatistics
}
```

### Structure de la base de données

```
chat_conversations
├── conversation_id (PK, autoincrement)
├── casting_id (unique, indexé)
├── casting_title
├── last_updated
└── message_count

chat_messages
├── message_id (PK, autoincrement)
├── conversation_id (FK → chat_conversations, CASCADE)
├── text
├── is_bot
├── timestamp
└── suggested_actors_json (nullable, TEXT)
```

### Sérialisation JSON

Les `suggestedActors` sont sérialisés en JSON avec **Gson** :

```kotlin
// Sauvegarde
val suggestedActorsJson = gson.toJson(suggestedActors)

// Chargement
val type = object : TypeToken<List<SuggestedActor>>() {}.type
val suggestedActors = gson.fromJson<List<SuggestedActor>>(json, type)
```

---

## ⚙️ Configuration

### Configuration Gemini

**Fichier:** `GeminiConfig.kt`

```kotlin
object GeminiConfig {
    // Clé API partagée (chatbot + analyse vidéo)
    const val GEMINI_API_KEY = "AIzaSyAQLtb31U2T2C46_HLcS3BROMos9yrHIe8"
    
    // Modèle utilisé
    const val MODEL_PRO_2_5 = "gemini-2.5-pro"
    
    // URL de base
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}
```

### Configuration Retrofit

**Fichier:** `ApiClient.kt`

#### Service Gemini (URL séparée)

```kotlin
fun getGeminiService(): GeminiApiService {
    val geminiRetrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)  // Plus long pour Gemini
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .build()
    
    return geminiRetrofit.create(GeminiApiService::class.java)
}
```

#### Service Backend CastMate

```kotlin
fun getCastingService(): CastingApiService {
    return getRetrofit().create(CastingApiService::class.java)
}

// Configuration OkHttp avec intercepteurs
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenManager))
    .addInterceptor(ErrorInterceptor(tokenManager, gson))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### Configuration du prompt système

**Fichier:** `GeminiChatbotRepository.kt` → `buildSystemPrompt()`

Le prompt système inclut :

1. **Contexte du casting**
   - Titre, description du rôle, synopsis
   - Types, âge requis, lieu, conditions, prix

2. **Liste des candidats**
   - Pour chaque candidat :
     - Informations acteur (nom, email, statut)
     - Score IA global et sous-scores (émotions, posture, intonation, expressivité)
     - Points forts et recommandations IA
     - Résumé de performance

3. **Instructions de comportement**
   - Salutations
   - Questions hors application
   - Filtrage d'acteurs
   - Utilisation des scores IA
   - Format de réponse JSON

4. **Exemples de réponses**

---

## 🚨 Gestion des erreurs

### Types d'erreurs

#### 1. Erreurs API Gemini

```kotlin
// Erreur HTTP
if (!response.isSuccessful) {
    val errorCode = response.code()
    val errorMessage = "Erreur Gemini ${errorCode}: ${response.message()}"
    Result.failure(ApiException.UnknownException(errorMessage))
}

// Erreur parsing
catch (e: Exception) {
    Result.failure(ApiException.UnknownException("Erreur parsing: ${e.message}"))
}
```

#### 2. Erreurs Backend CastMate

```kotlin
// Erreur réseau
catch (e: IOException) {
    errorMessage = "Erreur de connexion. Vérifiez votre internet."
}

// Erreur API
refreshResult.onFailure {
    errorMessage = "Erreur lors du chargement du casting"
}
```

#### 3. Erreurs de validation

```kotlin
// Query vide
if (query.isBlank()) {
    return Result.failure(
        ApiException.BadRequestException("La question ne peut pas être vide")
    )
}
```

#### 4. Erreurs de persistance

```kotlin
// Gestion silencieuse (logs uniquement)
catch (e: Exception) {
    android.util.Log.e("ChatConversationRepo", "Erreur: ${e.message}")
    // Retourne liste vide ou valeur par défaut
}
```

### Affichage des erreurs dans l'UI

**Fichier:** `ChatbotContent.kt`

```kotlin
// Variable d'état
var errorMessage by remember { mutableStateOf<String?>(null) }

// Affichage
errorMessage?.let { error ->
    item {
        ErrorBubble(message = error)
    }
}

// Composant d'erreur
@Composable
private fun ErrorBubble(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Text(
            text = "❌ $message",
            color = Color(0xFFD32F2F)
        )
    }
}
```

---

## ⚡ Performance et optimisations

### Optimisations implémentées

#### 1. Rechargement du casting avant chaque requête

```kotlin
// Recharger le casting pour avoir les candidats à jour
val refreshResult = castingRepository.getCastingById(castingId)
refreshResult.onSuccess { updatedCasting ->
    currentCasting = updatedCasting
    // Appeler Gemini avec données à jour
}
```

**Raison:** Garantit que les scores IA et statuts sont à jour.

#### 2. Timeouts adaptés

- **Gemini API:** `60 secondes` (read timeout) - réponses longues
- **Backend API:** `30 secondes` - réponses rapides

#### 3. Parsing JSON optimisé

```kotlin
// Extraction directe du JSON depuis le texte
val jsonStart = answerText.indexOf("{")
val jsonEnd = answerText.lastIndexOf("}") + 1
val jsonText = answerText.substring(jsonStart, jsonEnd)
```

#### 4. Persistance asynchrone

```kotlin
// Sauvegarde en arrière-plan (Dispatchers.IO)
suspend fun saveMessage(...) = withContext(Dispatchers.IO) {
    // Opérations Room
}
```

#### 5. Scroll automatique optimisé

```kotlin
// Scroll uniquement si nouveaux messages
LaunchedEffect(chatHistory.size) {
    if (chatHistory.isNotEmpty()) {
        listState.animateScrollToItem(chatHistory.size - 1)
    }
}
```

### Recommandations futures

1. **Cache des réponses Gemini** (pour questions similaires)
2. **Pagination des messages** (si historique très long)
3. **Compression des JSON** (suggestedActors)
4. **Lazy loading** des acteurs suggérés
5. **Debounce** sur la saisie utilisateur

---

## 📝 Exemples d'utilisation

### Exemple 1 : Question simple

**Question utilisateur:**
```
"Trouve-moi les acteurs de 25-35 ans"
```

**Flux:**
1. `ChatbotContent` → Sauvegarde message utilisateur
2. Rechargement casting
3. `GeminiChatbotRepository.queryChatbot()` → Construction prompt
4. Appel Gemini API
5. Parsing réponse JSON
6. Filtrage acteurs 25-35 ans
7. Création `ChatbotResponse`
8. Affichage avec suggestions

**Réponse attendue:**
```json
{
  "answer": "J'ai trouvé 3 acteurs correspondant à votre critère d'âge (25-35 ans)...",
  "suggestedActors": [
    {
      "acteurId": "123",
      "prenom": "Jean",
      "nom": "Dupont",
      "age": 28,
      "matchScore": 0.95,
      "matchReasons": ["Âge correspond (28 ans)", "Score IA élevé (85/100)"]
    }
  ],
  "totalCandidates": 15,
  "filteredCount": 3
}
```

### Exemple 2 : Question avec score IA

**Question utilisateur:**
```
"Qui a le meilleur score IA ?"
```

**Réponse attendue:**
```json
{
  "answer": "L'acteur avec le meilleur score IA est Mohamed Ben Ali avec un score de 92/100. Voici les détails de son analyse...",
  "suggestedActors": [
    {
      "acteurId": "456",
      "prenom": "Mohamed",
      "nom": "Ben Ali",
      "matchScore": 0.98,
      "matchReasons": [
        "Score IA exceptionnel (92/100)",
        "Excellente expressivité (90/100)",
        "Posture professionnelle (88/100)"
      ]
    }
  ]
}
```

### Exemple 3 : Salutation

**Question utilisateur:**
```
"Bonjour"
```

**Réponse attendue:**
```json
{
  "answer": "Bonjour ! Je suis votre assistant IA CastMate pour le casting 'Recherche acteur principal'. J'ai analysé 12 candidat(s), dont 8 avec vidéo d'audition. Voici les meilleurs candidats :",
  "suggestedActors": [
    // Acteurs avec statut ACCEPTE ou scores IA élevés
  ]
}
```

---

## 🔐 Sécurité

### Clé API Gemini

- **Stockage:** Hardcodée dans `GeminiConfig.kt` (⚠️ À sécuriser en production)
- **Recommandation:** Utiliser `BuildConfig` ou variables d'environnement
- **Partage:** Même clé pour chatbot et analyse vidéo

### Authentification Backend

- **Token JWT:** Géré par `TokenManager` et `AuthInterceptor`
- **Stockage:** SharedPreferences (⚠️ À migrer vers Keychain/EncryptedSharedPreferences)
- **Expiration:** Gérée par `ErrorInterceptor` (redirection login)

### Données sensibles

- **Conversations:** Stockées localement uniquement (Room)
- **Pas de synchronisation cloud** (pour l'instant)
- **Suppression CASCADE** si conversation supprimée

---

## 📊 Métriques et monitoring

### Logs disponibles

**Tags de logs:**
- `GeminiChatbot` → Appels Gemini
- `ChatConversationRepo` → Opérations persistance
- `ChatbotContent` → UI et chargement
- `CastMateDatabase` → Base de données

**Exemples:**
```
D/GeminiChatbot: 🤖 Interrogation de Gemini pour casting: Mon Casting
D/GeminiChatbot: 📝 Question: Trouve-moi les acteurs de 25-35 ans
D/GeminiChatbot: ✅ Réponse Gemini reçue: 3 acteurs suggérés
D/ChatConversationRepo: 💬 Message sauvegardé: conversation=1, isBot=false
```

### Statistiques disponibles

```kotlin
val stats = conversationRepository.getStatistics()
// stats.totalConversations
// stats.totalMessages
// stats.averageMessagesPerConversation
```

---

## 🚀 Améliorations futures

### Court terme

1. **Cache des réponses Gemini** (éviter appels redondants)
2. **Indicateur de frais API** (coûts Gemini)
3. **Export de conversation** (PDF, texte)

### Moyen terme

1. **Synchronisation cloud** (Firebase/Backend)
2. **Recherche dans l'historique** (Room FTS)
3. **Suggestions proactives** (acteurs recommandés automatiquement)

### Long terme

1. **Multi-langues** (anglais, arabe)
2. **Voice input** (reconnaissance vocale)
3. **Analytics avancés** (graphiques, tendances)

---

## 📚 Références

### Documentation externe

- **Google Gemini API:** https://ai.google.dev/api/generate-content
- **Retrofit:** https://square.github.io/retrofit/
- **Room Database:** https://developer.android.com/training/data-storage/room
- **Jetpack Compose:** https://developer.android.com/jetpack/compose

### Fichiers clés du projet

- `GeminiChatbotRepository.kt` - Logique métier chatbot
- `ChatbotContent.kt` - Interface utilisateur
- `GeminiApiService.kt` - Service API Gemini
- `ChatConversationRepository.kt` - Persistance
- `GeminiConfig.kt` - Configuration
- `ChatbotModels.kt` - Modèles de données

---

## ✅ Checklist de consommation

### Avant chaque requête

- [ ] Casting rechargé (candidats à jour)
- [ ] Token JWT valide
- [ ] Clé API Gemini configurée
- [ ] Connexion internet disponible

### Après chaque requête

- [ ] Réponse Gemini parsée correctement
- [ ] Acteurs suggérés mappés avec données complètes
- [ ] Message sauvegardé dans Room
- [ ] UI mise à jour avec nouvelles données

### En cas d'erreur

- [ ] Message d'erreur affiché à l'utilisateur
- [ ] Logs détaillés pour débogage
- [ ] État de chargement réinitialisé
- [ ] Pas de crash de l'application

---

**Document créé le:** 2024  
**Version:** 1.0  
**Auteur:** Documentation CastMate


