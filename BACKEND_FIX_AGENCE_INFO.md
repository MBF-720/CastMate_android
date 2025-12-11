# 🔴 PROBLÈME BACKEND - Informations d'agence incomplètes dans les castings

## 📋 Résumé du problème

L'objet `recruteur` retourné dans les endpoints `GET /castings` et `GET /castings/:id` ne contient **pas toutes les informations de l'agence**, notamment :
- ❌ `tel` (téléphone) - **MANQUANT**
- ❌ `gouvernorat` - **MANQUANT**
- ❌ `siteWeb` - **MANQUANT**
- ❌ `description` - **MANQUANT**
- ❌ `socialLinks` (facebook, instagram) - **MANQUANT**

**Seules les informations suivantes sont disponibles :**
- ✅ `id` - **PRÉSENT**
- ✅ `nomAgence` - **PRÉSENT**
- ✅ `responsable` - **PRÉSENT**
- ✅ `email` - **PRÉSENT**
- ✅ `media.photoFileId` - **PRÉSENT** (mais erreur 403 lors du téléchargement)

## 🔍 Détails techniques

### Endpoints concernés
```
GET /castings
GET /castings/:id
```

### Structure actuelle retournée (PROBLÈME CONFIRMÉ)
```json
{
  "recruteur": {
    "id": null,  // ⚠️ PROBLÈME : ID est null
    "nomAgence": "dez ta5tef",
    "responsable": "dez hey",
    "email": "contact@agence-casting.tn",
    "tel": null,  // ⚠️ PROBLÈME : Manquant
    "gouvernorat": null,  // ⚠️ PROBLÈME : Manquant
    "siteWeb": null,  // ⚠️ PROBLÈME : Manquant
    "description": null,  // ⚠️ PROBLÈME : Manquant
    "socialLinks": null,  // ⚠️ PROBLÈME : Manquant
    "media": {
      "photoFileId": "6915d2118b76250e18120477",
      "photoMimeType": "image/jpeg"
    }
  }
}
```

**Logs réels confirmant le problème :**
```
🔍 Recruteur: RecruteurInfo(id=null, nomAgence=dez ta5tef, responsable=dez hey, email=**************, tel=null, gouvernorat=null, siteWeb=null, description=null, socialLinks=null, media=RecruteurMedia(photoFileId=6915d2118b76250e18120477, photoMimeType=image/jpeg))
🔍 Recruteur ID: null
⚠️ ID agence null - utilisation des informations de base du recruteur
```

### Structure attendue
```json
{
  "recruteur": {
    "id": "690b8649159b39cd4e919149",
    "nomAgence": "Agence de Casting Tunis",
    "responsable": "Mohamed Ben Ali",
    "email": "contact@agence-casting.tn",
    "tel": "+21612345678",
    "gouvernorat": "Tunis",
    "siteWeb": "https://agence-casting.tn",
    "description": "Agence spécialisée dans le casting.",
    "socialLinks": {
      "facebook": "https://facebook.com/agence-casting-tunis",
      "instagram": "https://instagram.com/agence_casting_tunis"
    },
    "media": {
      "photoFileId": "6915b8029293304d0e454639",
      "photoMimeType": "image/jpeg"
    }
  }
}
```

## 🎯 Solution requise

### Option 1 : Inclure toutes les informations dans `recruteur` (RECOMMANDÉ)

**Modifier les endpoints `GET /castings` et `GET /castings/:id` pour inclure toutes les informations de l'agence :**

```typescript
// Dans le contrôleur ou service du casting
async getCastingById(id: string): Promise<Casting> {
  const casting = await CastingModel.findById(id)
    .populate({
      path: 'recruteur',
      select: 'id nomAgence responsable email tel gouvernorat siteWeb description socialLinks media', // ⚠️ Ajouter tous les champs
      populate: {
        path: 'media',
        select: 'photoFileId photoMimeType'
      }
    })
    .lean();
    
  return casting;
}
```

**Avantages :**
- ✅ Toutes les informations sont disponibles immédiatement
- ✅ Pas besoin d'appel API supplémentaire
- ✅ Meilleure performance

### Option 2 : Permettre aux acteurs d'accéder à `GET /agence/:id` (SI OPTION 1 IMPOSSIBLE)

**Si pour des raisons de sécurité, vous ne voulez pas inclure toutes les informations dans `recruteur`, permettez aux acteurs d'accéder à `GET /agence/:id` :**

```typescript
// Dans le contrôleur de l'agence
@Get(':id')
async getAgenceById(@Param('id') id: string, @Req() req: Request) {
  // ⚠️ PERMETTRE aux acteurs (rôle ACTEUR) d'accéder aux informations publiques de l'agence
  const agence = await AgenceModel.findById(id)
    .select('nomAgence responsable email tel gouvernorat siteWeb description socialLinks media')
    .lean();
    
  if (!agence) {
    throw new NotFoundException('Agence non trouvée');
  }
  
  return agence;
}
```

**Note :** 
- Actuellement, l'endpoint `GET /agence/:id` retourne probablement un 403 (Forbidden) pour les acteurs, ce qui empêche l'application mobile de récupérer les informations complètes.
- **PROBLÈME CRITIQUE** : Même si `GET /agence/:id` était accessible, on ne peut pas l'appeler car `recruteur.id` est `null` dans les castings, donc on ne connaît pas l'ID de l'agence.

## 📝 Notes importantes

1. **Sécurité** : 
   - Les informations demandées (`tel`, `gouvernorat`, `siteWeb`, `description`, `socialLinks`) sont des informations **publiques** qui peuvent être affichées aux acteurs
   - Ces informations ne sont pas sensibles et peuvent être partagées

2. **Performance** : 
   - **Option 1** est recommandée car elle évite un appel API supplémentaire
   - L'option 1 est plus performante et réduit la latence

3. **Cohérence** : 
   - Les informations de l'agence dans `recruteur` devraient être cohérentes avec les informations retournées par `GET /agence/:id`
   - Actuellement, il y a une incohérence entre ce qui est retourné dans `recruteur` et ce qui est disponible dans le profil complet de l'agence

4. **Téléchargement du logo (erreur 403)** : 
   - ⚠️ **PROBLÈME SÉPARÉ** : L'endpoint `/media/{fileId}` retourne un 403 (Forbidden) lors du téléchargement du logo de l'agence
   - **Message d'erreur** : `{"message":"Accès refusé à ce fichier","error":"Forbidden","statusCode":403}`
   - **Solution suggérée** : Permettre aux acteurs de télécharger les logos des agences (informations publiques)

## 🔗 Références

- Endpoints concernés : `GET /castings`, `GET /castings/:id`
- Modèle concerné : `Agence` avec champs `tel`, `gouvernorat`, `siteWeb`, `description`, `socialLinks`
- Format attendu : Voir `RecruteurInfo` dans `Casting.kt` du frontend pour la structure attendue

---

**Date du rapport :** 13 novembre 2025  
**Priorité :** 🔴 Haute (bloque l'affichage des informations complètes de l'agence)

