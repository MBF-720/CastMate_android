package com.example.projecct_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
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
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(onSignUpClick: () -> Unit = {}, onLoginClick: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isRoleDropdownExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    val roles = listOf("ACTEUR", "RECRUTEUR")
    val roleDisplayNames = mapOf(
        "ACTEUR" to "Acteur",
        "RECRUTEUR" to "Agence / Recruteur"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                .fillMaxWidth()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(15.dp)),
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
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Titre
                    Text(
                        text = "Sign Up",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Champ Email (obligatoire)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        placeholder = { Text("ex: john.doe@example.com", color = LightGray) },
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
                    
                    // Champs First Name et Last Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = prenom,
                            onValueChange = { prenom = it },
                            label = { Text("Prénom") },
                            placeholder = { Text("ex: Jean", color = LightGray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightBlue,
                                unfocusedBorderColor = LightBlue,
                                focusedContainerColor = LightGray,
                                unfocusedContainerColor = LightGray
                            ),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            placeholder = { Text("ex: Dupont", color = LightGray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightBlue,
                                unfocusedBorderColor = LightBlue,
                                focusedContainerColor = LightGray,
                                unfocusedContainerColor = LightGray
                            ),
                            singleLine = true
                        )
                    }
                    
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
                    
                    // Sélecteur de rôle (obligatoire)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRole?.let { roleDisplayNames[it] ?: it } ?: "",
                            onValueChange = { },
                            label = { Text("Rôle *") },
                            placeholder = { Text("Sélectionnez votre rôle", color = LightGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isRoleDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightBlue,
                                unfocusedBorderColor = LightBlue,
                                focusedContainerColor = LightGray,
                                unfocusedContainerColor = LightGray
                            ),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (isRoleDropdownExpanded) 
                                        Icons.Default.ArrowDropUp 
                                    else 
                                        Icons.Default.ArrowDropDown,
                                    contentDescription = "Rôle",
                                    tint = DarkBlue
                                )
                            },
                            singleLine = true
                        )
                        
                        DropdownMenu(
                            expanded = isRoleDropdownExpanded,
                            onDismissRequest = { isRoleDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(roleDisplayNames[role] ?: role) },
                                    onClick = {
                                        selectedRole = role
                                        isRoleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
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
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "L'email et le mot de passe sont obligatoires"
                                return@Button
                            }
                            
                            if (selectedRole.isNullOrBlank()) {
                                errorMessage = "Veuillez sélectionner un rôle"
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
                                val result = authRepository.register(
                                    email = email.trim(),
                                    password = password,
                                    nom = nom.takeIf { it.isNotBlank() },
                                    prenom = prenom.takeIf { it.isNotBlank() },
                                    role = selectedRole
                                )
                                
                                result.onSuccess { authResponse ->
                                    isLoading = false
                                    onSignUpClick()
                                }
                                
                                result.onFailure { exception ->
                                    isLoading = false
                                    errorMessage = when (exception) {
                                        is ApiException.ConflictException -> 
                                            "Cet email est déjà utilisé"
                                        is ApiException.BadRequestException -> {
                                            // Afficher le message détaillé de l'erreur
                                            val message = exception.message ?: "Vérifiez vos informations"
                                            // Limiter à 150 caractères pour l'affichage
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

