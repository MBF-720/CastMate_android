package com.example.projecct_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.data.model.ApiException
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun ResetPasswordScreen(
    token: String,
    email: String,
    userType: String,
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorException by remember { mutableStateOf<Throwable?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository() }
    
    val isAgency = userType.equals("RECRUTEUR", ignoreCase = true) || 
                   userType.equals("agency", ignoreCase = true) ||
                   userType.equals("agence", ignoreCase = true)
    
    // Afficher le dialogue d'erreur détaillé
    if (showErrorDialog && errorMessage != null) {
        com.example.projecct_mobile.ui.components.ErrorDetailDialog(
            title = if (isAgency) "Erreur de réinitialisation" else "Reset Error",
            message = errorMessage ?: "",
            exception = errorException,
            isAgency = isAgency,
            onDismiss = {
                showErrorDialog = false
                errorMessage = null
                errorException = null
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Section bleue avec l'icône
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Orange),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 48.sp)
                }
            }
            
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
                    text = if (isAgency) "Réinitialiser le Mot de Passe" else "Reset Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = if (isAgency) {
                        "Entrez votre nouveau mot de passe pour $email"
                    } else {
                        "Enter your new password for $email"
                    },
                    fontSize = 14.sp,
                    color = GrayBorder,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Champ Nouveau Mot de Passe
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        passwordError = null
                        errorMessage = null
                    },
                    label = { Text(if (isAgency) "Nouveau mot de passe" else "New password") },
                    placeholder = { Text(if (isAgency) "Entrez votre nouveau mot de passe" else "Enter your new password", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (passwordError != null) Red else GrayBorder
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (passwordError != null) Red else DarkBlue.copy(alpha = 0.6f)
                        )
                    },
                    isError = passwordError != null,
                    supportingText = passwordError?.let { 
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Champ Confirmer Mot de Passe
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        confirmPasswordError = null
                        errorMessage = null
                    },
                    label = { Text(if (isAgency) "Confirmer le mot de passe" else "Confirm password") },
                    placeholder = { Text(if (isAgency) "Confirmez votre nouveau mot de passe" else "Confirm your new password", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (confirmPasswordError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (confirmPasswordError != null) Red else GrayBorder
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (confirmPasswordError != null) Red else DarkBlue.copy(alpha = 0.6f)
                        )
                    },
                    isError = confirmPasswordError != null,
                    supportingText = confirmPasswordError?.let { 
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
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
                
                // Bouton Submit
                Button(
                    onClick = {
                        // Réinitialiser les erreurs
                        passwordError = null
                        confirmPasswordError = null
                        errorMessage = null
                        errorException = null
                        successMessage = null
                        
                        // Validation
                        if (newPassword.isBlank()) {
                            passwordError = if (isAgency) "Le mot de passe est requis" else "Password is required"
                            return@Button
                        }
                        
                        if (newPassword.length < 8) {
                            passwordError = if (isAgency) "Le mot de passe doit contenir au moins 8 caractères" else "Password must be at least 8 characters"
                            return@Button
                        }
                        
                        if (confirmPassword.isBlank()) {
                            confirmPasswordError = if (isAgency) "Veuillez confirmer le mot de passe" else "Please confirm password"
                            return@Button
                        }
                        
                        if (newPassword != confirmPassword) {
                            confirmPasswordError = if (isAgency) "Les mots de passe ne correspondent pas" else "Passwords do not match"
                            return@Button
                        }
                        
                        isLoading = true
                        
                        // Appeler l'API pour changer le mot de passe via AuthRepository
                        scope.launch {
                            try {
                                isLoading = true
                                
                                android.util.Log.d("ResetPassword", "🔄 Appel de resetPassword avec token: ${token.take(10)}..., email: $email")
                                
                                // Utiliser AuthRepository pour appeler l'API backend
                                val result = withContext(Dispatchers.IO) {
                                    authRepository.resetPassword(token, newPassword, email)
                                }
                                
                                isLoading = false
                                
                                result.onSuccess { resetPasswordResponse ->
                                    android.util.Log.d("ResetPassword", "✅ Mot de passe changé avec succès")
                                    
                                    // Supprimer le token stocké localement après succès
                                    withContext(Dispatchers.IO) {
                                        tokenManager.clearResetToken(email)
                                    }
                                    
                                    successMessage = if (isAgency) {
                                        "Mot de passe changé avec succès ! Redirection vers la connexion..."
                                    } else {
                                        "Password changed successfully! Redirecting to login..."
                                    }
                                    
                                    // Attendre 2 secondes puis rediriger
                                    kotlinx.coroutines.delay(2000)
                                    onSuccess()
                                }
                                
                                result.onFailure { exception ->
                                    errorException = exception
                                    android.util.Log.e("ResetPassword", "❌ Erreur: ${exception.message}", exception)
                                    
                                    when (exception) {
                                        is ApiException.NotFoundException -> {
                                            // Token non trouvé (404)
                                            errorMessage = if (isAgency) {
                                                "Token non trouvé ou expiré. Veuillez demander un nouveau lien de réinitialisation."
                                            } else {
                                                "Token not found or expired. Please request a new reset link."
                                            }
                                        }
                                        is ApiException.BadRequestException -> {
                                            // Token invalide ou mot de passe non conforme (400)
                                            errorMessage = if (isAgency) {
                                                "Token invalide ou expiré, ou mot de passe non conforme (minimum 8 caractères)."
                                            } else {
                                                "Invalid or expired token, or password does not meet requirements (minimum 8 characters)."
                                            }
                                        }
                                        else -> {
                                            errorMessage = if (isAgency) {
                                                "Erreur lors du changement de mot de passe: ${exception.message}"
                                            } else {
                                                "Error changing password: ${exception.message}"
                                            }
                                        }
                                    }
                                    
                                    showErrorDialog = true
                                    
                                    // Supprimer le token local même en cas d'erreur (il ne sert plus)
                                    withContext(Dispatchers.IO) {
                                        tokenManager.clearResetToken(email)
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorException = e
                                errorMessage = if (isAgency) {
                                    "Erreur de connexion. Vérifiez votre internet"
                                } else {
                                    "Connection error. Check your internet"
                                }
                                showErrorDialog = true
                                android.util.Log.e("ResetPassword", "❌ Exception: ${e.message}", e)
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
                        Text(
                            if (isAgency) "Réinitialiser" else "Reset",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                            if (isAgency) "Retour" else "Back",
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

