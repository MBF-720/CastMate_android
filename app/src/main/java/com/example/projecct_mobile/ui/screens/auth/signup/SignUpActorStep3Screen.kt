package com.example.projecct_mobile.ui.screens.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

/**
 * Étape 3 - Centres d'intérêt pour l'inscription ACTEUR
 */
@Composable
fun SignUpActorStep3Screen(
    onBackClick: () -> Unit = {},
    onFinishClick: (centresInteret: List<String>) -> Unit = {}
) {
    val defaultInterests = listOf(
        "Publicité", "Cinéma", "Théâtre", "Mannequinat", 
        "Télévision", "Web série", "Documentaire", "Court métrage"
    )
    
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var customTag by remember { mutableStateOf("") }
    var allInterests by remember { mutableStateOf(defaultInterests) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                        text = "Étape 3/3",
                        fontSize = 16.sp,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Centres d'intérêt",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
        
        // Contenu
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sélectionnez vos centres d'intérêt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                
                // Liste des centres d'intérêt
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allInterests) { interest ->
                        InterestChip(
                            text = interest,
                            isSelected = selectedInterests.contains(interest),
                            onClick = {
                                selectedInterests = if (selectedInterests.contains(interest)) {
                                    selectedInterests - interest
                                } else {
                                    selectedInterests + interest
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Ajouter un tag personnalisé",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayBorder
                )
                
                // Ajout de tag personnalisé
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customTag,
                        onValueChange = { customTag = it },
                        placeholder = { Text("Ajouter un tag") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (customTag.isNotBlank() && !allInterests.contains(customTag.trim())) {
                                    allInterests = allInterests + customTag.trim()
                                    selectedInterests = selectedInterests + customTag.trim()
                                    customTag = ""
                                }
                            }
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            if (customTag.isNotBlank() && !allInterests.contains(customTag.trim())) {
                                allInterests = allInterests + customTag.trim()
                                selectedInterests = selectedInterests + customTag.trim()
                                customTag = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkBlue, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = White
                        )
                    }
                }
                
                // Tags sélectionnés
                if (selectedInterests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tags sélectionnés (${selectedInterests.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayBorder
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedInterests.toList()) { interest ->
                            SelectedInterestChip(
                                text = interest,
                                onRemove = {
                                    selectedInterests = selectedInterests - interest
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Bouton Terminer
                Button(
                    onClick = {
                        onFinishClick(selectedInterests.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    enabled = selectedInterests.isNotEmpty()
                ) {
                    Text(
                        text = "Terminer",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
fun InterestChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) DarkBlue else GrayBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) LightBlue else White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) DarkBlue else Black
        )
    }
}

@Composable
fun SelectedInterestChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.border(
            width = 1.dp,
            color = DarkBlue,
            shape = RoundedCornerShape(20.dp)
        ),
        shape = RoundedCornerShape(20.dp),
        color = LightBlue
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DarkBlue
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = DarkBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpActorStep3ScreenPreview() {
    Projecct_MobileTheme {
        SignUpActorStep3Screen()
    }
}

