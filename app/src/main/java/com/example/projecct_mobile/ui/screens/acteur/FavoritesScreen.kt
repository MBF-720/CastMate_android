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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.toCastingItem
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem

/**
 * Page des favoris pour les acteurs
 */
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {},
    onCastingClick: (CastingItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMyCandidaturesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val acteurRepository = remember { ActeurRepository() }
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    
    var castings by remember { mutableStateOf<List<CastingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Charger les favoris
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        
        try {
            // Récupérer l'ID de l'acteur
            val userResult = userRepository.getCurrentUser()
            if (!userResult.isSuccess) {
                errorMessage = "Impossible de récupérer l'utilisateur actuel"
                isLoading = false
                return@LaunchedEffect
            }
            
            val user = userResult.getOrNull()
            val userId = user?.actualId
            if (userId.isNullOrBlank()) {
                errorMessage = "ID utilisateur introuvable"
                isLoading = false
                return@LaunchedEffect
            }
            
            // Récupérer les favoris
            val favoritesResult = acteurRepository.getFavorites(userId)
            favoritesResult.onSuccess { favorites ->
                castings = favorites.map { it.toCastingItem() }
                favoriteIds = favorites.mapNotNull { it.actualId }.toSet()
                isLoading = false
            }
            favoritesResult.onFailure { exception ->
                errorMessage = getErrorMessage(exception)
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = getErrorMessage(e)
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White
                    )
                }
                
                Text(
                    text = "Mes favoris",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                Spacer(modifier = Modifier.size(44.dp)) // Pour centrer le titre
            }
        }
        
        // Contenu
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
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val userResult = userRepository.getCurrentUser()
                                        if (userResult.isSuccess) {
                                            val user = userResult.getOrNull()
                                            val userId = user?.actualId
                                            if (!userId.isNullOrBlank()) {
                                                val favoritesResult = acteurRepository.getFavorites(userId)
                                                favoritesResult.onSuccess { favorites ->
                                                    castings = favorites.map { it.toCastingItem() }
                                                    favoriteIds = favorites.mapNotNull { it.actualId }.toSet()
                                                    isLoading = false
                                                }
                                                favoritesResult.onFailure { exception ->
                                                    errorMessage = getErrorMessage(exception)
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = getErrorMessage(e)
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                } else if (castings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Aucun favori",
                                tint = GrayBorder,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Aucun favori",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = GrayBorder
                            )
                            Text(
                                text = "Ajoutez des castings à vos favoris\nen cliquant sur l'icône cœur",
                                fontSize = 14.sp,
                                color = GrayBorder,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(castings) { casting ->
                            FavoriteCastingItemCard(
                                casting = casting,
                                isFavorite = favoriteIds.contains(casting.id),
                                onItemClick = { onCastingClick(casting) },
                                onFavoriteClick = {
                                    scope.launch {
                                        try {
                                            val userResult = userRepository.getCurrentUser()
                                            if (userResult.isSuccess) {
                                                val user = userResult.getOrNull()
                                                val userId = user?.actualId
                                                if (!userId.isNullOrBlank()) {
                                                    val result = if (favoriteIds.contains(casting.id)) {
                                                        acteurRepository.removeFavorite(userId, casting.id)
                                                    } else {
                                                        acteurRepository.addFavorite(userId, casting.id)
                                                    }
                                                    
                                                    result.onSuccess {
                                                        // Mettre à jour la liste locale
                                                        if (favoriteIds.contains(casting.id)) {
                                                            favoriteIds = favoriteIds - casting.id
                                                            castings = castings.filter { it.id != casting.id }
                                                        } else {
                                                            favoriteIds = favoriteIds + casting.id
                                                        }
                                                    }
                                                    result.onFailure { exception ->
                                                        android.util.Log.e("FavoritesScreen", "Erreur favoris: ${exception.message}")
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("FavoritesScreen", "Exception favoris: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Barre de navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                ActorBottomNavigationBar(
                    selectedItem = NavigationItem.PROFILE,
                    onCandidaturesClick = { onMyCandidaturesClick() },
                    onHomeClick = { onHomeClick() },
                    onProfileClick = { onProfileClick() }
                )
            }
        }
    }
}

@Composable
private fun FavoriteCastingItemCard(
    casting: CastingItem,
    isFavorite: Boolean,
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
                    if (exception !is ApiException.ForbiddenException) {
                        android.util.Log.e("FavoritesScreen", "Erreur téléchargement affiche: ${exception.message}")
                    }
                    isLoadingImage = false
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesScreen", "Exception téléchargement affiche: ${e.message}")
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
            // Image de l'affiche
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
            
            // Informations du casting
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
                    }
                }
                
                // Prix et favori
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
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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

