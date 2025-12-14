package com.example.projecct_mobile.ui.screens.agence.casting

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Candidat
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.ChatbotResponse
import com.example.projecct_mobile.data.model.SuggestedActor
import com.example.projecct_mobile.data.model.TrainingFeedback
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.utils.CoilImageLoader
import com.example.projecct_mobile.ui.screens.interview.InterviewDateProposalDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AgencyCastingDetailScreen(
    casting: Casting,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onViewActorProfile: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onChatbotClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = remember { CoilImageLoader.getImageLoader(context) }
    val afficheUrl = casting.actualAfficheUrl
    val acteurRepository = remember { ActeurRepository() }
    val castingRepository = remember { com.example.projecct_mobile.data.repository.CastingRepository() }
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Détails", "Candidats")
    var currentCasting by remember { mutableStateOf(casting) }
    var isUpdatingStatus by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Affichage du message d'erreur si nécessaire
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Red, RoundedCornerShape(8.dp))
                    .clickable { errorMessage = null }
                    .padding(16.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = White,
                    fontSize = 14.sp
                )
            }
        }
        
        // Image en arrière-plan (fixe)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {
            // Affiche du casting en arrière-plan
            if (afficheUrl != null) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(afficheUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader
                )
                
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    is coil.compose.AsyncImagePainter.State.Error -> {
                        // Image par défaut avec gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            DarkBlue,
                                            Color(0xFF1E3A8A)
                                        )
                                    )
                                )
                        )
                    }
                    else -> {
                        Image(
                            painter = painter,
                            contentDescription = "Affiche du casting",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // Image par défaut avec gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DarkBlue,
                                    Color(0xFF1E3A8A)
                                )
                            )
                        )
                )
            }

            // Overlay sombre pour améliorer la lisibilité
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // Boutons d'action (Retour, Modifier, Supprimer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = White
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Bouton Chatbot
                    IconButton(
                        onClick = onChatbotClick, // Naviguer vers l'interface chatbot dédiée
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat, // Icône chatbot/IA
                            contentDescription = "Chatbot IA",
                            tint = White
                        )
                    }
                    
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = White
                        )
                    }
                    
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Red
                        )
                    }
                }
            }

            // Titre du casting
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = casting.titre ?: "Sans titre",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        casting.types?.take(2)?.forEach { type ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = type,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Carte blanche qui scroll (avec onglets)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    spotColor = DarkBlue.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Onglets personnalisés (style "pill")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(DarkBlue)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selectedTabIndex == index) White
                                    else Color.Transparent
                                )
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                                Text(
                                    text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) DarkBlue else White
                                )
                            }
                    }
                }

                // Contenu scrollable selon l'onglet sélectionné
                val scrollState = rememberScrollState()
                
                when (selectedTabIndex) {
                    0 -> {
                        // Onglet "Détails"
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CastingDetailsContent(
                                casting = currentCasting,
                                onToggleStatus = { newStatus ->
                                    scope.launch {
                                        isUpdatingStatus = true
                                        try {
                                            val result = castingRepository.updateCasting(
                                                id = currentCasting.actualId ?: "",
                                                titre = currentCasting.titre ?: "",
                                                descriptionRole = currentCasting.descriptionRole,
                                                synopsis = currentCasting.synopsis,
                                                lieu = currentCasting.lieu,
                                                dateDebut = currentCasting.dateDebut,
                                                dateFin = currentCasting.dateFin,
                                                prix = currentCasting.prix,
                                                types = currentCasting.types,
                                                age = currentCasting.age,
                                                ouvert = newStatus,
                                                conditions = currentCasting.conditions,
                                                afficheFile = null
                                            )
                                            result.onSuccess { updatedCasting ->
                                                currentCasting = updatedCasting
                                                android.util.Log.d("AgencyCastingDetail", "✅ Statut mis à jour: ${if (newStatus) "Ouvert" else "Fermé"}")
                                            }
                                            result.onFailure { exception ->
                                                errorMessage = "Erreur: ${exception.message}"
                                                android.util.Log.e("AgencyCastingDetail", "❌ Erreur mise à jour statut: ${exception.message}")
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Erreur: ${e.message}"
                                            android.util.Log.e("AgencyCastingDetail", "❌ Exception: ${e.message}")
                                        } finally {
                                            isUpdatingStatus = false
                                        }
                                    }
                                },
                                isUpdating = isUpdatingStatus,
                                onOpenChatbot = onChatbotClick
                            )
                        }
                    }
                    1 -> {
                        // Onglet "Candidats"
                        CandidatesContent(
                            castingId = currentCasting.actualId ?: "",
                            candidates = currentCasting.candidats ?: emptyList(),
                            modifier = Modifier.fillMaxSize(),
                            onAcceptCandidateWithInterview = { acteurId ->
                                // Cette fonction est gérée dans CandidateCard avec le dialogue InterviewDateProposalDialog
                                // Le dialogue appelle acceptCandidateWithInterview directement
                            },
                            onRejectCandidate = { acteurId ->
                                scope.launch {
                                    try {
                                        val result = castingRepository.rejectCandidate(
                                            castingId = currentCasting.actualId ?: "",
                                            acteurId = acteurId
                                        )
                                        result.onSuccess {
                                            // Recharger le casting pour obtenir la liste mise à jour
                                            val refreshResult = castingRepository.getCastingById(currentCasting.actualId ?: "")
                                            refreshResult.onSuccess { refreshedCasting ->
                                                currentCasting = refreshedCasting
                                                android.util.Log.d("AgencyCastingDetail", "✅ Candidat refusé")
                                            }
                                        }
                                        result.onFailure { exception ->
                                            errorMessage = "Erreur: ${exception.message}"
                                            android.util.Log.e("AgencyCastingDetail", "❌ Erreur refus: ${exception.message}")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur: ${e.message}"
                                        android.util.Log.e("AgencyCastingDetail", "❌ Exception: ${e.message}")
                                    }
                                }
                            },
                            onViewProfile = onViewActorProfile,
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CastingDetailsContent(
    casting: Casting,
    onToggleStatus: (Boolean) -> Unit = {},
    isUpdating: Boolean = false,
    onOpenChatbot: () -> Unit = {}
) {
    // Description
    if (!casting.descriptionRole.isNullOrBlank()) {
        DetailSection(title = "Description du rôle") {
            Text(
                text = casting.descriptionRole ?: "",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }
    }
    
    // Synopsis
    if (!casting.synopsis.isNullOrBlank()) {
        DetailSection(title = "Synopsis") {
            Text(
                text = casting.synopsis ?: "",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }
    }
    
    // Dates
    DetailSection(title = "Dates") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = DarkBlue,
                modifier = Modifier.size(20.dp)
            )
            Column {
                if (!casting.dateDebut.isNullOrBlank()) {
                    Text(
                        text = "Début: ${casting.dateDebut}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                if (!casting.dateFin.isNullOrBlank()) {
                    Text(
                        text = "Fin: ${casting.dateFin}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
    
    // Informations du rôle avec design amélioré
    DetailSection(title = "Informations du rôle") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Carte de rémunération mise en évidence
            casting.prix?.let { prix ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkBlue.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DarkBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Rémunération",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$prix DT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                    }
                }
            }
            
            // Autres informations avec icônes
            if (!casting.age.isNullOrBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Person,
                    label = "Âge requis",
                    value = casting.age ?: ""
                )
            }
            if (!casting.lieu.isNullOrBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Place,
                    label = "Lieu",
                    value = casting.lieu ?: ""
                )
            }
            
            // Statut avec switch interactif
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (casting.ouvert) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (casting.ouvert) Color(0xFF4CAF50) else Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Statut du casting",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (casting.ouvert) "Les acteurs peuvent postuler" else "Candidatures fermées",
                                fontSize = 11.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                    Switch(
                        checked = casting.ouvert,
                        onCheckedChange = { onToggleStatus(it) },
                        enabled = !isUpdating,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = Red
                        )
                    )
                }
            }
        }
    }
    
    // Conditions
    if (!casting.conditions.isNullOrBlank()) {
        DetailSection(title = "Conditions") {
            Text(
                text = casting.conditions ?: "",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }
    }
    
    // Bouton Chatbot IA
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onOpenChatbot,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkBlue
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "💬 Parler au Chatbot IA",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = White
        )
    }
    
    // Texte d'aide
    Text(
        text = "Le chatbot IA vous aide à trouver les meilleurs acteurs parmi vos candidats",
        fontSize = 12.sp,
        color = Color(0xFF999999),
        modifier = Modifier.padding(top = 8.dp),
        lineHeight = 16.sp
    )
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(DarkBlue, RoundedCornerShape(2.dp))
            )
        Text(
            text = title,
                fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        }
        content()
        
        // Séparateur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF999999)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )
    }
}

