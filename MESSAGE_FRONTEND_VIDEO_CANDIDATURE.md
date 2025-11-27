# 📧 Message pour l'Équipe Frontend

## 🎬 Nouvelle Fonctionnalité : Candidature avec Vidéo d'Audition

Bonjour équipe frontend 👋

Nous avons implémenté la fonctionnalité de **candidature avec vidéo d'audition** et **feedback IA**. Voici les informations essentielles pour consommer l'API :

---

## 🚀 Endpoint Modifié

### `POST /castings/:id/apply`

**Changements :**

- Accepte maintenant `multipart/form-data` (au lieu de JSON uniquement)
- Nouveaux champs optionnels :
  - `video` : File (vidéo d'audition, max 50MB, max 30s)
  - `aiFeedback` : String (JSON string du feedback IA)

**Rétrocompatibilité :** ✅ Les candidatures sans vidéo fonctionnent toujours comme avant.

---

## 📤 Exemple d'utilisation

```typescript
// Candidature AVEC vidéo
const formData = new FormData();
formData.append('video', videoFile); // File object
formData.append('aiFeedback', JSON.stringify(aiFeedback)); // Optionnel

await fetch(`/api/castings/${castingId}/apply`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    // ⚠️ Ne pas définir Content-Type pour FormData
  },
  body: formData,
});

// Candidature SANS vidéo (comme avant)
await fetch(`/api/castings/${castingId}/apply`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
  },
  body: new FormData(), // Body vide
});
```

---

## 📥 Nouvel Endpoint : Récupérer la vidéo

### `GET /castings/:id/candidates/:acteurId/video`

Permet de récupérer la vidéo d'audition d'un candidat.

**Permissions :**
- Acteur propriétaire de la vidéo
- Recruteur propriétaire du casting
- Admin

```typescript
const response = await fetch(
  `/api/castings/${castingId}/candidates/${acteurId}/video`,
  {
    headers: { 'Authorization': `Bearer ${token}` },
  }
);
const blob = await response.blob();
const videoUrl = URL.createObjectURL(blob);
```

---

## 📋 Contraintes Vidéo

- **Taille max** : 50 MB
- **Durée max** : 30 secondes (vérification côté frontend recommandée)
- **Formats** : MP4, MPEG, MOV, AVI, WEBM

---

## 📊 Structure du Feedback IA

```typescript
interface AiFeedback {
  globalScore: number; // 0-100
  emotions: { detected: string[]; coherence: number; intensity: number; comment: string; };
  posture: { score: number; strengths: string[]; improvements: string[]; comment: string; };
  intonation: { score: number; clarity: number; rhythm: number; expressiveness: number; comment: string; };
  expressivite: { score: number; facialExpressions: string; bodyLanguage: string; comment: string; };
  recommendations: string[];
  strengths: string[];
  summary: string;
}
```

---

## 📚 Documentation Complète

Un guide détaillé avec exemples complets est disponible dans :

**`GUIDE_FRONTEND_VIDEO_CANDIDATURE.md`**

Ce guide contient :
- ✅ Exemples complets avec Fetch API et Axios
- ✅ Exemple React avec hooks
- ✅ Workflow recommandé (enregistrement → analyse IA → envoi)
- ✅ Exemple avec 2 essais de vidéo
- ✅ Gestion des erreurs
- ✅ Structure complète des réponses API

---

## ⚠️ Points Importants

1. **Content-Type** : Ne pas définir manuellement pour `FormData` (le navigateur le fait automatiquement)
2. **Validation** : Vérifier la taille (50MB) et durée (30s) côté frontend avant l'envoi
3. **Feedback IA** : Optionnel, peut être omis si non disponible
4. **Rétrocompatibilité** : Les candidatures existantes continuent de fonctionner

---

## 🆘 Questions ?

N'hésitez pas à nous contacter si vous avez des questions ou besoin d'aide pour l'intégration.

Bonne intégration ! 🚀

