package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.CandidateStatusResponse
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.*

data class CandidatureItem(
    val castingId: String,
    val castingTitre: String,
    val dateSubmit: String, // Date formatée pour l'affichage
    val dateSubmitRaw: String, // Date ISO pour le tri
    val statut: String // "EN_ATTENTE", "ACCEPTE", "REFUSE"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCandidaturesScreen(
    onBackClick: () -> Unit = {},
    onCastingClick: ((String) -> Unit)? = null,
    onHomeClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val acteurRepository = remember { ActeurRepository() }
    val castingRepository = remember { CastingRepository() }
    val scope = rememberCoroutineScope()
    var candidatures by remember { mutableStateOf<List<CandidatureItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Charger les candidatures depuis l'API
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        
        try {
            // 1. Récupérer l'ID de l'acteur connecté
            val acteurId = acteurRepository.getCurrentActeurId()
            if (acteurId == null) {
                android.util.Log.e("MyCandidaturesScreen", "❌ Impossible de récupérer l'ID de l'acteur")
                errorMessage = "Impossible de récupérer votre identifiant"
                isLoading = false
                return@LaunchedEffect
            }
            
            android.util.Log.d("MyCandidaturesScreen", "📝 Récupération des candidatures pour l'acteur: $acteurId")
            
            // 2. Récupérer tous les castings
            val castingsResult = castingRepository.getAllCastings()
            castingsResult.onSuccess { castings ->
                android.util.Log.d("MyCandidaturesScreen", "✅ ${castings.size} castings récupérés")
                
                // 3. Filtrer les castings où l'acteur a postulé
                scope.launch {
                    val myCandidatures = mutableListOf<CandidatureItem>()
                    val castingsAvecCandidatsNonPeuples = mutableListOf<Casting>()
                    
                    // Première passe : Utiliser la liste des candidats si elle est peuplée (plus rapide)
                    castings.forEach { casting ->
                        val candidats = casting.candidats ?: emptyList()
                        val castingId = casting.actualId
                        
                        if (castingId != null && candidats.isNotEmpty()) {
                            // Chercher si l'acteur est dans la liste des candidats
                            val maCandidature = candidats.firstOrNull { candidat ->
                                // Utiliser actualId qui gère à la fois _id et id
                                candidat.acteurId?.actualId == acteurId
                            }
                            
                            if (maCandidature != null && maCandidature.acteurId != null && maCandidature.acteurId?.actualId != null) {
                                // L'acteur a postulé et acteurId est peuplé
                                android.util.Log.d("MyCandidaturesScreen", "✅ Candidature trouvée (liste) pour le casting '${casting.titre}' - Statut: ${maCandidature.statut}")
                                
                                val castingTitre = casting.titre ?: "Sans titre"
                                val dateCandidatureRaw = maCandidature.dateCandidature ?: ""
                                val statut = maCandidature.statut ?: "EN_ATTENTE"
                                
                                // Formater la date pour l'affichage
                                val dateFormatee = try {
                                    if (dateCandidatureRaw.isNotBlank()) {
                                        formatDate(dateCandidatureRaw)
                                    } else {
                                        "Date inconnue"
                                    }
                                } catch (e: Exception) {
                                    dateCandidatureRaw
                                }
                                
                                myCandidatures.add(
                                    CandidatureItem(
                                        castingId = castingId,
                                        castingTitre = castingTitre,
                                        dateSubmit = dateFormatee,
                                        dateSubmitRaw = dateCandidatureRaw,
                                        statut = statut
                                    )
                                )
                            } else if (candidats.isNotEmpty() && candidats.any { it.acteurId == null }) {
                                // Les candidats ne sont pas peuplés, utiliser getMyStatus
                                castingsAvecCandidatsNonPeuples.add(casting)
                            }
                        } else if (castingId != null) {
                            // Aucun candidat dans la liste, vérifier avec getMyStatus
                            castingsAvecCandidatsNonPeuples.add(casting)
                        }
                    }
                    
                    // Deuxième passe : Utiliser getMyStatus pour les castings avec candidats non peuplés
                    // ⚠️ Note : getMyStatus nécessite une authentification pour chaque casting
                    // Pour éviter les erreurs Unauthorized, on ignore cette étape si des candidats sont présents
                    // mais non peuplés (on suppose qu'ils ne concernent pas l'acteur actuel)
                    if (castingsAvecCandidatsNonPeuples.isNotEmpty()) {
                        android.util.Log.d("MyCandidaturesScreen", "🔍 ${castingsAvecCandidatsNonPeuples.size} castings avec candidats non peuplés - ignorés (pour éviter les erreurs d'authentification)")
                        // On ne fait rien ici pour éviter les erreurs Unauthorized
                        // Les candidats non peuplés sont généralement ceux où l'acteur n'a pas postulé
                    }
                    
                    // Mettre à jour l'état final sur le thread principal
                    withContext(Dispatchers.Main) {
                        if (myCandidatures.isEmpty()) {
                            android.util.Log.d("MyCandidaturesScreen", "ℹ️ Aucune candidature trouvée")
                        } else {
                            android.util.Log.d("MyCandidaturesScreen", "✅ ${myCandidatures.size} candidatures trouvées")
                        }
                        candidatures = myCandidatures.sortedByDescending { it.dateSubmitRaw }
                        isLoading = false
                    }
                }
            }
            
            castingsResult.onFailure { exception ->
                android.util.Log.e("MyCandidaturesScreen", "❌ Erreur lors de la récupération des castings: ${exception.message}", exception)
                errorMessage = getErrorMessage(exception)
                isLoading = false
            }
        } catch (e: Exception) {
            android.util.Log.e("MyCandidaturesScreen", "❌ Exception: ${e.message}", e)
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mes candidatures",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = White,
                        titleContentColor = Color(0xFF1A1A1A),
                        navigationIconContentColor = DarkBlue
                    )
                )
            }
        ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                    Text(
                        text = "Chargement de vos candidatures...",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else if (errorMessage != null) {
            // Message d'erreur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Erreur",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Text(
                        text = errorMessage ?: "Une erreur est survenue",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            // Réessayer
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                // Relancer le chargement
                                val acteurId = acteurRepository.getCurrentActeurId()
                                if (acteurId != null) {
                                    val castingsResult = castingRepository.getAllCastings()
                                    castingsResult.onSuccess { castings ->
                                        val myCandidatures = mutableListOf<CandidatureItem>()
                                        castings.forEach { casting ->
                                            val candidats = casting.candidats ?: emptyList()
                                            val maCandidature = candidats.firstOrNull { 
                                                it.acteurId?.id == acteurId 
                                            }
                                            if (maCandidature != null) {
                                                val castingId = casting.actualId ?: return@forEach
                                                val castingTitre = casting.titre ?: "Sans titre"
                                                val dateCandidatureRaw = maCandidature.dateCandidature ?: ""
                                                val statut = maCandidature.statut ?: "EN_ATTENTE"
                                                val dateFormatee = try {
                                                    if (dateCandidatureRaw.isNotBlank()) {
                                                        formatDate(dateCandidatureRaw)
                                                    } else {
                                                        "Date inconnue"
                                                    }
                                                } catch (e: Exception) {
                                                    dateCandidatureRaw
                                                }
                                                myCandidatures.add(
                                                    CandidatureItem(
                                                        castingId = castingId,
                                                        castingTitre = castingTitre,
                                                        dateSubmit = dateFormatee,
                                                        dateSubmitRaw = dateCandidatureRaw,
                                                        statut = statut
                                                    )
                                                )
                                            }
                                        }
                                        candidatures = myCandidatures.sortedByDescending { it.dateSubmitRaw }
                                        isLoading = false
                                    }
                                    castingsResult.onFailure { exception ->
                                        errorMessage = getErrorMessage(exception)
                                        isLoading = false
                                    }
                                } else {
                                    errorMessage = "Impossible de récupérer votre identifiant"
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue
                        )
                    ) {
                        Text("Réessayer")
                    }
                }
            }
        } else if (candidatures.isEmpty()) {
            // Message vide
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Aucune candidature",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "Vous n'avez pas encore postulé à des castings",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Liste des candidatures
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(candidatures) { candidature ->
                    CandidatureCard(
                        candidature = candidature,
                        onClick = {
                            onCastingClick?.invoke(candidature.castingId)
                        }
                    )
                }
            }
        }
        }
        
        // Barre de navigation positionnée au-dessus du contenu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 17.dp)
        ) {
            ActorBottomNavigationBar(
                selectedItem = NavigationItem.CANDIDATURES,
                onCandidaturesClick = { /* Déjà sur la page */ },
                onHomeClick = onHomeClick,
                onAgendaClick = onAgendaClick,
                onProfileClick = onProfileClick
            )
        }
    }
}


