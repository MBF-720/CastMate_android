package com.example.projecct_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(onSignUpClick: () -> Unit = {}, onLoginClick: () -> Unit = {}) {
    var nomAgence by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var siteWeb by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White)
    ) {
        // Section bleue décorative
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkBlue)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Onde décorative
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(DarkBlue)
            )
        }

        // Contenu blanc principal
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            // Bordure pointillée
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = LightBlue,
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Titre
                    Text(
                        text = "Inscription Agence",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Nom de l'agence (obligatoire)
                    OutlinedTextField(
                        value = nomAgence,
                        onValueChange = { nomAgence = it },
                        label = { Text("Nom de l'agence *") },
                        placeholder = { Text("ex: Agence de Casting Tunis", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Responsable (obligatoire)
                    OutlinedTextField(
                        value = responsable,
                        onValueChange = { responsable = it },
                        label = { Text("Responsable *") },
                        placeholder = { Text("ex: Mohamed Ben Ali", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champ Email (obligatoire)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        placeholder = { Text("ex: contact@agence.tn", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Téléphone (obligatoire)
                    OutlinedTextField(
                        value = tel,
                        onValueChange = { tel = it },
                        label = { Text("Téléphone *") },
                        placeholder = { Text("ex: +21612345678", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Gouvernorat (obligatoire)
                    OutlinedTextField(
                        value = gouvernorat,
                        onValueChange = { gouvernorat = it },
                        label = { Text("Gouvernorat *") },
                        placeholder = { Text("ex: Tunis", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champ Password (obligatoire)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe *") },
                        placeholder = { Text("Minimum 8 caractères", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Champ Confirm Password (obligatoire)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe *") },
                        placeholder = { Text("Répétez votre mot de passe", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Site web (optionnel)
                    OutlinedTextField(
                        value = siteWeb,
                        onValueChange = { siteWeb = it },
                        label = { Text("Site web") },
                        placeholder = { Text("ex: https://www.agence.tn", color = LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Description (optionnel)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Description de l'agence", color = LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = LightBlue,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Ligne pointillée
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LightBlue)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Message d'erreur
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // Bouton Create Account
                    Button(
                        onClick = {
                            // Validation
                            if (nomAgence.isBlank() || responsable.isBlank() || email.isBlank() 
                                || password.isBlank() || tel.isBlank() || gouvernorat.isBlank()) {
                                errorMessage = "Veuillez remplir tous les champs obligatoires"
                                return@Button
                            }

                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Veuillez saisir une adresse email valide"
                                return@Button
                            }
                            
                            if (password != confirmPassword) {
                                errorMessage = "Les mots de passe ne correspondent pas"
                                return@Button
                            }
                            
                            if (password.length < 8) {
                                errorMessage = "Le mot de passe doit contenir au moins 8 caractères"
                                return@Button
                            }
                            
                            isLoading = true
                            errorMessage = null
                            
                            scope.launch {
                                val result = authRepository.signupAgence(
                                    nomAgence = nomAgence.trim(),
                                    responsable = responsable.trim(),
                                    email = email.trim(),
                                    motDePasse = password,
                                    tel = tel.trim(),
                                    gouvernorat = gouvernorat.trim(),
                                    siteWeb = siteWeb.takeIf { it.isNotBlank() },
                                    description = description.trim().takeIf { it.isNotBlank() } ?: ""
                                )
                                
                                result.onSuccess {
                                    isLoading = false
                                    onSignUpClick()
                                }
                                
                                result.onFailure { exception ->
                                    isLoading = false
                                    errorMessage = when (exception) {
                                        is ApiException.BadRequestException -> {
                                            val message = exception.message ?: "Vérifiez vos informations"
                                            if (message.length > 150) {
                                                message.take(150) + "..."
                                            } else {
                                                message
                                            }
                                        }
                                        is ApiException.NetworkException -> 
                                            "Erreur de connexion. Vérifiez votre connexion internet."
                                        else -> 
                                            "Erreur: ${exception.message ?: "Erreur inconnue"}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White
                            )
                        } else {
                            Text("create Account", fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Ligne pointillée
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LightBlue)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lien "Already Have an Account, Login"
                    Text(
                        text = buildAnnotatedString {
                            append("Already Have an Account, ")
                            withStyle(style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = AccentBlue
                            )) {
                                append("Login")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoginClick() },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Projecct_MobileTheme {
        SignUpScreen()
    }
}

