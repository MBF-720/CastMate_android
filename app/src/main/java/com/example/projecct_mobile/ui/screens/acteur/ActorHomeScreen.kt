package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.toCastingItem
import com.example.projecct_mobile.ui.theme.*

/**
 * Page d'accueil pour les acteurs avec menu utilisateur
 */
@Composable
fun ActorHomeScreen(
    onCastingClick: (CastingItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var castings by remember { mutableStateOf<List<CastingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPrenom by remember { mutableStateOf("") }
    var userNom by remember { mutableStateOf("") }
    
    val castingRepository = remember { CastingRepository() }
    val acteurRepository = remember { ActeurRepository() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Charger les informations utilisateur
    LaunchedEffect(Unit) {
        val acteurResult = acteurRepository.getCurrentActeur()
        acteurResult.onSuccess { acteur ->
            userPrenom = acteur.prenom
            userNom = acteur.nom
            userName = "${acteur.prenom} ${acteur.nom}".trim().ifEmpty { "Utilisateur" }
            userEmail = acteur.email
        }
        acteurResult.onFailure { exception ->
            // Si on ne peut pas charger le profil acteur, utiliser l'email depuis TokenManager
            android.util.Log.w("ActorHomeScreen", "Impossible de charger le profil acteur: ${exception.message}")
            try {
                val tokenManager = com.example.projecct_mobile.data.local.TokenManager(context)
                val email = tokenManager.getUserEmailSync()
                if (email != null) {
                    userEmail = email
                    userName = email.split("@").firstOrNull() ?: "Utilisateur"
                    userPrenom = userName
                    userNom = ""
                }
            } catch (e: Exception) {
                // Ignorer les erreurs
                android.util.Log.w("ActorHomeScreen", "Erreur lors de la récupération de l'email: ${e.message}")
            }
        }
    }
    
    // Charger les castings
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        
        try {
            val result = castingRepository.getAllCastings()
            
            result.onSuccess { apiCastings ->
                castings = apiCastings.map { it.toCastingItem() }
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Contenu du drawer amélioré
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(White)
            ) {
                // Header amélioré avec avatar
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar circulaire
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${userPrenom.take(1).uppercase()}${userNom.take(1).uppercase()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        
                        // Nom et prénom
                        Column {
                            Text(
                                text = userPrenom.ifEmpty { "Prénom" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = userNom.ifEmpty { "Nom" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            if (userEmail.isNotEmpty()) {
                                Text(
                                    text = userEmail,
                                    fontSize = 14.sp,
                                    color = White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Menu items avec meilleur espacement
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
                
                // Logout en bas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = GrayBorder.copy(alpha = 0.3f)
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.Logout,
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
        Column(modifier = Modifier.fillMaxSize()) {
            // En-tête avec menu hamburger à gauche et nom au centre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu hamburger à gauche
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Titre au centre
                    Text(
                        text = "Cast Mate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    // Espace pour équilibrer
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
            
            // Barre de recherche
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher un casting...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { showComingSoon = "Filtres" },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            DarkBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtres",
                        tint = DarkBlue
                    )
                }
            }
            
            // Contenu - utilise fillMaxHeight au lieu de weight pour éviter les conflits
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorMessage(
                            message = errorMessage ?: "Erreur",
                            onRetry = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val result = castingRepository.getAllCastings()
                                        result.onSuccess { apiCastings ->
                                            castings = apiCastings.map { it.toCastingItem() }
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
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    items(castings.filter { 
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }) { casting ->
                        LocalCastingItemCard(
                            casting = casting,
                            onItemClick = { onCastingClick(casting) },
                            onFavoriteClick = { 
                                showComingSoon = "Favoris"
                            }
                        )
                    }
                }
                }
            }
            
            // Barre de navigation
            BottomNavigationBar(
                onHomeClick = { /* Déjà sur la page d'accueil */ },
                onAgendaClick = { onAgendaClick() },
                onHistoryClick = { /* Retiré */ },
                onProfileClick = { onProfileClick() },
                onAdvancedClick = { showComingSoon = "Fonctionnalité avancée" }
            )
        }
    }
    
    // Alerte Coming Soon
    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
}

@Composable
private fun LocalCastingItemCard(
    casting: CastingItem,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = casting.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = casting.date,
                        fontSize = 14.sp,
                        color = GrayBorder,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (casting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = RedHeart,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = casting.description,
                fontSize = 14.sp,
                color = Black.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = casting.compensation,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
                
                Text(
                    text = casting.role,
                    fontSize = 14.sp,
                    color = DarkBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdvancedClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Navbar flottante avec transparence
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    onClick = onHomeClick,
                    isSelected = true
                )
                
                // Bouton + au milieu
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .clickable { onAdvancedClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Fonctionnalité avancée",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Profile
                NavigationItem(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    onClick = onProfileClick,
                    isSelected = false
                )
                
                // Espace pour équilibrer (remplace Historique)
                Spacer(modifier = Modifier.width(48.dp))
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

