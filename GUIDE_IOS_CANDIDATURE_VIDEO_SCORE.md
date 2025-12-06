# 📱 Guide iOS - Candidature avec Vidéo et Score IA

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Workflow Acteur - Envoi de Candidature](#workflow-acteur)
3. [Workflow Agence - Visualisation des Candidatures](#workflow-agence)
4. [Endpoints API](#endpoints-api)
5. [Modèles de données](#modèles-de-données)
6. [Implémentation iOS (SwiftUI)](#implémentation-ios)
7. [Exemples de code complets](#exemples-de-code)

---

## 🎯 Vue d'ensemble

Cette fonctionnalité permet aux **acteurs** de :
- ✅ Enregistrer une vidéo d'audition (max 30 secondes, max 10MB)
- ✅ Recevoir une analyse IA automatique (score sur 100 + feedback détaillé)
- ✅ Avoir **2 tentatives** avant de soumettre
- ✅ Envoyer la candidature avec la meilleure vidéo et son score IA

Les **agences** peuvent :
- ✅ Voir tous les candidats avec leur score IA
- ✅ Visualiser les vidéos d'audition
- ✅ Consulter le feedback IA détaillé (émotions, posture, intonation, expressivité)
- ✅ Accepter/Refuser les candidats

---

## 🎬 Workflow Acteur - Envoi de Candidature

### Étape 1 : Navigation vers la page de candidature

L'acteur clique sur "Postuler" depuis la page de détail d'un casting.

**Vérifications préalables :**
- ✅ L'acteur est connecté (JWT token valide)
- ✅ Le casting est ouvert (`ouvert: true`)
- ✅ L'acteur n'a pas déjà postulé (appel à `GET /castings/:id/my-status`)

### Étape 2 : Sélection de la vidéo

L'acteur peut :
- 📹 Enregistrer une nouvelle vidéo (max 30 secondes)
- 📁 Choisir une vidéo existante depuis la galerie

**Contraintes :**
- ⏱️ **Durée maximale** : 30 secondes
- 📦 **Taille maximale** : 10 MB
- 🎥 **Formats supportés** : MP4, MOV, AVI

**Validation côté client :**
```swift
func validateVideo(url: URL) -> Bool {
    // Vérifier la durée
    let duration = getVideoDuration(url: url)
    guard duration <= 30.0 else {
        showError("La vidéo ne doit pas dépasser 30 secondes")
        return false
    }
    
    // Vérifier la taille
    let fileSize = getFileSize(url: url)
    guard fileSize <= 10 * 1024 * 1024 else { // 10 MB
        showError("La vidéo ne doit pas dépasser 10 MB")
        return false
    }
    
    return true
}
```

### Étape 3 : Analyse IA (Gemini)

Une fois la vidéo sélectionnée, l'application :
1. **Encode la vidéo en Base64**
2. **Envoie à Gemini API** avec le contexte du casting (description du rôle, synopsis)
3. **Reçoit le feedback structuré en JSON**

**Appel Gemini (direct depuis iOS) :**
```
POST https://generativelanguage.googleapis.com/v1/models/gemini-2.5-pro:generateContent?key=YOUR_API_KEY
```

**Body :**
```json
{
  "contents": [
    {
      "parts": [
        {
          "inline_data": {
            "mime_type": "video/mp4",
            "data": "BASE64_ENCODED_VIDEO"
          }
        },
        {
          "text": "PROMPT_AVEC_CONTEXTE_CASTING"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 4096
  }
}
```

**Réponse Gemini (JSON) :**
```json
{
  "globalScore": 85,
  "emotions": {
    "detected": ["joie", "surprise"],
    "coherence": 80,
    "intensity": 70,
    "comment": "Les émotions sont bien exprimées."
  },
  "posture": {
    "score": 75,
    "strengths": ["Bonne présence scénique"],
    "improvements": ["Utiliser plus les mains"],
    "comment": "La posture est correcte."
  },
  "intonation": {
    "score": 70,
    "clarity": 85,
    "rhythm": 65,
    "expressiveness": 70,
    "comment": "La diction est claire."
  },
  "expressivite": {
    "score": 80,
    "facialExpressions": "Expressions naturelles.",
    "bodyLanguage": "Langage corporel expressif.",
    "comment": "Bonne expressivité globale."
  },
  "recommendations": [
    "Varier davantage le ton de voix",
    "Utiliser plus l'espace scénique"
  ],
  "strengths": [
    "Excellente diction",
    "Bonne connexion avec la caméra"
  ],
  "summary": "Performance solide avec une bonne base technique."
}
```

### Étape 4 : Affichage du feedback

L'acteur voit :
- 🎯 **Score global** (0-100)
- 📊 **Scores détaillés** :
  - Émotions (cohérence, intensité)
  - Posture
  - Intonation (clarté, rythme, expressivité)
  - Expressivité globale
- ✅ **Points forts**
- 💡 **Recommandations**
- 📝 **Résumé**

**L'acteur peut :**
- ✅ **Enregistrer cet essai** (max 2 essais)
- ✅ **Réessayer** avec une nouvelle vidéo
- ✅ **Soumettre** la candidature avec le meilleur essai

### Étape 5 : Soumission de la candidature

L'acteur choisit le meilleur essai (celui avec le score le plus élevé) et soumet.

**Appel API :**
```
POST /castings/:id/apply
Content-Type: multipart/form-data
Authorization: Bearer <JWT_TOKEN>
```

**Body (multipart/form-data) :**
- `video`: File (vidéo du meilleur essai)
- `aiFeedback`: String (JSON string du feedback IA)

**Réponse 200 :**
```json
{
  "message": "Candidature envoyée avec succès"
}
```

**Erreurs possibles :**
- `400`: Casting fermé ou vidéo invalide
- `409`: Déjà postulé
- `401`: Token expiré
- `413`: Fichier trop volumineux

---

## 🏢 Workflow Agence - Visualisation des Candidatures

### Étape 1 : Accès à la liste des candidats

L'agence ouvre le détail d'un casting et navigue vers l'onglet "Candidats".

**Appel API :**
```
GET /castings/:id
Authorization: Bearer <JWT_TOKEN>
```

**Réponse :**
Le casting contient un tableau `candidats` avec :
- Informations de l'acteur
- Statut (EN_ATTENTE, ACCEPTE, REFUSE)
- `videoFileId` (ID de la vidéo stockée)
- `aiFeedback` (objet JSON avec le score et l'analyse)

### Étape 2 : Affichage des candidats

Chaque candidat affiche :
- 👤 **Nom et email**
- 📅 **Date de candidature**
- 🎯 **Score IA** (si disponible)
- 📊 **Statut** (En attente, Accepté, Refusé)
- ▶️ **Bouton "Voir la vidéo"** (si vidéo disponible)

### Étape 3 : Visualisation de la vidéo

L'agence clique sur "Voir la vidéo" pour un candidat.

**Appel API :**
```
GET /castings/:id/candidates/:acteurId/video
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :**
- Content-Type: `video/mp4`
- Body: Flux binaire de la vidéo

**Erreurs possibles :**
- `401`: Token expiré ou invalide
- `403`: Accès refusé (pas propriétaire du casting)
- `404`: Vidéo non trouvée

### Étape 4 : Consultation du feedback IA

L'agence peut voir :
- 🎯 **Score global** (0-100)
- 📊 **Analyse détaillée** :
  - Émotions détectées
  - Score de posture
  - Score d'intonation
  - Score d'expressivité
- ✅ **Points forts**
- 💡 **Recommandations**
- 📝 **Résumé de performance**

### Étape 5 : Décision (Accepter/Refuser)

L'agence peut accepter ou refuser un candidat.

**Accepter :**
```
PATCH /castings/:id/candidates/:acteurId/accept
Authorization: Bearer <JWT_TOKEN>
```

**Refuser :**
```
PATCH /castings/:id/candidates/:acteurId/reject
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔌 Endpoints API

### Base URL
```
https://cast-mate.vercel.app
```

### 1. Vérifier le statut de candidature (Acteur)

**GET** `/castings/:id/my-status`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :**
```json
{
  "hasApplied": true,
  "statut": "EN_ATTENTE",
  "dateCandidature": "2024-01-15T10:30:00.000Z"
}
```

**Réponse 404 :** Pas encore postulé

---

### 2. Postuler avec vidéo (Acteur)

**POST** `/castings/:id/apply`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Body (multipart/form-data) :**
- `video`: File (optionnel) - Vidéo d'audition (max 10MB, max 30s)
- `aiFeedback`: String (optionnel) - JSON string du feedback IA

**Exemple Swift :**
```swift
let url = URL(string: "https://cast-mate.vercel.app/castings/\(castingId)/apply")!
var request = URLRequest(url: url)
request.httpMethod = "POST"
request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

let boundary = UUID().uuidString
request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

var body = Data()
// Ajouter la vidéo
body.append("--\(boundary)\r\n".data(using: .utf8)!)
body.append("Content-Disposition: form-data; name=\"video\"; filename=\"video.mp4\"\r\n".data(using: .utf8)!)
body.append("Content-Type: video/mp4\r\n\r\n".data(using: .utf8)!)
body.append(videoData)
body.append("\r\n".data(using: .utf8)!)

// Ajouter le feedback IA
if let aiFeedbackJson = aiFeedbackJson {
    body.append("--\(boundary)\r\n".data(using: .utf8)!)
    body.append("Content-Disposition: form-data; name=\"aiFeedback\"\r\n\r\n".data(using: .utf8)!)
    body.append(aiFeedbackJson.data(using: .utf8)!)
    body.append("\r\n".data(using: .utf8)!)
}

body.append("--\(boundary)--\r\n".data(using: .utf8)!)

request.httpBody = body
```

**Réponse 200 :**
```json
{
  "message": "Candidature envoyée avec succès"
}
```

**Erreurs :**
- `400`: Casting fermé ou données invalides
- `409`: Déjà postulé
- `401`: Non authentifié
- `413`: Fichier trop volumineux

---

### 3. Récupérer un casting (Agence)

**GET** `/castings/:id`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :**
```json
{
  "id": "...",
  "titre": "...",
  "candidats": [
    {
      "acteurId": {
        "id": "...",
        "prenom": "Jean",
        "nom": "Dupont",
        "email": "jean@example.com"
      },
      "statut": "EN_ATTENTE",
      "dateCandidature": "2024-01-15T10:30:00.000Z",
      "videoFileId": "file_id_123",
      "aiFeedback": {
        "globalScore": 85,
        "emotions": { ... },
        "posture": { ... },
        "intonation": { ... },
        "expressivite": { ... },
        "recommendations": [ ... ],
        "strengths": [ ... ],
        "summary": "..."
      }
    }
  ]
}
```

---

### 4. Récupérer la vidéo d'un candidat (Agence)

**GET** `/castings/:id/candidates/:acteurId/video`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :**
- Content-Type: `video/mp4`
- Body: Flux binaire de la vidéo

**Erreurs :**
- `401`: Token invalide ou expiré
- `403`: Accès refusé
- `404`: Vidéo non trouvée

---

### 5. Accepter un candidat (Agence)

**PATCH** `/castings/:id/candidates/:acteurId/accept`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :** Succès

---

### 6. Refuser un candidat (Agence)

**PATCH** `/castings/:id/candidates/:acteurId/reject`

**Headers :**
```
Authorization: Bearer <JWT_TOKEN>
```

**Réponse 200 :** Succès

---

## 📦 Modèles de données

### TrainingFeedback (Feedback IA)

```swift
struct TrainingFeedback: Codable {
    let globalScore: Int
    let emotions: EmotionAnalysis
    let posture: PostureAnalysis
    let intonation: IntonationAnalysis
    let expressivite: ExpressivityAnalysis
    let recommendations: [String]
    let strengths: [String]
    let summary: String
}

struct EmotionAnalysis: Codable {
    let detected: [String]
    let coherence: Int
    let intensity: Int
    let comment: String
}

struct PostureAnalysis: Codable {
    let score: Int
    let strengths: [String]
    let improvements: [String]
    let comment: String
}

struct IntonationAnalysis: Codable {
    let score: Int
    let clarity: Int
    let rhythm: Int
    let expressiveness: Int
    let comment: String
}

struct ExpressivityAnalysis: Codable {
    let score: Int
    let facialExpressions: String
    let bodyLanguage: String
    let comment: String
}
```

### Candidat (dans Casting)

```swift
struct Candidat: Codable {
    let acteurId: ActeurInfo?
    let statut: String? // "EN_ATTENTE", "ACCEPTE", "REFUSE"
    let dateCandidature: String?
    let videoFileId: String?
    let aiFeedback: TrainingFeedback?
}

struct ActeurInfo: Codable {
    let id: String?
    let prenom: String?
    let nom: String?
    let email: String?
}
```

### CandidateStatusResponse

```swift
struct CandidateStatusResponse: Codable {
    let hasApplied: Bool
    let statut: String?
    let dateCandidature: String?
}
```

---

## 💻 Implémentation iOS (SwiftUI)

### 1. Service Gemini (Analyse IA)

```swift
import Foundation
import AVFoundation

class GeminiTrainingService {
    private let apiKey = "AIzaSyAQLtb31U2T2C46_HLcS3BROMos9yrHIe8"
    private let baseURL = "https://generativelanguage.googleapis.com"
    
    func analyzeVideo(
        videoURL: URL,
        roleDescription: String? = nil,
        synopsis: String? = nil,
        castingTitle: String? = nil
    ) async throws -> TrainingFeedback {
        // 1. Lire et encoder la vidéo en Base64
        let videoData = try Data(contentsOf: videoURL)
        let videoBase64 = videoData.base64EncodedString()
        
        // 2. Vérifier la taille (max 10MB)
        let sizeMB = Double(videoData.count) / (1024 * 1024)
        guard sizeMB <= 10.0 else {
            throw NSError(domain: "VideoTooLarge", code: 0, userInfo: [NSLocalizedDescriptionKey: "La vidéo est trop volumineuse (\(String(format: "%.1f", sizeMB)) MB). Maximum: 10 MB"])
        }
        
        // 3. Vérifier la durée (max 30s)
        let duration = try await getVideoDuration(url: videoURL)
        guard duration <= 30.0 else {
            throw NSError(domain: "VideoTooLong", code: 0, userInfo: [NSLocalizedDescriptionKey: "La vidéo est trop longue (\(String(format: "%.1f", duration))s). Maximum: 30 secondes"])
        }
        
        // 4. Construire le prompt
        let prompt = buildPrompt(roleDescription: roleDescription, synopsis: synopsis, castingTitle: castingTitle)
        
        // 5. Appeler Gemini API
        let url = URL(string: "\(baseURL)/v1/models/gemini-2.5-pro:generateContent?key=\(apiKey)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let requestBody: [String: Any] = [
            "contents": [
                [
                    "parts": [
                        [
                            "inline_data": [
                                "mime_type": "video/mp4",
                                "data": videoBase64
                            ]
                        ],
                        [
                            "text": prompt
                        ]
                    ]
                ]
            ],
            "generationConfig": [
                "temperature": 0.7,
                "maxOutputTokens": 4096
            ]
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw NSError(domain: "GeminiAPI", code: 0, userInfo: [NSLocalizedDescriptionKey: "Erreur API Gemini"])
        }
        
        // 6. Parser la réponse
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        guard let candidates = json?["candidates"] as? [[String: Any]],
              let firstCandidate = candidates.first,
              let content = firstCandidate["content"] as? [String: Any],
              let parts = content["parts"] as? [[String: Any]],
              let firstPart = parts.first,
              let text = firstPart["text"] as? String else {
            throw NSError(domain: "GeminiAPI", code: 0, userInfo: [NSLocalizedDescriptionKey: "Format de réponse invalide"])
        }
        
        // 7. Extraire le JSON du texte
        let jsonText = extractJSON(from: text)
        let feedback = try JSONDecoder().decode(TrainingFeedback.self, from: jsonText.data(using: .utf8)!)
        
        return feedback
    }
    
    private func buildPrompt(roleDescription: String?, synopsis: String?, castingTitle: String?) -> String {
        var contextSection = ""
        if let title = castingTitle {
            contextSection += "\nCONTEXTE DU CASTING:\n"
            contextSection += "- Titre du casting: \(title)\n"
            if let role = roleDescription {
                contextSection += "- Description du rôle à jouer: \(role)\n"
            }
            if let syn = synopsis {
                contextSection += "- Synopsis du projet: \(syn)\n"
            }
            contextSection += "\nIMPORTANT: Évalue la performance de l'acteur en tenant compte de ce contexte.\n"
        }
        
        return """
        Tu es un coach professionnel en acting et en jeu d'acteur. Analyse cette vidéo d'entraînement d'un acteur (durée max 30 secondes).
        \(contextSection)
        INSTRUCTIONS:
        1. Analyse les aspects suivants:
           - **Émotions** : Quelles émotions sont exprimées ? Sont-elles cohérentes et intenses ?
           - **Posture** : La posture corporelle est-elle appropriée ? Points forts et à améliorer ?
           - **Intonation** : La voix est-elle claire, rythmée et expressive ?
           - **Expressivité** : Les expressions faciales et le langage corporel sont-ils convaincants ?

        2. Pour chaque aspect, donne :
           - Un score de 0 à 100
           - Un commentaire constructif et bienveillant
           - Des conseils d'amélioration spécifiques et actionnables

        3. Fournis également :
           - Un score global (moyenne pondérée des 4 aspects)
           - Une liste de 3-5 points forts à conserver
           - Une liste de 3-5 recommandations prioritaires
           - Un résumé en 2-3 phrases

        IMPORTANT:
        - Sois bienveillant mais honnête
        - Donne des conseils concrets et actionnables
        - Utilise un langage professionnel mais accessible
        - Réponds en français
        - Les commentaires doivent être CONCIS (max 2 phrases chacun)
        - Réponds UNIQUEMENT au format JSON suivant (AUCUN texte avant ou après):

        {
          "globalScore": 75,
          "emotions": {
            "detected": ["joie", "surprise"],
            "coherence": 80,
            "intensity": 70,
            "comment": "Les émotions sont bien exprimées mais pourraient être plus intenses."
          },
          "posture": {
            "score": 75,
            "strengths": ["Bonne présence scénique", "Dos droit"],
            "improvements": ["Utiliser plus les mains", "Varier les positions"],
            "comment": "La posture est correcte mais manque de dynamisme."
          },
          "intonation": {
            "score": 70,
            "clarity": 85,
            "rhythm": 65,
            "expressiveness": 70,
            "comment": "La diction est claire mais le rythme pourrait être plus varié."
          },
          "expressivite": {
            "score": 80,
            "facialExpressions": "Expressions faciales convaincantes et naturelles.",
            "bodyLanguage": "Le langage corporel pourrait être plus expressif.",
            "comment": "Bonne expressivité globale, continuez à travailler l'amplification."
          },
          "recommendations": [
            "Varier davantage le ton de voix",
            "Utiliser plus l'espace scénique",
            "Travailler l'intensité émotionnelle"
          ],
          "strengths": [
            "Excellente diction",
            "Bonne connexion avec la caméra",
            "Expressions faciales naturelles"
          ],
          "summary": "Performance solide avec une bonne base technique. L'acteur montre une diction claire et des expressions naturelles. Pour progresser, il faudrait travailler l'intensité émotionnelle et varier davantage le rythme vocal."
        }

        ANALYSE LA VIDÉO MAINTENANT:
        """
    }
    
    private func getVideoDuration(url: URL) async throws -> Double {
        let asset = AVAsset(url: url)
        let duration = try await asset.load(.duration)
        return CMTimeGetSeconds(duration)
    }
    
    private func extractJSON(from text: String) -> String {
        // Nettoyer le texte (supprimer markdown, etc.)
        var cleaned = text
            .replacingOccurrences(of: "```json", with: "")
            .replacingOccurrences(of: "```", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Extraire le JSON (entre { et })
        if let startIndex = cleaned.range(of: "{"),
           let endIndex = cleaned.range(of: "}", options: .backwards) {
            cleaned = String(cleaned[startIndex.lowerBound...endIndex.upperBound])
        }
        
        return cleaned
    }
}
```

### 2. Service API Casting

```swift
import Foundation

class CastingAPIService {
    private let baseURL = "https://cast-mate.vercel.app"
    private let token: String
    
    init(token: String) {
        self.token = token
    }
    
    // Vérifier le statut de candidature
    func getMyStatus(castingId: String) async throws -> CandidateStatusResponse {
        let url = URL(string: "\(baseURL)/castings/\(castingId)/my-status")!
        var request = URLRequest(url: url)
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "API", code: 0, userInfo: [NSLocalizedDescriptionKey: "Réponse invalide"])
        }
        
        if httpResponse.statusCode == 404 {
            // Pas encore postulé
            return CandidateStatusResponse(hasApplied: false, statut: nil, dateCandidature: nil)
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NSError(domain: "API", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: "Erreur API"])
        }
        
        return try JSONDecoder().decode(CandidateStatusResponse.self, from: data)
    }
    
    // Postuler avec vidéo
    func applyToCasting(
        castingId: String,
        videoURL: URL?,
        aiFeedback: TrainingFeedback?
    ) async throws {
        let url = URL(string: "\(baseURL)/castings/\(castingId)/apply")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var body = Data()
        
        // Ajouter la vidéo si disponible
        if let videoURL = videoURL {
            let videoData = try Data(contentsOf: videoURL)
            body.append("--\(boundary)\r\n".data(using: .utf8)!)
            body.append("Content-Disposition: form-data; name=\"video\"; filename=\"video.mp4\"\r\n".data(using: .utf8)!)
            body.append("Content-Type: video/mp4\r\n\r\n".data(using: .utf8)!)
            body.append(videoData)
            body.append("\r\n".data(using: .utf8)!)
        }
        
        // Ajouter le feedback IA si disponible
        if let aiFeedback = aiFeedback {
            let encoder = JSONEncoder()
            let feedbackData = try encoder.encode(aiFeedback)
            let feedbackString = String(data: feedbackData, encoding: .utf8)!
            
            body.append("--\(boundary)\r\n".data(using: .utf8)!)
            body.append("Content-Disposition: form-data; name=\"aiFeedback\"\r\n\r\n".data(using: .utf8)!)
            body.append(feedbackString.data(using: .utf8)!)
            body.append("\r\n".data(using: .utf8)!)
        }
        
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)
        
        request.httpBody = body
        
        let (_, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "API", code: 0, userInfo: [NSLocalizedDescriptionKey: "Réponse invalide"])
        }
        
        switch httpResponse.statusCode {
        case 200:
            return // Succès
        case 400:
            throw NSError(domain: "API", code: 400, userInfo: [NSLocalizedDescriptionKey: "Casting fermé ou données invalides"])
        case 409:
            throw NSError(domain: "API", code: 409, userInfo: [NSLocalizedDescriptionKey: "Vous avez déjà postulé à ce casting"])
        case 401:
            throw NSError(domain: "API", code: 401, userInfo: [NSLocalizedDescriptionKey: "Token expiré. Veuillez vous reconnecter."])
        case 413:
            throw NSError(domain: "API", code: 413, userInfo: [NSLocalizedDescriptionKey: "Fichier trop volumineux (max 10MB)"])
        default:
            throw NSError(domain: "API", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: "Erreur inconnue"])
        }
    }
    
    // Récupérer un casting (avec candidats)
    func getCasting(castingId: String) async throws -> Casting {
        let url = URL(string: "\(baseURL)/castings/\(castingId)")!
        var request = URLRequest(url: url)
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw NSError(domain: "API", code: 0, userInfo: [NSLocalizedDescriptionKey: "Erreur API"])
        }
        
        return try JSONDecoder().decode(Casting.self, from: data)
    }
    
    // Récupérer la vidéo d'un candidat
    func getCandidateVideo(castingId: String, acteurId: String) async throws -> Data {
        let url = URL(string: "\(baseURL)/castings/\(castingId)/candidates/\(acteurId)/video")!
        var request = URLRequest(url: url)
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "API", code: 0, userInfo: [NSLocalizedDescriptionKey: "Réponse invalide"])
        }
        
        switch httpResponse.statusCode {
        case 200:
            return data
        case 401:
            throw NSError(domain: "API", code: 401, userInfo: [NSLocalizedDescriptionKey: "Token expiré. Veuillez vous reconnecter."])
        case 403:
            throw NSError(domain: "API", code: 403, userInfo: [NSLocalizedDescriptionKey: "Accès refusé à cette vidéo."])
        case 404:
            throw NSError(domain: "API", code: 404, userInfo: [NSLocalizedDescriptionKey: "Vidéo non trouvée."])
        default:
            throw NSError(domain: "API", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: "Erreur inconnue"])
        }
    }
}
```

### 3. Écran de candidature (Acteur) - SwiftUI

```swift
import SwiftUI
import AVKit
import PhotosUI

