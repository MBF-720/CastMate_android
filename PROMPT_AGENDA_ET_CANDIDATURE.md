# PROMPT DÉTAILLÉ : SYSTÈME D'AGENDA ET CANDIDATURE CASTMATE

## 📋 CONTEXTE DU PROJET

CastMate est une application Android développée en **Kotlin avec Jetpack Compose** qui connecte des acteurs et des agences de casting. Le système permet aux acteurs de postuler à des castings et aux agences de gérer les candidatures avec un système d'interviews.

---

## 🎯 PARTIE 1 : SYSTÈME D'AGENDA POUR ACTEUR ET AGENCE

### Vue d'ensemble
L'écran **Agenda** (`AgendaScreen.kt`) est partagé entre les acteurs et les agences, mais affiche des données différentes selon le rôle de l'utilisateur.

### Architecture de l'Agenda

#### 1. Détection du rôle utilisateur
```kotlin
// Fichier: AgendaScreen.kt (lignes 78-86)
val tokenManager = remember { TokenManager(context) }
var userRole by remember { mutableStateOf<String?>(null) }

LaunchedEffect(Unit) {
    userRole = withContext(Dispatchers.IO) {
        tokenManager.getUserRoleSync() // Récupère "ACTEUR", "RECRUTEUR", ou "ADMIN"
    }
}
```

#### 2. Chargement des interviews selon le rôle
```kotlin
// Fichier: AgendaScreen.kt (lignes 88-98)
val result = withContext(Dispatchers.IO) {
    when (userRole?.uppercase()) {
        "ACTEUR" -> interviewRepository.getActorInterviews(selectedStatusFilter)
        "RECRUTEUR", "ADMIN" -> interviewRepository.getAgencyInterviews(selectedStatusFilter)
        else -> Result.success(emptyList())
    }
}
```

#### 3. API Endpoints utilisés

**Pour les ACTEURS :**
- `GET /interviews/actor?status={PENDING|CONFIRMED|CANCELLED}`
- Retourne toutes les interviews où l'acteur est concerné
- Fichier: `InterviewApiService.kt` (lignes 52-55)

**Pour les AGENCES :**
- `GET /interviews/agency?status={PENDING|CONFIRMED|CANCELLED}&castingId={optional}`
- Retourne toutes les interviews des castings de l'agence
- Fichier: `InterviewApiService.kt` (lignes 70-74)

#### 4. Affichage différencié dans l'Agenda

**Pour les ACTEURS :**
- Les interviews **PENDING** affichent : "Cliquez pour choisir une date parmi les 3 proposées"
- Les interviews **CONFIRMED** affichent la date et l'heure sélectionnées
- Clic sur une interview PENDING → Navigation vers `SelectInterviewDateScreen`

**Pour les AGENCES :**
- Les interviews **PENDING** affichent : "En attente de la sélection de l'acteur"
- Les interviews **CONFIRMED** affichent la date et l'heure confirmées
- Les agences ne peuvent PAS cliquer pour sélectionner une date (elles proposent, elles ne choisissent pas)

**Code de différenciation :**
```kotlin
// Fichier: AgendaScreen.kt (lignes 536-555)
if (interview.statusEnum == InterviewStatus.PENDING) {
    Text(
        text = when (userRole?.uppercase()) {
            "ACTEUR" -> "Cliquez pour choisir une date parmi les 3 proposées"
            "RECRUTEUR", "ADMIN" -> "En attente de la sélection de l'acteur"
            else -> "En attente de confirmation"
        }
    )
}
```

#### 5. Barre de navigation différente selon le rôle

**Pour les ACTEURS :**
- Utilise `ActorBottomNavigationBar` avec 4 éléments : Candidatures, Home, Agenda (sélectionné), Profile
- Fichier: `ActorBottomNavigationBar.kt`

**Pour les AGENCES :**
- Utilise `AgencyBottomNavigationBar` avec 4 éléments : Accueil, Agenda (sélectionné), + (créer), Profil
- Fichier: `CastingListAgencyScreen.kt` (lignes 754-821)

---

## 🎬 PARTIE 2 : PROCESSUS DE CANDIDATURE D'UN ACTEUR

### Étape 1 : L'acteur consulte un casting
- Écran : `CastingDetailScreen.kt`
- L'acteur voit les détails du casting (titre, description, rôle, âge, compensation)

### Étape 2 : L'acteur postule au casting

#### Option A : Candidature avec vidéo (recommandée)
```kotlin
// Fichier: CastingApplicationScreen.kt (lignes 251-286)
val result = castingRepository.applyToCastingWithVideo(
    id = castingId,
    context = context,
    videoUri = bestAttempt.videoUri, // Vidéo d'audition (max 30s, 10MB)
    aiFeedback = aiFeedbackJson // Feedback IA optionnel
)
```

