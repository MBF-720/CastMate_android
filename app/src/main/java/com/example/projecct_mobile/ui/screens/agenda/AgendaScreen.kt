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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.InterviewResponse
import com.example.projecct_mobile.data.model.InterviewStatus
import com.example.projecct_mobile.data.repository.InterviewRepository
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import com.example.projecct_mobile.ui.screens.agence.casting.AgencyBottomNavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

@Composable
fun AgendaScreen(
    onBackClick: () -> Unit = {},
    onInterviewClick: (InterviewResponse) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null,
    onHomeClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onCreateCastingClick: () -> Unit = {},
    onCandidaturesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val interviewRepository = remember { InterviewRepository() }
    val scope = rememberCoroutineScope()
    
    var userRole by remember { mutableStateOf<String?>(null) }
    var interviews by remember { mutableStateOf<List<InterviewResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<InterviewStatus?>(null) }
    
    // État pour le calendrier
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Extraire les dates des interviews pour marquer le calendrier
    val interviewDates = remember(interviews) {
        val dates = mutableSetOf<String>()
        interviews.forEach { interview ->
            when (interview.statusEnum) {
                InterviewStatus.PENDING -> {
                    // Pour PENDING, marquer toutes les dates proposées
                    interview.proposedDates.forEach { proposedDate ->
                        dates.add(proposedDate.date)
                    }
                }
                InterviewStatus.CONFIRMED -> {
                    // Pour CONFIRMED, marquer la date sélectionnée
                    interview.selectedDate?.let { dates.add(it) }
                }
                else -> {}
            }
        }
        dates
    }
    
    // Détection du rôle utilisateur
    LaunchedEffect(Unit) {
        userRole = withContext(Dispatchers.IO) {
            tokenManager.getUserRoleSync()
        }
        android.util.Log.d("AgendaScreen", "🔍 Rôle détecté: '$userRole'")
    }
    
    // Chargement des interviews selon le rôle
    LaunchedEffect(userRole, selectedStatusFilter) {
        if (userRole != null) {
            isLoading = true
            errorMessage = null
            
            val result = withContext(Dispatchers.IO) {
                when (userRole?.uppercase()) {
                    "ACTEUR" -> interviewRepository.getActorInterviews(selectedStatusFilter)
                    "RECRUTEUR", "ADMIN" -> interviewRepository.getAgencyInterviews(selectedStatusFilter)
                    else -> Result.success(emptyList())
                }
            }
            
            isLoading = false
            
            result.onSuccess { loadedInterviews ->
                interviews = loadedInterviews
                android.util.Log.d("AgendaScreen", "✅ Interviews chargées: ${loadedInterviews.size}")
            }
            
            result.onFailure { exception ->
                errorMessage = "Erreur: ${exception.message}"
                android.util.Log.e("AgendaScreen", "❌ Erreur chargement interviews: ${exception.message}")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
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
                
                // Filtres par statut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Toutes", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == InterviewStatus.PENDING,
                        onClick = { selectedStatusFilter = InterviewStatus.PENDING },
                        label = { Text("En attente", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == InterviewStatus.CONFIRMED,
                        onClick = { selectedStatusFilter = InterviewStatus.CONFIRMED },
                        label = { Text("Confirmées", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == InterviewStatus.CANCELLED,
                        onClick = { selectedStatusFilter = InterviewStatus.CANCELLED },
                        label = { Text("Annulées", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Contenu avec liste des interviews
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Erreur",
                                tint = Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = errorMessage ?: "Erreur",
                                fontSize = 16.sp,
                                color = Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                interviews.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                text = "Aucune interview",
                                fontSize = 16.sp,
                                color = GrayBorder,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    // Filtrer les interviews par date sélectionnée
                    val currentSelectedDate = selectedDate
                    val filteredInterviews = remember(interviews, currentSelectedDate) {
                        if (currentSelectedDate == null) {
                            interviews
                        } else {
                            val dateString = currentSelectedDate.format(DateTimeFormatter.ISO_DATE)
                            interviews.filter { interview ->
                                when (interview.statusEnum) {
                                    InterviewStatus.PENDING -> {
                                        interview.proposedDates.any { it.date == dateString }
                                    }
                                    InterviewStatus.CONFIRMED -> {
                                        interview.selectedDate == dateString
                                    }
                                    else -> false
                                }
                            }
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Calendrier
                        item {
                            CalendarWidget(
                                currentMonth = currentMonth,
                                selectedDate = selectedDate,
                                interviewDates = interviewDates,
                                onMonthChange = { month -> currentMonth = month },
                                onDateSelected = { date -> 
                                    selectedDate = if (selectedDate == date) null else date
                                }
                            )
                        }
                        
                        // Liste des interviews (filtrées par date si une date est sélectionnée)
                        if (filteredInterviews.isEmpty() && currentSelectedDate != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Aucune interview pour cette date",
                                            fontSize = 14.sp,
                                            color = GrayBorder
                                        )
                                        TextButton(onClick = { selectedDate = null }) {
                                            Text("Afficher toutes les interviews", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredInterviews) { interview ->
                                InterviewCard(
                                    interview = interview,
                                    userRole = userRole,
                                    onClick = {
                                        // Seuls les acteurs peuvent cliquer sur les interviews PENDING
                                        if (interview.statusEnum == InterviewStatus.PENDING 
                                            && userRole?.uppercase() == "ACTEUR") {
                                            onInterviewClick(interview)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barre de navigation du bas selon le rôle
        when (userRole?.uppercase()) {
            "RECRUTEUR", "ADMIN" -> {
                // Navbar agence (même que CastingListAgencyScreen)
                AgencyBottomNavigationBar(
                    onHomeClick = onHomeClick,
                    onAgendaClick = onAgendaClick,
                    onHistoryClick = { /* Coming soon */ },
                    onProfileClick = {
                        onNavigateToProfile?.invoke()
                    },
                    onCreateCastingClick = onCreateCastingClick
                )
            }
            "ACTEUR" -> {
                // Navbar acteur (même que CastingListScreen)
                ActorBottomNavigationBar(
                    selectedItem = NavigationItem.AGENDA,
                    onCandidaturesClick = onCandidaturesClick,
                    onHomeClick = onHomeClick,
                    onAgendaClick = onAgendaClick,
                    onProfileClick = {
                        onNavigateToProfile?.invoke()
                    }
                )
            }
            else -> {
                // Navbar par défaut (fallback)
                AgendaBottomNavigationBar(
                    onHomeClick = onHomeClick,
                    onHistoryClick = { /* Naviguer vers history */ },
                    onProfileClick = {
                        onNavigateToProfile?.invoke()
                    }
                )
            }
        }
    }
}

@Composable
fun InterviewCard(
    interview: InterviewResponse,
    userRole: String?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
    
    val isClickable = interview.statusEnum == InterviewStatus.PENDING 
        && userRole?.uppercase() == "ACTEUR"
    
    val statusColor = when (interview.statusEnum) {
        InterviewStatus.PENDING -> Color(0xFFFF9800)
        InterviewStatus.CONFIRMED -> Color(0xFF4CAF50)
        InterviewStatus.CANCELLED -> Red
    }
    
    val statusText = when (interview.statusEnum) {
        InterviewStatus.PENDING -> "En attente"
        InterviewStatus.CONFIRMED -> "Confirmée"
        InterviewStatus.CANCELLED -> "Annulée"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // En-tête avec statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interview.castingTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Informations selon le rôle
            when (userRole?.uppercase()) {
                "ACTEUR" -> {
                    Text(
                        text = "Agence: ${interview.agenceName}",
                        fontSize = 14.sp,
                        color = GrayBorder
                    )
                    
                    when (interview.statusEnum) {
                        InterviewStatus.PENDING -> {
                            Text(
                                text = "Cliquez pour choisir une date parmi les 3 proposées",
                                fontSize = 14.sp,
                                color = DarkBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        InterviewStatus.CONFIRMED -> {
                            interview.selectedDate?.let { date ->
                                interview.selectedTime?.let { time ->
                                    val formattedDate = try {
                                        val parsed = dateFormat.parse(date)
                                        parsed?.let { displayDateFormat.format(it) } ?: date
                                    } catch (e: Exception) {
                                        date
                                    }
                                    Text(
                                        text = "Interview confirmée le $formattedDate à $time",
                                        fontSize = 14.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
                "RECRUTEUR", "ADMIN" -> {
                    Text(
                        text = "Acteur: ${interview.acteurName}",
                        fontSize = 14.sp,
                        color = GrayBorder
                    )
                    
                    when (interview.statusEnum) {
                        InterviewStatus.PENDING -> {
                            Text(
                                text = "En attente de sélection de date par l'acteur",
                                fontSize = 14.sp,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        InterviewStatus.CONFIRMED -> {
                            interview.selectedDate?.let { date ->
                                interview.selectedTime?.let { time ->
                                    val formattedDate = try {
                                        val parsed = dateFormat.parse(date)
                                        parsed?.let { displayDateFormat.format(it) } ?: date
                                    } catch (e: Exception) {
                                        date
                                    }
                                    Text(
                                        text = "Interview confirmée le $formattedDate à $time",
                                        fontSize = 14.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
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
fun CalendarWidget(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    interviewDates: Set<String>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayIndex = (firstDayOfMonth.value - 1) % 7
    
    // Jours du mois précédent
    val previousMonth = currentMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    val daysFromPreviousMonth = firstDayIndex
    
    // Jours du mois suivant
    val totalCells = 42 // 6 semaines * 7 jours
    val daysFromNextMonth = totalCells - daysFromPreviousMonth - daysInMonth
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
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
                        contentDescription = "Mois précédent",
                        tint = DarkBlue
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)),
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
                        contentDescription = "Mois suivant",
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
                var dayCounter = 1
                var previousMonthDay = daysInPreviousMonth - daysFromPreviousMonth + 1
                var nextMonthDay = 1
                
                repeat(6) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { dayOfWeek ->
                            val (dayToShow, isCurrentMonth, localDate) = when {
                                week == 0 && dayOfWeek < firstDayIndex -> {
                                    val day = previousMonthDay
                                    previousMonthDay++
                                    val date = previousMonth.atDay(day)
                                    Triple(day, false, date)
                                }
                                dayCounter <= daysInMonth -> {
                                    val day = dayCounter
                                    dayCounter++
                                    val date = currentMonth.atDay(day)
                                    Triple(day, true, date)
                                }
                                else -> {
                                    val day = nextMonthDay
                                    nextMonthDay++
                                    val date = currentMonth.plusMonths(1).atDay(day)
                                    Triple(day, false, date)
                                }
                            }
                            
                            val dateString = localDate.format(DateTimeFormatter.ISO_DATE)
                            val hasInterview = interviewDates.contains(dateString)
                            val isSelected = selectedDate == localDate
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable {
                                        if (isCurrentMonth) {
                                            onDateSelected(localDate)
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
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
                                    
                                    // Point bleu si le jour contient une interview
                                    if (hasInterview && isCurrentMonth) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) White else DarkBlue, 
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
