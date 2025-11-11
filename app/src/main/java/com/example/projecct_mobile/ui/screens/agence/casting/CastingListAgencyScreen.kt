package com.example.projecct_mobile.ui.screens.agence.casting

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.Casting
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
    initialAgencyEmail: String = ""
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

    LaunchedEffect(Unit, loadData) {
        if (!loadData) return@LaunchedEffect
        isLoading = true
        errorMessage = null

        try {
            val agencyId = withContext(Dispatchers.IO) { tokenManager?.getUserIdSync() }
            val result = castingRepository?.getAllCastings()

            result?.onSuccess { apiCastings ->
                val filtered = agencyId?.let { id ->
                    apiCastings.filter { it.belongsToRecruiter(id) }
                } ?: apiCastings
                castings = filtered.map { it.toCastingItem() }
                isLoading = false
            }

            result?.onFailure { exception ->
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
                                    onEditClick = { onItemClick(casting) },
                                    onFavoriteToggle = {
                                        castings = castings.map { item ->
                                            if (item.id == casting.id) item.copy(isFavorite = !item.isFavorite)
                                            else item
                                        }
                                    }
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
    val recruiterValue = recruteur ?: return false
    return when (recruiterValue) {
        is String -> recruiterValue.equals(recruiterId, ignoreCase = true)
        is Map<*, *> -> recruiterValue.values.any { value ->
            value?.toString()?.equals(recruiterId, ignoreCase = true) == true
        }
        else -> recruiterValue.toString().contains(recruiterId, ignoreCase = true)
    }
}

@Composable
private fun LocalAgencyCastingCard(
    casting: CastingItem,
    onEditClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = DarkBlue.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = casting.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkBlue,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.3.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = GrayBorder,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = casting.date,
                            fontSize = 13.sp,
                            color = GrayBorder,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (casting.isFavorite) RedHeart.copy(alpha = 0.1f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (casting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoris",
                        tint = RedHeart,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = casting.description,
                fontSize = 14.sp,
                color = Black.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = casting.compensation,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Red,
                    letterSpacing = 0.3.sp
                )

                Text(
                    text = casting.role,
                    fontSize = 14.sp,
                    color = DarkBlue,
                    fontWeight = FontWeight.SemiBold
                )
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
