# 🔧 Message pour l'équipe Backend - Correction du format JSON

## Problème identifié

L'application Android rencontre des erreurs de parsing JSON lors de la réception des réponses de l'endpoint `POST /training/submit`.

### Erreurs observées

1. **Erreur 1** : `IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 956 path $.session.nftRewardld`
   - Le backend renvoie le champ `nftRewardld` (faute de frappe) au lieu de `nftRewardId`
   - Le champ peut être une chaîne (ID) ou un objet

2. **Erreur 2** : `IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 991 path $.nftReward.trainingSessionId`
   - Le champ `trainingSessionId` dans `nftReward` est envoyé comme une chaîne alors qu'un objet est attendu

---

## Format actuel (incorrect) renvoyé par le backend

### Exemple de réponse problématique :

```json
{
  "session": {
    "id": "session123",
    "acteurId": "actor456",
    "niveau": 1,
    "globalScore": 84,
    "nftRewardld": "nft789",  // ❌ ERREUR 1: Nom de champ incorrect
    "emotions": { ... }
  },
  "nftReward": {
    "id": "nft789",
    "acteurId": "actor456",
    "trainingSessionId": "session123",  // ❌ ERREUR 2: String au lieu d'objet
    "niveau": 1,
    "score": 84,
    "tokenId": "token123",
    "serialNumber": "001"
  }
}
```

---

## Format attendu (correct) par l'application Android

### Solution recommandée :

```json
{
  "session": {
    "id": "session123",
    "_id": "session123",
    "acteurId": "actor456",
    "niveau": 1,
    "globalScore": 84,
    "nftRewardId": {  // ✅ Nom de champ corrigé
      "id": "nft789",
      "_id": "nft789",
      "tokenId": "token123",
      "serialNumber": "001",
      "imageUrl": "https://..."
    },
    "emotions": { ... }
  },
  "nftReward": {
    "id": "nft789",
    "_id": "nft789",
    "acteurId": "actor456",
    "trainingSessionId": {  // ✅ Objet au lieu de string
      "id": "session123",
      "_id": "session123",
      "niveau": 1,
      "globalScore": 84
    },
    "niveau": 1,
    "score": 84,
    "tokenId": "token123",
    "serialNumber": "001"
  }
}
```

---

## Actions à effectuer côté backend

### 1. Corriger le nom du champ `nftRewardld` → `nftRewardId`

**Fichier concerné** : Le modèle/schéma de réponse pour `TrainingSession`

**Correction** :
- ❌ `nftRewardld` → ✅ `nftRewardId`

### 2. S'assurer que `session.nftRewardId` est un objet, pas une string

**Format attendu** :
```typescript
nftRewardId: {
  id?: string;
  _id?: string;
  tokenId?: string;
  serialNumber?: string;
  imageUrl?: string;
} | null
```

**Si seul l'ID est disponible**, utilisez au minimum :
```json
{
  "nftRewardId": {
    "id": "nft789"
  }
}
```

### 3. S'assurer que `nftReward.trainingSessionId` est un objet, pas une string

**Format attendu** :
```typescript
trainingSessionId: {
  id?: string;
  _id?: string;
  niveau?: number;
  globalScore?: number;
} | null
```

**Si seul l'ID est disponible**, utilisez au minimum :
```json
{
  "trainingSessionId": {
    "id": "session123"
  }
}
```

---

## Schéma TypeScript recommandé

```typescript
interface SubmitTrainingResponse {
  session: TrainingSession;
  nftReward?: NFTReward | null;
}

interface TrainingSession {
  id?: string;
  _id?: string;
  acteurId: string;
  niveau: number;
  globalScore: number;
  emotions: TrainingEmotions;
  strengths: string[];
  recommendations: string[];
  nftRewarded: boolean;
  nftRewardId?: NFTRewardInfo | null;  // ✅ Objet, pas string
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface NFTRewardInfo {
  id?: string;
  _id?: string;
  tokenId?: string;
  serialNumber?: string;
  imageUrl?: string;
}

interface NFTReward {
  id?: string;
  _id?: string;
  acteurId: string;
  trainingSessionId?: TrainingSessionInfo | null;  // ✅ Objet, pas string
  niveau: number;
  score: number;
  tokenId: string;
  serialNumber: string;
  transactionId?: string;
  imageUrl?: string;
  metadataUrl?: string;
  status: "PENDING" | "MINTED" | "FAILED";
  mintedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface TrainingSessionInfo {
  id?: string;
  _id?: string;
  niveau?: number;
  globalScore?: number;
}
```

---

## ✅ Workaround retiré (Résolu)

~~Pour éviter de bloquer les utilisateurs, l'équipe Android a implémenté des adaptateurs Gson personnalisés qui :~~
- ~~✅ Gèrent le champ mal orthographié `nftRewardld`~~
- ~~✅ Acceptent les strings ET les objets pour `nftRewardId` et `trainingSessionId`~~
- ~~⚠️ **Mais cela reste un workaround temporaire**~~

**✅ Les workarounds ont été retirés après la correction du backend. L'application utilise maintenant la configuration Gson standard.**

---

## Tests à effectuer après correction

1. ✅ Vérifier que `session.nftRewardId` est bien un objet (ou `null`)
2. ✅ Vérifier que `nftReward.trainingSessionId` est bien un objet (ou `null`)
3. ✅ Vérifier qu'il n'y a plus de champ `nftRewardld` dans la réponse
4. ✅ Tester avec une session sans NFT (vérifier que `nftRewardId` est `null`)
5. ✅ Tester avec une session avec NFT (vérifier que `nftRewardId` est un objet complet)

---

## ✅ Statut : RÉSOLU

**Date de correction backend** : $(date)  
**Date de retrait des workarounds Android** : $(date)  
**Priorité** : ✅ Résolu  
**Impact utilisateur** : ✅ Format JSON cohérent et stable, code Android simplifié

