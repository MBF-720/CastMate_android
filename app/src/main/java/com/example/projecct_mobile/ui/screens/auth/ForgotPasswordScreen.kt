package com.example.projecct_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {}, 
    onSubmitClick: () -> Unit = {},
    userRole: String = "actor", // "actor" ou "agency"
    onForgotPassword: (String) -> Unit = {} // Callback pour envoyer l'email
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Texte adapté selon le type d'utilisateur
    val isAgency = userRole.equals("agency", ignoreCase = true) || 
                   userRole.equals("recruteur", ignoreCase = true) ||
                   userRole.equals("agence", ignoreCase = true)

    Box(modifier = Modifier.fillMaxSize()) {
        // Section bleue avec l'icône de cadenas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(DarkBlue)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Icône cadenas
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(DarkBlue),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Placeholder pour l'icône cadenas
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Orange), // Orange pour la cadenas
                    contentAlignment = Alignment.Center
                ) {
                    // Vous pouvez ajouter une vraie icône ici
                    Text("🔒", fontSize = 48.sp)
                }
            }
            
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
                .fillMaxWidth()
                .padding(top = 120.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Titre
                Text(
                    text = if (isAgency) "Mot de passe oublié ?" else "Forgot Password?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = if (isAgency) {
                        "Ne vous inquiétez pas ! Entrez l'adresse email associée à votre compte agence."
                    } else {
                        "Don't worry! It happens. Please enter the email address associated with your account."
                    },
                    fontSize = 14.sp,
                    color = GrayBorder,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Champ Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("ex: john.doe@example.com", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                
                // Message de succès
                successMessage?.let { success ->
                    Text(
                        text = success,
                        color = DarkBlue,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Bouton Submit (Mode test - validation simple)
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Veuillez entrer votre email"
                            successMessage = null
                            return@Button
                        }
                        
                        // Validation simple de l'email
                        if (!email.contains("@") || !email.contains(".")) {
                            errorMessage = "Veuillez entrer un email valide"
                            successMessage = null
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        
                        // Appeler la fonction de réinitialisation
                        scope.launch {
                            try {
                                // Appeler le callback qui fait l'appel API
                                onForgotPassword(email.trim())
                                
                                // Attendre un peu pour voir les logs
                                delay(2000)
                                
                                // Simuler le succès (le message réel viendra du backend)
                                isLoading = false
                                successMessage = if (isAgency) {
                                    "Un email de réinitialisation a été envoyé à $email"
                                } else {
                                    "A reset email has been sent to $email"
                                }
                                errorMessage = null
                                
                                // Navigation après 2 secondes
                                delay(2000)
                                onSubmitClick()
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message ?: if (isAgency) {
                                    "Erreur lors de l'envoi de l'email"
                                } else {
                                    "Error sending email"
                                }
                                successMessage = null
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
                        Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bouton Back
                TextButton(
                    onClick = onBackClick
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isAgency) "Retour à la connexion" else "Back to Login",
                            color = DarkBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    Projecct_MobileTheme {
        ForgotPasswordScreen()
    }
}

