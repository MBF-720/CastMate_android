package com.example.projecct_mobile.ui.screens.casting

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

data class CastingItem(
    val id: String,
    val title: String,
    val date: String,
    val description: String,
    val role: String,
    val age: String,
    val compensation: String,
    val isFavorite: Boolean = false
)

/**
 * Extension function pour convertir un Casting de l'API en CastingItem pour l'UI
 */
fun com.example.projecct_mobile.data.model.Casting.toCastingItem(isFavorite: Boolean = false): CastingItem {
    return CastingItem(
        id = this.actualId ?: "",
        title = this.titre ?: "Sans titre",
        date = this.dateDebut ?: this.dateFin ?: "Date non spécifiée",
        description = this.descriptionRole ?: this.synopsis ?: "Aucune description",
        role = this.descriptionRole ?: "Rôle non spécifié",
        age = "", // Pas disponible dans l'API
        compensation = this.remuneration ?: "Non spécifié",
        isFavorite = isFavorite
    )
}

@Composable
fun CastingListScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (CastingItem) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var castings by remember { mutableStateOf<List<CastingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    val castingRepository = remember { com.example.projecct_mobile.data.repository.CastingRepository() }
    val scope = rememberCoroutineScope()
    
    // Charger les castings depuis l'API au démarrage
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        
        try {
            val result = castingRepository.getAllCastings()
            
            result.onSuccess { apiCastings ->
                android.util.Log.d("CastingListScreen", "Castings reçus: ${apiCastings.size}")
                castings = apiCastings.map { it.toCastingItem() }
                android.util.Log.d("CastingListScreen", "Castings convertis: ${castings.size}")
                isLoading = false
            }
            
            result.onFailure { exception ->
                android.util.Log.e("CastingListScreen", "Erreur: ${exception.message}", exception)
                errorMessage = com.example.projecct_mobile.ui.components.getErrorMessage(exception)
                isLoading = false
            }
        } catch (e: Exception) {
            android.util.Log.e("CastingListScreen", "Exception: ${e.message}", e)
            errorMessage = com.example.projecct_mobile.ui.components.getErrorMessage(e)
            isLoading = false
        }
    }
    
    var favoriteCastingItems by remember { mutableStateOf(castings) }
    
    // Mettre à jour favoriteCastingItems quand castings change
    LaunchedEffect(castings) {
        favoriteCastingItems = castings
    }
    
    // Filtrer les castings selon la recherche
    val filteredCastings = remember(castings, searchQuery) {
        if (searchQuery.isBlank()) {
            castings
        } else {
            castings.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkBlue)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                    
                    Text(
                        text = "Casting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    Box(
                        modifier = Modifier
                            .clickable {
                                android.util.Log.d("CastingListScreen", "Bouton agenda cliqué")
                                onAgendaClick()
                            }
                            .size(48.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Barre de recherche et filtre
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search casting.", color = LightGray) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = LightGray
                            )
                        },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = White,
                            unfocusedBorderColor = LightGray,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = { showComingSoon = "Filtres" },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = DarkBlue
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Liste des castings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredCastings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Aucun casting trouvé" else "Aucun casting disponible",
                                    color = GrayBorder,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }
                    } else {
                        items(filteredCastings) { casting ->
                            CastingItemCard(
                                casting = casting,
                                onFavoriteClick = {
                                    castings = castings.map { item ->
                                        if (item.id == casting.id) item.copy(isFavorite = !item.isFavorite)
                                        else item
                                    }
                                },
                                onItemClick = { clickedCasting ->
                                    android.util.Log.d("CastingListScreen", "Clic sur casting: ${clickedCasting.id} - ${clickedCasting.title}")
                                    try {
                                        onItemClick(clickedCasting)
                                        android.util.Log.d("CastingListScreen", "Navigation déclenchée pour casting: ${clickedCasting.id}")
                                    } catch (e: Exception) {
                                        android.util.Log.e("CastingListScreen", "Erreur lors de la navigation: ${e.message}", e)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Barre de navigation du bas
        BottomNavigationBar(
            onHomeClick = onHomeClick,
            onHistoryClick = { showComingSoon = "Historique" },
            onProfileClick = {
                onNavigateToProfile?.invoke() ?: onProfileClick()
            }
        )
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
fun CastingItemCard(
    casting: CastingItem,
    onFavoriteClick: () -> Unit,
    onItemClick: (CastingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    android.util.Log.d("CastingItemCard", "Carte cliquée pour: ${casting.id}")
                    onItemClick(casting)
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGray)
                    .clickable { onItemClick(casting) },
                contentAlignment = Alignment.Center
            ) {
                Text("📷", fontSize = 32.sp)
            }
            
            // Informations du casting
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Titre et date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = casting.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = casting.date,
                        fontSize = 12.sp,
                        color = GrayBorder
                    )
                }
                
                // Description
                Text(
                    text = casting.description,
                    fontSize = 12.sp,
                    color = GrayBorder,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rôle et âge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoBadge(label = "rôle", value = casting.role)
                    InfoBadge(label = "age", value = casting.age)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Compensation et favori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = casting.compensation,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red
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
fun InfoBadge(label: String, value: String) {
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

@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(DarkBlue),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = Icons.Default.Home,
                label = "Home",
                onClick = onHomeClick
            )
            
            NavigationItem(
                icon = Icons.Default.Tune,
                label = "History",
                onClick = onHistoryClick
            )
            
            NavigationItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CastingListScreenPreview() {
    Projecct_MobileTheme {
        CastingListScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CastingItemCardPreview() {
    Projecct_MobileTheme {
        CastingItemCard(
            casting = CastingItem(
                id = "1",
                title = "Dune : Part 3",
                date = "30/10/2025",
                description = "Paul Atreides faces new political and spiritual challenges as...",
                role = "Arven",
                age = "20+",
                compensation = "20$"
            ),
            onFavoriteClick = {},
            onItemClick = {}
        )
    }
}

