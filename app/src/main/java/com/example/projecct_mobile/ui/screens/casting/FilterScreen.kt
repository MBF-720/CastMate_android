package com.example.projecct_mobile.ui.screens.casting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.CastingFilters
import com.example.projecct_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    initialFilters: CastingFilters = CastingFilters(),
    onBackClick: () -> Unit = {},
    onApplyFilter: (CastingFilters) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf(initialFilters.searchQuery) }
    var selectedTypes by remember { mutableStateOf(initialFilters.selectedTypes.toSet()) }
    var minPrice by remember { mutableStateOf(initialFilters.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(initialFilters.maxPrice?.toString() ?: "") }
    var lieu by remember { mutableStateOf(initialFilters.lieu ?: "") }
    var dateDebut by remember { mutableStateOf(initialFilters.dateDebut ?: "") }
    var dateFin by remember { mutableStateOf(initialFilters.dateFin ?: "") }
    
    // Types disponibles
    val availableTypes = listOf("Cinéma", "Télévision", "Théâtre", "Publicité", "Court-métrage", "Documentaire")
    
    // Date picker state avec validation (pas de dates passées)
    val currentTimeMillis = remember { System.currentTimeMillis() }
    val datePickerStateDebut = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= currentTimeMillis - (24 * 60 * 60 * 1000) // Aujourd'hui ou futur
            }
        }
    )
    val datePickerStateFin = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val minDate = datePickerStateDebut.selectedDateMillis ?: currentTimeMillis
                return utcTimeMillis >= minDate // Date de fin >= date de début
            }
        }
    )
    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    
    // Formatter pour les dates
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Espacement en haut pour donner plus d'espace à la zone bleue
            Spacer(modifier = Modifier.height(32.dp))
            
            // Barre de navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Titre "Filtres"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = White
                            )
                        }
                        
                        Text(
                            text = "Filtres",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        
                        // Bouton réinitialiser (icône)
                        IconButton(onClick = {
                            searchQuery = ""
                            selectedTypes = emptySet()
                            minPrice = ""
                            maxPrice = ""
                            lieu = ""
                            dateDebut = ""
                            dateFin = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Réinitialiser",
                                tint = White
                            )
                        }
                    }
                    
                    // Espace supplémentaire pour agrandir la zone bleue
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
            
            // Espacement avant la carte blanche (donner plus d'espace à la zone bleue)
            Spacer(modifier = Modifier.height(20.dp))
            
            // Carte blanche avec les options de filtrage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(20.dp)
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Recherche textuelle
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Rechercher") },
                        placeholder = { Text("Titre, description...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Recherche",
                                tint = DarkBlue
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder
                        )
                    )
                }
                
                // Types de casting (multi-sélection)
                item {
                    Column {
                        Text(
                            text = "Types de casting",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White, RoundedCornerShape(12.dp))
                                .border(1.dp, GrayBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableTypes.forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTypes = if (selectedTypes.contains(type)) {
                                                selectedTypes - type
                                            } else {
                                                selectedTypes + type
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedTypes.contains(type),
                                        onCheckedChange = { checked ->
                                            selectedTypes = if (checked) {
                                                selectedTypes + type
                                            } else {
                                                selectedTypes - type
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = DarkBlue,
                                            uncheckedColor = GrayBorder
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Plage de prix
                item {
                    Column {
                        Text(
                            text = "Plage de prix (TND)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = minPrice,
                                onValueChange = { 
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                        minPrice = it
                                    }
                                },
                                label = { Text("Prix min") },
                                placeholder = { Text("0") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBlue,
                                    unfocusedBorderColor = GrayBorder
                                )
                            )
                            
                            OutlinedTextField(
                                value = maxPrice,
                                onValueChange = { 
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                        maxPrice = it
                                    }
                                },
                                label = { Text("Prix max") },
                                placeholder = { Text("10000") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBlue,
                                    unfocusedBorderColor = GrayBorder
                                )
                            )
                        }
                    }
                }
                
                // Lieu
                item {
                    OutlinedTextField(
                        value = lieu,
                        onValueChange = { lieu = it },
                        label = { Text("Lieu") },
                        placeholder = { Text("Ex: Tunis, Sfax...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Lieu",
                                tint = DarkBlue
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder
                        )
                    )
                }
                
                // Dates
                item {
                    Column {
                        Text(
                            text = "Dates",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = if (dateDebut.isNotEmpty()) {
                                        try {
                                            val date = dateFormatter.parse(dateDebut)
                                            displayFormatter.format(date ?: Date())
                                        } catch (e: Exception) {
                                            dateDebut
                                        }
                                    } else "",
                                    onValueChange = { },
                                    label = { Text("Date début") },
                                    placeholder = { Text("JJ/MM/AAAA") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePickerDebut = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Date",
                                                tint = DarkBlue
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DarkBlue,
                                        unfocusedBorderColor = GrayBorder
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showDatePickerDebut = true }
                                )
                            }
                            
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = if (dateFin.isNotEmpty()) {
                                        try {
                                            val date = dateFormatter.parse(dateFin)
                                            displayFormatter.format(date ?: Date())
                                        } catch (e: Exception) {
                                            dateFin
                                        }
                                    } else "",
                                    onValueChange = { },
                                    label = { Text("Date fin") },
                                    placeholder = { Text("JJ/MM/AAAA") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePickerFin = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Date",
                                                tint = DarkBlue
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DarkBlue,
                                        unfocusedBorderColor = GrayBorder
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showDatePickerFin = true }
                                )
                            }
                        }
                    }
                }
            }
            
            // Bouton Apply en bas (surélevé de 5dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
            ) {
                Button(
                    onClick = {
                        val filters = CastingFilters(
                            searchQuery = searchQuery.trim(),
                            selectedTypes = selectedTypes.toList(),
                            minPrice = minPrice.toDoubleOrNull(),
                            maxPrice = maxPrice.toDoubleOrNull(),
                            lieu = lieu.takeIf { it.isNotBlank() },
                            dateDebut = dateDebut.takeIf { it.isNotBlank() },
                            dateFin = dateFin.takeIf { it.isNotBlank() }
                        )
                        onApplyFilter(filters)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    )
                ) {
                    Text(
                        text = "Appliquer les filtres",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        }
    }
    
    // Date pickers avec coins arrondis
    if (showDatePickerDebut) {
        DatePickerModal(
            onDismissRequest = { showDatePickerDebut = false },
            onConfirm = {
                datePickerStateDebut.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    dateDebut = dateFormatter.format(date)
                }
                showDatePickerDebut = false
            },
            title = "Sélectionner la date de début",
            state = datePickerStateDebut
        )
    }
    
    if (showDatePickerFin) {
        DatePickerModal(
            onDismissRequest = { showDatePickerFin = false },
            onConfirm = {
                datePickerStateFin.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    dateFin = dateFormatter.format(date)
                }
                showDatePickerFin = false
            },
            title = "Sélectionner la date de fin",
            state = datePickerStateFin
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    state: DatePickerState
) {
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = DarkBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annuler", color = GrayBorder)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(28.dp))
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
            )
            DatePicker(
                state = state,
                colors = DatePickerDefaults.colors(
                    containerColor = White,
                    selectedDayContainerColor = DarkBlue,
                    todayContentColor = DarkBlue,
                    todayDateBorderColor = DarkBlue
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    Projecct_MobileTheme {
        FilterScreen()
    }
}
