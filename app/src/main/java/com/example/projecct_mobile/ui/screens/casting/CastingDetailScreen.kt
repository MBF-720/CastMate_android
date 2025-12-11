package com.example.projecct_mobile.ui.screens.casting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.projecct_mobile.R
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.AgenceProfile
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.AgenceRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.components.CandidatureSuccessDialog
import com.example.projecct_mobile.ui.components.CandidatureAlreadyAppliedDialog
import com.example.projecct_mobile.ui.components.CastingClosedDialog
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

@Composable
fun CastingDetailScreen(
    casting: Casting,
    onBackClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onNavigateToApplication: ((Casting) -> Unit)? = null, // Nouveau callback pour navigation vers candidature avec vidéo
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null,
    onNavigateToCandidatures: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val acteurRepository = remember { ActeurRepository() }
    val castingRepository = remember { CastingRepository() }
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    var afficheImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    
    // Charger l'état des favoris pour ce casting
    LaunchedEffect(casting.actualId) {
        val castingId = casting.actualId
        if (castingId != null) {
            try {
                val userResult = userRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    val userId = user?.actualId
                    if (!userId.isNullOrBlank()) {
                        val favoritesResult = acteurRepository.getFavorites(userId)
                        favoritesResult.onSuccess { favorites ->
                            isFavorite = favorites.any { it.actualId == castingId }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CastingDetailScreen", "Erreur chargement favoris: ${e.message}")
            }
        }
    }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAlreadyAppliedDialog by remember { mutableStateOf(false) }
    var showCastingClosedDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasAlreadyApplied by remember { mutableStateOf(false) }
    var hasConfirmedInterview by remember { mutableStateOf(false) }
    var isLoadingStatus by remember { mutableStateOf(true) }
    val tabs = listOf("overview", "Film", "production")
    
    // Vérifier si l'acteur a déjà postulé et s'il a une interview confirmée
    LaunchedEffect(casting.actualId) {
        val castingId = casting.actualId
        if (castingId != null) {
            isLoadingStatus = true
            try {
                // 1. Vérifier si l'acteur a déjà postulé
                val statusResult = castingRepository.getMyStatus(castingId)
                statusResult.onSuccess { status ->
                    hasAlreadyApplied = status.hasApplied
                }.onFailure { exception ->
                    // Si erreur 401, l'acteur n'a pas encore postulé (normal)
                    if (exception is ApiException.UnauthorizedException) {
                        hasAlreadyApplied = false
                    } else {
                        // Autre erreur, on assume qu'il n'a pas postulé pour permettre l'essai
                        hasAlreadyApplied = false
                    }
                }
                
                // 2. Vérifier si l'acteur a une interview confirmée pour ce casting
                try {
                    val interviewRepository = com.example.projecct_mobile.data.repository.InterviewRepository()
                    // Charger toutes les interviews pour être sûr de ne rien manquer
                    val allInterviewsResult = interviewRepository.getActorInterviews(null)
                    allInterviewsResult.onSuccess { allInterviews ->
                        // Vérifier s'il y a une interview confirmée pour ce casting
                        hasConfirmedInterview = allInterviews.any { interview ->
                            interview.castingId == castingId && 
                            interview.statusEnum == com.example.projecct_mobile.data.model.InterviewStatus.CONFIRMED
                        }
                        android.util.Log.d("CastingDetailScreen", "✅ Vérification interviews: ${allInterviews.size} interviews trouvées, confirmée: $hasConfirmedInterview pour casting $castingId")
                        if (hasConfirmedInterview) {
                            val confirmedInterview = allInterviews.firstOrNull { 
                                it.castingId == castingId && 
                                it.statusEnum == com.example.projecct_mobile.data.model.InterviewStatus.CONFIRMED 
                            }
                            android.util.Log.d("CastingDetailScreen", "📅 Interview confirmée: ${confirmedInterview?.selectedDate} à ${confirmedInterview?.selectedTime}")
                        }
                    }.onFailure { exception ->
                        android.util.Log.e("CastingDetailScreen", "❌ Erreur chargement interviews: ${exception.message}")
                        hasConfirmedInterview = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CastingDetailScreen", "❌ Exception chargement interviews: ${e.message}", e)
                    hasConfirmedInterview = false
                }
                
                isLoadingStatus = false
            } catch (e: Exception) {
                android.util.Log.e("CastingDetailScreen", "Erreur vérification statut: ${e.message}")
                hasAlreadyApplied = false
                hasConfirmedInterview = false
                isLoadingStatus = false
            }
        } else {
            isLoadingStatus = false
        }
    }

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
                    if (exception is ApiException.ForbiddenException) {
                        android.util.Log.d("CastingDetailScreen", "⚠️ Accès refusé à l'affiche (403)")
                    } else {
                        android.util.Log.e("CastingDetailScreen", "Erreur téléchargement affiche: ${exception.message}")
                    }
                    isLoadingImage = false
                }
            } catch (e: Exception) {
                android.util.Log.e("CastingDetailScreen", "Exception téléchargement affiche: ${e.message}")
                isLoadingImage = false
            }
        } else if (casting.actualAfficheFileId == null) {
            isLoadingImage = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                    Color(0xFFE57373),
                                    Color(0xFFAD1457)
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
            
            // Barre de navigation en haut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }

                IconButton(
                    onClick = {
                        scope.launch {
                            val castingId = casting.actualId
                            if (castingId != null) {
                                try {
                                    val userResult = userRepository.getCurrentUser()
                                    if (userResult.isSuccess) {
                                        val user = userResult.getOrNull()
                                        val userId = user?.actualId
                                        if (!userId.isNullOrBlank()) {
                                            val result = if (isFavorite) {
                                                acteurRepository.removeFavorite(userId, castingId)
                                            } else {
                                                acteurRepository.addFavorite(userId, castingId)
                                            }
                                            
                                            result.onSuccess {
                        isFavorite = !isFavorite
                                            }
                                            result.onFailure { exception ->
                                                android.util.Log.e("CastingDetailScreen", "Erreur favoris: ${exception.message}")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("CastingDetailScreen", "Exception favoris: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) RedHeart else White,
                        modifier = Modifier.size(24.dp)
                        )
                }
                    }
                }

        // Contenu scrollable qui se superpose à l'image (overlap)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp) // Commence plus haut pour créer l'overlap
        ) {
            // Surface blanche arrondie qui se superpose
            Surface(
            modifier = Modifier
                .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = White
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

                    // Contenu selon l'onglet sélectionné (avec espace pour le bouton fixe)
                    // Ajout de la détection de swipe gauche/droite
                    var dragOffset by remember { mutableStateOf(0f) }
                    
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        // Détecter la direction du swipe
                                        if (dragOffset > 100) {
                                            // Swipe vers la droite - onglet précédent
                                            if (selectedTabIndex > 0) {
                                                selectedTabIndex--
                                            }
                                        } else if (dragOffset < -100) {
                                            // Swipe vers la gauche - onglet suivant
                                            if (selectedTabIndex < tabs.size - 1) {
                                                selectedTabIndex++
                                            }
                                        }
                                        dragOffset = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragOffset += dragAmount
                                    }
                                )
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> OverviewContent(casting, onMapClick)
                        1 -> FilmContent(casting)
                        2 -> ProductionContent(casting)
                    }

                        // Espace pour le bouton fixe + barre de navigation
                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }

        // Bouton Submit FIXE en bas (au-dessus de la barre de navigation)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(White)
        ) {
            // Afficher le message d'erreur si présent (AU-DESSUS du bouton)
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFF44336),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Bouton Submit fixe
            Button(
                onClick = {
                    val castingId = casting.actualId
                    
                    // Vérifier d'abord si l'acteur a déjà postulé
                    if (hasAlreadyApplied) {
                        showAlreadyAppliedDialog = true
                        return@Button
                    }
                    
                    // Vérifier si l'acteur a une interview confirmée
                    if (hasConfirmedInterview) {
                        errorMessage = "Vous avez une interview confirmée pour ce casting. Vous ne pouvez plus postuler à nouveau."
                        return@Button
                    }
                    
                    // Vérifier d'abord si le casting est ouvert
                    if (!casting.ouvert) {
                        showCastingClosedDialog = true
                        return@Button
                    }
                    
                    // Naviguer vers l'écran de candidature avec vidéo
                    if (castingId != null) {
                        onNavigateToApplication?.invoke(casting) ?: run {
                            // Fallback : ancien comportement (candidature sans vidéo)
                            if (!isSubmitting) {
                                isSubmitting = true
                                errorMessage = null
                                showSuccessDialog = false
                                showAlreadyAppliedDialog = false
                                showCastingClosedDialog = false
                                scope.launch {
                                    try {
                                        val result = castingRepository.applyToCasting(castingId)
                                        result.onSuccess {
                                            android.util.Log.d("CastingDetailScreen", "✅ Candidature envoyée avec succès pour le casting: ${casting.titre}")
                                            errorMessage = null
                                            onSubmitClick()
                                            showSuccessDialog = true
                                            isSubmitting = false
                                        }
                                        result.onFailure { exception ->
                                            android.util.Log.e("CastingDetailScreen", "❌ Erreur lors de la candidature: ${exception.message}", exception)
                                            showSuccessDialog = false
                                            
                                            val errorMessageText = exception.message?.lowercase() ?: ""
                                            val isCastingClosed = errorMessageText.contains("n'accepte plus") ||
                                                    errorMessageText.contains("fermé") ||
                                                    errorMessageText.contains("closed") ||
                                                    errorMessageText.contains("n'accepte pas") ||
                                                    (!casting.ouvert)
                                            
                                            when {
                                                isCastingClosed -> {
                                                    showCastingClosedDialog = true
                                                    showAlreadyAppliedDialog = false
                                                    errorMessage = null
                                                }
                                                exception is ApiException.ConflictException -> {
                                                    showAlreadyAppliedDialog = true
                                                    showCastingClosedDialog = false
                                                    errorMessage = null
                                                }
                                                exception is ApiException.UnauthorizedException -> {
                                                    errorMessage = "Vous devez être connecté pour postuler"
                                                    showAlreadyAppliedDialog = false
                                                    showCastingClosedDialog = false
                                                }
                                                exception is ApiException.ForbiddenException -> {
                                                    if (errorMessageText.contains("n'accepte plus") || errorMessageText.contains("fermé")) {
                                                        showCastingClosedDialog = true
                                                        showAlreadyAppliedDialog = false
                                                        errorMessage = null
                                                    } else {
                                                        errorMessage = "Vous ne pouvez pas postuler à ce casting"
                                                        showAlreadyAppliedDialog = false
                                                        showCastingClosedDialog = false
                                                    }
                                                }
                                                exception is ApiException.BadRequestException -> {
                                                    if (errorMessageText.contains("n'accepte plus") || errorMessageText.contains("fermé")) {
                                                        showCastingClosedDialog = true
                                                        showAlreadyAppliedDialog = false
                                                        errorMessage = null
                                                    } else {
                                                        errorMessage = "Erreur lors de la candidature: ${exception.message}"
                                                        showAlreadyAppliedDialog = false
                                                        showCastingClosedDialog = false
                                                    }
                                                }
                                                else -> {
                                                    errorMessage = "Erreur: ${exception.message}"
                                                    showAlreadyAppliedDialog = false
                                                    showCastingClosedDialog = false
                                                }
                                            }
                                            isSubmitting = false
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("CastingDetailScreen", "❌ Exception lors de la candidature: ${e.message}", e)
                                        showSuccessDialog = false
                                        showAlreadyAppliedDialog = false
                                        showCastingClosedDialog = false
                                        errorMessage = "Erreur inconnue: ${e.message}"
                                        isSubmitting = false
                                    }
                                }
                            }
                        }
                    } else if (castingId == null) {
                        errorMessage = "ID de casting invalide"
                        showSuccessDialog = false
                        showAlreadyAppliedDialog = false
                        showCastingClosedDialog = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasAlreadyApplied || hasConfirmedInterview) Color.Gray else DarkBlue,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = White.copy(alpha = 0.6f)
                ),
                enabled = !isSubmitting && !hasAlreadyApplied && !hasConfirmedInterview && !isLoadingStatus
            ) {
                if (isSubmitting || isLoadingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else if (hasConfirmedInterview) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interview confirmée",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                } else if (hasAlreadyApplied) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Déjà postulé",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                } else {
                    Text(
                        text = "Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
            
            // Message informatif si interview confirmée
            if (hasConfirmedInterview && !isLoadingStatus) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Vous avez une interview confirmée pour ce casting. Consultez votre agenda pour plus de détails.",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barre de navigation du bas (même que ActorHomeScreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 17.dp)
            ) {
                ActorBottomNavigationBar(
                    selectedItem = NavigationItem.HOME,
                    onCandidaturesClick = {
                        // Naviguer vers "Mes candidatures"
                        onNavigateToCandidatures?.invoke()
                    },
                    onHomeClick = { 
                        // Retourner à la page d'accueil de l'acteur
                        onNavigateToHome?.invoke() 
                    },
                    onProfileClick = { 
                        // Naviguer vers les paramètres de l'acteur
                        onNavigateToProfile?.invoke() 
                    }
                )
            }
        }
    }
    
    // Dialogue de confirmation de candidature
    if (showSuccessDialog) {
        CandidatureSuccessDialog(
            onDismiss = { showSuccessDialog = false },
            onViewCandidatures = {
                onNavigateToCandidatures?.invoke()
            }
        )
    }
    
    // Dialogue pour "déjà postulé"
    if (showAlreadyAppliedDialog) {
        CandidatureAlreadyAppliedDialog(
            onDismiss = { showAlreadyAppliedDialog = false },
            onViewCandidatures = {
                onNavigateToCandidatures?.invoke()
            }
        )
    }
    
    // Dialogue pour "casting fermé"
    if (showCastingClosedDialog) {
        CastingClosedDialog(
            onDismiss = { showCastingClosedDialog = false }
        )
    }
}

@Composable
fun OverviewContent(casting: Casting, onMapClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Badge de statut (Ouvert/Fermé) - UNIQUEMENT dans Overview
        val isOpen = casting.ouvert == true
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isOpen) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFF44336).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                    )
        Text(
                        text = if (isOpen) "Ouvert" else "Fermé",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOpen) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
        
        // Description
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = casting.descriptionRole ?: casting.synopsis ?: "Aucune description disponible",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }

        // Dates
        val dateDebut = casting.dateDebut
        val dateFin = casting.dateFin
        if (!dateDebut.isNullOrBlank() || !dateFin.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                    text = "dates",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                if (!dateDebut.isNullOrBlank()) {
            Text(
                        text = formatDate(dateDebut),
                fontSize = 14.sp,
                        color = Color(0xFF666666)
            )
                }
                if (!dateFin.isNullOrBlank() && dateFin != dateDebut) {
            Text(
                        text = formatDate(dateFin),
                fontSize = 14.sp,
                        color = Color(0xFF666666)
            )
                }
            }
        }

        // Rôle (Character Type, Gender/Age, Physical Traits)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "rôle",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            
            // Types de casting
            if (!casting.types.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Character Type: ${casting.types.joinToString(", ")}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                )
            }
        }

            // Âge
            if (!casting.age.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                        text = "•",
                    fontSize = 14.sp,
                        color = Color(0xFF666666)
                )
                Text(
                        text = "Gender/Age: ${casting.age}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                )
            }
        }

            // Conditions physiques/traits
            if (!casting.conditions.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
            Text(
                        text = "•",
                fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                        text = "Physical Traits: ${casting.conditions}",
                fontSize = 14.sp,
                        color = Color(0xFF666666),
                lineHeight = 20.sp
            )
                }
            }
        }

        // Lieu
        if (!casting.lieu.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                    text = "Lieu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
            )
            Text(
                    text = casting.lieu,
                fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        // Prix
        if (casting.prix != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rémunération",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "${casting.prix}€",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
            )
        }
    }
}
}