**API Endpoint :**
- `POST /castings/{id}/apply`
- Content-Type: `multipart/form-data`
- Body: `video` (File), `aiFeedback` (String JSON optionnel)
- Fichier: `CastingApiService.kt` (lignes 195-201)

#### Option B : Candidature simple (sans vidéo)
```kotlin
// Fichier: CastingDetailScreen.kt (lignes 462-469)
val result = castingRepository.applyToCasting(castingId)
```

**API Endpoint :**
- `POST /castings/{id}/apply` (sans body)
- Fichier: `CastingApiService.kt` (lignes 178-179)

### Étape 3 : Statut de la candidature
Après la candidature, l'acteur peut voir son statut dans "Mes candidatures" :
- **EN_ATTENTE** : Candidature en attente d'examen par l'agence
- **ACCEPTE** : Candidature acceptée (peut avoir une interview)
- **REFUSE** : Candidature refusée

---

## 🏢 PARTIE 3 : L'AGENCE ACCEPTE UN CANDIDAT ET PROPOSE 3 DATES

### Étape 1 : L'agence consulte les candidatures
- Écran : `AgencyCastingDetailScreen.kt`
- L'agence voit la liste des candidats avec leurs statuts

### Étape 2 : L'agence accepte un candidat avec interview

L'agence peut accepter un candidat de deux façons :

#### Option A : Acceptation simple (sans interview)
```kotlin
// Fichier: CastingApiService.kt (lignes 216-220)
@PATCH("castings/{id}/candidates/{acteurId}/accept")
suspend fun acceptCandidate(
    @Path("id") id: String,
    @Path("acteurId") acteurId: String
): Response<Unit>
```

#### Option B : Acceptation avec interview (3 dates proposées) ⭐
```kotlin
// Fichier: AgencyCastingDetailScreen.kt (lignes 1227-1250)
val result = castingRepository.acceptCandidateWithInterview(
    castingId = castingId,
    acteurId = acteurId,
    dates = dates // List<Pair<String, String?>> - 3 dates avec heures
)
```

**Dialogue de sélection des dates :**
- Composant : `InterviewDateProposalDialog.kt`
- L'agence doit sélectionner **EXACTEMENT 3 dates différentes**
- Chaque date doit avoir une **heure obligatoire**
- Toutes les dates doivent être **dans le futur**
- Validation : `InterviewDateProposalDialog.kt` (lignes 108-165)

**Format des dates :**
- Date : `"YYYY-MM-DD"` (ex: "2025-12-15")
- Heure : `"HH:mm"` (ex: "14:30")

**API Endpoint :**
```kotlin
// Fichier: CastingApiService.kt (lignes 242-247)
@POST("castings/{id}/candidates/{acteurId}/accept-with-interview")
suspend fun acceptCandidateWithInterview(
    @Path("id") id: String,
    @Path("acteurId") acteurId: String,
    @Body request: AcceptCandidateWithInterviewRequest
): Response<AcceptCandidateWithInterviewResponse>
```

**Body de la requête :**
```json
{
  "proposedDates": [
    { "date": "2025-12-15", "time": "14:30" },
    { "date": "2025-12-16", "time": "10:00" },
    { "date": "2025-12-18", "time": "16:45" }
  ]
}
```

**Réponse de l'API :**
- Crée automatiquement une interview avec le statut **PENDING**
- Retourne l'interview créée avec les 3 dates proposées
- Fichier: `Interview.kt` (lignes 127-133)

