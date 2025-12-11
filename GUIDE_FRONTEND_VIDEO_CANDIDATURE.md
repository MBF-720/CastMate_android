# 🎬 Guide Frontend - Candidature avec Vidéo d'Audition

## 📋 Vue d'ensemble

L'API backend supporte maintenant les **candidatures avec vidéo d'audition** et le **feedback IA**. Ce guide explique comment consommer ces nouvelles fonctionnalités.

---

## 🚀 Endpoint : Postuler à un casting

### URL

```
POST /castings/:id/apply
```

### Authentification

- **Requis** : JWT Token (rôle ACTEUR uniquement)
- **Header** : `Authorization: Bearer <token>`

---

## 📤 Candidature avec Vidéo d'Audition

### Format de la requête

- **Content-Type** : `multipart/form-data`
- **Body** :
  - `video` : File (optionnel) - Vidéo d'audition
  - `aiFeedback` : String (optionnel) - JSON string du feedback IA

### Contraintes Vidéo

- **Taille maximale** : 50 MB
- **Durée maximale** : 30 secondes (vérification côté frontend recommandée)
- **Formats supportés** :
  - `video/mp4`
  - `video/mpeg`
  - `video/quicktime` (MOV)
  - `video/x-msvideo` (AVI)
  - `video/webm`

### Exemple JavaScript/TypeScript

#### Avec Fetch API

```typescript
async function postulerAvecVideo(
  castingId: string,
  videoFile: File,
  aiFeedback?: any,
  token: string
) {
  const formData = new FormData();
  
  // Ajouter la vidéo
  formData.append('video', videoFile);
  
  // Ajouter le feedback IA si disponible
  if (aiFeedback) {
    formData.append('aiFeedback', JSON.stringify(aiFeedback));
  }
  
  const response = await fetch(`/api/castings/${castingId}/apply`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      // ⚠️ NE PAS définir Content-Type manuellement pour FormData
      // Le navigateur le fera automatiquement avec le boundary
    },
    body: formData,
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Erreur lors de la candidature');
  }
  
  return await response.json();
}
```

#### Avec Axios

```typescript
import axios from 'axios';

async function postulerAvecVideo(
  castingId: string,
  videoFile: File,
  aiFeedback?: any,
  token: string
) {
  const formData = new FormData();
  formData.append('video', videoFile);
  
  if (aiFeedback) {
    formData.append('aiFeedback', JSON.stringify(aiFeedback));
  }
  
  const response = await axios.post(
    `/api/castings/${castingId}/apply`,
    formData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data',
      },
    }
  );
  
  return response.data;
}
```

#### Exemple complet avec React

```typescript
import { useState } from 'react';
import axios from 'axios';

function CandidatureForm({ castingId, token }: { castingId: string; token: string }) {
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [aiFeedback, setAiFeedback] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fonction appelée après l'analyse IA de la vidéo
  const handleVideoAnalysis = (feedback: any) => {
    setAiFeedback(feedback);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!videoFile) {
      setError('Veuillez sélectionner une vidéo');
      return;
    }

    // Vérifier la taille (50 MB max)
    if (videoFile.size > 50 * 1024 * 1024) {
      setError('La vidéo ne doit pas dépasser 50 MB');
      return;
    }

    // Vérifier la durée (30s max) - exemple avec une bibliothèque
    // const duration = await getVideoDuration(videoFile);
    // if (duration > 30) {
    //   setError('La vidéo ne doit pas dépasser 30 secondes');
    //   return;
    // }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('video', videoFile);
      
      if (aiFeedback) {
        formData.append('aiFeedback', JSON.stringify(aiFeedback));
      }

      const response = await axios.post(
        `/api/castings/${castingId}/apply`,
        formData,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      console.log('Candidature envoyée avec succès:', response.data);
      // Rediriger ou afficher un message de succès
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erreur lors de l\'envoi de la candidature');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="file"
        accept="video/mp4,video/mpeg,video/quicktime,video/x-msvideo,video/webm"
        onChange={(e) => setVideoFile(e.target.files?.[0] || null)}
      />
      <button type="submit" disabled={loading || !videoFile}>
        {loading ? 'Envoi...' : 'Postuler'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}
```

---

## 📤 Candidature sans Vidéo (Rétrocompatibilité)

### Format de la requête

- **Content-Type** : `application/json` ou `multipart/form-data` (body vide)
- **Body** : Vide ou `{}`

### Exemple

```typescript
// Méthode 1 : Body vide avec multipart/form-data
async function postulerSansVideo(castingId: string, token: string) {
  const formData = new FormData();
  
  const response = await fetch(`/api/castings/${castingId}/apply`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: formData,
  });
  
  return await response.json();
}

// Méthode 2 : Body vide (compatible avec l'ancien comportement)
async function postulerSansVideo(castingId: string, token: string) {
  const response = await fetch(`/api/castings/${castingId}/apply`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({}),
  });
  
  return await response.json();
}
```

---

## 📊 Structure du Feedback IA

### Format JSON attendu

