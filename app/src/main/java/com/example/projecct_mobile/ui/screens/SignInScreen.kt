package com.example.projecct_mobile.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

@Composable
fun SignInScreen(onSignInClick: () -> Unit = {}, onSignUpClick: () -> Unit = {}, onForgotPasswordClick: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                    placeholder = { Text("Email", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Champ Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder
                    )
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
                
                // Bouton Sign in
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text("Sign in", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    SocialButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = White,
                        borderColor = GrayBorder
                    ) {
                        // Placeholder pour l'icône Google
                        Text("G+", color = Black, fontWeight = FontWeight.Bold)
                    }
                    
                    // Bouton LinkedIn
                    SocialButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkBlue,
                        borderColor = DarkBlue
                    ) {
                        Text("in", color = White, fontWeight = FontWeight.Bold)
                    }
                    
                    // Bouton Facebook
                    SocialButton(
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

