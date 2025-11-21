# Prompt pour Cursor IA Backend - Chatbot de Filtrage d'Acteurs

## Contexte
Je développe une application CastMate qui permet aux agences de casting de gérer leurs castings et aux acteurs de postuler. J'ai besoin d'ajouter un chatbot intelligent qui aide les agences à filtrer et trouver les meilleurs acteurs parmi ceux qui ont postulé à un casting.

## Objectif
Créer un endpoint API qui utilise l'IA (OpenAI GPT ou similaire) pour analyser les candidatures d'un casting et répondre aux questions des agences en langage naturel pour les aider à trouver les acteurs les plus adaptés.

## Structure de données existante

### Modèle Casting
```typescript
{
  _id: string;
  titre: string;
  descriptionRole: string;
  synopsis: string;
  lieu: string;
  dateDebut: string;
  dateFin: string;
  prix: number;
  types: string[]; // ["Cinéma", "Télévision", etc.]
  age: string; // "25-35 ans"
  ouvert: boolean;
  conditions: string;
  recruteur: {
    _id: string;
    nomAgence: string;
    // ... autres champs
  };
  candidats: [
    {
      acteurId: {
        _id: string;
        nom: string;
        prenom: string;
        email: string;
        age: number;
        gouvernorat: string;
        experience: number; // années d'expérience
        centresInteret: string[];
        media: {
          photoFileId: string;
        };
        // ... autres champs
      };
      statut: "EN_ATTENTE" | "ACCEPTE" | "REFUSE";
      dateCandidature: string;
    }
  ];
}
```

### Modèle Acteur (complet)
```typescript
{
  _id: string;
  nom: string;
  prenom: string;
  email: string;
  tel: string;
  age: number;
  gouvernorat: string;
  experience: number;
  centresInteret: string[];
  socialLinks: {
    instagram?: string;
    facebook?: string;
    // ...
  };
  media: {
    photoFileId: string;
    cvFileId?: string;
    galleryFileIds?: string[];
  };
  createdAt: string;
  updatedAt: string;
}
```

## Endpoint à créer

### POST `/castings/:castingId/chatbot/query`

**Description:** Endpoint qui permet aux agences de poser des questions en langage naturel sur les candidats d'un casting et reçoit des réponses intelligentes avec des suggestions d'acteurs.

**Authentification:** Requise (JWT), rôle RECRUTEUR ou ADMIN uniquement

**Paramètres:**
- `castingId` (path): ID du casting

**Body (JSON):**
```json
{
  "query": "Trouve-moi les acteurs de plus de 30 ans avec au moins 5 ans d'expérience qui habitent à Tunis",
  "context": {
    "preferLocalization": true, // Optionnel: privilégier la localisation
    "maxResults": 10 // Optionnel: nombre max de résultats à suggérer
  }
}
```

**Réponse 200:**
```json
{
  "answer": "J'ai trouvé 3 acteurs qui correspondent à vos critères :\n\n1. **Mohamed Ben Fredj** (32 ans, 7 ans d'expérience, Tunis)\n   - Centres d'intérêt: Cinéma, Théâtre\n   - Statut: EN_ATTENTE\n\n2. **Sara Ali** (35 ans, 6 ans d'expérience, Tunis)\n   - Centres d'intérêt: Télévision, Publicité\n   - Statut: EN_ATTENTE\n\n3. **Ahmed Khaled** (31 ans, 5 ans d'expérience, Ariana)\n   - Centres d'intérêt: Cinéma\n   - Statut: EN_ATTENTE",
  "suggestedActors": [
    {
      "acteurId": "507f1f77bcf86cd799439011",
      "nom": "Mohamed",
      "prenom": "Ben Fredj",
      "age": 32,
      "experience": 7,
      "gouvernorat": "Tunis",
      "matchScore": 0.95, // Score de correspondance (0-1)
      "matchReasons": [
        "Âge correspond (32 ans)",
        "Expérience suffisante (7 ans)",
        "Localisation proche (Tunis)"
      ]
    },
    // ... autres acteurs
  ],
  "totalCandidates": 15,
  "filteredCount": 3
}
```

**Erreurs:**
- `401`: Non authentifié
- `403`: Accès refusé (seulement le propriétaire du casting ou admin)
- `404`: Casting non trouvé
- `400`: Requête invalide (query vide, etc.)

## Fonctionnalités du chatbot

### 1. Analyse des critères
Le chatbot doit pouvoir comprendre et analyser :
- **Critères démographiques**: âge, localisation (gouvernorat)
- **Critères professionnels**: années d'expérience, centres d'intérêt
- **Critères du casting**: correspondance avec descriptionRole, types, age requis
- **Statut**: filtrer par statut (EN_ATTENTE, ACCEPTE, REFUSE)

### 2. Questions supportées (exemples)
- "Trouve-moi les acteurs de 25-35 ans"
- "Quels acteurs ont plus de 5 ans d'expérience ?"
- "Montre-moi les candidats de Tunis"
- "Qui sont les meilleurs candidats pour ce rôle ?"
- "Filtre les acteurs qui ont de l'expérience en cinéma"
- "Trouve les acteurs jeunes (moins de 30 ans) avec de l'expérience"
- "Quels candidats correspondent le mieux au rôle ?"
- "Montre-moi les acteurs acceptés"
- "Combien de candidats sont en attente ?"

### 3. Suggestions intelligentes
Le chatbot doit :
- Calculer un score de correspondance pour chaque acteur
- Expliquer pourquoi chaque acteur est suggéré
- Trier les résultats par pertinence
- Limiter le nombre de résultats (par défaut 10)