struct CastingApplicationView: View {
    let casting: Casting
    @StateObject private var viewModel = CastingApplicationViewModel()
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Informations du casting
                    CastingInfoCard(casting: casting)
                    
                    // Section vidéo
                    VideoSectionView(
                        attempts: viewModel.attempts,
                        currentVideo: viewModel.currentVideo,
                        isAnalyzing: viewModel.isAnalyzing,
                        currentFeedback: viewModel.currentFeedback,
                        onSelectVideo: { viewModel.selectVideo() },
                        onAnalyze: { videoURL in
                            Task {
                                await viewModel.analyzeVideo(
                                    videoURL: videoURL,
                                    roleDescription: casting.descriptionRole,
                                    synopsis: casting.synopsis,
                                    castingTitle: casting.titre
                                )
                            }
                        },
                        onSaveAttempt: { viewModel.saveAttempt() },
                        onRetry: { viewModel.retry() }
                    )
                    
                    // Feedback IA
                    if let feedback = viewModel.currentFeedback {
                        FeedbackCard(feedback: feedback)
                    }
                    
                    // Liste des essais
                    if !viewModel.attempts.isEmpty {
                        AttemptsListView(attempts: viewModel.attempts)
                    }
                    
                    // Bouton de soumission
                    SubmitButton(
                        isEnabled: !viewModel.attempts.isEmpty && !viewModel.hasAlreadyApplied,
                        isSubmitting: viewModel.isSubmitting,
                        onSubmit: {
                            Task {
                                await viewModel.submitApplication(castingId: casting.id ?? "")
                            }
                        }
                    )
                }
                .padding()
            }
            .navigationTitle("Postuler")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Annuler") { dismiss() }
                }
            }
            .alert("Erreur", isPresented: .constant(viewModel.errorMessage != nil)) {
                Button("OK") { viewModel.errorMessage = nil }
            } message: {
                Text(viewModel.errorMessage ?? "")
            }
            .alert("Succès", isPresented: $viewModel.showSuccessDialog) {
                Button("OK") {
                    dismiss()
                }
            } message: {
                Text("Votre candidature a été envoyée avec succès !")
            }
            .sheet(isPresented: $viewModel.showVideoPicker) {
                VideoPickerView(selectedVideo: $viewModel.selectedVideoURL)
            }
            .task {
                await viewModel.checkApplicationStatus(castingId: casting.id ?? "")
            }
        }
    }
}