/**
 * Composant pour afficher une icône de lien social (icône Material) avec label
 */
@Composable
private fun SocialLinkIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = backgroundColor.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Composant pour afficher une icône de lien social (image drawable) avec label
 */
@Composable
private fun SocialLinkImageIcon(
    painter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = backgroundColor.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Formate une date ISO en format lisible en français
 * Exemple: "2024-01-15T00:00:00.000Z" -> "15 janvier 2024"
 *          "2024-01-15" -> "15 janvier 2024"
 */
fun formatDate(dateString: String): String {
    return try {
        // Essayer de parser différents formats de date
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
            // Si le parsing échoue, retourner la date originale
            dateString
        }
    } catch (e: Exception) {
        // En cas d'erreur, retourner la date originale
        dateString
    }
}

@Composable
fun FilmContent(casting: Casting) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Synopsis
        if (!casting.synopsis.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                    text = "Synopsis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                    text = casting.synopsis,
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            }
        }

        // Description du rôle
        if (!casting.descriptionRole.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                    text = "Description du rôle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                    text = casting.descriptionRole,
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }
        }
    }
}

@Composable
fun ProductionContent(casting: Casting) {
    val recruteur = casting.recruteur
    val context = LocalContext.current
    val acteurRepository = remember { ActeurRepository() }
    val agenceRepository = remember { AgenceRepository() }
    val scope = rememberCoroutineScope()
    var logoImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingLogo by remember { mutableStateOf(false) }
    var agenceProfile by remember { mutableStateOf<AgenceProfile?>(null) }
    var isLoadingAgence by remember { mutableStateOf(false) }

    // Récupérer les informations complètes de l'agence
    LaunchedEffect(recruteur?.actualId) {
        val agenceId = recruteur?.actualId
        android.util.Log.d("ProductionContent", "🔍 Recruteur: $recruteur")
        android.util.Log.d("ProductionContent", "🔍 Recruteur ID (actualId): $agenceId")
        android.util.Log.d("ProductionContent", "🔍 Recruteur tel: ${recruteur?.tel}")
        android.util.Log.d("ProductionContent", "🔍 Recruteur gouvernorat: ${recruteur?.gouvernorat}")
        android.util.Log.d("ProductionContent", "🔍 Recruteur siteWeb: ${recruteur?.siteWeb}")
        android.util.Log.d("ProductionContent", "🔍 Recruteur description: ${recruteur?.description}")
        android.util.Log.d("ProductionContent", "🔍 Recruteur socialLinks: ${recruteur?.socialLinks}")
        
        if (agenceId != null && agenceProfile == null && !isLoadingAgence) {
            isLoadingAgence = true
            android.util.Log.d("ProductionContent", "📞 Récupération des informations complètes de l'agence: $agenceId")
            try {
                val result = agenceRepository.getAgenceById(agenceId)
                result.onSuccess { profile ->
                    android.util.Log.d("ProductionContent", "✅ Informations agence récupérées: ${profile.nomAgence}")
                    android.util.Log.d("ProductionContent", "✅ Téléphone: ${profile.tel}")
                    android.util.Log.d("ProductionContent", "✅ Gouvernorat: ${profile.gouvernorat}")
                    android.util.Log.d("ProductionContent", "✅ Site web: ${profile.siteWeb}")
                    android.util.Log.d("ProductionContent", "✅ Description: ${profile.description}")
                    android.util.Log.d("ProductionContent", "✅ Social links: ${profile.socialLinks}")
                    agenceProfile = profile
                    isLoadingAgence = false
                }
                result.onFailure { exception ->
                    android.util.Log.e("ProductionContent", "❌ Erreur récupération agence: ${exception.message}")
                    android.util.Log.e("ProductionContent", "❌ Exception type: ${exception::class.simpleName}")
                    // On continue avec les informations de base du recruteur si l'appel échoue
                    isLoadingAgence = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductionContent", "Exception récupération agence: ${e.message}", e)
                isLoadingAgence = false
            }
        } else {
            if (agenceId == null) {
                android.util.Log.w("ProductionContent", "⚠️ ID agence null - utilisation des informations de base du recruteur")
            }
            isLoadingAgence = false
        }
    }

    // Utiliser les informations complètes de l'agence si disponibles, sinon utiliser les informations de base du recruteur
    val agenceInfo = agenceProfile ?: run {
        // Convertir RecruteurInfo en AgenceProfile si on n'a pas les informations complètes
        if (recruteur != null) {
            AgenceProfile(
                id = recruteur.actualId,
                idAlt = recruteur.idAlt,
                nomAgence = recruteur.nomAgence,
                responsable = recruteur.responsable,
                email = recruteur.email,
                tel = recruteur.tel,
                gouvernorat = recruteur.gouvernorat,
                siteWeb = recruteur.siteWeb,
                description = recruteur.description,
                socialLinks = recruteur.socialLinks?.let { links ->
                    com.example.projecct_mobile.data.model.AgenceSocialLinks(
                        facebook = links.facebook,
                        instagram = links.instagram
                    )
                },
                media = recruteur.media?.let { media ->
                    com.example.projecct_mobile.data.model.UserMedia(
                        photoFileId = media.photoFileId,
                        documentFileId = null,
                        gallery = null
                    )
                },
                createdAt = null,
                updatedAt = null
            )
        } else {
            null
        }
    }

    // Télécharger le logo de l'agence si disponible
    LaunchedEffect(agenceInfo?.media?.photoFileId) {
        val photoFileId = agenceInfo?.media?.photoFileId
        if (photoFileId != null && logoImage == null && !isLoadingLogo) {
            isLoadingLogo = true
            try {
                val result = acteurRepository.downloadMedia(photoFileId)
                result.onSuccess { bytes ->
                    if (bytes != null && bytes.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            bitmap?.let {
                                logoImage = it.asImageBitmap()
                            }
                        }
                    }
                    isLoadingLogo = false
                }
                result.onFailure { exception ->
                    if (exception is ApiException.ForbiddenException) {
                        android.util.Log.d("ProductionContent", "⚠️ Accès refusé au logo (403)")
                    } else {
                        android.util.Log.e("ProductionContent", "Erreur téléchargement logo: ${exception.message}")
                    }
                    isLoadingLogo = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductionContent", "Exception téléchargement logo: ${e.message}")
                isLoadingLogo = false
            }
        } else if (photoFileId == null) {
            isLoadingLogo = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Titre de la section
        Text(
            text = "Agence de casting",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        if (agenceInfo != null) {
            // Indicateur de chargement des informations complètes
            if (isLoadingAgence && agenceProfile == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DarkBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chargement des informations...",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Logo de l'agence
            if (logoImage != null || isLoadingLogo) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DarkBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingLogo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = DarkBlue,
                                strokeWidth = 2.dp
                            )
                        } else if (logoImage != null) {
                            Image(
                                bitmap = logoImage!!,
                                contentDescription = "Logo de l'agence",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Icône par défaut si le logo n'est pas disponible
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Logo",
                                tint = DarkBlue,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // Nom de l'agence
            if (!agenceInfo.nomAgence.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Nom de l'agence",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                        text = agenceInfo.nomAgence,
                        fontSize = 16.sp,
                        color = Black,
                        fontWeight = FontWeight.SemiBold
            )
        }
    }

            // Responsable
            if (!agenceInfo.responsable.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Responsable",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                        text = agenceInfo.responsable,
                fontSize = 14.sp,
                        color = Color(0xFF555555)
            )
                }
        }

            // Téléphone
            if (!agenceInfo.tel.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Téléphone",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            try {
                                val phoneNumber = agenceInfo.tel!!.trim()
                                // Nettoyer le numéro de téléphone (supprimer les espaces, tirets, etc.)
                                val cleanPhoneNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                                intent.data = android.net.Uri.parse("tel:$cleanPhoneNumber")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("ProductionContent", "Erreur ouverture téléphone: ${e.message}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Appeler",
                            tint = DarkBlue,
                            modifier = Modifier.size(20.dp)
            )
            Text(
                            text = agenceInfo.tel,
                fontSize = 14.sp,
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Email
            if (!agenceInfo.email.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Email",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            try {
                                val emailAddress = agenceInfo.email!!.trim()
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO)
                                intent.data = android.net.Uri.parse("mailto:$emailAddress")
                                intent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                                context.startActivity(android.content.Intent.createChooser(intent, "Envoyer un email"))
                            } catch (e: Exception) {
                                android.util.Log.e("ProductionContent", "Erreur ouverture email: ${e.message}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Envoyer un email",
                            tint = DarkBlue,
                            modifier = Modifier.size(20.dp)
            )
            Text(
                            text = agenceInfo.email,
                fontSize = 14.sp,
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Gouvernorat
            if (!agenceInfo.gouvernorat.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Gouvernorat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DarkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = agenceInfo.gouvernorat,
                fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }
            }

            // Description
            if (!agenceInfo.description.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Text(
                        text = agenceInfo.description,
                fontSize = 14.sp,
                        color = Color(0xFF555555),
                lineHeight = 20.sp
            )
        }
    }

            // Liens et réseaux sociaux
            val socialLinks = agenceInfo.socialLinks
            val hasSiteWeb = !agenceInfo.siteWeb.isNullOrBlank()
            val hasFacebook = !socialLinks?.facebook.isNullOrBlank()
            val hasInstagram = !socialLinks?.instagram.isNullOrBlank()
            
            // Logs de débogage pour vérifier les liens
            android.util.Log.d("ProductionContent", "🔗 Site web: '${agenceInfo.siteWeb}' (hasSiteWeb: $hasSiteWeb)")
            android.util.Log.d("ProductionContent", "🔗 Social links: $socialLinks")
            android.util.Log.d("ProductionContent", "🔗 Facebook: '${socialLinks?.facebook}' (hasFacebook: $hasFacebook)")
            android.util.Log.d("ProductionContent", "🔗 Instagram: '${socialLinks?.instagram}' (hasInstagram: $hasInstagram)")
            
            // Afficher la section "Liens" si au moins un lien est disponible
            if (hasSiteWeb || hasFacebook || hasInstagram) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
            Text(
                        text = "Liens",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    // Icônes cliquables en ligne (style similaire aux autres écrans)
        Row(
            modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
                        // Site web
                        if (hasSiteWeb) {
                            val siteWebUrl = agenceInfo.siteWeb!!.let { url ->
                                when {
                                    url.startsWith("http://") || url.startsWith("https://") -> url
                                    url.startsWith("www.") -> "https://$url"
                                    else -> "https://$url"
                                }
                            }
                            
                            SocialLinkIcon(
                                icon = Icons.Default.Language,
                                label = "Site web",
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse(siteWebUrl)
                                        context.startActivity(android.content.Intent.createChooser(intent, "Ouvrir avec"))
                                    } catch (e: Exception) {
                                        android.util.Log.e("ProductionContent", "Erreur ouverture site web: ${e.message}")
                                    }
                                },
                                backgroundColor = DarkBlue.copy(alpha = 0.1f),
                                iconTint = DarkBlue
                            )
                        }
                        
                        // Facebook
                        if (hasFacebook) {
                            val facebookUrl = socialLinks!!.facebook!!.let { url ->
                                when {
                                    url.startsWith("http://") || url.startsWith("https://") -> url
                                    url.startsWith("www.") -> "https://$url"
                                    url.startsWith("facebook.com") -> "https://$url"
                                    url.startsWith("fb.com") -> "https://$url"
                                    else -> "https://$url"
                                }
                            }
                            
                            SocialLinkImageIcon(
                                painter = painterResource(id = R.drawable.facebook),
                                label = "Facebook",
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse(facebookUrl)
                                        context.startActivity(android.content.Intent.createChooser(intent, "Ouvrir avec"))
                                    } catch (e: Exception) {
                                        android.util.Log.e("ProductionContent", "Erreur ouverture lien Facebook: ${e.message}")
                                    }
                                },
                                backgroundColor = Color(0xFF1877F2).copy(alpha = 0.1f)
            )
        }
                        
                        // Instagram
                        if (hasInstagram) {
                            val instagramUrl = socialLinks!!.instagram!!.let { url ->
                                when {
                                    url.startsWith("http://") || url.startsWith("https://") -> url
                                    url.startsWith("www.") -> "https://$url"
                                    url.startsWith("instagram.com") -> "https://$url"
                                    url.startsWith("@") -> "https://instagram.com/${url.substring(1)}"
                                    else -> "https://$url"
                                }
                            }
                            
                            SocialLinkImageIcon(
                                painter = painterResource(id = R.drawable.instagram),
                                label = "Instagram",
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse(instagramUrl)
                                        context.startActivity(android.content.Intent.createChooser(intent, "Ouvrir avec"))
                                    } catch (e: Exception) {
                                        android.util.Log.e("ProductionContent", "Erreur ouverture lien Instagram: ${e.message}")
                                    }
                                },
                                backgroundColor = Color(0xFFE4405F).copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        } else {
            // Aucune information sur l'agence
    Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Text(
                    text = "Aucune information sur l'agence disponible",
                fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
                if (recruteur == null) {
        Text(
                        text = "Les informations de l'agence n'ont pas été renvoyées par le backend",
            fontSize = 12.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center
        )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CastingDetailScreenPreview() {
    Projecct_MobileTheme {
        CastingDetailScreen(
            casting = com.example.projecct_mobile.data.model.Casting(
                idAlt = "1",
                titre = "Dune : Part 3",
                dateDebut = "2025-10-30",
                dateFin = "2025-10-30",
                descriptionRole = "Arven",
                synopsis = "Paul Atreides faces new political and spiritual challenges as...",
                age = "20+",
                prix = 20.0,
                lieu = "Paris",
                ouvert = true
            )
        )
    }
}

