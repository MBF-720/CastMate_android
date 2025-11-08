package com.example.projecct_mobile.ui.screens.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.CastingItemCard
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun AgendaScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (CastingItem) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null
) {
    var showComingSoon by remember { mutableStateOf(false) }
    
    // Afficher l'alerte "Coming Soon" au chargement
    LaunchedEffect(Unit) {
        showComingSoon = true
    }
    // État pour le calendrier
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().dayOfMonth) }
    
    // Liste d'exemple pour l'agenda
    val agendaCastings = remember {
        listOf(
            CastingItem(
                id = "1",
                title = "Dune : Part 3",
                date = "30/10/2025",
                description = "Paul Atreides faces new political and spiritual challenges...",
                role = "Arven",
                age = "20+",
                compensation = "20$",
                isFavorite = true
            ),
            CastingItem(
                id = "2",
                title = "Keeper",
                date = "25/11/2025",
                description = "An intense thriller about a young security guard...",
                role = "men",
                age = "18+",
                compensation = "20$",
                isFavorite = false
            ),
            CastingItem(
                id = "3",
                title = "Mutiny",
                date = "15/12/2025",
                description = "A historical drama set during a naval rebellion...",
                role = "spy",
                age = "30+",
                compensation = "20$",
                isFavorite = true
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
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
                    Text(
                        text = "Back",
                        fontSize = 16.sp,
                        color = White,
                        modifier = Modifier.clickable { onBackClick() }
                    )

                    Text(
                        text = "Agenda",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Contenu avec calendrier et liste
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calendrier
                item {
                    CalendarWidget(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onMonthChange = { month -> currentMonth = month },
                        onDateSelected = { day -> selectedDate = day }
                    )
                }
                
                // Liste des castings
                if (agendaCastings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Empty",
                                    tint = GrayBorder,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Aucun casting dans l'agenda",
                                    fontSize = 16.sp,
                                    color = GrayBorder,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(agendaCastings) { casting ->
                        AgendaCastingCard(
                            casting = casting,
                            onFavoriteClick = { /* Toggle favorite */ },
                            onItemClick = { clickedCasting ->
                                android.util.Log.d("AgendaScreen", "Clic sur casting: ${clickedCasting.id} - ${clickedCasting.title}")
                                try {
                                    onItemClick(clickedCasting)
                                    android.util.Log.d("AgendaScreen", "Navigation déclenchée pour casting: ${clickedCasting.id}")
                                } catch (e: Exception) {
                                    android.util.Log.e("AgendaScreen", "Erreur lors de la navigation: ${e.message}", e)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Barre de navigation du bas avec History sélectionné
        AgendaBottomNavigationBar(
            onHomeClick = { /* Naviguer vers home */ },
            onHistoryClick = { /* Naviguer vers history */ },
            onProfileClick = {
                onNavigateToProfile?.invoke()
            }
        )
    }
    
    // Alerte Coming Soon
    if (showComingSoon) {
        ComingSoonAlert(
            onDismiss = { showComingSoon = false },
            featureName = "Agenda"
        )
    }
}

@Composable
fun CalendarWidget(
    currentMonth: YearMonth,
    selectedDate: Int,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (Int) -> Unit
) {
    val daysOfWeek = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayIndex = (firstDayOfMonth.value - 1) % 7 // Convertir DayOfWeek en index (0-6)
    
    // Jours du mois précédent à afficher
    val previousMonth = currentMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    val daysFromPreviousMonth = firstDayIndex
    
    // Jours du mois suivant à afficher
    val totalCells = 42 // 6 semaines * 7 jours
    val daysFromNextMonth = totalCells - daysFromPreviousMonth - daysInMonth
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // En-tête du calendrier avec navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = DarkBlue
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = DarkBlue
                    )
                }
            }
            
            // Jours de la semaine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayBorder,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Grille du calendrier
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Créer 6 lignes (semaines)
                var dayCounter = 1
                var previousMonthDay = daysInPreviousMonth - daysFromPreviousMonth + 1
                var nextMonthDay = 1
                
                repeat(6) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { dayOfWeek ->
                            val (dayToShow, isCurrentMonth) = when {
                                week == 0 && dayOfWeek < firstDayIndex -> {
                                    // Jours du mois précédent
                                    val day = previousMonthDay
                                    previousMonthDay++
                                    Pair(day, false)
                                }
                                dayCounter <= daysInMonth -> {
                                    // Jours du mois actuel
                                    val day = dayCounter
                                    dayCounter++
                                    Pair(day, true)
                                }
                                else -> {
                                    // Jours du mois suivant
                                    val day = nextMonthDay
                                    nextMonthDay++
                                    Pair(day, false)
                                }
                            }
                            
                            val isSelected = isCurrentMonth && dayToShow == selectedDate
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable {
                                        if (isCurrentMonth) {
                                            onDateSelected(dayToShow)
                                        }
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .background(DarkBlue, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayToShow.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> White
                                        isCurrentMonth -> Black
                                        else -> GrayBorder.copy(alpha = 0.5f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaCastingCard(
    casting: CastingItem,
    onFavoriteClick: () -> Unit,
    onItemClick: (CastingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    android.util.Log.d("AgendaCastingCard", "Carte cliquée pour: ${casting.id}")
                    onItemClick(casting)
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date badge
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBlue)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = casting.date.split("/").firstOrNull() ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = casting.date.split("/").getOrNull(1) ?: "",
                    fontSize = 12.sp,
                    color = White
                )
            }

            // Informations
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = casting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                Text(
                    text = casting.role,
                    fontSize = 12.sp,
                    color = GrayBorder
                )
                Text(
                    text = casting.compensation,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }

            // Favori
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

@Composable
fun AgendaBottomNavigationBar(
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
                onClick = onHomeClick,
                isSelected = false
            )
            
            NavigationItem(
                icon = Icons.Default.Tune,
                label = "History",
                onClick = onHistoryClick,
                isSelected = true
            )
            
            NavigationItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = onProfileClick,
                isSelected = false
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
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
        // Ligne de soulignement pour l'élément sélectionné
        if (isSelected) {
            HorizontalDivider(
                modifier = Modifier
                    .width(40.dp)
                    .padding(top = 4.dp),
                thickness = 2.dp,
                color = White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AgendaScreenPreview() {
    Projecct_MobileTheme {
        AgendaScreen()
    }
}