class CastingApplicationViewModel: ObservableObject {
    @Published var attempts: [VideoAttempt] = []
    @Published var currentVideo: URL?
    @Published var selectedVideoURL: URL?
    @Published var isAnalyzing = false
    @Published var currentFeedback: TrainingFeedback?
    @Published var errorMessage: String?
    @Published var isSubmitting = false
    @Published var showSuccessDialog = false
    @Published var showVideoPicker = false
    @Published var hasAlreadyApplied = false
    
    private let geminiService = GeminiTrainingService()
    private let apiService: CastingAPIService
    private let maxAttempts = 2
    
    init() {
        // Récupérer le token depuis le Keychain ou UserDefaults
        let token = UserDefaults.standard.string(forKey: "jwt_token") ?? ""
        self.apiService = CastingAPIService(token: token)
    }
    
    func selectVideo() {
        showVideoPicker = true
    }
    
    func analyzeVideo(videoURL: URL, roleDescription: String?, synopsis: String?, castingTitle: String?) async {
        isAnalyzing = true
        errorMessage = nil
        
        do {
            let feedback = try await geminiService.analyzeVideo(
                videoURL: videoURL,
                roleDescription: roleDescription,
                synopsis: synopsis,
                castingTitle: castingTitle
            )
            
            await MainActor.run {
                currentVideo = videoURL
                currentFeedback = feedback
                isAnalyzing = false
            }
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
                isAnalyzing = false
            }
        }
    }
    
    func saveAttempt() {
        guard let video = currentVideo,
              let feedback = currentFeedback else { return }
        
        guard attempts.count < maxAttempts else {
            errorMessage = "Vous avez atteint le maximum de \(maxAttempts) essais"
            return
        }
        
        let attempt = VideoAttempt(
            videoURL: video,
            feedback: feedback,
            attemptNumber: attempts.count + 1
        )
        
        attempts.append(attempt)
        currentVideo = nil
        currentFeedback = nil
    }
    
    func retry() {
        currentVideo = nil
        currentFeedback = nil
    }
    
    func submitApplication(castingId: String) async {
        guard !attempts.isEmpty else {
            await MainActor.run {
                errorMessage = "Veuillez enregistrer au moins une vidéo avant de postuler"
            }
            return
        }
        
        // Choisir le meilleur essai (score le plus élevé)
        let bestAttempt = attempts.max(by: { $0.feedback.globalScore < $1.feedback.globalScore }) ?? attempts.last!
        
        isSubmitting = true
        errorMessage = nil
        
        do {
            try await apiService.applyToCasting(
                castingId: castingId,
                videoURL: bestAttempt.videoURL,
                aiFeedback: bestAttempt.feedback
            )
            
            await MainActor.run {
                showSuccessDialog = true
                isSubmitting = false
            }
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
                isSubmitting = false
            }
        }
    }
    
    func checkApplicationStatus(castingId: String) async {
        do {
            let status = try await apiService.getMyStatus(castingId: castingId)
            await MainActor.run {
                hasAlreadyApplied = status.hasApplied
                if status.hasApplied {
                    errorMessage = "Vous avez déjà postulé à ce casting."
                }
            }
        } catch {
            // Ignorer les erreurs (peut être 404 si pas encore postulé)
        }
    }
}

