package com.example.projecct_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.graphics.Color
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit = {}, 
    onSignUpClick: () -> Unit = {}, 
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    isGoogleLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Section bleue simplifiée en haut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlue.copy(alpha = 0.8f))
                    )
                )
        )

        // Contenu blanc principal avec effet flottant
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 140.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = DarkBlue.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Titre avec style moderne
                Text(
                    text = "Bienvenue",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connectez-vous pour continuer",
                    fontSize = 15.sp,
                    color = GrayBorder,
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Champ Email avec style moderne
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("ex: john.doe@example.com", color = GrayBorder.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.3f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = DarkBlue.copy(alpha = 0.8f)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Champ Password avec style moderne
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Entrez votre mot de passe", color = GrayBorder.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.3f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = DarkBlue.copy(alpha = 0.8f)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Remember Me checkbox et Forgot Password
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
                
                // Message d'erreur
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Bouton Sign in avec API
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Veuillez remplir tous les champs"
                            return@Button
                        }
                        
                        if (!email.contains("@") || !email.contains(".")) {
                            errorMessage = "Veuillez entrer un email valide"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                val result = authRepository.login(email.trim(), password)
                                
                                result.onSuccess {
                                    isLoading = false
                                    onSignInClick()
                                }
                                
                                result.onFailure { exception ->
                                    isLoading = false
                                    errorMessage = getErrorMessage(exception)
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = getErrorMessage(e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    enabled = !isLoading,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
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
                
                // Séparateur "Or"
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
                
                // Bouton "Continuer avec Google" sans ombre
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Black,
                        containerColor = White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        GrayBorder.copy(alpha = 0.8f)
                    ),
                    enabled = !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DarkBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
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
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Lien "Don't Have an Account, Sign Up"
                Text(
                    text = buildAnnotatedString {
                        append("Don't Have an Account, ")
                        withStyle(style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = AccentBlue
                        )) {
                            append("Sign Up")
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

@Composable
fun SocialButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    Projecct_MobileTheme {
        SignInScreen()
    }
}

