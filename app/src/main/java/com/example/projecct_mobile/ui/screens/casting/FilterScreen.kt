package com.example.projecct_mobile.ui.screens.casting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

@Composable
fun FilterScreen(
    onBackClick: () -> Unit = {},
    onApplyFilter: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("09-10-2025") }
    var endDate by remember { mutableStateOf("09-11-2025") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf(50f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                        text = "Casting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    IconButton(onClick = { /* Calendar action */ }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = White
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
                        placeholder = { Text("Search casting", color = LightGray) },
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
                        onClick = { },
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
        
        // Carte blanche avec les options de filtrage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(8.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(White)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icône de cœur
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = RedHeart,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerField(
                        label = "Date début",
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DatePickerField(
                        label = "Date fin",
                        value = endDate,
                        onValueChange = { endDate = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Pays
                OutlinedTextField(
                    value = selectedCountry,
                    onValueChange = { selectedCountry = it },
                    label = { Text("Pays") },
                    placeholder = { Text("Sélectionner un pays") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = DarkBlue
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // Catégories
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { selectedCategory = it },
                    label = { Text("Categories") },
                    placeholder = { Text("Sélectionner une catégorie") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = DarkBlue
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                // Plage de prix
                Column {
                    Text(
                        text = "Plage de prix",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Slider(
                        value = priceRange,
                        onValueChange = { priceRange = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = DarkBlue,
                            activeTrackColor = DarkBlue
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Bouton Apply
                Button(
                    onClick = onApplyFilter,
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
                        text = "Apply",
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

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Date",
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

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    Projecct_MobileTheme {
        FilterScreen()
    }
}

