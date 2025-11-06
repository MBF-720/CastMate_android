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
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit = {}, 
    onSignUpClick: () -> Unit = {}, 
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Section bleue avec gradient et icône utilisateur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                // Icône utilisateur avec effet
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
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Contenu blanc principal avec ombre
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
                
                // Titre avec style amélioré
                Text(
                    text = "Connexion",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connectez-vous à votre compte",
                    fontSize = 14.sp,
                    color = GrayBorder,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Champ Email avec style amélioré
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("ex: john.doe@example.com", color = GrayBorder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.6f),
                        focusedContainerColor = LightGray.copy(alpha = 0.3f),
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = DarkBlue.copy(alpha = 0.6f)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Champ Password avec style amélioré
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Entrez votre mot de passe", color = GrayBorder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.6f),
                        focusedContainerColor = LightGray.copy(alpha = 0.3f),
                        unfocusedContainerColor = White
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = DarkBlue.copy(alpha = 0.6f)
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
                
                // Bouton Sign in (MODE TEST - validation simple sans API)
                Button(
                    onClick = {
                        // Validation simple (mode test)
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Veuillez remplir tous les champs"
                            return@Button
                        }
                        
                        // Validation simple de l'email
                        if (!email.contains("@") || !email.contains(".")) {
                            errorMessage = "Veuillez entrer un email valide"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        // Simulation d'un délai (mode test - pas de vérification API)
                        scope.launch {
                            delay(800) // Simulation d'un délai de connexion
                            isLoading = false
                            // Mode test : accepte n'importe quel email/password valide
                            onSignInClick()
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
                
                // Bouton "Continuer avec Google" avec style amélioré
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
                    )
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

