# 🐛 Fix Backend : Normalisation des URLs d'affiches (afficheUrl)

## 📋 Problème identifié

Le backend retourne actuellement des URLs avec `http://localhost:3000` dans le champ `afficheUrl` des castings, ce qui empêche le frontend Android de charger les images correctement.

### Exemple de réponse actuelle (incorrecte) :
```json
{
  "media": {
    "afficheUrl": "http://localhost:3000/media/6915b9169293304d0e454646"
  }
}
```

### Réponse attendue (correcte) :
```json
{
  "media": {
    "afficheUrl": "https://cast-mate.vercel.app/media/6915b9169293304d0e454646"
  }
}
```

## 🎯 Solution requise

Le backend doit normaliser les URLs dans le champ `afficheUrl` pour utiliser l'URL de production au lieu de `localhost:3000`.

### Fichiers à modifier

1. **Controller ou Service qui génère les réponses de castings**
   - Probablement dans : `controllers/castingController.js` ou similaire
   - Ou dans le modèle : `models/Casting.js` si la transformation se fait au niveau du modèle

### Code à implémenter

#### Option 1 : Normalisation dans le modèle (recommandé)

Si vous utilisez Mongoose, ajoutez une méthode virtuelle ou un transform dans le schéma :

```javascript
// Dans votre modèle Casting
const castingSchema = new Schema({
  // ... autres champs ...
  media: {
    afficheFileId: String,
    afficheUrl: String,
    // ... autres champs media ...
  }
});

// Méthode pour normaliser l'URL
castingSchema.methods.normalizeAfficheUrl = function() {
  if (this.media && this.media.afficheUrl) {
    // Remplacer localhost par l'URL de production
    const PRODUCTION_URL = process.env.PRODUCTION_URL || 'https://cast-mate.vercel.app';
    this.media.afficheUrl = this.media.afficheUrl
      .replace('http://localhost:3000', PRODUCTION_URL)
      .replace('http://127.0.0.1:3000', PRODUCTION_URL);
  }
  return this;
};

// Ou utiliser un transform dans toJSON
castingSchema.set('toJSON', {
  transform: function(doc, ret) {
    if (ret.media && ret.media.afficheUrl) {
      const PRODUCTION_URL = process.env.PRODUCTION_URL || 'https://cast-mate.vercel.app';
      ret.media.afficheUrl = ret.media.afficheUrl
        .replace('http://localhost:3000', PRODUCTION_URL)
        .replace('http://127.0.0.1:3000', PRODUCTION_URL);
    }
    return ret;
  }
});
```

#### Option 2 : Normalisation dans le controller

Dans votre controller, avant de retourner les données :

```javascript
// Dans castingController.js ou similaire
const normalizeAfficheUrl = (casting) => {
  if (casting.media && casting.media.afficheUrl) {
    const PRODUCTION_URL = process.env.PRODUCTION_URL || 'https://cast-mate.vercel.app';
    casting.media.afficheUrl = casting.media.afficheUrl
      .replace('http://localhost:3000', PRODUCTION_URL)
      .replace('http://127.0.0.1:3000', PRODUCTION_URL);
  }
  return casting;
};

// Exemple d'utilisation dans une route
app.get('/castings', async (req, res) => {
  const castings = await Casting.find();
  const normalizedCastings = castings.map(normalizeAfficheUrl);
  res.json(normalizedCastings);
});
```

#### Option 3 : Normalisation à la source (lors de la création de l'URL)

Si vous générez l'URL quelque part (par exemple lors de l'upload d'une affiche), utilisez directement l'URL de production :

```javascript
// Lors de la création/sauvegarde d'un casting
const PRODUCTION_URL = process.env.PRODUCTION_URL || 'https://cast-mate.vercel.app';
const afficheUrl = `${PRODUCTION_URL}/media/${fileId}`;

casting.media = {
  afficheFileId: fileId,
  afficheUrl: afficheUrl, // ✅ URL de production dès le départ
  // ... autres champs ...
};
```

## 🔧 Configuration recommandée

Ajoutez une variable d'environnement pour l'URL de production :

```env
# .env
PRODUCTION_URL=https://cast-mate.vercel.app
```

Puis utilisez-la dans votre code :

```javascript
const PRODUCTION_URL = process.env.PRODUCTION_URL || 'https://cast-mate.vercel.app';
```

## ✅ Points à vérifier

1. **Toutes les routes qui retournent des castings** :
   - `GET /castings` (liste des castings)
   - `GET /castings/:id` (détail d'un casting)
   - `GET /castings/agency/:agencyId` (castings d'une agence)
   - Toute autre route retournant des objets Casting

2. **Format de l'URL** :
   - L'URL doit être complète : `https://cast-mate.vercel.app/media/{fileId}`
   - Pas de slash final : `https://cast-mate.vercel.app/media/...` (pas `.../media/.../`)

3. **Compatibilité** :
   - Le frontend Android gère déjà un fallback pour les anciens castings sans `afficheUrl`
   - Mais il est préférable que le backend retourne toujours des URLs valides

## 🧪 Tests à effectuer

1. Vérifier que les URLs retournées utilisent `https://cast-mate.vercel.app` et non `localhost:3000`
2. Tester avec un casting existant qui a déjà une `afficheUrl` avec `localhost`
3. Tester avec un nouveau casting créé après le fix
4. Vérifier que les images se chargent correctement depuis le frontend Android

## 📝 Notes

- Le frontend Android a déjà une normalisation en place comme solution temporaire
- Mais la vraie solution est que le backend retourne directement les bonnes URLs
- Cela évite aussi des problèmes si d'autres clients (web, iOS) utilisent l'API











