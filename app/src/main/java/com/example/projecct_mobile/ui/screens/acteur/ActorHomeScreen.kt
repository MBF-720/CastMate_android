package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.toCastingItem
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import com.example.projecct_mobile.data.model.CastingFilters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Page d'accueil pour les acteurs avec menu utilisateur
 */
@Composable
fun ActorHomeScreen(
    onCastingClick: (CastingItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onFilterClick: (CastingFilters) -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onMyCandidaturesClick: () -> Unit = {},
    loadData: Boolean = true,
    initialCastings: List<CastingItem> = emptyList(),
    initialUserName: String = "",
    initialUserEmail: String = "",
    initialUserPrenom: String = "",
    initialUserNom: String = "",
    reloadKey: Int = 0,
    initialFilters: CastingFilters = CastingFilters()
) {
    var filters by remember { mutableStateOf(initialFilters) }
    var searchQuery by remember { mutableStateOf(filters.searchQuery) }
    var castings by remember { mutableStateOf(initialCastings) }
    var allCastingsData by remember { mutableStateOf<List<Casting>>(emptyList()) } // Stocker les Casting originaux pour le filtrage
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf(initialUserName) }
    var userEmail by remember { mutableStateOf(initialUserEmail) }
    var userPrenom by remember { mutableStateOf(initialUserPrenom) }
    var userNom by remember { mutableStateOf(initialUserNom) }
    
    val castingRepository = remember(loadData) {
        if (loadData) CastingRepository() else null
    }
    val acteurRepository = remember(loadData) {
        if (loadData) ActeurRepository() else null
    }
    val userRepository = remember(loadData) {
        if (loadData) UserRepository() else null
    }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Fonction pour appliquer tous les filtres
    val applyFilters = remember { 
        { castingsData: List<Casting>, currentFilters: CastingFilters, favoriteIdsSet: Set<String> ->
            castingsData.filter { casting ->
                // Filtre par recherche textuelle
                val matchesSearch = currentFilters.searchQuery.isBlank() || 
                    (casting.titre?.contains(currentFilters.searchQuery, ignoreCase = true) == true) ||
                    (casting.descriptionRole?.contains(currentFilters.searchQuery, ignoreCase = true) == true) ||
                    (casting.synopsis?.contains(currentFilters.searchQuery, ignoreCase = true) == true)
                
                // Filtre par types
                val matchesTypes = currentFilters.selectedTypes.isEmpty() || 
                    (casting.types?.any { type -> currentFilters.selectedTypes.contains(type) } == true)
                
                // Filtre par prix
                val matchesPrice = when {
                    currentFilters.minPrice != null && currentFilters.maxPrice != null -> {
                        casting.prix != null && casting.prix >= currentFilters.minPrice && casting.prix <= currentFilters.maxPrice
                    }
                    currentFilters.minPrice != null -> {
                        casting.prix != null && casting.prix >= currentFilters.minPrice
                    }
                    currentFilters.maxPrice != null -> {
                        casting.prix != null && casting.prix <= currentFilters.maxPrice
                    }
                    else -> true
                }
                
                // Filtre par lieu
                val matchesLieu = currentFilters.lieu.isNullOrBlank() ||
                    (casting.lieu?.contains(currentFilters.lieu ?: "", ignoreCase = true) == true)
                
                // Filtre par date début
                val matchesDateDebut = currentFilters.dateDebut.isNullOrBlank() ||
                    (casting.dateDebut?.let { it >= (currentFilters.dateDebut ?: "") } == true)
                
                // Filtre par date fin
                val matchesDateFin = currentFilters.dateFin.isNullOrBlank() ||
                    (casting.dateFin?.let { it <= (currentFilters.dateFin ?: "") } == true)
                
                matchesSearch && matchesTypes && matchesPrice && matchesLieu && matchesDateDebut && matchesDateFin
            }.map { casting ->
                val item = casting.toCastingItem()
                val castingId = casting.actualId
                item.copy(isFavorite = castingId != null && favoriteIdsSet.contains(castingId))
            }
        }
    }
    
    // Charger les informations utilisateur
    LaunchedEffect(Unit, loadData) {
        if (!loadData) return@LaunchedEffect
        val acteurResult = acteurRepository?.getCurrentActeur()
        acteurResult?.onSuccess { acteur ->
            userPrenom = acteur.prenom ?: ""
            userNom = acteur.nom ?: ""
            userName = listOfNotNull(acteur.prenom, acteur.nom)
                .joinToString(" ")
                .ifBlank { "Utilisateur" }
            userEmail = acteur.email ?: userEmail
        }
        acteurResult?.onFailure { exception ->
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
    
    // Charger les favoris et les castings - fusionné en un seul LaunchedEffect pour éviter les requêtes en double
    // Ne pas utiliser reloadKey pour éviter les rechargements en boucle
    LaunchedEffect(Unit, loadData) {
        if (!loadData) {
            isLoading = false
            errorMessage = null
            return@LaunchedEffect
        }
        
        try {
            isLoading = true
            errorMessage = null
            
            // Récupérer l'ID de l'acteur pour filtrer les candidatures
            val acteurId = acteurRepository?.getCurrentActeurId()
            
            // Charger les favoris en premier
            val userResult = userRepository?.getCurrentUser()
            var loadedFavoriteIds = favoriteIds // Utiliser les favoris existants par défaut
            
            if (userResult?.isSuccess == true) {
                val user = userResult.getOrNull()
                val userId = user?.actualId
                if (!userId.isNullOrBlank()) {
                    try {
                        val favoritesResult = acteurRepository?.getFavorites(userId)
                        favoritesResult?.onSuccess { favorites ->
                            loadedFavoriteIds = favorites.mapNotNull { it.actualId }.toSet()
                            favoriteIds = loadedFavoriteIds
                        }
                        favoritesResult?.onFailure {
                            // En cas d'erreur, utiliser les favoris existants
                            android.util.Log.w("ActorHomeScreen", "Erreur chargement favoris, utilisation des favoris existants")
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        // Requête annulée, c'est normal
                        throw e
                    } catch (e: Exception) {
                        android.util.Log.e("ActorHomeScreen", "Erreur chargement favoris: ${e.message}")
                    }
                }
            }
            
            // Ensuite charger les castings
            val result = castingRepository?.getAllCastings()
            
            result?.onSuccess { apiCastings ->
                // Filtrer les castings où l'acteur a déjà postulé
                val filteredCastings = if (acteurId != null) {
                    apiCastings.filter { casting ->
                        val candidats = casting.candidats ?: emptyList()
                        // Exclure les castings où l'acteur a déjà postulé
                        val hasApplied = candidats.any { candidat ->
                            candidat.acteurId?.actualId == acteurId
                        }
                        !hasApplied
                    }
                } else {
                    // Si on ne peut pas récupérer l'ID de l'acteur, afficher tous les castings
                    apiCastings
                }
                
                // Trier les castings par date de création (les plus récents en haut)
                val sortedCastings = filteredCastings.sortedByDescending { it.getCreationTimestamp() }
                
                // Stocker les Casting originaux pour le filtrage
                allCastingsData = sortedCastings
                
                // Appliquer les filtres et convertir en CastingItem
                val filteredAndMapped = applyFilters(sortedCastings, filters, loadedFavoriteIds)
                castings = filteredAndMapped
                
                isLoading = false
            }
            
            result?.onFailure { exception ->
                errorMessage = getErrorMessage(exception)
                isLoading = false
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Requête annulée - c'est normal quand on navigue rapidement
            android.util.Log.d("ActorHomeScreen", "⚠️ Requête annulée (normal)")
            // Ne pas mettre à jour isLoading car la composition a peut-être changé
        } catch (e: Exception) {
            errorMessage = getErrorMessage(e)
            isLoading = false
        }
    }
    
    // Mettre à jour isFavorite quand favoriteIds change et réappliquer les filtres
    LaunchedEffect(favoriteIds, filters) {
        if (!loadData) return@LaunchedEffect
        val filteredAndMapped = applyFilters(allCastingsData, filters, favoriteIds)
        castings = filteredAndMapped
    }
    
    // Synchroniser searchQuery avec filters
    LaunchedEffect(searchQuery) {
        filters = filters.copy(searchQuery = searchQuery)
    }
    
            Column(
                modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            ) {
        // Espacement en haut pour donner plus d'espace à la zone bleue
        Spacer(modifier = Modifier.height(24.dp))
        
        // Barre supérieure avec titre et champ de recherche
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                .padding(horizontal = 16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                // Titre
                            Text(
                    text = "CastMate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                    textAlign = TextAlign.Start
                    )
            
                // Barre de recherche avec filtre
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                        placeholder = { Text("Search casting", color = GrayBorder) },
                    leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = GrayBorder)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = White,
                            unfocusedBorderColor = White.copy(alpha = 0.5f),
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedPlaceholderColor = GrayBorder,
                            unfocusedPlaceholderColor = GrayBorder
                    ),
                    singleLine = true
                )
                
                IconButton(
                        onClick = { onFilterClick(filters) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                                White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
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
            
        // Espacement avant la liste (donner plus d'espace à la zone bleue)
        Spacer(modifier = Modifier.height(20.dp))
        
        // Contenu avec arrondis aux coins supérieurs - étendre pour cacher le fond bleu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
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
                                    if (castingRepository == null) return@launch
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val result = castingRepository.getAllCastings()
                                        result.onSuccess { apiCastings ->
                                        // Trier les castings par date de création (les plus récents en haut)
                                        val sortedCastings = apiCastings.sortedByDescending { it.getCreationTimestamp() }
                                        castings = sortedCastings.map { it.toCastingItem() }
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
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    // Afficher un indicateur si des filtres sont actifs
                    if (filters.hasActiveFilters()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${castings.size} casting(s) trouvé(s)",
                                    fontSize = 14.sp,
                                    color = GrayBorder,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(onClick = {
                                    filters = CastingFilters()
                                    searchQuery = ""
                                }) {
                                    Text(
                                        text = "Réinitialiser",
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                }
                            }
                        }
                    }
                    
                    items(castings) { casting ->
                        LocalCastingItemCard(
                            casting = casting,
                            onItemClick = { onCastingClick(casting) },
                            onFavoriteClick = {
                                scope.launch {
                                    try {
                                        val userResult = userRepository?.getCurrentUser()
                                        if (userResult?.isSuccess == true) {
                                            val user = userResult.getOrNull()
                                            val userId = user?.actualId
                                            if (!userId.isNullOrBlank()) {
                                                val isCurrentlyFavorite = favoriteIds.contains(casting.id)
                                                val result = if (isCurrentlyFavorite) {
                                                    acteurRepository?.removeFavorite(userId, casting.id)
                                                } else {
                                                    acteurRepository?.addFavorite(userId, casting.id)
                                                }
                                                
                                                result?.onSuccess {
                                                    // Mettre à jour la liste locale
                                                    if (isCurrentlyFavorite) {
                                                        favoriteIds = favoriteIds - casting.id
                                                    } else {
                                                        favoriteIds = favoriteIds + casting.id
                                                    }
                                                    // Mettre à jour l'état local des favoris
                                                    // La liste se mettra à jour automatiquement via LaunchedEffect(favoriteIds)
                                                }
                                                result?.onFailure { exception ->
                                                    android.util.Log.e("ActorHomeScreen", "Erreur favoris: ${exception.message}")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorHomeScreen", "Exception favoris: ${e.message}")
                                    }
                                }
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
                    selectedItem = NavigationItem.HOME,
                    onCandidaturesClick = { onMyCandidaturesClick() },
                onHomeClick = { /* Déjà sur la page d'accueil */ },
                    onProfileClick = { onProfileClick() }
            )
            }
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

/**
 * Fonction d'extension pour obtenir le timestamp de création d'un casting
 * Utilisé pour trier les castings par date (les plus récents en haut)
 */
private fun Casting.getCreationTimestamp(): Long {
    // Utiliser createdAt si disponible, sinon updatedAt
    val dateString = createdAt ?: updatedAt
    if (dateString != null) {
        try {
            // Parser la date ISO pour le tri
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            var parsedDate: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(dateString)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    // Continuer avec le format suivant
                }
            }
            return parsedDate?.time ?: 0L
        } catch (e: Exception) {
            return 0L
        }
    }
    return 0L
}

@Composable
private fun LocalCastingItemCard(
    casting: CastingItem,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current
    val acteurRepository = remember { ActeurRepository() }
    var afficheImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    
    // Télécharger l'affiche si disponible
    LaunchedEffect(casting.afficheFileId) {
        if (casting.afficheFileId != null && afficheImage == null && !isLoadingImage) {
            isLoadingImage = true
            try {
                val result = acteurRepository.downloadMedia(casting.afficheFileId)
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
                    // Ne pas afficher d'erreur pour les 403 (permissions backend)
                    // C'est normal si le backend bloque l'accès aux affiches
                    if (exception is ApiException.ForbiddenException) {
                        android.util.Log.d("ActorHomeScreen", "⚠️ Accès refusé à l'affiche (403) - normal si permissions backend restrictives")
                    } else {
                        android.util.Log.e("ActorHomeScreen", "Erreur téléchargement affiche: ${exception.message}")
                    }
                    isLoadingImage = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ActorHomeScreen", "Exception téléchargement affiche: ${e.message}")
                isLoadingImage = false
            }
        } else if (casting.afficheFileId == null) {
            isLoadingImage = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
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
            // Image de l'affiche à gauche (plus grande, rectangulaire)
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
                    // Titre (sans date pour donner plus d'espace)
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
                    }
                }
                
                // Prix et favori en bas à droite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = casting.compensation,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                IconButton(
                    onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (casting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = RedHeart,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
            Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = GrayBorder,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Black,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ActorHomeScreenPreview() {
    val sampleCastings = listOf(
        CastingItem(
            id = "1",
            title = "Casting Film A",
            date = "12/12/2025",
            description = "Rôle principal pour un film d'action.",
            role = "Héros",
            age = "25-35 ans",
            compensation = "500 €", 
            isFavorite = true
        ),
        CastingItem(
            id = "2",
            title = "Publicité Mode",
            date = "05/01/2026",
            description = "Shooting photo pour une marque de vêtements.",
            role = "Mannequin",
            age = "18-28 ans",
            compensation = "300 €"
        )
    )

    Projecct_MobileTheme {
        ActorHomeScreen(
            onCastingClick = {},
            onProfileClick = {},
            onAgendaClick = {},
            onFilterClick = {},
            onHistoryClick = {},
            onLogoutClick = {},
            loadData = false,
            initialCastings = sampleCastings,
            initialUserName = "Sarah Doe",
            initialUserEmail = "sarah.doe@example.com",
            initialUserPrenom = "Sarah",
            initialUserNom = "Doe"
        )
    }
}

