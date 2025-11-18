# 🔴 PROBLÈME BACKEND - Erreur 500 lors de la récupération d'un casting

## 📋 Résumé du problème

L'endpoint `GET /castings/:id` retourne une erreur **500 Internal Server Error** lors de la récupération d'un casting par son ID.

## 🔍 Détails techniques

### Endpoint concerné
```
GET /castings/:id
Headers: (optionnel) Authorization: Bearer {token}
```

### Comportement actuel
- ✅ La liste des castings (`GET /castings`) fonctionne correctement
- ❌ La récupération d'un casting spécifique (`GET /castings/:id`) retourne **500 Internal Server Error**

### Exemple de requête qui échoue
```
GET /castings/6915b9169293304d0e454645
Headers:
  (optionnel) Authorization: Bearer {token}
  
Response: 500 Internal Server Error
{
  "statusCode": 500,
  "message": "Internal server error"
}
```

### Logs côté client (réels)
```
🎬 Navigation vers castingDetail avec ID: '6915b9169293304d0e454645'
🔍 Chargement du casting avec ID: '6915b9169293304d0e454645'
📞 Appel de getCastingById avec ID: 6915b9169293304d0e454645
📞 Réponse reçue: code=500, isSuccessful=false
❌ Erreur serveur (500): {"statusCode":500,"message":"Internal server error"}
```

**Note importante :** L'ID `6915b9169293304d0e454645` est valide et existe dans la liste des castings (`GET /castings` retourne ce casting avec succès). Le problème se produit uniquement lors de la récupération individuelle.

## 🎯 Solution requise

### ✅ CAUSE IDENTIFIÉE (logs serveur)

**Problème identifié :** Le schéma Mongoose valide le casting lors de la récupération et échoue car un candidat dans le tableau `candidats` n'a pas de `acteurId`.

**Erreur exacte :**
```
Casting validation failed: candidats.0.acteurId: Path `acteurId` is required.
```

**Explication :**
- Le casting a un tableau `candidats` avec au moins un élément (`candidats[0]`)
- Ce candidat a un `acteurId` qui est `undefined` ou `null`
- Le schéma Mongoose exige que `acteurId` soit requis (`required: true`)
- Lors de la récupération avec `.findById()`, Mongoose valide le document et échoue

**Solution :**

### Points à vérifier (PRIORITÉ)

1. **Population des relations** : ⚠️ **PROBLÈME PROBABLE**
   - Vérifier que `recruteur` est correctement populé (objet `RecruteurInfo` avec `id`, `nomAgence`, `responsable`, `email`, `media`)
   - Vérifier que `candidats` est correctement populé avec `acteurId` (objet `ActeurInfo`)
   - Vérifier que `media` (affiche) est correctement structuré
   - **Erreur probable** : Tentative d'accès à un champ qui n'existe pas ou référence circulaire

2. **Sérialisation JSON** : ⚠️ **PROBLÈME PROBABLE**
   - Vérifier que tous les champs sont correctement sérialisés
   - Vérifier qu'il n'y a pas de références circulaires (ex: `recruteur.castings` qui référence le casting)
   - Vérifier que les objets imbriqués (`recruteur.media`, `candidats[].acteurId.media`) sont correctement sérialisés

3. **Validation de l'ID** : Vérifier que l'ID est valide (format MongoDB ObjectId) - **Probablement OK** car l'ID existe dans la liste

4. **Logs serveur** : ⚠️ **CRITIQUE**
   - Vérifier les logs du serveur pour voir l'erreur exacte (stack trace)
   - L'erreur générique "Internal server error" cache la vraie cause

### Code suggéré (SOLUTION DIRECTE)

**Solution 1 : Utiliser `.lean()` pour éviter la validation Mongoose (RECOMMANDÉ)**

```typescript
// Dans le contrôleur ou service du casting
async getCastingById(id: string): Promise<Casting> {
  try {
    // 1. Valider l'ID
    if (!id || !ObjectId.isValid(id)) {
      throw new NotFoundException('ID de casting invalide');
    }

    // 2. Récupérer le casting avec toutes les relations
    // ⚠️ CRITIQUE : Utiliser .lean() AVANT toute opération pour éviter la validation Mongoose
    // ⚠️ CRITIQUE : .lean() convertit en objet JavaScript pur, évite la validation
    const casting = await CastingModel.findById(id)
      .populate({
        path: 'recruteur',
        select: 'id nomAgence responsable email media',
        populate: {
          path: 'media',
          select: 'photoFileId photoMimeType'
        }
      })
      .populate({
        path: 'candidats.acteurId',
        select: 'id nom prenom email media',
        populate: {
          path: 'media',
          select: 'photoFileId photoMimeType'
        }
      })
      .populate({
        path: 'media',
        select: 'afficheFileId afficheMimeType afficheOriginalName afficheLength afficheUploadDate'
      })
      .lean(); // ⚠️ CRITIQUE : Doit être appelé AVANT toute validation

    if (!casting) {
      throw new NotFoundException('Casting non trouvé');
    }

    // 3. Filtrer les candidats invalides (sans acteurId)
    if (casting.candidats && Array.isArray(casting.candidats)) {
      casting.candidats = casting.candidats.filter(
        candidat => candidat.acteurId != null
      );
    }

    return casting;
  } catch (error) {
    console.error('❌ ERREUR lors de la récupération du casting:', error);
    console.error('❌ Stack trace:', error.stack);
    console.error('❌ ID utilisé:', id);
    
    if (error instanceof HttpException) {
      throw error;
    }
    
    throw new InternalServerErrorException(
      `Erreur lors de la récupération du casting: ${error.message}`
    );
  }
}
```

