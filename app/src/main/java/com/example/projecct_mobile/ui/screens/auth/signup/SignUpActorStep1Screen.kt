package com.example.projecct_mobile.ui.screens.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns
import com.example.projecct_mobile.ui.theme.*

/**
 * Étape 1 - Informations personnelles pour l'inscription ACTEUR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpActorStep1Screen(
    initialNom: String = "",
    initialPrenom: String = "",
    initialEmail: String = "",
    initialPhotoUrl: String? = null,
    onBackClick: () -> Unit = {},
    onNextClick: (nom: String, prenom: String, age: String, email: String, motDePasse: String, telephone: String, gouvernorat: String, photoUrl: String?) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var nom by remember { mutableStateOf(initialNom) }
    var prenom by remember { mutableStateOf(initialPrenom) }
    var ageInt by remember { mutableStateOf(18) }
    var age by remember { mutableStateOf("18") }
    var email by remember { mutableStateOf(initialEmail) }
    var motDePasse by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf(initialPhotoUrl) }
    
    val scrollState = rememberScrollState()
    var formError by remember { mutableStateOf<String?>(null) }
    val gouvernorats = remember {
        listOf(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
            "Bizerte", "Béja", "Jendouba", "Kef", "Siliana", "Sousse",
            "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine", "Sidi Bouzid",
            "Gabès", "Médenine", "Tataouine", "Gafsa", "Tozeur", "Kébili"
        )
    }
    var gouvernoratExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête simplifié
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                    
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "Étape 1/3",
                            fontSize = 14.sp,
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Créer votre profil",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Informations personnelles",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        // Contenu moderne
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    spotColor = DarkBlue.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Photo de profil simplifiée
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(90.dp)
                            .clickable { /* TODO: Ouvrir sélecteur d'image */ },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (photoUrl != null) Color.Transparent else LightGray.copy(alpha = 0.5f)
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
                                    modifier = Modifier.size(32.dp)
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
                            fontWeight = FontWeight.Medium, 
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder
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
                            fontWeight = FontWeight.Medium, 
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder
                    ),
                    singleLine = true
                )
                
                // Âge avec boutons d'incrémentation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton -
                    IconButton(
                        onClick = { 
                            if (ageInt > 1) {
                                ageInt--
                                age = ageInt.toString()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                DarkBlue.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuer",
                            tint = DarkBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Champ âge
                    OutlinedTextField(
                        value = ageInt.toString(),
                        onValueChange = { 
                            val newValue = it.toIntOrNull()
                            if (newValue != null && newValue > 0 && newValue <= 120) {
                                ageInt = newValue
                                age = newValue.toString()
                            }
                        },
                        label = { 
                            Text(
                                "Âge *", 
                                fontWeight = FontWeight.Medium, 
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedTextColor = Color(0xFF1A1A1A),
                            unfocusedTextColor = Color(0xFF1A1A1A),
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = GrayBorder
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    // Bouton +
                    IconButton(
                        onClick = { 
                            if (ageInt < 120) {
                                ageInt++
                                age = ageInt.toString()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                DarkBlue.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Augmenter",
                            tint = DarkBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { 
                        Text(
                            "Email *", 
                            fontWeight = FontWeight.Medium, 
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder
                    ),
                    singleLine = true,
                    enabled = initialEmail.isEmpty() // Désactivé si rempli par Google
                )
                
                // Mot de passe
                OutlinedTextField(
                    value = motDePasse,
                    onValueChange = { motDePasse = it },
                    label = { 
                        Text(
                            "Mot de passe *", 
                            fontWeight = FontWeight.Medium, 
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                // Numéro de téléphone
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { 
                        Text(
                            "Numéro de téléphone *", 
                            fontWeight = FontWeight.Medium, 
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                
                // Gouvernorat
                ExposedDropdownMenuBox(
                    expanded = gouvernoratExpanded,
                    onExpandedChange = { gouvernoratExpanded = !gouvernoratExpanded }
                ) {
                    OutlinedTextField(
                        value = gouvernorat,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                "Gouvernorat *",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder.copy(alpha = 0.5f),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedTextColor = Color(0xFF1A1A1A),
                            unfocusedTextColor = Color(0xFF1A1A1A),
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = GrayBorder
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = gouvernoratExpanded)
                        },
                        singleLine = true,
                        placeholder = { Text("Sélectionnez un gouvernorat") }
                    )

                    ExposedDropdownMenu(
                        expanded = gouvernoratExpanded,
                        onDismissRequest = { gouvernoratExpanded = false }
                    ) {
                        gouvernorats.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    gouvernorat = item
                                    gouvernoratExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Message d'erreur de validation
                formError?.let { message ->
                    Text(
                        text = message,
                        color = Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                // Bouton Suivant moderne
                Button(
                    onClick = {
                        if (nom.isBlank() || prenom.isBlank() || ageInt <= 0
                            || email.isBlank() || motDePasse.isBlank()
                            || telephone.isBlank() || gouvernorat.isBlank()) {
                            formError = "Veuillez remplir tous les champs obligatoires"
                            return@Button
                        }

                        if (ageInt !in 16..80) {
                            formError = "L'âge doit être compris entre 16 et 80 ans"
                            return@Button
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            formError = "Veuillez saisir une adresse email valide"
                            return@Button
                        }

                        if (motDePasse.length < 8) {
                            formError = "Le mot de passe doit contenir au moins 8 caractères"
                            return@Button
                        }

                        val phoneRegex = Regex("^[0-9]{8}\$")
                        val sanitizedPhone = telephone.replace(" ", "")
                        if (!phoneRegex.matches(sanitizedPhone)) {
                            formError = "Le numéro de téléphone doit contenir exactement 8 chiffres"
                            return@Button
                        }

                        if (!gouvernorats.contains(gouvernorat)) {
                            formError = "Veuillez sélectionner un gouvernorat dans la liste"
                            return@Button
                        }

                        formError = null
                        onNextClick(nom, prenom, ageInt.toString(), email, motDePasse, sanitizedPhone, gouvernorat, photoUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = DarkBlue.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        disabledContainerColor = GrayBorder.copy(alpha = 0.5f)
                    ),
                    enabled = nom.isNotBlank() && prenom.isNotBlank() && ageInt > 0 
                        && email.isNotBlank() && motDePasse.isNotBlank() 
                        && telephone.isNotBlank() && gouvernorat.isNotBlank()
                        && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                ) {
                    Text(
                        text = "Continuer",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.8.sp
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