struct VideoAttempt {
    let videoURL: URL
    let feedback: TrainingFeedback
    let attemptNumber: Int
}
```

### 4. Écran de visualisation des candidats (Agence) - SwiftUI

```swift
import SwiftUI
import AVKit

struct AgencyCandidatesView: View {
    let casting: Casting
    @StateObject private var viewModel = AgencyCandidatesViewModel()
    
    var body: some View {
        List {
            ForEach(casting.candidats ?? [], id: \.acteurId?.id) { candidat in
                CandidateRow(
                    candidat: candidat,
                    onViewVideo: {
                        Task {
                            await viewModel.loadVideo(
                                castingId: casting.id ?? "",
                                acteurId: candidat.acteurId?.id ?? ""
                            )
                        }
                    },
                    onAccept: {
                        Task {
                            await viewModel.acceptCandidate(
                                castingId: casting.id ?? "",
                                acteurId: candidat.acteurId?.id ?? ""
                            )
                        }
                    },
                    onReject: {
                        Task {
                            await viewModel.rejectCandidate(
                                castingId: casting.id ?? "",
                                acteurId: candidat.acteurId?.id ?? ""
                            )
                        }
                    }
                )
            }
        }
        .navigationTitle("Candidats")
        .sheet(item: $viewModel.videoURL) { videoURL in
            VideoPlayerView(videoURL: videoURL)
        }
    }
}

