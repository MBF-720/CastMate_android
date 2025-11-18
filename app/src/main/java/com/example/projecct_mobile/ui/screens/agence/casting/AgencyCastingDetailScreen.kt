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
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Candidat
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AgencyCastingDetailScreen(
    casting: Casting,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onViewActorProfile: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val acteurRepository = remember { ActeurRepository() }
    val castingRepository = remember { com.example.projecct_mobile.data.repository.CastingRepository() }
    val scope = rememberCoroutineScope()
    var afficheImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Détails", "Candidats")
    var currentCasting by remember { mutableStateOf(casting) }
    var isUpdatingStatus by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Télécharger l'affiche si disponible
    LaunchedEffect(casting.actualAfficheFileId) {
        if (casting.actualAfficheFileId != null && afficheImage == null && !isLoadingImage) {
            isLoadingImage = true
            try {
                val result = acteurRepository.downloadMedia(casting.actualAfficheFileId!!)
                result.onSuccess { bytes ->
                    if (bytes != null && bytes.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            bitmap?.let {
                                afficheImage = it.asImageBitmap()
                            }
                        }
                    }
                    isLoadingImage = false
                }
                result.onFailure { exception ->
                    android.util.Log.e("AgencyCastingDetail", "Erreur téléchargement affiche: ${exception.message}")
                    isLoadingImage = false
                }
            } catch (e: Exception) {
                android.util.Log.e("AgencyCastingDetail", "Exception téléchargement affiche: ${e.message}")
                isLoadingImage = false
            }
        } else if (casting.actualAfficheFileId == null) {
            isLoadingImage = false
        }
    }

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
            if (afficheImage != null) {
                Image(
                    bitmap = afficheImage!!,
                    contentDescription = "Affiche du casting",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
            
            if (isLoadingImage) {
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
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                isUpdating = isUpdatingStatus
                            )
                        }
                    }
                    1 -> {
                        // Onglet "Candidats"
                        CandidatesContent(
                            castingId = currentCasting.actualId ?: "",
                            candidates = currentCasting.candidats ?: emptyList(),
                            modifier = Modifier.fillMaxSize(),
                            onAcceptCandidate = { acteurId ->
                                scope.launch {
                                    try {
                                        val result = castingRepository.acceptCandidate(
                                            castingId = currentCasting.actualId ?: "",
                                            acteurId = acteurId
                                        )
                                        result.onSuccess {
                                            // Recharger le casting pour obtenir la liste mise à jour
                                            val refreshResult = castingRepository.getCastingById(currentCasting.actualId ?: "")
                                            refreshResult.onSuccess { refreshedCasting ->
                                                currentCasting = refreshedCasting
                                                android.util.Log.d("AgencyCastingDetail", "✅ Candidat accepté")
                                            }
                                        }
                                        result.onFailure { exception ->
                                            errorMessage = "Erreur: ${exception.message}"
                                            android.util.Log.e("AgencyCastingDetail", "❌ Erreur acceptation: ${exception.message}")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur: ${e.message}"
                                        android.util.Log.e("AgencyCastingDetail", "❌ Exception: ${e.message}")
                                    }
                                }
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
                            onViewProfile = onViewActorProfile
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
    isUpdating: Boolean = false
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
    onAcceptCandidate: (String) -> Unit = {},
    onRejectCandidate: (String) -> Unit = {},
    onViewProfile: (String) -> Unit = {}
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
                    onAccept = {
                        candidat.acteurId?.actualId?.let { onAcceptCandidate(it) }
                    },
                    onReject = {
                        candidat.acteurId?.actualId?.let { onRejectCandidate(it) }
                    },
                    onViewProfile = {
                        candidat.acteurId?.actualId?.let { onViewProfile(it) }
                    }
                )
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidat: Candidat,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onViewProfile: () -> Unit = {}
) {
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
            
            // Boutons d'action (seulement si en attente)
            if (candidat.statut?.uppercase() == "EN_ATTENTE" || candidat.statut?.uppercase() == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
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
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accepter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