**Solution 2 : Rendre `acteurId` optionnel dans le schéma Mongoose**

```typescript
// Dans le schéma Mongoose du casting
const CandidatSchema = new Schema({
  acteurId: {
    type: Schema.Types.ObjectId,
    ref: 'Acteur',
    required: false, // ⚠️ Changer de true à false
    default: null
  },
  statut: {
    type: String,
    enum: ['EN_ATTENTE', 'ACCEPTE', 'REFUSE'],
    required: true
  },
  dateCandidature: {
    type: Date,
    required: true
  }
});
```

**Solution 3 : Filtrer les candidats invalides avant la validation**

```typescript
// Nettoyer les candidats invalides avant de retourner
if (casting.candidats && Array.isArray(casting.candidats)) {
  casting.candidats = casting.candidats.filter(
    candidat => candidat.acteurId != null && candidat.acteurId !== undefined
  );
  
  // Sauvegarder le casting nettoyé (optionnel, seulement si vous voulez corriger la base de données)
  // await CastingModel.findByIdAndUpdate(id, { candidats: casting.candidats });
}
```

### Structure attendue selon la documentation

```typescript
interface Casting {
  id: string;
  titre: string;
  descriptionRole: string;
  synopsis: string;
  lieu: string;
  dateDebut: string; // Format ISO
  dateFin: string; // Format ISO
  prix: number;
  types?: string[];
  age?: string;
  ouvert: boolean;
  conditions: string;
  recruteur: {
    id: string;
    nomAgence: string;
    responsable: string;
    email: string;
    media?: {
      photoFileId?: string;
      photoMimeType?: string;
    };
  };
  candidats: Array<{
    acteurId: {
      id: string;
      nom: string;
      prenom: string;
      email: string;
      media?: {
        photoFileId?: string;
        photoMimeType?: string;
      };
    };
    statut: 'EN_ATTENTE' | 'ACCEPTE' | 'REFUSE';
    dateCandidature: string; // Format ISO
  }>;
  media?: {
    afficheFileId?: string;
    afficheMimeType?: string;
    afficheOriginalName?: string;
    afficheLength?: number;
    afficheUploadDate?: string;
  };
  createdAt?: string;
  updatedAt?: string;
}
```

## 📝 Notes importantes

1. **Cause identifiée** : ✅ **RÉSOLU** - L'erreur vient de la validation Mongoose qui exige `acteurId` dans `candidats[0]`, mais ce champ est `undefined`. Utiliser `.lean()` ou rendre `acteurId` optionnel dans le schéma.

2. **Tests** : 
   - Tester l'endpoint avec différents IDs pour voir si le problème est spécifique à certains castings
   - Tester avec un casting qui n'a pas de candidats
   - Tester avec un casting qui n'a pas de media (affiche)
   - Tester avec un casting qui a un recruteur sans media

3. **Structure de données** : 
   - Vérifier que la structure de données retournée correspond exactement à la documentation fournie
   - Vérifier qu'il n'y a pas de références circulaires (ex: `recruteur.castings` qui référence le casting)
   - Vérifier que tous les objets imbriqués sont correctement sérialisés

4. **Comparaison avec GET /castings** :
   - `GET /castings` fonctionne correctement et retourne le casting avec l'ID `6915b9169293304d0e454645`
   - `GET /castings/6915b9169293304d0e454645` retourne 500
   - **Question** : Quelle est la différence entre les deux endpoints ? Probablement la population des relations ou la sérialisation

5. **Solution temporaire** :
   - Si le problème persiste, considérer utiliser `GET /castings` et filtrer par ID côté frontend (solution de contournement)

## 🔗 Références

- Endpoint concerné : `GET /castings/:id`
- Modèle concerné : `Casting` avec relations `recruteur`, `candidats`, `media`
- Format attendu : Voir `Casting.kt` dans le frontend pour la structure attendue

---

**Date du rapport :** 13 novembre 2025  
**Priorité :** 🔴 Haute (bloque l'affichage des détails d'un casting)