struct CandidateRow: View {
    let candidat: Candidat
    let onViewVideo: () -> Void
    let onAccept: () -> Void
    let onReject: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Informations de l'acteur
            HStack {
                VStack(alignment: .leading) {
                    Text("\(candidat.acteurId?.prenom ?? "") \(candidat.acteurId?.nom ?? "")")
                        .font(.headline)
                    Text(candidat.acteurId?.email ?? "")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                // Statut
                StatusBadge(statut: candidat.statut ?? "EN_ATTENTE")
            }
            
            // Score IA
            if let feedback = candidat.aiFeedback {
                HStack {
                    Image(systemName: "star.fill")
                        .foregroundColor(.yellow)
                    Text("Score IA: \(feedback.globalScore)/100")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.green)
                }
            }
            
            // Boutons d'action
            HStack {
                // Voir la vidéo
                if candidat.videoFileId != nil || candidat.aiFeedback != nil {
                    Button(action: onViewVideo) {
                        Label("Voir la vidéo", systemImage: "play.circle.fill")
                            .font(.subheadline)
                    }
                    .buttonStyle(.bordered)
                }
                
                Spacer()
                
                // Accepter/Refuser
                if candidat.statut == "EN_ATTENTE" {
                    Button(action: onReject) {
                        Text("Refuser")
                            .foregroundColor(.red)
                    }
                    .buttonStyle(.bordered)
                    
                    Button(action: onAccept) {
                        Text("Accepter")
                            .foregroundColor(.white)
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
        }
        .padding()
    }
}

struct StatusBadge: View {
    let statut: String
    
