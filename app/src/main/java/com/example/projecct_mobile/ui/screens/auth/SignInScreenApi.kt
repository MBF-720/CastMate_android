package com.example.projecct_mobile.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Version avec API - À utiliser quand l'API sera disponible
 */
@Composable
fun SignInScreenApi(
    onSignInClick: () -> Unit = {}, 
    onSignUpClick: () -> Unit = {}, 
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Section bleue avec l'icône utilisateur
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(DarkBlue)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Icône utilisateur
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(DarkBlue),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Placeholder pour l'icône utilisateur (vous pouvez utiliser une vraie icône)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Black)
                )
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
                Spacer(modifier = Modifier.height(20.dp))
                
                // Titre
                Text(
                    text = "Sign in",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Champ Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    placeholder = { Text("Entrez votre mot de passe", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lien "Forgot your password?"
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        "Forgot your password?",
                        color = GrayBorder,
                        fontSize = 14.sp
                    )
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
                
                // Bouton Sign in
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Veuillez remplir tous les champs"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            val result = authRepository.login(email.trim(), password)
                            
                            result.onSuccess { authResponse ->
                                isLoading = false
                                onSignInClick()
                            }
                            
                            result.onFailure { exception ->
                                isLoading = false
                                errorMessage = when (exception) {
                                    is ApiException.UnauthorizedException -> 
                                        "Email ou mot de passe incorrect"
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
                        Text("Sign in", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                
                // Boutons sociaux
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bouton Google
                    SocialButtonApi(
                        modifier = Modifier.weight(1f),
                        backgroundColor = White,
                        borderColor = GrayBorder
                    ) {
                        // Placeholder pour l'icône Google
                        Text("G+", color = Black, fontWeight = FontWeight.Bold)
                    }
                    
                    // Bouton LinkedIn
                    SocialButtonApi(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkBlue,
                        borderColor = DarkBlue
                    ) {
                        Text("in", color = White, fontWeight = FontWeight.Bold)
                    }
                    
                    // Bouton Facebook
                    SocialButtonApi(
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentBlue,
                        borderColor = AccentBlue
                    ) {
                        Text("f", color = White, fontWeight = FontWeight.Bold)
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
private fun SocialButtonApi(
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
fun SignInScreenApiPreview() {
    Projecct_MobileTheme {
        SignInScreenApi()
    }
}

