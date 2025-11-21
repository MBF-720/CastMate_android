package com.example.projecct_mobile.ui.screens.agence.casting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import kotlinx.coroutines.launch
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.repository.AgenceRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.toCastingItem
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CastingListAgencyScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (CastingItem) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onCreateCastingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = onProfileClick,
    onAgendaClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    loadData: Boolean = true,
    initialCastings: List<CastingItem> = emptyList(),
    initialAgencyName: String = "",
    initialAgencyEmail: String = "",
    refreshTrigger: Int = 0 // Clé pour forcer le rafraîchissement
) {
    var searchQuery by remember { mutableStateOf("") }
    var castings by remember { mutableStateOf(initialCastings) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    var agencyName by remember { mutableStateOf(initialAgencyName) }
    var agencyEmail by remember { mutableStateOf(initialAgencyEmail) }

    val castingRepository = remember(loadData) {
        if (loadData) CastingRepository() else null
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember(loadData) { if (loadData) TokenManager(context) else null }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(loadData) {
        if (!loadData) return@LaunchedEffect

        val email = withContext(Dispatchers.IO) { tokenManager?.getUserEmailSync() }
        agencyEmail = email.orEmpty()
        val derivedName = initialAgencyName.takeIf { it.isNotBlank() }
            ?: email?.substringBefore('@')?.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase() else ch.toString()
            } ?: "Agence"
        agencyName = derivedName
    }

    // Clé pour forcer le rechargement de la liste
    var refreshKey by remember { mutableStateOf(refreshTrigger) }
    
    // Mettre à jour refreshKey quand refreshTrigger change
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > refreshKey) {
            refreshKey = refreshTrigger
        }
    }
    
    LaunchedEffect(refreshKey, loadData) {
        if (!loadData) return@LaunchedEffect
        isLoading = true
        errorMessage = null

        try {
            val agencyId = withContext(Dispatchers.IO) { tokenManager?.getUserIdSync() }
            android.util.Log.d("CastingListAgency", "🔄 Chargement des castings pour agence ID: $agencyId")
            
            val result = castingRepository?.getAllCastings()

            result?.onSuccess { apiCastings ->
                android.util.Log.d("CastingListAgency", "📋 Castings reçus de l'API: ${apiCastings.size}")
                
                val filtered = agencyId?.let { id ->
                    val filteredList = apiCastings.filter { casting ->
                        val belongs = casting.belongsToRecruiter(id)
                        android.util.Log.d("CastingListAgency", "🔍 Casting '${casting.titre}': belongs=$belongs, recruteurId=${casting.recruteur?.actualId}")
                        belongs
                    }
                    android.util.Log.d("CastingListAgency", "✅ Castings filtrés pour agence $id: ${filteredList.size}")
                    filteredList
                } ?: apiCastings
                
                castings = filtered.map { it.toCastingItem() }
                android.util.Log.d("CastingListAgency", "✅ Castings affichés: ${castings.size}")
                isLoading = false
            }

            result?.onFailure { exception ->
                android.util.Log.e("CastingListAgency", "❌ Erreur: ${exception.message}", exception)
                errorMessage = getErrorMessage(exception)
                isLoading = false
            }
        } catch (e: Exception) {
            android.util.Log.e("CastingListAgency", "❌ Exception: ${e.message}", e)
            errorMessage = getErrorMessage(e)
            isLoading = false
        }
    }
    

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DarkBlue,
                                    DarkBlue.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = agencyName.take(2).uppercase().ifEmpty { "AG" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }

                        Column {
                            Text(
                                text = agencyEmail.ifEmpty { agencyName.ifEmpty { "Agence" } },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            if (agencyName.isNotEmpty() && agencyName != agencyEmail) {
                                Text(
                                    text = agencyName,
                                    fontSize = 14.sp,
                                    color = White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    DrawerMenuItem(
                        icon = Icons.Default.CalendarToday,
                        text = "Agenda",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAgendaClick()
                        }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.History,
                        text = "Historique",
                        onClick = {
                            scope.launch { drawerState.close() }
                            showComingSoon = "Historique"
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = GrayBorder.copy(alpha = 0.3f)
                    )
                    DrawerMenuItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        text = "Déconnexion",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        },
                        textColor = Red
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5FB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(DarkBlue, DarkBlueLight)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(alpha = 0.18f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = White
                                )
                            }

                            Text(
                                text = "Castings",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(alpha = 0.18f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Gérez vos annonces et suivez vos candidatures",
                            fontSize = 15.sp,
                            color = White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 28.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Rechercher un casting...", color = GrayBorder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = DarkBlue.copy(alpha = 0.7f)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFFF3F6FF),
                                unfocusedContainerColor = Color(0xFFF3F6FF),
                                cursorColor = DarkBlue,
                                focusedTextColor = Black,
                                unfocusedTextColor = Black,
                                focusedPlaceholderColor = GrayBorder,
                                unfocusedPlaceholderColor = GrayBorder
                            ),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkBlue)
                                .clickable { showComingSoon = "Filtres" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filtres",
                                tint = White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorMessage(
                            message = errorMessage ?: "Erreur",
                            onRetry = {
                                scope.launch {
                                    if (castingRepository == null) return@launch
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val agencyId = withContext(Dispatchers.IO) { tokenManager?.getUserIdSync() }
                                        val result = castingRepository.getAllCastings()
                                        result.onSuccess { apiCastings ->
                                            val filtered = agencyId?.let { id ->
                                                apiCastings.filter { it.belongsToRecruiter(id) }
                                            } ?: apiCastings
                                            castings = filtered.map { it.toCastingItem() }
                                            isLoading = false
                                        }
                                        result.onFailure { exception ->
                                            errorMessage = getErrorMessage(exception)
                                            isLoading = false
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = getErrorMessage(e)
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val filteredItems = castings.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredItems.isEmpty()) {
                            item {
                                EmptyCastingState(onCreateCastingClick)
                            }
                        } else {
                            items(filteredItems) { casting ->
                                LocalAgencyCastingCard(
                                    casting = casting,
                                    onEditClick = { onItemClick(casting) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AgencyBottomNavigationBar(
                onHomeClick = { /* déjà sur l'écran */ },
                onAgendaClick = onAgendaClick,
                onHistoryClick = { showComingSoon = "Historique" },
                onProfileClick = onProfileClick,
                onCreateCastingClick = onCreateCastingClick
            )
        }
    }

    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
}

@Composable
private fun EmptyCastingState(onCreateCastingClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            tint = GrayBorder,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = "Aucun casting pour le moment",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkBlue
        )
        Text(
            text = "Créez votre première annonce pour la rendre visible ici.",
            fontSize = 14.sp,
            color = GrayBorder,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Button(
            onClick = onCreateCastingClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
        ) {
            Text("Créer un casting", color = White, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Casting.belongsToRecruiter(recruiterId: String): Boolean {
    // Selon la documentation, recruteur est maintenant un objet RecruteurInfo
    // Utiliser actualId qui gère à la fois id et idAlt
    val recruiterActualId = recruteur?.actualId
    android.util.Log.d("CastingListAgency", "🔍 Vérification casting: recruteurId=$recruiterActualId, attendu=$recruiterId")
    return recruiterActualId?.equals(recruiterId, ignoreCase = true) == true
}

@Composable
private fun LocalAgencyCastingCard(
    casting: CastingItem,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val agenceRepository = remember { AgenceRepository() }
    val scope = rememberCoroutineScope()
    
    var afficheImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    
    // Charger l'image si afficheFileId existe
    LaunchedEffect(casting.afficheFileId) {
        if (casting.afficheFileId != null && casting.afficheFileId.isNotBlank()) {
            isLoadingImage = true
            val result = agenceRepository.downloadMedia(casting.afficheFileId)
            result.onSuccess { bytes ->
                try {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    afficheImage = bitmap?.asImageBitmap()
                } catch (e: Exception) {
                    android.util.Log.e("LocalAgencyCastingCard", "Erreur décodage image: ${e.message}")
                }
                isLoadingImage = false
            }
            result.onFailure {
                android.util.Log.e("LocalAgencyCastingCard", "Erreur chargement image: ${it.message}")
                isLoadingImage = false
            }
        } else if (casting.afficheFileId == null) {
            isLoadingImage = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .border(2.dp, DarkBlue, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image de l'affiche à gauche
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkBlue.copy(alpha = 0.1f),
                                DarkBlue.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(1.dp, DarkBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingImage -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DarkBlue,
                            strokeWidth = 2.dp
                        )
                    }
                    afficheImage != null -> {
                        Image(
                            bitmap = afficheImage!!,
                            contentDescription = "Affiche du casting",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Text("📷", fontSize = 48.sp)
                    }
                }
            }
            
            // Informations du casting à droite
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Titre
                    Text(
                        text = casting.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Description
                    Text(
                        text = casting.description,
                        fontSize = 13.sp,
                        color = Color(0xFF555555),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    
                    // Rôle et âge
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "rôle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                            Text(
                                text = casting.role,
                                fontSize = 13.sp,
                                color = Color(0xFF555555)
                            )
                        }
                        if (casting.age.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "age",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                Text(
                                    text = casting.age,
                                    fontSize = 13.sp,
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                        
                        // Statut du casting (Ouvert/Fermé)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (casting.ouvert) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (casting.ouvert) Color(0xFF4CAF50) else Red,
                                modifier = Modifier.size(14.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (casting.ouvert) Color(0xFF4CAF50).copy(alpha = 0.1f) else Red.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (casting.ouvert) "Ouvert" else "Fermé",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (casting.ouvert) Color(0xFF4CAF50) else Red,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Prix en bas à droite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = casting.compensation,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencyBottomNavigationBar(
    onHomeClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateCastingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = DarkBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    onClick = onHomeClick,
                    isSelected = true
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .clickable { onCreateCastingClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer un casting",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                NavigationItem(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    onClick = onProfileClick,
                    isSelected = false
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DarkBlue else GrayBorder,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) DarkBlue else GrayBorder,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CastingListAgencyScreenPreview() {
    val sampleCastings = listOf(
        CastingItem(
            id = "1",
            title = "Casting Film B",
            date = "18/12/2025",
            description = "Recherche acteurs secondaires.",
            role = "Second rôle",
            age = "20-35 ans",
            compensation = "400 €",
            isFavorite = false
        ),
        CastingItem(
            id = "2",
            title = "Publicité Luxe",
            date = "08/01/2026",
            description = "Publicité TV pour une marque de luxe.",
            role = "Mannequin",
            age = "25-40 ans",
            compensation = "600 €"
        )
    )

    Projecct_MobileTheme {
        CastingListAgencyScreen(
            onBackClick = {},
            onItemClick = {},
            onFilterClick = {},
            onCreateCastingClick = {},
            onAgendaClick = {},
            onProfileClick = {},
            onLogoutClick = {},
            loadData = false,
            initialCastings = sampleCastings,
            initialAgencyName = "CastMate Agency",
            initialAgencyEmail = "contact@castmate.com"
        )
    }
}