### 4. Intégration IA
- Utiliser OpenAI GPT-4 ou GPT-3.5-turbo
- Créer un prompt système qui explique le contexte
- Analyser la requête de l'utilisateur
- Filtrer les candidats selon les critères
- Générer une réponse naturelle en français

## Exemple de prompt système pour l'IA

```
Tu es un assistant intelligent qui aide les agences de casting à trouver les meilleurs acteurs parmi les candidats.

CONTEXTE:
- Casting: {titre}
- Description du rôle: {descriptionRole}
- Synopsis: {synopsis}
- Types: {types}
- Âge requis: {age}
- Lieu: {lieu}
- Conditions: {conditions}

CANDIDATS DISPONIBLES:
{candidats}

TÂCHE:
Analyse la question de l'agence et trouve les acteurs les plus pertinents.
Pour chaque acteur suggéré, calcule un score de correspondance (0-1) et explique pourquoi il correspond.

RÉPONSE ATTENDUE:
1. Une réponse naturelle en français expliquant les résultats
2. Une liste d'acteurs suggérés avec leurs informations clés
3. Un score de correspondance pour chaque acteur
4. Les raisons de la correspondance

IMPORTANT:
- Ne suggère que des acteurs qui correspondent vraiment aux critères
- Sois précis et concis
- Utilise un langage professionnel mais accessible
```

## Implémentation suggérée

### 1. Route Express/Node.js
```typescript
router.post('/castings/:castingId/chatbot/query', 
  authenticate, 
  authorize(['RECRUTEUR', 'ADMIN']),
  async (req, res) => {
    const { castingId } = req.params;
    const { query, context } = req.body;
    
    // 1. Vérifier que le casting existe et appartient à l'utilisateur
    const casting = await Casting.findById(castingId)
      .populate('candidats.acteurId')
      .populate('recruteur');
    
    if (!casting) {
      return res.status(404).json({ error: 'Casting non trouvé' });
    }
    
    // 2. Vérifier les permissions
    if (casting.recruteur._id.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ error: 'Accès refusé' });
    }
    
    // 3. Préparer le contexte pour l'IA
    const systemPrompt = generateSystemPrompt(casting);
    const candidatesData = formatCandidates(casting.candidats);
    
    // 4. Appeler l'IA (OpenAI)
    const aiResponse = await callOpenAI({
      systemPrompt,
      userQuery: query,
      candidates: candidatesData
    });
    
    // 5. Parser la réponse de l'IA et extraire les suggestions
    const parsedResponse = parseAIResponse(aiResponse);
    
    // 6. Filtrer et scorer les acteurs suggérés
    const suggestedActors = filterAndScoreActors(
      casting.candidats,
      parsedResponse.suggestedActorIds,
      query
    );
    
    // 7. Retourner la réponse
    res.json({
      answer: parsedResponse.answer,
      suggestedActors,
      totalCandidates: casting.candidats.length,
      filteredCount: suggestedActors.length
    });
  }
);
```

### 2. Fonction d'appel OpenAI
```typescript
async function callOpenAI({ systemPrompt, userQuery, candidates }) {
  const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });
  
  const response = await openai.chat.completions.create({
    model: 'gpt-4', // ou 'gpt-3.5-turbo' pour économiser
    messages: [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: `${userQuery}\n\nCandidats:\n${JSON.stringify(candidates, null, 2)}` }
    ],
    temperature: 0.7,
    max_tokens: 1000
  });
  
  return response.choices[0].message.content;
}
```

### 3. Fonction de scoring
```typescript
function calculateMatchScore(acteur, casting, query) {
  let score = 0;
  const reasons = [];
  
  // Correspondance avec l'âge requis
  if (casting.age && acteur.age) {
    const ageRange = parseAgeRange(casting.age);
    if (acteur.age >= ageRange.min && acteur.age <= ageRange.max) {
      score += 0.3;
      reasons.push(`Âge correspond (${acteur.age} ans)`);
    }
  }
  
  // Expérience
  if (acteur.experience && acteur.experience >= 3) {
    score += 0.2;
    reasons.push(`Expérience: ${acteur.experience} ans`);
  }
  
  // Localisation
  if (casting.lieu && acteur.gouvernorat) {
    if (isLocationMatch(casting.lieu, acteur.gouvernorat)) {
      score += 0.2;
      reasons.push(`Localisation: ${acteur.gouvernorat}`);
    }
  }
  
  // Centres d'intérêt correspondant aux types du casting
  if (casting.types && acteur.centresInteret) {
    const matchingInterests = casting.types.filter(type => 
      acteur.centresInteret.some(interest => 
        interest.toLowerCase().includes(type.toLowerCase())
      )
    );
    if (matchingInterests.length > 0) {
      score += 0.3;
      reasons.push(`Intérêts: ${matchingInterests.join(', ')}`);
    }
  }
  
  return { score: Math.min(score, 1), reasons };
}
```

## Sécurité

1. **Authentification**: Seuls les propriétaires du casting ou les admins peuvent utiliser le chatbot
2. **Rate limiting**: Limiter le nombre de requêtes par utilisateur (ex: 50 requêtes/heure)
3. **Validation**: Valider que la query n'est pas vide et ne dépasse pas 500 caractères
4. **Coûts**: Surveiller les coûts OpenAI et mettre en cache les réponses similaires si possible

## Tests suggérés

1. Test avec différents types de questions
2. Test avec casting sans candidats
3. Test avec permissions insuffisantes
4. Test avec query invalide
5. Test de performance avec beaucoup de candidats

## Notes importantes

- Le chatbot doit être rapide (< 3 secondes de réponse)
- Les réponses doivent être en français
- Le format JSON de réponse doit être strict pour faciliter l'intégration Android
- En cas d'erreur OpenAI, retourner une erreur 500 avec message approprié