```typescript
interface AiFeedback {
  globalScore: number; // 0-100
  emotions: {
    detected: string[]; // ["joie", "tristesse", etc.]
    coherence: number; // 0-100
    intensity: number; // 0-100
    comment: string;
  };
  posture: {
    score: number; // 0-100
    strengths: string[];
    improvements: string[];
    comment: string;
  };
  intonation: {
    score: number; // 0-100
    clarity: number; // 0-100
    rhythm: number; // 0-100
    expressiveness: number; // 0-100
    comment: string;
  };
  expressivite: {
    score: number; // 0-100
    facialExpressions: string;
    bodyLanguage: string;
    comment: string;
  };
  recommendations: string[];
  strengths: string[];
  summary: string;
  analyzedAt?: string; // ISO date string (optionnel, sera ajouté par le backend)
}
```

### Exemple de Feedback IA

```typescript
const aiFeedback: AiFeedback = {
  globalScore: 75,
  emotions: {
    detected: ['joie', 'détermination'],
    coherence: 80,
    intensity: 70,
    comment: 'Les émotions sont bien exprimées avec une bonne cohérence.',
  },
  posture: {
    score: 70,
    strengths: ['Bonne posture', 'Présence scénique'],
    improvements: ['Varier les positions'],
    comment: 'Posture correcte avec une bonne présence.',
  },
  intonation: {
    score: 75,
    clarity: 80,
    rhythm: 70,
    expressiveness: 75,
    comment: 'Bonne diction et rythme, pourrait être plus expressif.',
  },
  expressivite: {
    score: 80,
    facialExpressions: 'Expressif et naturel',
    bodyLanguage: 'Gestes appropriés',
    comment: 'Très bonne expressivité faciale et corporelle.',
  },
  recommendations: [
    'Varier le ton de voix',
    'Plus d\'expressivité dans les gestes',
  ],
  strengths: [
    'Bonne diction',
    'Présence scénique',
    'Expressivité faciale',
  ],
  summary: 'Performance solide avec une bonne base technique. Quelques améliorations possibles pour varier le ton et l\'expressivité.',
};
```

---

## 📥 Récupérer la Vidéo d'Audition

### URL

```
GET /castings/:id/candidates/:acteurId/video
```

### Authentification

- **Requis** : JWT Token
- **Header** : `Authorization: Bearer <token>`
- **Permissions** :
  - Acteur propriétaire de la vidéo
  - Recruteur propriétaire du casting
  - Admin

### Exemple

```typescript
async function getCandidateVideo(
  castingId: string,
  acteurId: string,
  token: string
) {
  const response = await fetch(
    `/api/castings/${castingId}/candidates/${acteurId}/video`,
    {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    }
  );
  
  if (!response.ok) {
    throw new Error('Erreur lors de la récupération de la vidéo');
  }
  
  // Récupérer le blob vidéo
  const blob = await response.blob();
  const videoUrl = URL.createObjectURL(blob);
  
  return videoUrl;
}

// Utilisation dans React
function VideoPlayer({ castingId, acteurId, token }: Props) {
  const [videoUrl, setVideoUrl] = useState<string | null>(null);
  
  useEffect(() => {
    getCandidateVideo(castingId, acteurId, token)
      .then(setVideoUrl)
      .catch(console.error);
    
    return () => {
      if (videoUrl) {
        URL.revokeObjectURL(videoUrl);
      }
    };
  }, [castingId, acteurId, token]);
  
  if (!videoUrl) return <div>Chargement...</div>;
  
  return (
    <video controls src={videoUrl}>
      Votre navigateur ne supporte pas la lecture vidéo.
    </video>
  );
}
```

---

## 📋 Réponse de l'API

### Structure de la réponse GET /castings/:id

```typescript
interface CastingResponse {
  _id: string;
  titre: string;
  // ... autres champs du casting
  candidats: {
    acteurId: {
      _id: string;
      nom: string;
      prenom: string;
      email: string;
      // ...
    };
    statut: 'EN_ATTENTE' | 'ACCEPTE' | 'REFUSE';
    dateCandidature: string; // ISO date
    auditionVideo?: {
      fileId: string; // ID pour récupérer la vidéo via GET /castings/:id/candidates/:acteurId/video
      mimeType: string;
      duration?: number; // Durée en secondes (si disponible)
      size: number; // Taille en bytes
      uploadedAt: string; // ISO date
    };
    aiFeedback?: {
      globalScore: number;
      emotions: {
        detected: string[];
        coherence: number;
        intensity: number;
        comment: string;
      };
      posture: {
        score: number;
        strengths: string[];
        improvements: string[];
        comment: string;
      };
      intonation: {
        score: number;
        clarity: number;
        rhythm: number;
        expressiveness: number;
        comment: string;
      };
      expressivite: {
        score: number;
        facialExpressions: string;
        bodyLanguage: string;
        comment: string;
      };
      recommendations: string[];
      strengths: string[];
      summary: string;
      analyzedAt: string; // ISO date
    };
  }[];
}
```

---

## ⚠️ Codes d'erreur HTTP