@Composable
private fun InfoRowWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlue.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
private fun CandidatesContent(
    castingId: String,
    candidates: List<Candidat>,
    modifier: Modifier = Modifier,
    onAcceptCandidateWithInterview: (String) -> Unit = {},
    onRejectCandidate: (String) -> Unit = {},
    onViewProfile: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    if (candidates.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFCCCCCC)
                )
                Text(
                    text = "Aucun candidat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Aucun acteur n'a encore postulé à ce casting",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${candidates.size} candidat${if (candidates.size > 1) "s" else ""}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(candidates) { candidat ->
                CandidateCard(
                    candidat = candidat,
                    castingId = castingId,
                    onAcceptWithInterview = {
                        candidat.acteurId?.actualId?.let { onAcceptCandidateWithInterview(it) }
                    },
                    onReject = {
                        candidat.acteurId?.actualId?.let { onRejectCandidate(it) }
                    },
                    onViewProfile = {
                        candidat.acteurId?.actualId?.let { onViewProfile(it) }
                    },
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidat: Candidat,
    castingId: String,
    onAcceptWithInterview: () -> Unit = {},
    onReject: () -> Unit = {},
    onViewProfile: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val castingRepository = remember { com.example.projecct_mobile.data.repository.CastingRepository() }
    var showVideoDialog by remember { mutableStateOf(false) }
    var videoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isLoadingVideo by remember { mutableStateOf(false) }
    var videoError by remember { mutableStateOf<String?>(null) }
    var showInterviewDialog by remember { mutableStateOf(false) }
    var isAcceptingWithInterview by remember { mutableStateOf(false) }
    
    // Fonction pour télécharger la vidéo via l'endpoint candidat
           suspend fun downloadVideoViaCandidate(castingId: String, acteurId: String) {
               try {
                   val castingService = ApiClient.getCastingService()
                   val response = castingService.getCandidateVideo(castingId, acteurId)
                   if (response.isSuccessful && response.body() != null) {
                       val bytes = response.body()!!.bytes()
                       val tempFile = java.io.File.createTempFile("audition_video", ".mp4", context.cacheDir)
                       tempFile.writeBytes(bytes)
                       
                       // Utiliser FileProvider pour créer un content:// URI sécurisé
                       videoUri = androidx.core.content.FileProvider.getUriForFile(
                           context,
                           "${context.packageName}.fileprovider",
                           tempFile
                       )
                       showVideoDialog = true
                       isLoadingVideo = false
                       videoError = null
                   } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CandidateCard", "Erreur téléchargement vidéo: $errorCode - $errorBody")
                
                videoError = when (errorCode) {
                    401 -> {
                        val errorMsg = try {
                            val json = org.json.JSONObject(errorBody ?: "{}")
                            json.optString("message", "Token invalide ou expiré")
                        } catch (e: Exception) {
                            "Token invalide ou expiré"
                        }
                        if (errorMsg.contains("Token") || errorMsg.contains("expiré") || errorMsg.contains("invalide")) {
                            "Votre session a expiré. Veuillez vous reconnecter pour accéder à cette vidéo."
                        } else {
                            "Vous n'êtes pas autorisé à accéder à cette vidéo"
                        }
                    }
                    403 -> "Accès refusé à cette vidéo. Vous n'avez pas les permissions nécessaires."
                    404 -> "Vidéo non trouvée. Elle a peut-être été supprimée ou n'est pas encore disponible."
                    else -> "Erreur lors du téléchargement de la vidéo (code: $errorCode)"
                }
                isLoadingVideo = false
            }
        } catch (e: Exception) {
            android.util.Log.e("CandidateCard", "Exception téléchargement vidéo: ${e.message}", e)
            videoError = "Erreur: ${e.message ?: "Erreur inconnue"}"
            isLoadingVideo = false
        }
    }
    
    // Log pour déboguer
    LaunchedEffect(candidat.videoFileId, candidat.aiFeedback) {
        android.util.Log.d("CandidateCard", "videoFileId: ${candidat.videoFileId}, aiFeedback: ${candidat.aiFeedback != null}, acteurId: ${candidat.acteurId?.actualId}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DarkBlue.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(DarkBlue, Color(0xFF1E3A8A))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = "${candidat.acteurId?.prenom?.firstOrNull() ?: ""}${candidat.acteurId?.nom?.firstOrNull() ?: ""}"
                    Text(
                        text = initials.uppercase(),
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Column {
                    Text(
                        text = "${candidat.acteurId?.prenom ?: ""} ${candidat.acteurId?.nom ?: "Acteur"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = candidat.acteurId?.email ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    if (!candidat.dateCandidature.isNullOrBlank()) {
                        Text(
                            text = "Postulé le ${candidat.dateCandidature?.substring(0, 10) ?: ""}",
                            fontSize = 11.sp,
                            color = Color(0xFFBBBBBB)
                        )
                    }
                    // Afficher le score IA si disponible
                    candidat.aiFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Score IA: ${feedback.globalScore}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
            
            // Badge du statut
            val (statutText, statutColor) = when (candidat.statut?.uppercase()) {
                "ACCEPTE" -> "Accepté" to Color(0xFF4CAF50)
                "REFUSE" -> "Refusé" to Red
                else -> "En attente" to Color(0xFFFF9800)
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statutColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = statutText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = statutColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            }
            
            // Afficher le bouton vidéo si une vidéo est disponible (videoFileId ou aiFeedback présent)
            // Si aiFeedback existe, cela signifie qu'une vidéo a été envoyée
            val hasVideo = candidat.videoFileId != null || candidat.aiFeedback != null
            android.util.Log.d("CandidateCard", "hasVideo: $hasVideo, videoFileId: ${candidat.videoFileId}, aiFeedback: ${candidat.aiFeedback != null}")
            if (hasVideo) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Card pour la vidéo avec icône play
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoadingVideo && (candidat.videoFileId != null || candidat.acteurId?.actualId != null)) {
                            scope.launch {
                                isLoadingVideo = true
                                try {
                                    val acteurId = candidat.acteurId?.actualId
                                    if (acteurId == null) {
                                        android.util.Log.e("CandidateCard", "ActeurId est null")
                                        isLoadingVideo = false
                                        return@launch
                                    }
                                    
                                    // Essayer d'abord avec videoFileId, sinon utiliser l'endpoint candidat
                                    if (candidat.videoFileId != null) {
                                        // Méthode 1: Télécharger via fileId
                                           val acteurRepository = ActeurRepository()
                                           val result = acteurRepository.downloadMedia(candidat.videoFileId)
                                           result.onSuccess { bytes ->
                                               val tempFile = java.io.File.createTempFile("audition_video", ".mp4", context.cacheDir)
                                               tempFile.writeBytes(bytes)
                                               
                                               // Utiliser FileProvider pour créer un content:// URI sécurisé
                                               videoUri = androidx.core.content.FileProvider.getUriForFile(
                                                   context,
                                                   "${context.packageName}.fileprovider",
                                                   tempFile
                                               )
                                               showVideoDialog = true
                                               isLoadingVideo = false
                                               videoError = null
                                           }.onFailure { exception ->
                                            android.util.Log.e("CandidateCard", "Erreur téléchargement via fileId: ${exception.message}")
                                            // Essayer avec l'endpoint candidat
                                            downloadVideoViaCandidate(castingId, acteurId)
                                        }
                                    } else {
                                        // Méthode 2: Télécharger via endpoint candidat
                                        downloadVideoViaCandidate(castingId, acteurId)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("CandidateCard", "Exception: ${e.message}", e)
                                    videoError = "Erreur: ${e.message ?: "Erreur inconnue"}"
                                    isLoadingVideo = false
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (candidat.videoFileId != null) DarkBlue.copy(alpha = 0.1f) else Color(0xFFE0E0E0).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Icône play dans un cercle
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (candidat.videoFileId != null) DarkBlue else Color(0xFF9E9E9E),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingVideo) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Lire la vidéo",
                                        tint = White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vidéo d'audition",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                if (isLoadingVideo) {
                                    Text(
                                        text = "Chargement...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                } else if (candidat.videoFileId != null) {
                                    Text(
                                        text = "Appuyez pour regarder",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                } else {
                                    Text(
                                        text = "Vidéo en cours de traitement",
                                        fontSize = 12.sp,
                                        color = Color(0xFF999999),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                        
                        // Icône flèche (seulement si vidéo disponible)
                        if (!isLoadingVideo && candidat.videoFileId != null) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = DarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Boutons d'action (seulement si en attente)
            if (candidat.statut?.uppercase() == "EN_ATTENTE" || candidat.statut?.uppercase() == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton "Refuser"
                    Button(
                        onClick = onReject,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red.copy(alpha = 0.1f),
                            contentColor = Red
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refuser",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Bouton "Accepter avec interview"
                    Button(
                        onClick = { showInterviewDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accepter avec interview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
    
    // Dialogue pour proposer des dates d'interview
    if (showInterviewDialog) {
        InterviewDateProposalDialog(
            onDismiss = { showInterviewDialog = false },
            onConfirm = { dateOptions ->
                showInterviewDialog = false
                isAcceptingWithInterview = true
                
                scope.launch {
                    try {
                        val acteurId = candidat.acteurId?.actualId
                        if (acteurId == null) {
                            videoError = "Erreur: ID acteur non disponible"
                            isAcceptingWithInterview = false
                            return@launch
                        }
                        
                        val result = castingRepository.acceptCandidateWithInterview(
                            castingId = castingId,
                            acteurId = acteurId,
                            dates = dateOptions
                        )
                        
                        result.onSuccess { response ->
                            android.util.Log.d("CandidateCard", "✅ Candidat accepté avec interview créée")
                            // Recharger le casting pour obtenir la liste mise à jour
                            val refreshResult = castingRepository.getCastingById(castingId)
                            refreshResult.onSuccess { refreshedCasting ->
                                // Mettre à jour le casting dans le parent
                                // Note: Il faudrait passer un callback pour mettre à jour le casting
                                android.util.Log.d("CandidateCard", "✅ Casting rechargé")
                            }
                            isAcceptingWithInterview = false
                            onAcceptWithInterview() // Notifier le parent
                        }
                        
                        result.onFailure { exception ->
                            videoError = "Erreur: ${exception.message}"
                            android.util.Log.e("CandidateCard", "❌ Erreur acceptation avec interview: ${exception.message}")
                            isAcceptingWithInterview = false
                        }
                    } catch (e: Exception) {
                        videoError = "Erreur: ${e.message}"
                        android.util.Log.e("CandidateCard", "❌ Exception: ${e.message}")
                        isAcceptingWithInterview = false
                    }
                }
            }
        )
    }
    
    // Dialog d'erreur si le téléchargement échoue
    videoError?.let { error ->
        AlertDialog(
            onDismissRequest = { videoError = null },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        if (error.contains("401") || error.contains("Token") || error.contains("expiré")) 
                            "Session expirée" 
                        else 
                            "Erreur de téléchargement",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (error.contains("401") || error.contains("Token") || error.contains("expiré")) {
                            "Votre session a expiré. Veuillez vous reconnecter pour accéder à cette vidéo."
                        } else {
                            error
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        videoError = null
                        // Si c'est une erreur de session expirée, rediriger vers la connexion
                        if (error.contains("401") || error.contains("Token") || error.contains("expiré") || error.contains("Session")) {
                            onNavigateToLogin()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    )
                ) {
                    Text("Compris", color = White)
                }
            }
        )
    }
    
    // Dialog pour afficher la vidéo
    if (showVideoDialog && videoUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showVideoDialog = false
                videoUri?.let { uri ->
                    // Nettoyer le fichier temporaire
                    try {
                        val file = java.io.File(uri.path ?: "")
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CandidateCard", "Erreur suppression fichier: ${e.message}")
                    }
                }
                videoUri = null
            },
            title = {
                Text(
                    "Vidéo d'audition",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ouvrir la vidéo avec un Intent
                    LaunchedEffect(videoUri) {
                        videoUri?.let { uri ->
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "video/mp4")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("CandidateCard", "Erreur ouverture vidéo: ${e.message}", e)
                            }
                        }
                    }
                    Text(
                        "La vidéo s'ouvre dans votre lecteur vidéo par défaut",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showVideoDialog = false
                    videoUri = null
                }) {
                    Text("Fermer")
                }
            }
        )
    }
}

