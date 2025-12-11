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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.InterviewResponse
import com.example.projecct_mobile.data.model.InterviewStatus
import com.example.projecct_mobile.data.repository.InterviewRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import com.example.projecct_mobile.ui.screens.agence.casting.AgencyBottomNavigationBar
import com.example.projecct_mobile.ui.screens.agence.casting.AgencyNavigationItem
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.CastingItemCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Classes helper compatibles API 24 pour remplacer java.time
data class YearMonthCompat(
    val year: Int,
    val month: Int // 1-12
) {
    companion object {
        fun now(): YearMonthCompat {
            val calendar = Calendar.getInstance()
            return YearMonthCompat(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1
            )
        }
    }
    
    fun atDay(day: Int): DateCompat {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        return DateCompat(calendar)
    }
    
    fun lengthOfMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    fun minusMonths(months: Int): YearMonthCompat {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, -months)
        return YearMonthCompat(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1
        )
    }
    
    fun plusMonths(months: Int): YearMonthCompat {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, months)
        return YearMonthCompat(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1
        )
    }
    
    fun format(pattern: String, locale: Locale): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val dateFormat = SimpleDateFormat(pattern, locale)
        return dateFormat.format(calendar.time)
    }
}

data class DateCompat(
    private val calendar: Calendar
) {
    constructor(year: Int, month: Int, day: Int) : this(
        Calendar.getInstance().apply {
            set(year, month - 1, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    )
    
    val year: Int get() = calendar.get(Calendar.YEAR)
    val month: Int get() = calendar.get(Calendar.MONTH) + 1
    val dayOfMonth: Int get() = calendar.get(Calendar.DAY_OF_MONTH)
    
    val dayOfWeek: Int get() = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, etc.
    
    fun toCalendar(): Calendar = calendar.clone() as Calendar
    
    fun toDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    companion object {
        fun parse(dateString: String): DateCompat? {
            return try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(dateString)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    DateCompat(calendar)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateCompat) return false
        return year == other.year && month == other.month && dayOfMonth == other.dayOfMonth
    }
    
    override fun hashCode(): Int {
        return year * 10000 + month * 100 + dayOfMonth
    }
}

@Composable
fun AgendaScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (CastingItem) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null,
    onInterviewClick: ((InterviewResponse) -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onCandidaturesClick: (() -> Unit)? = null,
    onAgendaClick: (() -> Unit)? = null,
    onCreateCastingClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interviewRepository = remember { InterviewRepository() }
    val tokenManager = remember { TokenManager(context) }
    
    var interviews by remember { mutableStateOf<List<InterviewResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<InterviewStatus?>(null) }
    
    // État pour le calendrier
    var currentMonth by remember { mutableStateOf(YearMonthCompat.now()) }
    var selectedDate by remember { mutableStateOf<DateCompat?>(null) } // Date complète sélectionnée
    
    // Charger le rôle utilisateur et les interviews
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        
        try {
            // Récupérer le rôle utilisateur
            userRole = withContext(Dispatchers.IO) {
                tokenManager.getUserRoleSync()
            }
            
            // Charger les interviews selon le rôle
            val result = withContext(Dispatchers.IO) {
                when (userRole?.uppercase()) {
                    "ACTEUR" -> interviewRepository.getActorInterviews(selectedStatusFilter)
                    "RECRUTEUR", "ADMIN" -> interviewRepository.getAgencyInterviews(selectedStatusFilter)
                    else -> {
                        android.util.Log.w("AgendaScreen", "⚠️ Rôle utilisateur inconnu: $userRole")
                        Result.success(emptyList())
                    }
                }
            }
            
            result.onSuccess { interviewList ->
                interviews = interviewList
                isLoading = false
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Erreur lors du chargement des interviews"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }
    
    // Recharger quand le filtre change
    LaunchedEffect(selectedStatusFilter) {
        if (userRole != null) {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                when (userRole?.uppercase()) {
                    "ACTEUR" -> interviewRepository.getActorInterviews(selectedStatusFilter)
                    "RECRUTEUR", "ADMIN" -> interviewRepository.getAgencyInterviews(selectedStatusFilter)
                    else -> Result.success(emptyList())
                }
            }
            
            result.onSuccess { interviewList ->
                interviews = interviewList
                isLoading = false
            }.onFailure { exception ->
                errorMessage = exception.message
                isLoading = false
            }
        }
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
                // Espacement pour la status bar
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
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
                        interviews = interviews,
                        onMonthChange = { month -> currentMonth = month },
                        onDateSelected = { date -> 
                            selectedDate = if (selectedDate == date) null else date // Toggle: cliquer à nouveau désélectionne
                        }
                    )
                }
                
                // Filtres de statut
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null },
                            label = { Text("Tous") }
                        )
                        FilterChip(
                            selected = selectedStatusFilter == InterviewStatus.PENDING,
                            onClick = { selectedStatusFilter = InterviewStatus.PENDING },
                            label = { Text("En attente") }
                        )
                        FilterChip(
                            selected = selectedStatusFilter == InterviewStatus.CONFIRMED,
                            onClick = { selectedStatusFilter = InterviewStatus.CONFIRMED },
                            label = { Text("Confirmées") }
                        )
                        FilterChip(
                            selected = selectedStatusFilter == InterviewStatus.CANCELLED,
                            onClick = { selectedStatusFilter = InterviewStatus.CANCELLED },
                            label = { Text("Annulées") }
                        )
                    }
                }
                
                // Liste des interviews
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DarkBlue)
                        }
                    }
                } else if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Red
                                )
                                Text(
                                    text = errorMessage!!,
                                    color = Red
                                )
                            }
                        }
                    }
                } else if (interviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Empty",
                                    tint = GrayBorder,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Aucune interview dans l'agenda",
                                    fontSize = 16.sp,
                                    color = GrayBorder,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    // Filtrer les interviews selon la date sélectionnée
                    val filteredInterviews = if (selectedDate != null) {
                        interviews.filter { interview ->
                            // Vérifier si l'interview a la date sélectionnée (confirmée)
                            val hasSelectedDate = interview.selectedDate?.let { dateStr ->
                                try {
                                    DateCompat.parse(dateStr) == selectedDate
                                } catch (e: Exception) {
                                    false
                                }
                            } ?: false
                            
                            // Pour les interviews en attente, vérifier aussi les dates proposées
                            val hasProposedDate = if (interview.statusEnum == InterviewStatus.PENDING) {
                                interview.proposedDates.any { proposedDate ->
                                    try {
                                        DateCompat.parse(proposedDate.date) == selectedDate
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            } else {
                                false
                            }
                            
                            hasSelectedDate || hasProposedDate
                        }
                    } else {
                        interviews
                    }
                    
                    if (selectedDate != null && filteredInterviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Empty",
                                        tint = GrayBorder,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = "Aucune interview le ${selectedDate!!.dayOfMonth} ${getMonthName(selectedDate!!.month)}",
                                        fontSize = 16.sp,
                                        color = GrayBorder,
                                        fontWeight = FontWeight.Medium
                                    )
                                    TextButton(onClick = { selectedDate = null }) {
                                        Text("Afficher toutes les interviews")
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
                                    onInterviewClick?.invoke(interview)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Barre de navigation du bas - différente selon le rôle
        if (userRole?.uppercase() == "ACTEUR") {
            // Pour les acteurs, utiliser la même navbar que Home
            val interviewRepositoryForCount = remember { InterviewRepository() }
            var pendingInterviewsCount by remember { mutableStateOf(0) }
            
            LaunchedEffect(Unit) {
                try {
                    val interviewsResult = interviewRepositoryForCount.getActorInterviews(InterviewStatus.PENDING)
                    interviewsResult.onSuccess { interviews ->
                        pendingInterviewsCount = interviews.size
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AgendaScreen", "Erreur chargement interviews: ${e.message}")
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 17.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                ActorBottomNavigationBar(
                    selectedItem = NavigationItem.AGENDA,
                    onCandidaturesClick = onCandidaturesClick ?: {},
                    onHomeClick = onHomeClick ?: {},
                    onAgendaClick = onAgendaClick ?: {},
            onProfileClick = {
                onNavigateToProfile?.invoke()
                    },
                    pendingInterviewsCount = pendingInterviewsCount
                )
            }
        } else {
            // Pour les autres rôles (agence, admin), utiliser la navbar des agences
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 17.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                AgencyBottomNavigationBar(
                    selectedItem = AgencyNavigationItem.AGENDA,
                    onHomeClick = onHomeClick ?: {},
                    onAgendaClick = onAgendaClick ?: {},
                    onHistoryClick = { /* Naviguer vers history */ },
                    onProfileClick = {
                        onNavigateToProfile?.invoke()
                    },
                    onCreateCastingClick = onCreateCastingClick ?: {}
                )
            }
        }
    }
}

@Composable
fun InterviewCard(
    interview: InterviewResponse,
    userRole: String? = null,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
    
    // Formater la date
    val formattedDate = try {
        interview.selectedDate?.let { date ->
            dateFormat.parse(date)?.let { displayDateFormat.format(it) }
        } ?: "Date à confirmer"
    } catch (e: Exception) {
        interview.selectedDate ?: "Date à confirmer"
    }
    
    // Couleur selon le statut
    val statusColor = when (interview.statusEnum) {
        InterviewStatus.PENDING -> Color(0xFFFF9800) // Orange
        InterviewStatus.CONFIRMED -> Color(0xFF4CAF50) // Vert
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (interview.statusEnum == InterviewStatus.PENDING) {
                statusColor.copy(alpha = 0.05f)
            } else {
                White
            }
        ),
        border = if (interview.statusEnum == InterviewStatus.PENDING) {
            androidx.compose.foundation.BorderStroke(2.dp, statusColor.copy(alpha = 0.3f))
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (interview.statusEnum == InterviewStatus.PENDING) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badge de date
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
                    text = formattedDate.split(" ").firstOrNull() ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = formattedDate.split(" ").getOrNull(1)?.take(3) ?: "",
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
                    text = interview.castingTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (interview.statusEnum == InterviewStatus.CONFIRMED) {
                            "Agence: ${interview.agenceName}"
                        } else {
                            "Acteur: ${interview.acteurName}"
                        },
                        fontSize = 12.sp,
                        color = GrayBorder
                    )
                }
                
                // Message pour les interviews en attente - différent selon le rôle
                if (interview.statusEnum == InterviewStatus.PENDING) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (userRole?.uppercase()) {
                                "ACTEUR" -> "Cliquez pour choisir une date parmi les 3 proposées"
                                "RECRUTEUR", "ADMIN" -> "En attente de la sélection de l'acteur"
                                else -> "En attente de confirmation"
                            },
                            fontSize = 12.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                // Date et heure si confirmée
                if (interview.statusEnum == InterviewStatus.CONFIRMED && interview.selectedDate != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${formattedDate}${interview.selectedTime?.let { " à $it" } ?: ""}",
                            fontSize = 12.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Badge de statut
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Fonction helper pour obtenir le nom du mois
private fun getMonthName(month: Int): String {
    val months = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )
    return months.getOrNull(month - 1) ?: ""
}

@Composable
fun CalendarWidget(
    currentMonth: YearMonthCompat,
    selectedDate: DateCompat?,
    interviews: List<InterviewResponse>,
    onMonthChange: (YearMonthCompat) -> Unit,
    onDateSelected: (DateCompat) -> Unit
) {
    // Extraire les dates qui ont des interviews
    // - Dates confirmées (selectedDate)
    // - Dates proposées pour les interviews en attente (proposedDates)
    val datesWithInterviews = remember(interviews) {
        val dates = mutableSetOf<DateCompat>()
        
        interviews.forEach { interview ->
            // Ajouter la date sélectionnée si elle existe (interviews confirmées)
            interview.selectedDate?.let { dateStr ->
                try {
                    DateCompat.parse(dateStr)?.let { dates.add(it) }
                } catch (e: Exception) {
                    // Ignorer les dates invalides
                }
            }
            
            // Pour les interviews en attente, ajouter aussi les dates proposées
            if (interview.statusEnum == InterviewStatus.PENDING) {
                interview.proposedDates.forEach { proposedDate ->
                    try {
                        DateCompat.parse(proposedDate.date)?.let { dates.add(it) }
                    } catch (e: Exception) {
                        // Ignorer les dates invalides
                    }
                }
            }
        }
        
        dates
    }
    val daysOfWeek = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, etc. On veut Monday=0
    val firstDayIndex = (firstDayOfMonth.dayOfWeek - 2 + 7) % 7 // Convertir en index (0-6) avec Monday=0
    
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
                    text = currentMonth.format("MMMM yyyy", Locale.ENGLISH),
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
                            
                            // Créer la date complète pour cette cellule
                            val cellDate = if (isCurrentMonth) {
                                currentMonth.atDay(dayToShow)
                            } else {
                                null
                            }
                            
                            val isSelected = cellDate != null && selectedDate != null && cellDate == selectedDate
                            val hasInterview = cellDate != null && datesWithInterviews.contains(cellDate)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable {
                                        if (cellDate != null) {
                                            onDateSelected(cellDate)
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
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(2.dp)
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
                                    // Point indicateur pour les dates avec interviews
                                    if (hasInterview && !isSelected && isCurrentMonth) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    color = DarkBlue,
                                                    shape = CircleShape
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
@Deprecated("Utiliser InterviewCard à la place")
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