### Étape 3 : Création automatique de l'interview
Lorsque l'agence accepte avec interview, le backend :
1. Met à jour le statut du candidat à **ACCEPTE**
2. Crée une nouvelle interview avec :
   - `status: "PENDING"`
   - `proposedDates`: Les 3 dates proposées
   - `selectedDate: null` (sera rempli par l'acteur)
   - `selectedTime: null` (sera rempli par l'acteur)

---

## 🎭 PARTIE 4 : L'ACTEUR CHOISIT UNE DATE PARMI LES 3 PROPOSÉES

### Étape 1 : L'acteur voit l'interview dans son Agenda
- Écran : `AgendaScreen.kt`
- L'interview apparaît avec le statut **PENDING**
- Message : "Cliquez pour choisir une date parmi les 3 proposées"
- Un point bleu sur le calendrier indique la présence d'interviews

### Étape 2 : Navigation vers l'écran de sélection

**Protection par rôle :**
```kotlin
// Fichier: MainActivity.kt (lignes 2207-2211)
onInterviewClick = { interview ->
    // Seuls les acteurs peuvent sélectionner une date
    if (interview.statusEnum == InterviewStatus.PENDING 
        && userRole?.uppercase() == "ACTEUR") {
        navController.navigate("selectInterviewDate/${interview.id}")
    }
}
```

**Vérification supplémentaire :**
```kotlin
// Fichier: MainActivity.kt (lignes 2262-2273)
LaunchedEffect(Unit) {
    userRole = withContext(Dispatchers.IO) {
        tokenManager.getUserRoleSync()
    }
    
    // Si l'utilisateur n'est pas un acteur, rediriger
    if (userRole?.uppercase() != "ACTEUR") {
        navController.popBackStack()
        return@LaunchedEffect
    }
}
```

### Étape 3 : Écran de sélection de date
- Écran : `SelectInterviewDateScreen.kt`
- Affiche les informations du casting (titre, agence)
- Affiche les **3 dates proposées** sous forme de cartes cliquables
- L'acteur doit sélectionner **UNE SEULE date**

**Interface utilisateur :**
```kotlin
// Fichier: SelectInterviewDateScreen.kt (lignes 183-197)
if (interview.proposedDates.size == 3) {
    interview.proposedDates.forEachIndexed { index, dateOption ->
        DateOptionCard(
            date = dateOption.date,
            time = dateOption.time,
            isSelected = selectedDateIndex == index,
            onClick = {
                selectedDateIndex = if (selectedDateIndex == index) null else index
            }
        )
    }
}
```

### Étape 4 : Soumission de la sélection

**Validation :**
- L'acteur doit avoir sélectionné une date (obligatoire)
- Fichier: `SelectInterviewDateScreen.kt` (lignes 60-64)

**Envoi à l'API :**
```kotlin
// Fichier: SelectInterviewDateScreen.kt (lignes 70-75)
val result = interviewRepository.selectInterviewDate(
    interviewId = interview.id,
    date = selectedDateOption.date, // Format: "YYYY-MM-DD"
    time = selectedDateOption.time  // Format: "HH:mm"
)
```

**API Endpoint :**
```kotlin
// Fichier: InterviewApiService.kt (lignes 34-38)
@PATCH("interviews/{interviewId}/select-date")
suspend fun selectInterviewDate(
    @Path("interviewId") interviewId: String,
    @Body request: SelectInterviewDateRequest
): Response<InterviewResponse>
```

**Body de la requête :**
```json
{
  "selectedDate": "2025-12-15",
  "selectedTime": "14:30"
}
```

### Étape 5 : Confirmation et mise à jour
Après la sélection :
1. Le backend met à jour l'interview :
   - `status: "CONFIRMED"`
   - `selectedDate: "2025-12-15"`
   - `selectedTime: "14:30"`
2. L'interview apparaît comme **CONFIRMED** dans l'agenda de l'acteur ET de l'agence
3. Les deux parties voient la date et l'heure confirmées

---

## 📊 MODÈLES DE DONNÉES

### InterviewResponse
```kotlin
// Fichier: Interview.kt (lignes 45-84)
data class InterviewResponse(
    val id: String,
    val castingId: String,
    val castingTitle: String,
    val acteurId: String,
    val acteurName: String,
    val agenceId: String,
    val agenceName: String,
    val proposedDates: List<ProposedDateResponse>, // Les 3 dates proposées
    val selectedDate: String?, // Date choisie par l'acteur (null si PENDING)
    val selectedTime: String?, // Heure choisie par l'acteur (null si PENDING)
    val status: String, // "PENDING", "CONFIRMED", "CANCELLED"
    val createdAt: String,
    val updatedAt: String
)
```

### ProposedDateResponse
```kotlin
// Fichier: Interview.kt (lignes 31-40)
data class ProposedDateResponse(
    val date: String, // "YYYY-MM-DD"
    val time: String?, // "HH:mm"
    val isSelected: Boolean // false (sera true si sélectionnée)
)
```

### InterviewStatus (Enum)
```kotlin
// Fichier: Interview.kt (lignes 8-12)
enum class InterviewStatus {
    PENDING,    // Interview créée, en attente de sélection par l'acteur
    CONFIRMED,  // Date sélectionnée par l'acteur
    CANCELLED   // Interview annulée
}
```

---

## 🔄 FLUX COMPLET RÉSUMÉ

### Pour l'ACTEUR :
1. ✅ Consulte les castings disponibles
2. ✅ Postule à un casting (avec ou sans vidéo)
3. ✅ Attend la réponse de l'agence
4. ✅ Si accepté avec interview → Voit l'interview dans son Agenda (statut PENDING)
5. ✅ Clique sur l'interview PENDING
6. ✅ Voit les 3 dates proposées par l'agence
7. ✅ Sélectionne UNE date parmi les 3
8. ✅ L'interview passe en statut CONFIRMED
9. ✅ Voit l'interview confirmée dans son Agenda avec date/heure

### Pour l'AGENCE :
1. ✅ Consulte les candidatures pour ses castings
2. ✅ Accepte un candidat avec interview
3. ✅ Propose EXACTEMENT 3 dates différentes avec heures
4. ✅ L'interview est créée automatiquement (statut PENDING)
5. ✅ Voit l'interview dans son Agenda (statut PENDING)
6. ✅ Attend que l'acteur choisisse une date
7. ✅ Voit l'interview confirmée dans son Agenda une fois que l'acteur a choisi

---

## 🔐 SÉCURITÉ ET VALIDATIONS

### Validations côté Agence :
- ✅ Exactement 3 dates doivent être proposées
- ✅ Chaque date doit avoir une heure
- ✅ Les 3 dates doivent être différentes
- ✅ Toutes les dates doivent être dans le futur
- ✅ L'agence doit être propriétaire du casting

### Validations côté Acteur :
- ✅ Seuls les acteurs peuvent sélectionner une date
- ✅ La date sélectionnée doit être parmi les 3 proposées
- ✅ L'acteur doit être celui concerné par l'interview
- ✅ L'interview doit être en statut PENDING

### Validations Backend :
- ✅ Vérification des permissions (rôle ACTEUR vs RECRUTEUR)
- ✅ Vérification de la propriété (agence propriétaire du casting)
- ✅ Vérification que la date sélectionnée est bien proposée
- ✅ Vérification que l'interview est en statut PENDING

---

## 📁 FICHIERS CLÉS DU PROJET

### Écrans UI :
- `AgendaScreen.kt` - Écran d'agenda partagé (acteur/agence)
- `SelectInterviewDateScreen.kt` - Écran de sélection de date (acteur uniquement)
- `CastingApplicationScreen.kt` - Écran de candidature avec vidéo
- `AgencyCastingDetailScreen.kt` - Écran de gestion des candidatures (agence)
- `InterviewDateProposalDialog.kt` - Dialogue de proposition de 3 dates (agence)

### Repositories :
- `InterviewRepository.kt` - Gestion des interviews (CRUD)
- `CastingRepository.kt` - Gestion des castings et candidatures

### API Services :
- `InterviewApiService.kt` - Endpoints API pour les interviews
- `CastingApiService.kt` - Endpoints API pour les castings

### Modèles :
- `Interview.kt` - Modèles de données (InterviewResponse, ProposedDate, etc.)

### Navigation :
- `MainActivity.kt` - Navigation et routage de l'application

---

## 🎨 COMPOSANTS UI RÉUTILISABLES

### ActorBottomNavigationBar
- 4 éléments : Candidatures, Home, Agenda, Profile
- Utilisé par les acteurs

### AgencyBottomNavigationBar
- 4 éléments : Accueil, Agenda, + (créer), Profil
- Utilisé par les agences

### InterviewCard
- Affiche une interview dans l'agenda
- S'adapte selon le rôle (acteur vs agence)
- Affiche différents messages selon le statut

---

## 💡 POINTS IMPORTANTS À RETENIR

1. **L'agence PROPOSE, l'acteur CHOISIT** : L'agence ne peut pas sélectionner une date, elle propose 3 dates. Seul l'acteur peut choisir parmi ces 3 dates.

2. **Exactement 3 dates** : Le système exige exactement 3 dates différentes avec heures obligatoires.

3. **Statuts des interviews** :
   - **PENDING** : Interview créée, en attente de sélection par l'acteur
   - **CONFIRMED** : Date sélectionnée par l'acteur
   - **CANCELLED** : Interview annulée

4. **Agenda partagé mais différencié** : Le même écran Agenda est utilisé par les acteurs et les agences, mais affiche des données et des actions différentes selon le rôle.

5. **Sécurité par rôle** : Toutes les actions sont protégées par vérification du rôle utilisateur (ACTEUR vs RECRUTEUR/ADMIN).

---

## 🚀 UTILISATION DE CE PROMPT

Ce prompt peut être utilisé pour :
- Comprendre le système d'agenda et de candidature
- Déboguer des problèmes liés aux interviews
- Ajouter de nouvelles fonctionnalités
- Onboarding de nouveaux développeurs
- Documentation technique

**Pour utiliser avec Cursor IA ou un autre assistant :**
Copiez ce prompt complet et demandez des clarifications ou des modifications spécifiques selon vos besoins.

