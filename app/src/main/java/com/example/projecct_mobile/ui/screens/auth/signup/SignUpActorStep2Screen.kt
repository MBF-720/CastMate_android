package com.example.projecct_mobile.ui.screens.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

/**
 * Étape 2 - Informations professionnelles pour l'inscription ACTEUR
 */
@Composable
fun SignUpActorStep2Screen(
    onBackClick: () -> Unit = {},
    onNextClick: (anneesExperience: String, cvUrl: String?, instagram: String, youtube: String, tiktok: String) -> Unit = { _, _, _, _, _ -> }
) {
    var anneesExperience by remember { mutableStateOf("") }
    var cvUrl by remember { mutableStateOf<String?>(null) }
    var instagram by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    
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
                        text = "Étape 2/3",
                        fontSize = 16.sp,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Informations professionnelles",
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
                // Années d'expérience
                OutlinedTextField(
                    value = anneesExperience,
                    onValueChange = { anneesExperience = it },
                    label = { Text("Années d'expérience *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                // CV Upload
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Ouvrir sélecteur de fichier PDF */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cvUrl != null) LightBlue else LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Upload CV",
                                tint = DarkBlue
                            )
                            Column {
                                Text(
                                    text = if (cvUrl != null) "CV téléchargé" else "Télécharger votre CV (PDF)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkBlue
                                )
                                if (cvUrl == null) {
                                    Text(
                                        text = "Fichier PDF uniquement",
                                        fontSize = 12.sp,
                                        color = GrayBorder
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Réseaux sociaux (optionnels)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Instagram
                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = { Text("Instagram") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("📷", fontSize = 20.sp)
                    }
                )
                
                // YouTube
                OutlinedTextField(
                    value = youtube,
                    onValueChange = { youtube = it },
                    label = { Text("YouTube") },
                    placeholder = { Text("https://youtube.com/@channel") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("▶️", fontSize = 20.sp)
                    }
                )
                
                // TikTok
                OutlinedTextField(
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    label = { Text("TikTok") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("🎵", fontSize = 20.sp)
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Bouton Suivant
                Button(
                    onClick = {
                        if (anneesExperience.isNotBlank()) {
                            onNextClick(anneesExperience, cvUrl, instagram, youtube, tiktok)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    enabled = anneesExperience.isNotBlank()
                ) {
                    Text(
                        text = "Suivant",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpActorStep2ScreenPreview() {
    Projecct_MobileTheme {
        SignUpActorStep2Screen()
    }
}

