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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.AccentBlue
import com.example.projecct_mobile.ui.theme.Black
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun SignInAgencyScreen(
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = White.copy(alpha = 0.3f)
                        ),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = White.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = "Agency",
                            tint = White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 160.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    spotColor = DarkBlue.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Connexion Agence",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connectez-vous à votre compte agence",
                    fontSize = 14.sp,
                    color = GrayBorder,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = null
                        errorMessage = null
                    },
                    label = { Text("Email", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("ex: agence@example.com", color = GrayBorder) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (emailError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedLabelColor = if (emailError != null) Red else DarkBlue,
                        unfocusedLabelColor = if (emailError != null) Red else GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = if (emailError != null) Red else DarkBlue.copy(alpha = 0.6f)
                        )
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { 
                        { Text(it, color = Red, fontSize = 12.sp) }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = null
                        errorMessage = null
                    },
                    label = { Text("Mot de passe", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Entrez votre mot de passe", color = GrayBorder) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) Red else DarkBlue,
                        unfocusedBorderColor = if (passwordError != null) Red else GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedLabelColor = if (passwordError != null) Red else DarkBlue,
                        unfocusedLabelColor = if (passwordError != null) Red else GrayBorder
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DarkBlue
                            )
                        )
                        Text(
                            text = "Remember Me",
                            fontSize = 14.sp,
                            color = Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    TextButton(
                        onClick = onForgotPasswordClick
                    ) {
                        Text(
                            "Forgot your password?",
                            color = GrayBorder,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        // Réinitialiser les erreurs
                        emailError = null
                        passwordError = null
                        errorMessage = null
                        
                        // Validation de l'email
                        if (email.isBlank()) {
                            emailError = "L'email est requis"
                            return@Button
                        }

                        if (!email.contains("@") || !email.contains(".")) {
                            emailError = "Format d'email invalide"
                            return@Button
                        }
                        
                        // Validation du mot de passe
                        if (password.isBlank()) {
                            passwordError = "Le mot de passe est requis"
                            return@Button
                        }
                        
                        if (password.length < 6) {
                            passwordError = "Le mot de passe doit contenir au moins 6 caractères"
                            return@Button
                        }

                        isLoading = true

                        scope.launch {
                            try {
                                val result = authRepository.login(email.trim(), password, expectedRole = "RECRUTEUR")
                                
                                result.onSuccess {
                                    isLoading = false
                                    onSignInClick()
                                }
                                
                                result.onFailure { exception ->
                                    isLoading = false
                                    val errorMsg = getErrorMessage(exception)
                                    
                                    // Afficher l'erreur sur le champ approprié
                                    when {
                                        errorMsg.contains("email ou mot de passe incorrect", ignoreCase = true) ||
                                        errorMsg.contains("identifiants incorrect", ignoreCase = true) ||
                                        errorMsg.contains("utilisateur non trouvé", ignoreCase = true) ||
                                        errorMsg.contains("user not found", ignoreCase = true) ||
                                        errorMsg.contains("agence non trouvée", ignoreCase = true) ||
                                        errorMsg.contains("compte inexistant", ignoreCase = true) -> {
                                            // Les deux sont incorrects ou agence non trouvée
                                            emailError = "Email ou mot de passe incorrect"
                                            passwordError = "Email ou mot de passe incorrect"
                                        }
                                        errorMsg.contains("email", ignoreCase = true) || 
                                        errorMsg.contains("utilisateur", ignoreCase = true) ||
                                        errorMsg.contains("compte", ignoreCase = true) ||
                                        errorMsg.contains("agence", ignoreCase = true) -> {
                                            emailError = "Email incorrect ou compte agence inexistant"
                                        }
                                        errorMsg.contains("mot de passe", ignoreCase = true) ||
                                        errorMsg.contains("password", ignoreCase = true) -> {
                                            passwordError = "Mot de passe incorrect"
                                        }
                                        errorMsg.contains("réseau", ignoreCase = true) ||
                                        errorMsg.contains("network", ignoreCase = true) ||
                                        errorMsg.contains("connexion", ignoreCase = true) -> {
                                            errorMessage = "Erreur de connexion. Vérifiez votre connexion internet."
                                        }
                                        else -> {
                                            errorMessage = errorMsg
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = getErrorMessage(e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = DarkBlue.copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White
                        )
                    } else {
                        Text(
                            "Se connecter",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(GrayBorder)
                    )
                    Text(
                        "Or",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = GrayBorder
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(GrayBorder)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Black,
                        containerColor = White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        GrayBorder.copy(alpha = 0.8f)
                    ),
                    enabled = !isLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "G+",
                            color = Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            "Continuer avec Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Vous n'avez pas de compte ? ")
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("S'inscrire")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignUpClick() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInAgencyScreenPreview() {
    Projecct_MobileTheme {
        SignInAgencyScreen()
    }
}