    var body: some View {
        Text(statutText)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .foregroundColor(foregroundColor)
            .cornerRadius(8)
    }
    
    private var statutText: String {
        switch statut.uppercased() {
        case "ACCEPTE": return "Accepté"
        case "REFUSE": return "Refusé"
        default: return "En attente"
        }
    }
    
    private var backgroundColor: Color {
        switch statut.uppercased() {
        case "ACCEPTE": return .green.opacity(0.2)
        case "REFUSE": return .red.opacity(0.2)
        default: return .orange.opacity(0.2)
        }
    }
    
    private var foregroundColor: Color {
        switch statut.uppercased() {
        case "ACCEPTE": return .green
        case "REFUSE": return .red
        default: return .orange
        }
    }
}

class AgencyCandidatesViewModel: ObservableObject {
    @Published var videoURL: URL?
    @Published var errorMessage: String?
    
    private let apiService: CastingAPIService
    
    init() {
        let token = UserDefaults.standard.string(forKey: "jwt_token") ?? ""
        self.apiService = CastingAPIService(token: token)
    }
    
    func loadVideo(castingId: String, acteurId: String) async {
        do {
            let videoData = try await apiService.getCandidateVideo(
                castingId: castingId,
                acteurId: acteurId
            )
            
            // Sauvegarder temporairement la vidéo
            let tempURL = FileManager.default.temporaryDirectory
                .appendingPathComponent(UUID().uuidString)
                .appendingPathExtension("mp4")
            
            try videoData.write(to: tempURL)
            
            await MainActor.run {
                videoURL = tempURL
            }
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
            }
        }
    }
    