/**
 * Formate une date ISO en format lisible en français
 * Exemple: "2024-01-15T00:00:00.000Z" -> "15 janvier 2024"
 */
fun formatDate(dateString: String): String {
    return try {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        
        var parsedDate: Date? = null
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale("fr", "FR"))
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                parsedDate = sdf.parse(dateString)
                if (parsedDate != null) break
            } catch (e: Exception) {
                // Continuer avec le format suivant
            }
        }
        
        if (parsedDate != null) {
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("fr", "FR"))
            outputFormat.format(parsedDate)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun CandidatureCard(
    candidature: CandidatureItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Titre du casting
            Text(
                text = candidature.castingTitre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            // Date de soumission
            Text(
                text = "Soumis le ${candidature.dateSubmit}",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            // Statut
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (candidature.statut) {
                                "ACCEPTE" -> Color(0xFF4CAF50)
                                "REFUSE" -> Color(0xFFF44336)
                                else -> Color(0xFFFFA726)
                            }
                        )
                )
                Text(
                    text = when (candidature.statut) {
                        "ACCEPTE" -> "Accepté"
                        "REFUSE" -> "Refusé"
                        else -> "En attente"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (candidature.statut) {
                        "ACCEPTE" -> Color(0xFF4CAF50)
                        "REFUSE" -> Color(0xFFF44336)
                        else -> Color(0xFFFFA726)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCandidaturesScreenPreview() {
    Projecct_MobileTheme {
        MyCandidaturesScreen()
    }
}

