package com.example.projecct_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

@Composable
fun ForgotPasswordScreen(onBackClick: () -> Unit = {}, onSubmitClick: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }

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
                        .background(Color(0xFFFFA726)), // Orange pour la cadenas
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
                    text = "Forgot Password?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Don't worry! It happens. Please enter the email address associated with your account.",
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
                    placeholder = { Text("Email", color = GrayBorder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bouton Submit
                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            "Back to Login",
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