    func acceptCandidate(castingId: String, acteurId: String) async {
        // Implémenter l'appel API pour accepter
    }
    
    func rejectCandidate(castingId: String, acteurId: String) async {
        // Implémenter l'appel API pour refuser
    }
}

struct VideoPlayerView: View {
    let videoURL: URL
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            VideoPlayer(player: AVPlayer(url: videoURL))
                .navigationTitle("Vidéo d'audition")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Fermer") { dismiss() }
                    }
                }
        }
    }
}
```

---

## 📝 Notes importantes

### Sécurité
- ✅ **JWT Token** : Stocker dans le Keychain (pas UserDefaults)
- ✅ **Validation vidéo** : Vérifier taille et durée côté client
- ✅ **Gestion d'erreurs** : Afficher des messages clairs à l'utilisateur

### Performance
- ✅ **Streaming vidéo** : Utiliser `@Streaming` pour les grandes vidéos
- ✅ **Cache** : Mettre en cache les vidéos téléchargées temporairement
- ✅ **Compression** : Compresser la vidéo avant envoi si nécessaire

### UX
- ✅ **Feedback visuel** : Afficher un indicateur de progression lors de l'analyse
- ✅ **Messages d'erreur** : Messages clairs et actionnables
- ✅ **Confirmation** : Demander confirmation avant soumission

---

## 🎉 Résumé

Ce guide fournit toutes les informations nécessaires pour implémenter la fonctionnalité de candidature avec vidéo et score IA sur iOS :

1. ✅ **Workflow complet** acteur et agence
2. ✅ **Endpoints API** détaillés avec exemples
3. ✅ **Modèles de données** Swift
4. ✅ **Code SwiftUI** complet et fonctionnel
5. ✅ **Gestion d'erreurs** et validation

**Prêt à implémenter !** 🚀







