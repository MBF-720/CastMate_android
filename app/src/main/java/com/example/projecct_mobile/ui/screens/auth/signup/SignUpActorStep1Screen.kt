package com.example.projecct_mobile.ui.screens.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.projecct_mobile.ui.theme.*

/**
 * Étape 1 - Informations personnelles pour l'inscription ACTEUR
 */
@Composable
fun SignUpActorStep1Screen(
    initialNom: String = "",
    initialPrenom: String = "",
    initialEmail: String = "",
    initialPhotoUrl: String? = null,
    onBackClick: () -> Unit = {},
    onNextClick: (nom: String, prenom: String, age: String, email: String, telephone: String, gouvernorat: String, photoUrl: String?) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var nom by remember { mutableStateOf(initialNom) }
    var prenom by remember { mutableStateOf(initialPrenom) }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }
    var telephone by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf(initialPhotoUrl) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
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
                        text = "Étape 1/3",
                        fontSize = 16.sp,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Informations personnelles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
        
        // Contenu avec ombre
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    spotColor = DarkBlue.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo de profil
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = DarkBlue.copy(alpha = 0.3f)
                            )
                            .clickable { /* TODO: Ouvrir sélecteur d'image */ },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (photoUrl != null) Color.Transparent else LightGray
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl != null) {
                                // TODO: Charger l'image depuis l'URL
                                Text("📷", fontSize = 42.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Photo",
                                    tint = DarkBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Photo de profil (optionnelle)",
                        fontSize = 11.sp,
                        color = GrayBorder,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Nom
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { 
                        Text(
                            "Nom *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )
                
                // Prénom
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { 
                        Text(
                            "Prénom *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )
                
                // Âge
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { 
                        Text(
                            "Âge *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { 
                        Text(
                            "Email *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    enabled = initialEmail.isEmpty() // Désactivé si rempli par Google
                )
                
                // Numéro de téléphone
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { 
                        Text(
                            "Numéro de téléphone *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                
                // Gouvernorat
                OutlinedTextField(
                    value = gouvernorat,
                    onValueChange = { gouvernorat = it },
                    label = { 
                        Text(
                            "Gouvernorat *", 
                            fontWeight = FontWeight.SemiBold, 
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bouton Suivant avec style amélioré
                Button(
                    onClick = {
                        if (nom.isNotBlank() && prenom.isNotBlank() && age.isNotBlank() 
                            && email.isNotBlank() && telephone.isNotBlank() && gouvernorat.isNotBlank()) {
                            onNextClick(nom, prenom, age, email, telephone, gouvernorat, photoUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = DarkBlue.copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    enabled = nom.isNotBlank() && prenom.isNotBlank() && age.isNotBlank() 
                        && email.isNotBlank() && telephone.isNotBlank() && gouvernorat.isNotBlank()
                ) {
                    Text(
                        text = "Suivant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpActorStep1ScreenPreview() {
    Projecct_MobileTheme {
        SignUpActorStep1Screen()
    }
}

