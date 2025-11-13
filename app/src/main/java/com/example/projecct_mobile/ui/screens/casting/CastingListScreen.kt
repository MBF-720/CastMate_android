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
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        age = this.age ?: "",
        compensation = this.prix?.toString() ?: "Non spécifié",
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
    onSettingsClick: () -> Unit = onProfileClick,
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
                        Text(
                            text = "Back",
                            color = White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(White.copy(alpha = 0.18f))
                                .clickable { onBackClick() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )

                        Text(
                            text = "Casting",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                android.util.Log.d("CastingListScreen", "Bouton profil cliqué")
                                onSettingsClick()
                            },
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
                        text = "Découvrez les meilleurs castings du moment",
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Rechercher un casting...", color = GrayBorder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
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
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = DarkBlue.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Image placeholder moderne
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                DarkBlue.copy(alpha = 0.1f),
                                DarkBlue.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(1.dp, DarkBlue.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .clickable { onItemClick(casting) },
                contentAlignment = Alignment.Center
            ) {
                Text("📷", fontSize = 36.sp)
            }
            
            // Informations du casting
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Titre
                Text(
                    text = casting.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.3.sp
                )
                
                // Date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = GrayBorder,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = casting.date,
                        fontSize = 12.sp,
                        color = GrayBorder,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Description
                Text(
                    text = casting.description,
                    fontSize = 13.sp,
                    color = Black.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Rôle et âge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoBadge(label = "rôle", value = casting.role)
                    if (casting.age.isNotEmpty()) {
                        InfoBadge(label = "âge", value = casting.age)
                    }
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
                        fontWeight = FontWeight.ExtraBold,
                        color = Red,
                        letterSpacing = 0.3.sp
                    )
                    
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (casting.isFavorite) RedHeart.copy(alpha = 0.1f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (casting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = RedHeart,
                            modifier = Modifier.size(20.dp)
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