| Code | Description |
|------|-------------|
| `200` | Succès |
| `400` | Casting fermé, vidéo invalide, ou format non supporté |
| `401` | Non authentifié |
| `403` | Accès refusé (rôle incorrect ou pas de permission) |
| `404` | Casting, candidat ou vidéo non trouvé |
| `409` | Candidature déjà existante |

---

## 🔄 Workflow Recommandé

### 1. Enregistrement de la vidéo

```typescript
// L'utilisateur enregistre une vidéo (max 30s)
const videoBlob = await recordVideo(maxDuration: 30);
```

### 2. Validation côté frontend

```typescript
// Vérifier la taille
if (videoBlob.size > 50 * 1024 * 1024) {
  throw new Error('Vidéo trop volumineuse (max 50MB)');
}

// Vérifier la durée
const duration = await getVideoDuration(videoBlob);
if (duration > 30) {
  throw new Error('Vidéo trop longue (max 30s)');
}
```

### 3. Analyse IA (Gemini)

```typescript
// Envoyer la vidéo à Gemini AI pour analyse
const aiFeedback = await analyzeVideoWithGemini(videoBlob);
```

### 4. Envoi de la candidature

```typescript
// Créer un File à partir du blob
const videoFile = new File([videoBlob], 'audition.mp4', { type: 'video/mp4' });

// Envoyer au backend
await postulerAvecVideo(castingId, videoFile, aiFeedback, token);
```

---

## 🧪 Exemple complet avec 2 essais

```typescript
interface VideoAttempt {
  video: File;
  aiFeedback: any;
  attemptNumber: number;
}

function CandidatureAvecEssais({ castingId, token }: Props) {
  const [attempts, setAttempts] = useState<VideoAttempt[]>([]);
  const [currentVideo, setCurrentVideo] = useState<File | null>(null);
  const [currentFeedback, setCurrentFeedback] = useState<any>(null);
  const maxAttempts = 2;

  const handleRecordVideo = async () => {
    // Enregistrer la vidéo
    const videoBlob = await recordVideo(30);
    const videoFile = new File([videoBlob], 'audition.mp4', { type: 'video/mp4' });
    setCurrentVideo(videoFile);

    // Analyser avec Gemini
    const feedback = await analyzeVideoWithGemini(videoBlob);
    setCurrentFeedback(feedback);
  };

  const handleSaveAttempt = () => {
    if (!currentVideo || !currentFeedback) return;

    const newAttempt: VideoAttempt = {
      video: currentVideo,
      aiFeedback: currentFeedback,
      attemptNumber: attempts.length + 1,
    };

    setAttempts([...attempts, newAttempt]);
    setCurrentVideo(null);
    setCurrentFeedback(null);
  };

  const handleSubmit = async () => {
    if (attempts.length === 0) {
      alert('Veuillez enregistrer au moins une vidéo');
      return;
    }

    // Utiliser la dernière tentative (ou la meilleure selon le score)
    const bestAttempt = attempts.reduce((best, current) =>
      current.aiFeedback.globalScore > best.aiFeedback.globalScore
        ? current
        : best
    );

    await postulerAvecVideo(
      castingId,
      bestAttempt.video,
      bestAttempt.aiFeedback,
      token
    );
  };

  return (
    <div>
      <h2>Candidature avec Vidéo d'Audition</h2>
      
      {attempts.length < maxAttempts && (
        <div>
          <button onClick={handleRecordVideo}>
            Enregistrer une vidéo (Essai {attempts.length + 1}/{maxAttempts})
          </button>
          
          {currentVideo && currentFeedback && (
            <div>
              <p>Score: {currentFeedback.globalScore}/100</p>
              <button onClick={handleSaveAttempt}>
                Sauvegarder cet essai
              </button>
            </div>
          )}
        </div>
      )}

      {attempts.length > 0 && (
        <div>
          <h3>Essais enregistrés ({attempts.length}/{maxAttempts})</h3>
          {attempts.map((attempt, index) => (
            <div key={index}>
              <p>Essai {attempt.attemptNumber} - Score: {attempt.aiFeedback.globalScore}/100</p>
            </div>
          ))}
          
          <button onClick={handleSubmit} disabled={attempts.length === 0}>
            Envoyer la candidature
          </button>
        </div>
      )}
    </div>
  );
}
```

---

## 📝 Notes importantes

1. **Content-Type** : Ne pas définir manuellement `Content-Type` pour `FormData` avec Fetch API. Le navigateur le fait automatiquement avec le boundary approprié.

2. **Taille de la vidéo** : Vérifier côté frontend avant l'envoi pour éviter les erreurs 400.

3. **Durée de la vidéo** : La vérification de la durée (30s max) doit être faite côté frontend. Le backend accepte la vidéo sans vérifier la durée pour l'instant.

4. **Feedback IA** : Optionnel. Si non fourni, la candidature sera créée sans feedback IA.

5. **Rétrocompatibilité** : Les candidatures sans vidéo continuent de fonctionner comme avant.

6. **Permissions vidéo** : Seuls le recruteur propriétaire du casting, l'acteur concerné, ou un admin peuvent accéder aux vidéos d'audition.

---

## 🆘 Support

Pour toute question ou problème, contactez l'équipe backend.

