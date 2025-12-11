package com.example.projecct_mobile.ui.screens.agence.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpAgencyStep1Screen(
    initialNomAgence: String = "",
    initialNomResponsable: String = "",
    initialEmail: String = "",
    onBackClick: () -> Unit = {},
    onNextClick: (nomAgence: String, nomResponsable: String, email: String, telephone: String, gouvernorat: String, motDePasse: String) -> Unit = { _, _, _, _, _, _ -> }
) {
    var nomAgence by remember { mutableStateOf(initialNomAgence) }
    var nomResponsable by remember { mutableStateOf(initialNomResponsable) }
    var email by remember { mutableStateOf(initialEmail) }
    var telephone by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var confirmMotDePasse by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var telephoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isGouvernoratDialogOpen by remember { mutableStateOf(false) }
    
    val gouvernoratsTunisie = remember {
        listOf(
            "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
            "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia",
            "Manouba", "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid",
            "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        )
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (!emailRegex.matches(email)) {
            "Format d'email invalide"
        } else null
    }

    fun validateTelephone(telephone: String): String? {
        if (telephone.isBlank()) return null
        val digitsOnly = telephone.filter { it.isDigit() }
        return when {
            digitsOnly.isEmpty() -> "Le numéro doit contenir uniquement des chiffres"
            digitsOnly.length < 8 -> "Le numéro doit contenir au moins 8 chiffres"
            digitsOnly.length > 15 -> "Le numéro est trop long"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Le mot de passe est obligatoire"
            password.length < 8 -> "Le mot de passe doit contenir au moins 8 caractères"
            !password.any { it.isDigit() } -> "Le mot de passe doit contenir au moins un chiffre"
            else -> null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                            text = "Étape 1/2",
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
                        text = "Créer votre compte",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Informations de l'agence",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }
        }

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = nomAgence,
                    onValueChange = { nomAgence = it },
                    label = {
                        Text(
                            "Nom de l'agence *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue.copy(alpha = 0.8f),
                        unfocusedBorderColor = GrayBorder,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = Color(0xFF666666)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nomResponsable,
                    onValueChange = { nomResponsable = it },
                    label = {
                        Text(
                            "Nom du responsable *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = validateEmail(it)
                    },
                    label = {
                        Text(
                            "Email *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (emailError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    enabled = initialEmail.isEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = emailError?.let {
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )

                OutlinedTextField(
                    value = telephone,
                    onValueChange = {
                        val digitsOnly = it.filter { char -> char.isDigit() }
                        telephone = digitsOnly
                        telephoneError = validateTelephone(digitsOnly)
                    },
                    label = {
                        Text(
                            "Téléphone *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    placeholder = { Text("Ex: 12345678", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (telephoneError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (telephoneError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = telephoneError != null,
                    supportingText = telephoneError?.let {
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )

                OutlinedTextField(
                    value = motDePasse,
                    onValueChange = {
                        motDePasse = it
                        passwordError = validatePassword(it)
                        confirmPasswordError = if (confirmMotDePasse.isNotBlank() && confirmMotDePasse != it) {
                            "Les mots de passe ne correspondent pas"
                        } else null
                    },
                    label = {
                        Text(
                            "Mot de passe *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    placeholder = { Text("Minimum 8 caractères", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (passwordError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = passwordError?.let {
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )

                OutlinedTextField(
                    value = confirmMotDePasse,
                    onValueChange = {
                        confirmMotDePasse = it
                        confirmPasswordError = if (it != motDePasse) {
                            "Les mots de passe ne correspondent pas"
                        } else null
                    },
                    label = {
                        Text(
                            "Confirmer le mot de passe *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    placeholder = { Text("Répétez votre mot de passe", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (confirmPasswordError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (confirmPasswordError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPasswordError != null,
                    supportingText = confirmPasswordError?.let {
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gouvernorat,
                        onValueChange = { },
                        label = {
                            Text(
                                "Gouvernorat *",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        },
                        placeholder = { Text("Sélectionner un gouvernorat", color = GrayBorder) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isGouvernoratDialogOpen = true }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = DarkBlue
                                )
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isGouvernoratDialogOpen = true }
                    )
                }

                if (isGouvernoratDialogOpen) {
                    AlertDialog(
                        onDismissRequest = { isGouvernoratDialogOpen = false },
                        confirmButton = {
                            TextButton(onClick = { isGouvernoratDialogOpen = false }) {
                                Text("Fermer", color = DarkBlue)
                            }
                        },
                        title = {
                            Text(
                                text = "Sélectionnez un gouvernorat",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        text = {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(gouvernoratsTunisie) { gov ->
                                    Text(
                                        text = gov,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                gouvernorat = gov
                                                isGouvernoratDialogOpen = false
                                            }
                                            .padding(vertical = 8.dp),
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isFormValid = nomAgence.isNotBlank() &&
                    nomResponsable.isNotBlank() &&
                    email.isNotBlank() &&
                    emailError == null &&
                    telephone.isNotBlank() &&
                    telephoneError == null &&
                    gouvernorat.isNotBlank() &&
                    motDePasse.isNotBlank() &&
                    confirmMotDePasse.isNotBlank() &&
                    passwordError == null &&
                    confirmPasswordError == null &&
                    motDePasse == confirmMotDePasse

                Button(
                    onClick = {
                        emailError = validateEmail(email)
                        telephoneError = validateTelephone(telephone)
                        passwordError = validatePassword(motDePasse)
                        confirmPasswordError = if (motDePasse == confirmMotDePasse) null else "Les mots de passe ne correspondent pas"

                        if (isFormValid &&
                            emailError == null &&
                            telephoneError == null &&
                            passwordError == null &&
                            confirmPasswordError == null
                        ) {
                            onNextClick(nomAgence, nomResponsable, email, telephone, gouvernorat, motDePasse)
                        }
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
                    enabled = isFormValid
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
fun SignUpAgencyStep1ScreenPreview() {
    Projecct_MobileTheme {
        SignUpAgencyStep1Screen()
    }
}
